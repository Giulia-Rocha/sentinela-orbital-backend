package br.com.sentinela.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leitura_climatica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeituraClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id", nullable = false)
    private Regiao regiao;

    @Column(nullable = false)
    private Double temperatura;

    @Column(nullable = false)
    private Double umidade;

    @Column(nullable = false)
    private Double iuv;

    @Column(nullable = false)
    private Double hri;

    @Column(nullable = false)
    @Builder.Default
    private String fonte = "CPTEC";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}