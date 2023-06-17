package com.camc.apiSuma.services;

import org.springframework.stereotype.Service;

@Service
public class MockExternalService implements ExternalService {

    private boolean error = false;
    private Double lastPercentage = null;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;

    public void simularError() {
        error = true;
    }

    public void reanudarServicio() {
        error = false;
    }

    public boolean esError() {
        return error;
    }

    public boolean isServiceActive() {
        return !error;
    }

    public double getPorcentaje() {
        if (error) {
            retryCount++;
            if (retryCount <= MAX_RETRY_COUNT) {
                return getLastPercentageOrThrowException();
            } else {
                throw new RuntimeException("Fallo en el servicio externo, Error al obtener el porcentaje");
            }
        } else {
            retryCount = 0; // Reiniciar el contador de reintentos al recibir una respuesta exitosa
            // Simular la obtención del porcentaje desde el servicio externo
            double porcentaje = 10.0; // Reemplazar con tu lógica para obtener el porcentaje
            lastPercentage = porcentaje;
            return porcentaje;
        }
    }

    private double getLastPercentageOrThrowException() {
        if (lastPercentage != null) {
            return lastPercentage;
        } else {
            throw new RuntimeException("Fallo en el servicio externo, Error al obtener el porcentaje");
        }
    }
}
