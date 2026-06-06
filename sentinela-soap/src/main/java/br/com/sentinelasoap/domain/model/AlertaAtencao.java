package br.com.sentinelasoap.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATENCAO")
public class AlertaAtencao extends Alerta {

    @Override
    public String getNivel() { return "ATENCAO"; }
}
