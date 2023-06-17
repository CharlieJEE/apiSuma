package com.camc.apiSuma;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.camc.apiSuma.controller.ApiController;
import com.camc.apiSuma.model.EndpointHistoricalModel;
import com.camc.apiSuma.model.NumbersRequest;
import com.camc.apiSuma.services.EndpointHistoryService;
import com.camc.apiSuma.services.MockExternalService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@SpringJUnitConfig(classes = ApiSumaApplication.class)
class ApiSumaApplicationTests {
	
	private MockMvc mockMvc;

    @Mock
    private MockExternalService externalService;

    @Mock
    private EndpointHistoryService endpointHistoryService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(apiController).build();
    }

    @Test
    void testSumWithIncrease_Success() {
        // Arrange
        NumbersRequest numbersRequest = new NumbersRequest(5, 5);
        double percentage = 10.0;
        double sum = numbersRequest.getNumero1() + numbersRequest.getNumero1();
        double expected = sum + (sum * percentage / 100);

        when(externalService.getPorcentaje()).thenReturn(percentage);

        // Act
        ResponseEntity<String> response = apiController.sumarConIncremento(numbersRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Extraer la parte numérica de la respuesta
        String responseBody = response.getBody();
        double actual = Double.parseDouble(responseBody.split(":")[1].trim());

        assertEquals(expected, actual);
        verify(endpointHistoryService, times(1)).guardarHistorialEndpoint(any(EndpointHistoricalModel.class));
    }

    @Test
    void testSumWithIncrease_TooManyRequests() throws InterruptedException {
        // Arrange
        NumbersRequest numbersRequest = new NumbersRequest(2, 3);
        int maxRequestsPerMinute = 3;

        when(externalService.getPorcentaje()).thenReturn(0.0);

        // Act
        // Send more than the maximum requests per minute
        for (int i = 0; i < maxRequestsPerMinute + 2; i++) {
            apiController.sumarConIncremento(numbersRequest);
            Thread.sleep(1000); // Esperar 1 segundo entre las solicitudes
        }

        ResponseEntity<String> response = apiController.sumarConIncremento(numbersRequest);

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertTrue(response.getBody().contains("Se ha superado el límite de solicitudes por minuto. Vuelva a intentarlo en"));

        // Verificar que se haya llamado al menos 3 veces al método guardarHistorialEndpoint
        verify(endpointHistoryService, atLeast(3)).guardarHistorialEndpoint(any(EndpointHistoricalModel.class));
    }

    @Test
    void testSumWithIncrease_Exception() {
        // Arrange
        NumbersRequest numbersRequest = new NumbersRequest(2, 3);

        when(externalService.getPorcentaje()).thenThrow(new RuntimeException("Error")); // Lanzar una excepción

        // Act
        ResponseEntity<String> response = null;
        try {
            response = apiController.sumarConIncremento(numbersRequest);
        } catch (RuntimeException e) {
            // Handle the exception here (if needed)
        }

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Fallo en el servicio externo, Error al obtener el porcentaje", response.getBody());
        verify(endpointHistoryService, times(1)).guardarHistorialEndpoint(any(EndpointHistoricalModel.class));
    }


    @Test
    void testSimulateStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/simulate")
                .content("{\"estado\": \"error\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Creando simulación de ERROR en el servicio externo"));

        // Verificar que el método simulateError() se haya invocado en externalService
        Mockito.verify(externalService).simularError();
    }

}

