package br.com.mensageria.api.web;

import br.com.mensageria.api.application.PaymentService;
import br.com.mensageria.api.application.dto.PaymentRequestDTO;
import br.com.mensageria.api.application.dto.PaymentResponseDTO;
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
    public ResponseEntity<PaymentResponseDTO> pagamento(@RequestBody PaymentRequestDTO pagamento){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.pagar(pagamento));
    }

    @GetMapping("/verificar-pagamento/{transactionId}")
    public ResponseEntity<?> verificarPagamento(@PathVariable String transactionId){
        return ResponseEntity.ok(paymentService.verificarPagamento(transactionId));
    }
}
