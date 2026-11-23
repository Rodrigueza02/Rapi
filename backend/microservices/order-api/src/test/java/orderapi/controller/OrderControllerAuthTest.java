package orderapi.controller;

import dtos.CreateOrderRequest;
import orderapi.service.CancelOrderService;
import orderapi.service.CreateOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderControllerAuthTest {

    private CreateOrderService createOrderService;
    private CancelOrderService cancelOrderService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        createOrderService = mock(CreateOrderService.class);
        cancelOrderService = mock(CancelOrderService.class);
        controller = new OrderController(createOrderService, cancelOrderService);

        when(createOrderService.createOrder(any())).thenReturn("test-order-id");
    }

    /* ── Helpers ──────────────────────────────────────────────── */

    private Authentication buildAuth(String userId, String role) {
        return new Authentication() {
            @Override public String getName() { return userId; }
            @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(role));
            }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails()     { return null; }
            @Override public Object getPrincipal()   { return userId; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean b) {}
        };
    }

    private CreateOrderRequest buildRequest(String customerId) {
        return new CreateOrderRequest(
                customerId,
                "+573001234567",
                "Cra 7 #45-21, Bogotá",
                List.of("Hamburguesa"),
                new BigDecimal("42900")
        );
    }

    /* ── Test 1: ADMIN crea pedido para cualquier cliente ─────── */
    @Test
    void admin_puede_crear_pedido_para_cualquier_customer_id() {
        Authentication auth = buildAuth("admin-uuid-123", "ROLE_ADMIN");
        CreateOrderRequest request = buildRequest("customer-uuid-456");

        ResponseEntity<Map<String, String>> response =
                controller.createOrder(request, auth);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("ACCEPTED", response.getBody().get("status"));
        verify(createOrderService, times(1)).createOrder(request);
    }

    /* ── Test 2: CLIENTE crea pedido para sí mismo ────────────── */
    @Test
    void cliente_puede_crear_pedido_con_su_propio_customer_id() {
        String userId = "cliente-uuid-789";
        Authentication auth = buildAuth(userId, "ROLE_CLIENTE");
        CreateOrderRequest request = buildRequest(userId); // mismo id

        ResponseEntity<Map<String, String>> response =
                controller.createOrder(request, auth);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(createOrderService, times(1)).createOrder(request);
    }

    /* ── Test 3: CLIENTE intenta crear pedido para otro usuario── */
    @Test
    void cliente_recibe_403_al_crear_pedido_para_otro_usuario() {
        Authentication auth = buildAuth("cliente-uuid-789", "ROLE_CLIENTE");
        CreateOrderRequest request = buildRequest("otro-cliente-uuid-999"); // id diferente

        ResponseEntity<Map<String, String>> response =
                controller.createOrder(request, auth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().get("error"));
        verify(createOrderService, never()).createOrder(any());
    }

    /* ── Test 4: ADMIN puede crear pedido incluso si su UUID
                 coincide con el customer_id (caso límite) ─────── */
    @Test
    void admin_puede_crear_pedido_con_su_propio_uuid_como_customer_id() {
        String adminUuid = "admin-uuid-123";
        Authentication auth = buildAuth(adminUuid, "ROLE_ADMIN");
        CreateOrderRequest request = buildRequest(adminUuid);

        ResponseEntity<Map<String, String>> response =
                controller.createOrder(request, auth);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(createOrderService, times(1)).createOrder(request);
    }
}
