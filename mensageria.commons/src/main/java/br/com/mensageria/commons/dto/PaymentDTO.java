package br.com.mensageria.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDTO {
    String amount;
    PaymentMethodDTO payment_method;
    String expiration_time;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public PaymentMethodDTO getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(PaymentMethodDTO payment_method) {
        this.payment_method = payment_method;
    }

    public String getExpiration_time() {
        return expiration_time;
    }

    public void setExpiration_time(String expiration_time) {
        this.expiration_time = expiration_time;
    }
}
