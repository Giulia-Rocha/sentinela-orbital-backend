package br.com.sentinela.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRITICO")
public class AlertaCritico extends Alerta {

    public AlertaCritico() { super(); }

    @Override
    public String getNivel() { return "CRITICO"; }
}