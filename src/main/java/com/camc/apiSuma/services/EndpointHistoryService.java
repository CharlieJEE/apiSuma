package com.camc.apiSuma.services;

import com.camc.apiSuma.model.EndpointHistoricalModel;
import com.camc.apiSuma.repository.EndpointHistoricalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndpointHistoryService {

    private final EndpointHistoricalRepository endpointHistoricalRepository;

    @Autowired
    public EndpointHistoryService(EndpointHistoricalRepository endpointHistoricalRepository) {
        this.endpointHistoricalRepository = endpointHistoricalRepository;
    }

    public void saveEndpointHistorical(EndpointHistoricalModel historicalModel) {
        endpointHistoricalRepository.save(historicalModel);
    }

    public List<EndpointHistoricalModel> getAllEndpointHistorical() {
        return endpointHistoricalRepository.findAll();
    }
}
