package dtos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCreatedCommandTest {

    private static OrderCreatedCommand commandWith(String orderId, String phone, List<String> items, BigDecimal total) {
        return new OrderCreatedCommand(orderId, "customer-1", phone, "Calle 18 # 25-10, Pasto", items, total);
    }

    @Test
    void createsCommandWhenAllDataIsValid() {
        // Arrange
        List<String> items = List.of("Hamburguesa", "Gaseosa");

        // Act
        OrderCreatedCommand command = commandWith("order-1", "+573001234567", items, new BigDecimal("25000"));

        // Assert
        assertThat(command.orderId()).isEqualTo("order-1");
        assertThat(command.items()).containsExactly("Hamburguesa", "Gaseosa");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsNullOrBlankOrderId(String invalidOrderId) {
        // Arrange + Act + Assert
        assertThatThrownBy(() -> commandWith(invalidOrderId, "3001234567", List.of("Pizza"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("order_id");
    }

    @Test
    void rejectsInvalidPhone() {
        assertThatThrownBy(() -> commandWith("order-1", "abc", List.of("Pizza"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customer_phone");
    }

    @Test
    void rejectsEmptyItems() {
        assertThatThrownBy(() -> commandWith("order-1", "3001234567", List.of(), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("items");
    }

    @Test
    void rejectsZeroOrNegativeTotal() {
        assertThatThrownBy(() -> commandWith("order-1", "3001234567", List.of("Pizza"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total_amount");
    }

    @Test
    void keepsAnImmutableCopyOfTheItems() {
        // Arrange
        List<String> original = new ArrayList<>(List.of("Pizza"));
        OrderCreatedCommand command = commandWith("order-1", "3001234567", original, BigDecimal.TEN);

        // Act
        original.add("Intruder");

        // Assert
        assertThat(command.items()).containsExactly("Pizza");
        assertThatThrownBy(() -> command.items().add("Other"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void buildsTheCommandFromTheRestRequest() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                "customer-1", "3001234567", "Calle 18", List.of("Pizza"), BigDecimal.TEN);

        // Act
        OrderCreatedCommand command = OrderCreatedCommand.from("order-9", request);

        // Assert
        assertThat(command.orderId()).isEqualTo("order-9");
        assertThat(command.customerId()).isEqualTo("customer-1");
    }
}