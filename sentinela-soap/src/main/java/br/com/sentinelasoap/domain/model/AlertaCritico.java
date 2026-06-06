package br.com.sentinelasoap.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRITICO")
public class AlertaCritico extends Alerta {

    @Override
    public String getNivel() { return "CRITICO"; }
}
