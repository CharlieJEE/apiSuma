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

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private boolean isRequestEnabled = true;
    private long lastRequestTime = System.currentTimeMillis();

    @Autowired
    public ApiController(ExternalService externalService, EndpointHistoryService endpointHistoryService) {
        this.externalService = externalService;
        this.endpointHistoryService = endpointHistoryService;
    }

    @Operation(summary = "Suma de dos números más porcentaje")
    @PostMapping("/sum")
    public ResponseEntity<String> sumarConIncremento(@RequestBody NumbersRequest numbers) {
        if (!isRequestEnabled) {
            long elapsedTime = System.currentTimeMillis() - lastRequestTime;
            long timeToWait = Math.max((REQUEST_RESET_DELAY - elapsedTime) / 1000, 0);
            if (timeToWait > 0) {
                String mensajeError = "Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en "
                        + timeToWait + " segundos.";
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mensajeError);
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
                String mensajeError = "Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en "
                        + timeToWait + " segundos.";
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(mensajeError);
            } else {
                isRequestEnabled = true;
            }
        }

        try {
            double porcentaje = externalService.getPorcentaje();
            double suma = numbers.getNumero1() + numbers.getNumero2();
            double resultado = suma + (suma * porcentaje / 100);

            lastRequestTime = System.currentTimeMillis();

            // Guardar en el historial
            guardarHistoricoEndpoint("/api/sum", "SUCCESS", "Suma exitosa " + numbers.getNumero1() + "+" + numbers.getNumero2() + " con porcentaje: " + porcentaje + "% = " + resultado);

            return ResponseEntity.ok("Resultado: " + resultado);
        } catch (Exception e) {
            e.printStackTrace();

            // Guardar en el historial
            guardarHistoricoEndpoint("/api/sum", "ERROR", "Error al obtener el porcentaje desde el servicio externo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fallo en el servicio externo, Error al obtener el porcentaje");
            //throw ManejoExcepcionesApi.errorServicioExterno("Fallo en el servicio externo, Error al obtener el porcentaje");
        }
    }

    @Scheduled(fixedDelay = REQUEST_RESET_DELAY)
    private void resetRequestCount() {
        requestCount.set(0);
        isRequestEnabled = true;
    }

    @Operation(summary = "Consulta del historial de los llamados a los endpoints")
    @GetMapping("/history/all")
    public ResponseEntity<List<EndpointHistoricalModel>> obtenerHistorialCompletoEndpoint(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<EndpointHistoricalModel> listaHistorialEndpoint = endpointHistoryService.obtenerHistorialCompletoEndpoint(page, size);
        return ResponseEntity.ok(listaHistorialEndpoint);
    }

    @Operation(summary = "Simulación de error en el servicio de entrega de porcentaje")
    @PostMapping("/simulate")
    public ResponseEntity<String> simularEstado(@RequestBody String body) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(body));
            JsonObject jsonObject = jsonReader.readObject();
            String estado = jsonObject.getString("estado");
            MockExternalService mockService = (MockExternalService) externalService;

            if ("error".equalsIgnoreCase(estado)) {
                mockService.simularError();

                // Guardar en el historial
                guardarHistoricoEndpoint("/api/simulate", "ERROR EN EL SERVICIO", "Se simula un error en el servicio");

                return ResponseEntity.ok("Creando simulación de ERROR en el servicio externo");
            } else if ("activate".equalsIgnoreCase(estado)) {
                mockService.reanudarServicio();

                // Guardar en el historial
                guardarHistoricoEndpoint("/api/simulate", "SERVICIO ACTIVO", "Se activa servicio");

                return ResponseEntity.ok("Reanudando el funcionamiento del servicio externo");
            } else {
                return ResponseEntity.badRequest().body("Parámetro de estado inválido");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Cuerpo de solicitud inválido");
        }
    }

    @Operation(summary = "Verificación de estado del servicio de porcentaje")
    @GetMapping("/check/status")
    public ResponseEntity<String> verificarEstado() {
        if (externalService instanceof MockExternalService) {
            boolean esError = ((MockExternalService) externalService).esError();
            String estado = esError ? "El servicio no está disponible" : "Servicio disponible";

            // Guardar en el historial
            guardarHistoricoEndpoint("/api/check/status", "INFO", "Verificación de estado del servicio. Estado del servicio: " + estado);

            return ResponseEntity.ok("Estado del servicio: " + estado);
        } else {
            return ResponseEntity.ok("Estado del servicio: Desconocido");
        }
    }

    private void guardarHistoricoEndpoint(String nombreEndpoint, String respuesta, String detalleRespuesta) {
        EndpointHistoricalModel modeloHistorial = new EndpointHistoricalModel();
        modeloHistorial.setNombreEndpoint(nombreEndpoint);
        modeloHistorial.setRespuesta(respuesta);
        modeloHistorial.setDetalleRespuesta(detalleRespuesta);
        modeloHistorial.setFechaRespuesta(new Date());

        endpointHistoryService.guardarHistorialEndpoint(modeloHistorial);
    }
}
