package orderapi.controller;

import dtos.CreateOrderRequest;
import orderapi.service.CreateOrderService;
import orderapi.service.CancelOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * POST   /api/order        → crea pedido
 * DELETE /api/order/{id}   → cancela pedido (solo CREATED o ASSIGNED)
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
            @RequestBody CreateOrderRequest request) {
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
