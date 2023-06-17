package com.camc.apiSuma.services;

import com.camc.apiSuma.model.EndpointHistoricalModel;
import com.camc.apiSuma.repository.EndpointHistoricalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndpointHistoryService {

    private final EndpointHistoricalRepository endpointHistoricalRepository;

    @Autowired
    public EndpointHistoryService(EndpointHistoricalRepository endpointHistoricalRepository) {
        this.endpointHistoricalRepository = endpointHistoricalRepository;
    }

    public void guardarHistorialEndpoint(EndpointHistoricalModel historicalModel) {
        endpointHistoricalRepository.save(historicalModel);
    }

    public List<EndpointHistoricalModel> obtenerHistorialCompletoEndpoint(int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fechaRespuesta").descending());
    Page<EndpointHistoricalModel> pageResult = endpointHistoricalRepository.findAll(pageable);
    return pageResult.getContent();
}
}
