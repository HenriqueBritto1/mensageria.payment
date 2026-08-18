package br.com.mensageria.api.infra.repository;

import br.com.mensageria.api.infra.entity.ApiClients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiClientsRepository extends JpaRepository<ApiClients, Long> {
    ApiClients findByApiKey(String apiKey);
}
