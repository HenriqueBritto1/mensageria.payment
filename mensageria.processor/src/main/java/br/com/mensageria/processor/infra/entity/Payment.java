package br.com.mensageria.processor.infra.entity;


import br.com.mensageria.commons.enums.CurrencyEnum;
import br.com.mensageria.commons.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "correlation_id", unique = true, nullable = false)
    private String correlationId; //chave do processo

    @Column(name="merchant_id", nullable = false)
    private String merchantId; // id de quem chamou

    @Column(precision = 18, scale = 2)
    private BigDecimal amount; //valor

    @Column
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;// moeda

    @Column(name="external_reference")
    private String externalReference;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
