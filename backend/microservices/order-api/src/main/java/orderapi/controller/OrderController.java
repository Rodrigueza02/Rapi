
package orderapi.controller;

import dtos.CreateOrderRequest;
import orderapi.service.CreateOrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final CreateOrderService createOrderService;

    public OrderController(CreateOrderService createOrderService) {
        this.createOrderService = createOrderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        String authenticatedUserId = jwt.getSubject();

        if (!authenticatedUserId.equals(request.customerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "FORBIDDEN",
                            "message",
                            "No puedes crear pedidos a nombre de otro usuario"
                    ));
        }

        String orderId = createOrderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "order_id", orderId,
                        "status", "ACCEPTED",
                        "message", "Order accepted for processing"
                ));
    }
}
