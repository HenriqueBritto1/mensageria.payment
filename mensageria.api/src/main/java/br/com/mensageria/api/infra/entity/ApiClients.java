package br.com.mensageria.api.infra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="api_clients")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiClients {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(length=100)
    private String nome;

    @Column(name = "api_key", unique=true)
    private String apiKey;

    @Column
    private boolean ativo;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
}
