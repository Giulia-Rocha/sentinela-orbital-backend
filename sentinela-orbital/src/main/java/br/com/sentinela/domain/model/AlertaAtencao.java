package br.com.sentinela.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATENCAO")
public class AlertaAtencao extends Alerta {

    public AlertaAtencao() { super(); }

    @Override
    public String getNivel() { return "ATENCAO"; }
}