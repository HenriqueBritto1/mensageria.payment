package br.com.mensageria.processor.infra.repository;

import br.com.mensageria.processor.infra.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    Optional<PaymentRequest> findById(UUID id);
}
