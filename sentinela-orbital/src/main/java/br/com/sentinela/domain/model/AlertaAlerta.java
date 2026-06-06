package br.com.sentinela.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ALERTA")
public class AlertaAlerta extends Alerta {

    @Override
    public String getNivel() { return "ALERTA"; }
}
