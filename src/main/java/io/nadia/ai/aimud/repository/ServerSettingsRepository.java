package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.ServerSettings;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerSettingsRepository extends R2dbcRepository<ServerSettings, Long> {
}
