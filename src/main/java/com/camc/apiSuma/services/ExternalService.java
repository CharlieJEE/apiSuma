package com.camc.apiSuma.services;

public interface ExternalService {
    void simularError();
    void reanudarServicio();
    boolean isServiceActive();
    double getPorcentaje();
    boolean esError();
}