package aggregator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Estado actual de la rueda Round Robin.
 *
 * drivers        → lista ordenada de IDs de repartidores activos
 * current_index  → índice del próximo turno (0-based)
 * next_driver_id → ID del repartidor que recibirá el próximo pedido
 * turn_label     → "4 / 5" para mostrar en la UI
 */
public record RoundRobinDto(
        @JsonProperty("drivers")        List<String> drivers,
        @JsonProperty("current_index")  int          currentIndex,
        @JsonProperty("next_driver_id") String       nextDriverId,
        @JsonProperty("turn_label")     String       turnLabel
) {}
