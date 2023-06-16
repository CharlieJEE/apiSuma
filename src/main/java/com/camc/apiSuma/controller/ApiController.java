package com.camc.apiSuma.controller;

import com.camc.apiSuma.model.NumbersRequest;
import com.camc.apiSuma.model.EndpointHistoricalModel;
import com.camc.apiSuma.services.ExternalService;
import com.camc.apiSuma.services.MockExternalService;
import com.camc.apiSuma.services.EndpointHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
@Tag(name = "API Controller")
public class ApiController {

    private static final int MAX_REQUESTS_PER_MINUTE = 3;
    private static final int REQUEST_RESET_DELAY = 60000; // 1 minuto

    private ExternalService externalService;
    private EndpointHistoryService endpointHistoryService;

    public ApiController(ExternalService externalService) {
        this.externalService = externalService;
    }

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private boolean isRequestEnabled = true;
    private long lastRequestTime = System.currentTimeMillis();

    @Autowired
    public ApiController(ExternalService externalService, EndpointHistoryService endpointHistoryService) {
        this.externalService = externalService;
        this.endpointHistoryService = endpointHistoryService;
    }

    @Operation(summary = "Suma de dos numeros y genera porcentaje ")
    @PostMapping("/sum")
    public ResponseEntity<String> sumWithIncrease(@RequestBody NumbersRequest numbers) {
        if (!isRequestEnabled) {
            long elapsedTime = System.currentTimeMillis() - lastRequestTime;
            long timeToWait = Math.max((REQUEST_RESET_DELAY - elapsedTime) / 1000, 0);
            if (timeToWait > 0) {
                String errorMessage = "Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en "
                        + timeToWait + " segundos.";
                saveEndpointHistorical("/api/sum", "ERROR", errorMessage);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorMessage);
            } else {
                isRequestEnabled = true;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime >= REQUEST_RESET_DELAY) {
            requestCount.set(0);
            isRequestEnabled = true;
        }

        int count = requestCount.incrementAndGet();
        if (count > MAX_REQUESTS_PER_MINUTE) {
            isRequestEnabled = false;
            long elapsedTime = System.currentTimeMillis() - lastRequestTime;
            long timeToWait = Math.max((REQUEST_RESET_DELAY - elapsedTime) / 1000, 0);
            if (timeToWait > 0) {
                String errorMessage = "Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en "
                        + timeToWait + " segundos.";
                saveEndpointHistorical("/api/sum", "ERROR", errorMessage);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorMessage);
            } else {
                isRequestEnabled = true;
            }
        }

        try {
            double percentage = externalService.getPercentage();
            double sum = numbers.getNumber1() + numbers.getNumber2();
            double result = sum + (sum * percentage / 100);

            lastRequestTime = System.currentTimeMillis();

            // Guardar en el historial
            saveEndpointHistorical("/api/sum", "SUCCESS", "Suma exitosa con porcentaje: " + percentage + "%");

            return ResponseEntity.ok("Resultado: " + result);
        } catch (Exception e) {
            e.printStackTrace();

            // Guardar en el historial
            saveEndpointHistorical("/api/sum", "ERROR", "Error al obtener el porcentaje desde el servicio externo");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallo en el servicio externo, Error al obtener el porcentaje");
        }
    }

    @Scheduled(fixedDelay = REQUEST_RESET_DELAY)
    private void resetRequestCount() {
        requestCount.set(0);
        isRequestEnabled = true;
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulateStatus(@RequestBody String body) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(body));
            JsonObject jsonObject = jsonReader.readObject();
            String status = jsonObject.getString("status");
            MockExternalService mockService = (MockExternalService) externalService;

            if ("error".equalsIgnoreCase(status)) {
                mockService.simulateError();

                // Guardar en el historial
                saveEndpointHistorical("/api/simulate", "ERROR EL SERVICIO", "Se simula un error en el serivicio");

                return ResponseEntity.ok("Creando Simulacion de ERROR en el servicio externo");
            } else if ("success".equalsIgnoreCase(status)) {
                mockService.simulateSuccess();

                // Guardar en el historial
                saveEndpointHistorical("/api/simulate", "SERVICIO ACTIVO", "Se activa servicio");

                return ResponseEntity.ok("Reanudando el funcionamiento del Servicio externo");
            } else {
                return ResponseEntity.badRequest().body("Parámetro de estado inválido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Cuerpo de solicitud inválido");
        }
    }

    @GetMapping("/check/status")
    public ResponseEntity<String> checkStatus() {
        if (externalService instanceof MockExternalService) {
            boolean isError = ((MockExternalService) externalService).isError();
            String status = isError ? "El servicio no está disponible" : "Servicio disponible";

            // Guardar en el historial
            saveEndpointHistorical("/api/check/status", "INFO", "Verificación de estado del servicio Estado del servicio: " + status);

            return ResponseEntity.ok("Estado del servicio: " + status);
        } else {
            return ResponseEntity.ok("Estado del servicio: Desconocido");
        }
    }
    @GetMapping("/history/all")
    public ResponseEntity<List<EndpointHistoricalModel>> getAllEndpointHistorical(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
                    List<EndpointHistoricalModel> endpointHistoricalList = endpointHistoryService.getAllEndpointHistorical(page, size);
    return ResponseEntity.ok(endpointHistoricalList);
}

    private void saveEndpointHistorical(String endpointName, String response, String responseDetail) {
        EndpointHistoricalModel historicalModel = new EndpointHistoricalModel();
        historicalModel.setEndPointName(endpointName);
        historicalModel.setResponse(response);
        historicalModel.setResponseDetail(responseDetail);
        historicalModel.setResponseDate(new Date());

        endpointHistoryService.saveEndpointHistorical(historicalModel);
    }
}