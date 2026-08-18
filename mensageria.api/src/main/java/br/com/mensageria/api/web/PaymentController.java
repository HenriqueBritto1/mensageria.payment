package br.com.mensageria.api.web;

import br.com.mensageria.api.application.PaymentService;
import br.com.mensageria.api.application.dto.PaymentRequestDTO;
import br.com.mensageria.api.application.dto.PaymentResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pagamento")
    public ResponseEntity<PaymentResponseDTO> pagamento(@RequestBody PaymentRequestDTO pagamento){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.pagar(pagamento));
    }
}
