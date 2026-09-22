package br.com.mensageria.processor.application;

import br.com.mensageria.commons.exceptions.*;
import br.com.mensageria.processor.application.dto.*;
import br.com.mensageria.commons.dto.PaymentReceiveDTO;
import br.com.mensageria.commons.dto.PaymentValidatedDTO;

import br.com.mensageria.processor.application.mappers.MercadoPagoMapper;
import br.com.mensageria.processor.application.mappers.PaymentMapper;
import br.com.mensageria.processor.infra.entity.Payment;
import br.com.mensageria.commons.enums.PaymentStatus;
import br.com.mensageria.processor.infra.entity.PaymentRequest;
import br.com.mensageria.processor.infra.repository.PaymentRequestRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.mensageria.processor.infra.repository.PaymentRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ProcessorService {
    private final static Logger log = Logger.getLogger(ProcessorService.class.getName());

    @Value("${mercado-pago.token}")
    private String TOKEN;

    private final static String url = "https://api.mercadopago.com/";

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private PaymentRepository payRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Gson gson = new Gson();

    private final PaymentMapper paymentMapper = new PaymentMapper();

    private final MercadoPagoMapper mercadoMapper = new MercadoPagoMapper();

    private HttpClient httpClient() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    @Transactional
    public Object process(String msg) {

        PaymentValidatedDTO dto = gson.fromJson(msg, PaymentValidatedDTO.class);

        PaymentRequest request = paymentRequestRepository.findById(dto.pagamentoId())
                .orElseThrow(() -> new PaymentNotFound("Ocorreu um erro: Pagamento nao encontrado"));
        log.info("Processando order ID: "+ request.getId());

        try {
            // Chamada à API de pagamento.
            URI uri = URI.create(url + "v1/orders");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(
                            mercadoMapper.montarMercadoPagoRequest(dto, request))))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("X-Idempotency-Key", request.getId().toString())
                    .build();

            HttpResponse<String> response = httpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            log.info("Process - Response status" + response.statusCode());
            log.info(response.body());
            request.setStatus(PaymentStatus.FAILED);
            if (response.statusCode() != 201) {
                request.setRejectedReason(response.body());
            }
            paymentRequestRepository.save(request);

            //Valida resposta da API
            tratar(response.statusCode());

            Payment entity = new Payment();

            MercadoPagoResponseDTO responseMercado = gson.fromJson(response.body(), MercadoPagoResponseDTO.class);
            request.setStatus(PaymentStatus.PROCESSING);
            entity.setStatus(PaymentStatus.PROCESSING);
            entity.setOrderId(responseMercado.id());
            entity.setExternalReference(request.getExternalReference());
            entity.setAmount(request.getAmount());
            entity.setCurrency(request.getCurrency());
            entity.setCreatedAt(OffsetDateTime.now());
            entity.setCallbackUrl(request.getCallbackUrl());
            entity.setMerchantId(request.getMerchantId());
            entity.setCorrelationId(request.getCorrelationId());
            entity.setTransactionId(responseMercado.transactions().payments().getFirst().id());

            PaymentReceiveDTO responseAPI = paymentMapper.montarReceiveDTO(request, entity, responseMercado);

            payRepo.save(entity);
            paymentRequestRepository.save(request);
            log.info("Order criada com sucesso! Id: " + request.getId());
            return mapper.writeValueAsString(responseAPI);

        } catch (IOException e) {
            log.severe("Erro ao integrar com a API: "+ e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            log.severe("Erro ao integrar com a API: "+ e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }catch (Exception exception){
            log.severe("Erro ao integrar com a API: "+ exception.getMessage());
            throw exception;
        }
    }

    public Object verify(String msg) {
        try {
            String transactionId = gson.fromJson(msg, String.class);

            PaymentRequest pay = paymentRequestRepository.findById(UUID.fromString(transactionId))
                    .orElseThrow(() -> new PaymentNotFound("Pagamento não encontrado na base de dados"));
            Payment entity = payRepo.findByCorrelationIdContainingIgnoreCase(pay.getCorrelationId()).orElseThrow(() -> new PaymentNotFound("Pagamento não encontrado na base de dados"));

            //Faz requisição para API do mercado pago
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "v1/orders/" + entity.getOrderId()))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .build();

            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Verify - Response status: " + response.statusCode());

            //Valida resposta da API
            tratar(response.statusCode());

            MercadoPagoResponseDTO responseMercado = gson.fromJson(response.body(), MercadoPagoResponseDTO.class);

            pay.setStatus(PaymentStatus.valueOf(responseMercado.status().toUpperCase()));
            pay.setUpdatedAt(OffsetDateTime.now());
            entity.setStatus(PaymentStatus.valueOf(responseMercado.status().toUpperCase()));
            entity.setUpdatedAt(OffsetDateTime.now());

            paymentRequestRepository.save(pay);
            payRepo.save(entity);
            PaymentReceiveDTO dto = paymentMapper.montarReceiveDTO(pay, entity, responseMercado);

            var json = mapper.writeValueAsString(dto);

            return json;
        } catch (InterruptedException ex) {
            log.severe("Erro ao integrar com a API: "+ ex.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex.getMessage());
        } catch (Exception exception){
            log.severe("Erro ao integrar com a API: "+ exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Transactional
    public Object cancel(String msg) {
        try {
            String transactionId = gson.fromJson(msg, String.class);

            var entity = paymentRequestRepository.findById(UUID.fromString(transactionId))
                    .orElseThrow(() -> new PaymentNotFound("Pagamento não encontrado na base de dados"));
            var payment = payRepo.findByCorrelationIdContainingIgnoreCase(entity.getCorrelationId())
                    .orElseThrow(() -> new PaymentNotFound("Pagamento não encontrado na base de dados"));
            log.info("Cancelando order ID: "+ entity.getId());
            //Faz requisição para API do mercado pago
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create(url + "v1/orders/" + payment.getOrderId() + "/cancel"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("X-Idempotency-Key", UUID.randomUUID().toString())
                    .build();

            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Cancel order - Response status: " + response.statusCode());
            //Valida resposta da API
            tratar(response.statusCode());

            var dto = gson.fromJson(response.body(), MercadoPagoResponseDTO.class);

            entity.setStatus(PaymentStatus.valueOf(dto.status().toUpperCase()));
            entity.setUpdatedAt(OffsetDateTime.now());

            payment.setStatus(PaymentStatus.valueOf(dto.status().toUpperCase()));
            payment.setUpdatedAt(OffsetDateTime.now());
            paymentRequestRepository.save(entity);
            payRepo.save(payment);
            log.info("Order cancelado com sucesso! Id: " + entity.getId());
            PaymentReceiveDTO responseDTO = paymentMapper.montarReceiveDTO(entity, payment, dto);
            return mapper.writeValueAsString(responseDTO);
        } catch (InterruptedException ex) {
            log.severe("Erro ao integrar com a API: "+ ex.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex.getMessage());
        } catch (Exception exception){
            log.severe("Erro ao integrar com a API: "+ exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }


    private void tratar(int statusCode) {
        switch (statusCode) {
            case 200, 201, 204 -> {}
            case 400 -> throw new IllegalArgumentException("Parâmetro inválido");
            case 401 -> throw new InvalidTokenException("Token inválido");
            case 403 -> throw new PermissionNotAllowed("Acesso negado ao recurso");
            case 404 -> throw new PaymentNotFound("Order não encontrada");
            case 409 -> throw new ApiRuleException("Ação bloqueada por regra");
            case 423 -> throw new ResourceLocked("Chave de idempotência bloqueada");
            case 429 -> throw new RequestLimitExceeded("Limite de requisições excedido");
            case 422, 500 -> throw new RuntimeException("Chamada a API falhou");
        }
    }

    public Object refund(String dto) {
        try {
            String transactionId = gson.fromJson(dto, String.class);
            var paymentRequest = paymentRequestRepository.findById(UUID.fromString(transactionId))
                    .orElseThrow(() -> new PaymentNotFound("Pagamento não encontrado na base de dados"));
            var entity = payRepo.findByCorrelationIdContainingIgnoreCase(paymentRequest.getCorrelationId())
                    .orElseThrow(()->new PaymentNotFound("Pagamento não encontrado"));

            TransactionRequestDTO transactionRequest = new TransactionRequestDTO(
                    entity.getTransactionId(),
                    entity.getAmount().toString()
            );
            List<TransactionRequestDTO> transaction = new ArrayList<>();
            transaction.add(transactionRequest);
            RefundRequestDTO refundRequest = new RefundRequestDTO(
                    transaction);
            log.info("URL Mercado Pago: " + url);
            log.info("Request Body Mercado Pago: + " + gson.toJson(refundRequest));

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(refundRequest)))
                    .uri(URI.create(url + "v1/orders/" + entity.getOrderId() + "/refund"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("X-Idempotency-Key", UUID.randomUUID().toString())
                    .build();

            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Refund - Response Status: " + response.statusCode());
            tratar(response.statusCode());

            var responseDTO = gson.fromJson(response.body(), MercadoPagoRefundResponseDTO.class);
            entity.setStatus(PaymentStatus.valueOf(responseDTO.status().toUpperCase()));
            entity.setUpdatedAt(OffsetDateTime.now());
            payRepo.save(entity);

            PaymentReceiveDTO receiveDTO = paymentMapper.montarReceiveDTORefund(paymentRequest, entity, responseDTO);
            return mapper.writeValueAsString(receiveDTO);
        } catch (InterruptedException exception){
            log.severe("Erro ao integrar com a API: "+ exception.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception.getMessage());
        } catch (Exception exception){
            log.severe("Erro ao integrar com a API: "+ exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }
}
