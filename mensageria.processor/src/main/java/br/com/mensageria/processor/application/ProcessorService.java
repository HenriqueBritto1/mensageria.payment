package br.com.mensageria.processor.application;

import br.com.mensageria.processor.application.dto.MercadoPagoRequestDTO;
import br.com.mensageria.processor.application.dto.MercadoPagoResponseDTO;
import br.com.mensageria.processor.application.dto.PaymentReceiveDTO;
import br.com.mensageria.processor.application.dto.PaymentValidatedDTO;

import br.com.mensageria.processor.infra.entity.Payment;
import br.com.mensageria.commons.enums.PaymentStatus;
import br.com.mensageria.commons.exceptions.PaymentNotFound;
import br.com.mensageria.processor.infra.entity.PaymentRequest;
import br.com.mensageria.processor.infra.repository.PaymentRequestRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.mensageria.processor.infra.repository.PaymentRepository;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProcessorService {

    @Value("${mercado-pago.token}")
    private String TOKEN;

    private final static String url = "https://api.mercadopago.com/";

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private PaymentRepository payRepo;

    private ObjectMapper mapper = new ObjectMapper();

    private HttpClient httpClient(){
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    private final Gson gson = new Gson();

    @Transactional
    public Object process(String msg){
        PaymentValidatedDTO dto = gson.fromJson(msg, PaymentValidatedDTO.class);

        PaymentRequest request = paymentRequestRepository.findById(dto.pagamentoId())
                .orElseThrow(() -> new PaymentNotFound("Ocorreu um erro: Pagamento nao encontrado"));

        Payment entity = new Payment();
        try {
            var mercadoPagoRequest = gson.toJson(montarMercadoPagoRequest(dto, request));

            System.out.println(mercadoPagoRequest);

        // Chamada à API de pagamento.
        //valida se a resposta < 300 ou > 500
            URI uri = URI.create(url+ "v1/orders");
            HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(montarMercadoPagoRequest(dto,request))))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+ TOKEN)
                .header("X-Idempotency-Key", request.getId().toString())
                .build();

            HttpResponse<String> response = httpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            System.out.println("STATUS: " + response.statusCode());
            System.out.println("BODY: " + response.body());
            if (response.statusCode() >= 400) {
                request.setStatus(PaymentStatus.FAILED);
                request.setRejectedReason(response.body());
                paymentRequestRepository.save(request);
                throw new RuntimeException("Erro ao integrar com a API");
            }else {
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

                PaymentReceiveDTO responseAPI = new PaymentReceiveDTO(
                        request.getId(),
                        request.getCorrelationId(),
                        request.getAmount(),
                        request.getCurrency(),
                        request.getExternalReference(),
                        request.getStatus(),
                        responseMercado.id(),
                        responseMercado.client_token(),
                        request.getCallbackUrl(),
                        responseMercado.tiket_url(),
                        request.getCreatedAt()
                );
                payRepo.save(entity);
                paymentRequestRepository.save(request);
                return mapper.writeValueAsString(responseAPI);
            }
        }catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object verify(String msg){
        try {
            String transactionId = gson.fromJson(msg, String.class);

            PaymentRequest pay = paymentRequestRepository.findById(UUID.fromString(transactionId))
                    .orElseThrow(()->new PaymentNotFound("Pagamento não encontrado na base de dados"));
            Payment entity = payRepo.findByCorrelationIdContainingIgnoreCase(pay.getCorrelationId()).orElse(null);

            if(entity==null){
                return null;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url+ "v1/orders/"+entity.getOrderId()))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer "+ TOKEN)
                    .build();

            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            if(response.statusCode() >= 400) {
                throw new RuntimeException("Erro ao integrar com a API");
            }
            MercadoPagoResponseDTO responseMercado = gson.fromJson(response.body(), MercadoPagoResponseDTO.class);
            var responsejson = gson.toJson(responseMercado);
            System.out.println(responsejson);
            pay.setStatus(PaymentStatus.valueOf(responseMercado.status().toUpperCase()));
            pay.setUpdatedAt(OffsetDateTime.now());
            entity.setStatus(PaymentStatus.valueOf(responseMercado.status().toUpperCase()));
            entity.setUpdatedAt(OffsetDateTime.now());

            paymentRequestRepository.save(pay);
            payRepo.save(entity);
            PaymentReceiveDTO dto = montarDTO(pay, entity, responseMercado);

            var json = mapper.writeValueAsString(dto);
            System.out.println(json);
            return json;
        }catch(IOException e){
            throw new RuntimeException(e.getMessage());
        }catch(InterruptedException ex){
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex.getMessage());
        }
    }

    private PaymentReceiveDTO montarDTO(PaymentRequest entity, Payment payment, MercadoPagoResponseDTO mercadoPago){
        return new PaymentReceiveDTO(
                entity.getId(),
                entity.getCorrelationId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getExternalReference(),
                PaymentStatus.valueOf(mercadoPago.status().toUpperCase()),
                payment.getOrderId(),
                payment.getClientToken(),
                entity.getCallbackUrl(),
                mercadoPago.transactions().payments().getFirst().payment_method().ticket_url(),
                entity.getCreatedAt()
        );
    }

    private MercadoPagoRequestDTO montarMercadoPagoRequest(PaymentValidatedDTO dto, PaymentRequest entity){
        if(dto == null){
            return null;
        }
        MercadoPagoRequestDTO request = new MercadoPagoRequestDTO();
        request.setType("online");
        request.setPayer(dto.payer());
        request.setTotal_amount(entity.getAmount().toString());
        request.setExternal_reference(entity.getExternalReference());
        request.setTransactions(dto.transactions());

        return request;
    }

}
