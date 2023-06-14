package com.camc.apiSuma.services;

public interface ExternalService {
    void simulateError();
    void simulateSuccess();
    boolean isServiceActive();
    double getPercentage();
    boolean isError();
}