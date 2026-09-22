package br.com.mensageria.commons.dto;

import br.com.mensageria.commons.enums.CardFlag;
import br.com.mensageria.commons.enums.TypePayment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodDTO {
    CardFlag id;
    @NotNull
    TypePayment type;
    String token;
    Integer installments; //Numero de parcelas
    String statement_descriptor; //nome que aparecerá na fatura

    public CardFlag getId() {
        return id;
    }

    public void setId(CardFlag id) {
        this.id = id;
    }

    public TypePayment getType() {
        return type;
    }

    public void setType(TypePayment type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public String getStatement_descriptor() {
        return statement_descriptor;
    }

    public void setStatement_descriptor(String statement_descriptor) {
        this.statement_descriptor = statement_descriptor;
    }
}
