package br.com.mensageria.api.application;

import br.com.mensageria.api.application.dto.PaymentReceiveDTO;
import br.com.mensageria.api.application.dto.PaymentValidatedDTO;
import br.com.mensageria.api.application.dto.PaymentRequestDTO;
import br.com.mensageria.api.application.dto.PaymentResponseDTO;
import br.com.mensageria.api.infra.PaymentPublisher;
import br.com.mensageria.api.infra.entity.PaymentRequest;
import br.com.mensageria.api.infra.repository.PaymentRequestRepository;
import br.com.mensageria.commons.dto.Payer;
import br.com.mensageria.commons.dto.PaymentDTO;
import br.com.mensageria.commons.dto.PaymentMethodDTO;
import br.com.mensageria.commons.dto.Transactions;
import br.com.mensageria.commons.enums.CardFlag;
import br.com.mensageria.commons.enums.CurrencyEnum;
import br.com.mensageria.commons.enums.PaymentStatus;
import br.com.mensageria.commons.enums.TypePayment;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private PaymentPublisher publisher;

    private final Gson gson = new Gson();

    public PaymentResponseDTO pagar(PaymentRequestDTO pagamentoRequest){
        validarPagamento(pagamentoRequest);

        PaymentRequest pagamento = new PaymentRequest();
        Integer count = paymentRequestRepository.findLast().orElse(0);

        pagamento.setAmount(pagamentoRequest.total_amount());
        pagamento.setExternalReference("ext_"+ LocalDateTime.now().getYear()+"_"+ (count + 1));
        pagamento.setCurrency(CurrencyEnum.valueOf(pagamentoRequest.currency().toUpperCase()));
        pagamento.setCount(count+1);
        pagamento.setCallbackUrl(pagamentoRequest.callbackUrl());
        pagamento.setMerchantId(SecurityContextHolder.getContext().getAuthentication().getName());
        pagamento.setCreatedAt(OffsetDateTime.now());
        pagamento.setCorrelationId(UUID.randomUUID().toString());
        pagamento.setStatus(PaymentStatus.PENDENTE);
        paymentRequestRepository.save(pagamento);

        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setAmount(pagamentoRequest.total_amount().toString());
        paymentDto.setPayment_method(pagamentoRequest.payment_method());
        if(Objects.equals(pagamentoRequest.payment_method().getType(), TypePayment.bank_transfer)){
            paymentDto.setExpiration_time("P3D");
        }

        List<PaymentDTO> list = new ArrayList<>();
        list.add(paymentDto);

        Transactions transactions = new Transactions();
        transactions.setPayments(list);

        PaymentValidatedDTO dto = new PaymentValidatedDTO(
                pagamento.getId(),
                pagamento.getCorrelationId(),
                pagamentoRequest.payer(),
                transactions,
                pagamentoRequest.payer().getPhone(),
                pagamentoRequest.payer().getAddress()
        );

        PaymentReceiveDTO receiveDTO = publisher.publishAndReceivePayment(gson.toJson(dto));

        return new PaymentResponseDTO(
                receiveDTO.id(),
                receiveDTO.orderId(),
                pagamento.getStatus(),
                "Enviado para processamento"
        );
    }

    private void validarPagamento(PaymentRequestDTO request) {
        validarDadosComuns(request);

        switch (request.payment_method().getType()) {
            case credit_card, debit_card -> validarCartao(request);
            case bank_transfer -> validarPix(request);
            default -> throw new IllegalArgumentException("Tipo de pagamento não suportado");
        }
    }

    private void validarDadosComuns(PaymentRequestDTO pagamento) {
        if(pagamento.total_amount().compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("valor inválido!");
        }
        //validação simulada: só permitido BRL
        if(CurrencyEnum.valueOf(pagamento.currency().toUpperCase()) != CurrencyEnum.BRL){
            throw new IllegalArgumentException("Moeda não suportada!");
        }
        if(pagamento.callbackUrl().isEmpty()){
            throw new IllegalArgumentException("Url vazia");
        }
    }

    private void validarCartao(PaymentRequestDTO request) {
        PaymentMethodDTO method =  request.payment_method();
        if (method.getToken() == null || method.getToken().isBlank()) {
            throw new IllegalArgumentException("Token do cartão é obrigatório");
        }
        if (method.getToken().length() < 32 || method.getToken().length() > 33) {
            throw new IllegalArgumentException("Token do cartão deve possuir entre 32 e 33 caracteres");
        }
        if (method.getStatement_descriptor().length() > 50) {
            throw new IllegalArgumentException("statement_descriptor deve possuir no máximo 50 caracteres");
        }
        //Validação específica crédito
        if(Objects.equals(TypePayment.credit_card, method.getType())){
            if (method.getInstallments() == null) {
                throw new IllegalArgumentException("Número de parcelas é obrigatório para cartão de crédito");
            }
            if (method.getInstallments() < 1 || method.getInstallments() > 36) {
                throw new IllegalArgumentException("O número de parcelas deve estar entre 1 e 36");
            }
        }
        //Validação específica débito
        if (Objects.equals(TypePayment.debit_card, method.getType()) && method.getInstallments() != null) {
            throw new IllegalArgumentException("Cartão de débito não deve informar parcelas");
        }
    }

    private void validarPix(PaymentRequestDTO request) {
        if (request.payer() == null) {
            throw new IllegalArgumentException("payer é obrigatório para Pix");
        }
        if (!Objects.equals(request.payment_method().getType(), TypePayment.bank_transfer)) {
            throw new IllegalArgumentException("O tipo de pagamento Pix deve ser bank_transfer");
        }
        Payer payer = request.payer();

        if (payer.getEmail() == null || payer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email do payer é obrigatório para Pix");
        }

        if (!payer.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email do payer inválido");
        }
        if(request.payment_method().getToken()!=null){
            throw new IllegalArgumentException("Token não permitido para o pagamento");
        }
        if(request.payment_method().getInstallments()!=null){
            throw new IllegalArgumentException("Parcela não permitida para pix");
        }

    }


    public PaymentReceiveDTO verificarPagamento(String transactionId){
        return publisher.publishAndReceive(transactionId);
    }
}
