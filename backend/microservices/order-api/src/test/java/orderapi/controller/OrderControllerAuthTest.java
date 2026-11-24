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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderControllerAuthTest {

    private CreateOrderService createOrderService;
    private CancelOrderService cancelOrderService;
    private OrderController    controller;

    @BeforeEach void setUp() {
        createOrderService = mock(CreateOrderService.class);
        cancelOrderService = mock(CancelOrderService.class);
        controller = new OrderController(createOrderService, cancelOrderService);
        when(createOrderService.createOrder(any())).thenReturn("test-order-id");
    }

    private Authentication auth(String userId, String role) {
        return new Authentication() {
            @Override public String getName() { return userId; }
            @Override public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(role)); }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails()     { return null; }
            @Override public Object getPrincipal()   { return userId; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean b) {}
        };
    }

    private CreateOrderRequest req(String cid) {
        return new CreateOrderRequest(cid, "+573001234567", "Cra 7 #45-21",
                List.of("Hamburguesa"), new BigDecimal("42900"));
    }

    @Test void admin_crea_pedido_para_cualquier_customer() {
        var res = controller.createOrder(req("any-uuid"), auth("admin", "ROLE_ADMIN"));
        assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    }

    @Test void cliente_crea_su_propio_pedido() {
        var res = controller.createOrder(req("me"), auth("me", "ROLE_CLIENTE"));
        assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    }

    @Test void cliente_no_puede_crear_pedido_ajeno() {
        var res = controller.createOrder(req("otro"), auth("me", "ROLE_CLIENTE"));
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test void cancelacion_exitosa_devuelve_200() {
        doNothing().when(cancelOrderService).cancel("o-1");
        var res = controller.cancelOrder("o-1");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("CANCELLED", res.getBody().get("status"));
    }

    @Test void cancelar_pedido_entregado_devuelve_409_con_mensaje_claro() {
        doThrow(new IllegalStateException("Cannot cancel when the order is DELIVERED"))
                .when(cancelOrderService).cancel("o-2");
        var res = controller.cancelOrder("o-2");
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertTrue(res.getBody().get("message").contains("ya fue entregado"));
    }

    @Test void cancelar_pedido_on_the_way_devuelve_409() {
        doThrow(new IllegalStateException("Cannot cancel when the order is ON_THE_WAY"))
                .when(cancelOrderService).cancel("o-3");
        var res = controller.cancelOrder("o-3");
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }
}
