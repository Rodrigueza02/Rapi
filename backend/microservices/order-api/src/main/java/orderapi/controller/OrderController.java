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
 * DELETE /api/order/{id} → cancela pedido (ADMIN, solo estados cancelables)
 *
 * La cancelación real la ejecuta order-processor vía RabbitMQ.
 * El 403 anterior se debía a que SecurityConfig no tenía regla para DELETE.
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

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!isAdmin) {
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
                    "message",  "Order cancellation sent"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null && e.getMessage().contains("DELIVERED")
                    ? "Este pedido ya fue entregado y no puede cancelarse."
                    : "No se puede cancelar: " + e.getMessage();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "UNPROCESSABLE_STATE", "message", msg));
        }
    }
}
