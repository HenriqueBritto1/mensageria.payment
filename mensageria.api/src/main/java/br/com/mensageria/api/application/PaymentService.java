package br.com.mensageria.api.application;

import br.com.mensageria.api.application.dto.PaymentValidatedDTO;
import br.com.mensageria.api.application.dto.PaymentRequestDTO;
import br.com.mensageria.api.application.dto.PaymentResponseDTO;
import br.com.mensageria.api.infra.entity.PaymentRequest;
import br.com.mensageria.api.infra.repository.PaymentRequestRepository;
import br.com.mensageria.commons.enums.CurrencyEnum;
import br.com.mensageria.commons.enums.PaymentStatus;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final static String ROUTING_KEY= "payment.validated";
    private static final String EXCHANGE_NAME = "mensageria-payment-exchange";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    private final Gson gson = new Gson();

    public PaymentResponseDTO pagar(PaymentRequestDTO pagamentoRequest){
        validar(pagamentoRequest);

        PaymentRequest pagamento = new PaymentRequest();

        pagamento.setAmount(pagamentoRequest.amount());
        pagamento.setExternalReference(pagamentoRequest.externalReference());
        pagamento.setCurrency(CurrencyEnum.valueOf(pagamentoRequest.currency().toUpperCase()));

        pagamento.setCallbackUrl(pagamentoRequest.callbackUrl());
        pagamento.setMerchantId(SecurityContextHolder.getContext().getAuthentication().getName());
        pagamento.setCreatedAt(OffsetDateTime.now());
        pagamento.setCorrelationId(UUID.randomUUID().toString());
        pagamento.setStatus(PaymentStatus.PENDENTE);
        paymentRequestRepository.save(pagamento);

        PaymentValidatedDTO dto = new PaymentValidatedDTO(
                pagamento.getId(),
                pagamento.getCorrelationId()
        );

        String json = gson.toJson(dto);

        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, json);

        return new PaymentResponseDTO(
                pagamento.getId(),
                pagamento.getCorrelationId(),
                pagamento.getStatus(),
                "Enviado para processamento"
        );
    }

    private void validar(PaymentRequestDTO pagamento){
        if(pagamento.amount().compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("valor inválido!");
        }
        //validação simulada: só permitido BRL
        if(CurrencyEnum.valueOf(pagamento.currency().toUpperCase()) != CurrencyEnum.BRL){
            throw new IllegalArgumentException("Moeda não suportada!");
        }
        if (pagamento.externalReference().isEmpty()){
            throw new IllegalArgumentException("Referência não listada");
        }
        if(pagamento.callbackUrl().isEmpty()){
            throw new IllegalArgumentException("Url vazia");
        }
    }

}
