package com.camc.apiSuma.repository;

import com.camc.apiSuma.model.EndpointHistoricalModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointHistoricalRepository extends JpaRepository<EndpointHistoricalModel, Long> {
}
