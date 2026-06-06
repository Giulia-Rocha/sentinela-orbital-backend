package br.com.sentinelasoap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leitura_climatica")
@Data
@NoArgsConstructor
public class LeituraClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    private Double temperatura;
    private Double umidade;
    private Double iuv;
    private Double hri;
    private String fonte = "SOAP";
    private LocalDateTime timestamp = LocalDateTime.now();
}
