package br.com.mensageria.api.web;

import br.com.mensageria.api.application.PaymentService;
import br.com.mensageria.api.application.dto.PaymentCancelResponseDTO;
import br.com.mensageria.api.application.dto.PaymentRequestDTO;
import br.com.mensageria.api.application.dto.PaymentResponseDTO;
import br.com.mensageria.commons.dto.PaymentReceiveDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pagamento")
    @Operation(summary = "Realizar pagamento", description = "Cria order de pagamento diretamente com a API do mercado pago.")
    public ResponseEntity<PaymentResponseDTO> payment(@RequestBody @Valid PaymentRequestDTO pagamento){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.makePayment(pagamento));
    }

    @GetMapping("/pagamento/{transactionId}")
    @Operation(summary = "Verificar pagamento", description = "Verifica informações do pagamento e status atual")
    public ResponseEntity<PaymentReceiveDTO> verifyOrder(@PathVariable String transactionId){
        return ResponseEntity.ok(paymentService.verifyOrder(transactionId));
    }

    @DeleteMapping("/pagamento/{transactionId}")
    @Operation(summary = "Cancelar pagamento", description = "Cancela ordem de pagamento")
    public ResponseEntity<PaymentCancelResponseDTO> cancelOrder(@PathVariable String transactionId){
        return ResponseEntity.ok(paymentService.cancelOrder(transactionId));
    }

    @PostMapping("/pagamento/refund/{transactionId}")
    @Operation(summary = "Reembolsar pagamento", description = "Reembolsa transação completa")
    public ResponseEntity<PaymentResponseDTO> refundTransaction(@PathVariable String transactionId){
        return ResponseEntity.ok(paymentService.refund(transactionId));
    }
}
