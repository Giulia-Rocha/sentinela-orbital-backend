package br.com.sentinelasoap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regiao")
@Data
@NoArgsConstructor
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}
