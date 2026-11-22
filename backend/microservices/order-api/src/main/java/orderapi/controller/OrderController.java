package orderapi.controller;

import dtos.CreateOrderRequest;
import orderapi.service.CreateOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody CreateOrderRequest request) {

        String orderId = createOrderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "order_id", orderId,
                        "status", "ACCEPTED",
                        "message", "Order accepted for processing"
                ));
    }
}
