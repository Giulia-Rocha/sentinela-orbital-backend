package br.com.sentinelasoap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    @ManyToOne
    @JoinColumn(name = "leitura_id")
    private LeituraClimatica leitura;

    private String mensagem;
    private Boolean ativo = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public abstract String getNivel();
}
