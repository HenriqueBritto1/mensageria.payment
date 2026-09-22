package br.com.mensageria.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CardFlag {
    visa,
    master,
    debelo,
    //boleto,
    pix;
}
