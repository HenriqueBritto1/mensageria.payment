package br.com.mensageria.api.infra.repository;

import br.com.mensageria.api.infra.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    Optional<PaymentRequest> findById(UUID uuid);
    PaymentRequest findDistinctById(UUID id);
}
