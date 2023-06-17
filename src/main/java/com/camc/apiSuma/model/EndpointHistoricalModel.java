package com.camc.apiSuma.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;

@Entity
@Table(name = "tb_endpointHistorical")
public class EndpointHistoricalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreEndpoint;
    private String respuesta;
    private String detalleRespuesta;
    private Date fechaRespuesta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreEndpoint() {
        return nombreEndpoint;
    }

    public void setNombreEndpoint(String nombreEndpoint) {
        this.nombreEndpoint = nombreEndpoint;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getDetalleRespuesta() {
        return detalleRespuesta;
    }

    public void setDetalleRespuesta(String detalleRespuesta) {
        this.detalleRespuesta = detalleRespuesta;
    }

    public Date getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Date fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
}
