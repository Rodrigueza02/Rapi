package orderapi.controller;

import dtos.CreateOrderRequest;
import orderapi.service.CancelOrderService;
import orderapi.service.CreateOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * POST   /api/order      → crea pedido
 * DELETE /api/order/{id} → cancela pedido (solo CREATED o ASSIGNED)
 *
 * Reglas de autorización en la creación de pedidos:
 *   ADMIN    → puede crear pedidos con cualquier customer_id
 *   CLIENTE  → solo puede crear pedidos con su propio customer_id
 *              (customer_id debe coincidir con el subject del JWT)
 *   Sin auth → rechazado por Spring Security antes de llegar aquí
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final CancelOrderService cancelOrderService;

    public OrderController(CreateOrderService createOrderService,
                           CancelOrderService cancelOrderService) {
        this.createOrderService = createOrderService;
        this.cancelOrderService = cancelOrderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        // La autenticación siempre existe aquí porque Spring Security
        // exige ROLE_CLIENTE o ROLE_ADMIN antes de llegar a este método.
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!isAdmin) {
            // Para CLIENTE: el customer_id debe coincidir con el
            // identificador del usuario autenticado (subject del JWT).
            String authenticatedUserId = authentication.getName();
            if (!authenticatedUserId.equals(request.customerId())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error",   "FORBIDDEN",
                                "message", "No puedes crear pedidos a nombre de otro usuario"
                        ));
            }
        }

        String orderId = createOrderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "order_id", orderId,
                        "status",   "ACCEPTED",
                        "message",  "Order accepted for processing"
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String id) {
        try {
            cancelOrderService.cancel(id);
            return ResponseEntity.ok(Map.of(
                    "order_id", id,
                    "status",   "CANCELLED",
                    "message",  "Order cancelled successfully"
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
