package br.com.mensageria.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TypePayment {
    credit_card,
    debit_card,
    //ticket,
    bank_transfer
}
