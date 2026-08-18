package br.com.mensageria.processor.application;

import br.com.mensageria.processor.application.dto.PaymentValidatedDTO;

import br.com.mensageria.commons.exceptions.InsuficientBalanceException;
import br.com.mensageria.processor.infra.entity.Payment;
import br.com.mensageria.commons.enums.PaymentStatus;
import br.com.mensageria.commons.exceptions.PagamentoNaoEncontrado;
import br.com.mensageria.processor.infra.entity.PaymentRequest;
import br.com.mensageria.processor.infra.repository.PaymentRequestRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.mensageria.processor.infra.repository.PaymentRepository;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class ProcessorService {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private PaymentRepository payRepo;

    private HttpClient httpClient(){
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    private final Gson gson = new Gson();

    @Transactional
    public void process(String msg){
        PaymentValidatedDTO dto = gson.fromJson(msg, PaymentValidatedDTO.class);

        PaymentRequest request = paymentRequestRepository.findById(dto.pagamentoId())
                .orElseThrow(() -> new PagamentoNaoEncontrado("Ocorreu um erro: Pagamento nao encontrado"));

        Payment entity = new Payment();
        entity.setOrderId(dto.pagamentoId().toString());
        entity.setExternalReference(request.getExternalReference());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setCallbackUrl(request.getCallbackUrl());
        entity.setMerchantId(request.getMerchantId());
        entity.setCorrelationId(request.getCorrelationId());

        // Chamada à API de pagamento.
        //valida se a resposta < 300 ou > 500

        //validação simulada
        if(request.getAmount().compareTo(new BigDecimal(10000))>=0){
            request.setStatus(PaymentStatus.RECUSADO);
            request.setRejectedReason("Saldo insuficiente");
            paymentRequestRepository.save(request);
            return;
        }else {
            request.setStatus(PaymentStatus.APROVADO);
            entity.setStatus(PaymentStatus.APROVADO);
        }
        payRepo.save(entity);
        paymentRequestRepository.save(request);
    }
}
