package br.com.mensageria.api.infra.repository;

import br.com.mensageria.api.infra.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    Optional<PaymentRequest> findById(UUID uuid);
    PaymentRequest findDistinctById(UUID id);

    @Query(value = "SELECT p.count FROM payment_request p ORDER BY p.count DESC LIMIT 1", nativeQuery = true)
    Optional<Integer> findLast();
}
