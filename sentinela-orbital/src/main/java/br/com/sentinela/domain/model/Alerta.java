package br.com.sentinela.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "alerta")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "nivel")
@Data
@NoArgsConstructor
public abstract class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id", nullable = false)
    private Regiao regiao;

    @ManyToOne
    @JoinColumn(name = "leitura_id", nullable = false)
    private LeituraClimatica leitura;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private Boolean ativo = true;


    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public abstract String getNivel();
}