package br.com.mensageria.processor.application.mappers;

import br.com.mensageria.commons.dto.PaymentValidatedDTO;
import br.com.mensageria.processor.application.dto.MercadoPagoRequestDTO;
import br.com.mensageria.processor.infra.entity.PaymentRequest;

public class MercadoPagoMapper {
    public MercadoPagoRequestDTO montarMercadoPagoRequest(PaymentValidatedDTO dto, PaymentRequest entity) {
        if (dto == null) {
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
