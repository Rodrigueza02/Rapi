package notificationrouter.service;

import dtos.*;
import notificationrouter.ports.NotificationCommandPort;
import notificationrouter.strategy.NotificationMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Enruta eventos de dominio a los canales de notificación (SMS, Push).
 *
 * <p>Política de manejo de eventos:
 * <ul>
 *   <li>Si una estrategia soporta el evento, se publica la notificación.</li>
 *   <li>Si la estrategia no produce comando ({@code Optional.empty}), se registra
 *       una advertencia — indica un fallo en la lógica de mapeo, no un evento ignorable.</li>
 *   <li>Si ninguna estrategia soporta el evento y el evento pertenece al conjunto
 *       de tipos con notificación esperada ({@code NOTIFICATION_EVENTS}), se
 *       registra una advertencia.</li>
 *   <li>Eventos sin notificación (ORDER_CREATED, ORDER_PREPARED, ORDER_ON_THE_WAY)
 *       se saltan silenciosamente en DEBUG.</li>
 * </ul>
 *
 * <p>No se lanza excepción por eventos sin estrategia: el router no debe detener
 * el consumo Kafka por tipos de evento legítimos que no requieren notificación.
 */
@Service
public class NotificationRouterService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRouterService.class);

    /**
     * Tipos de evento que sí deben producir una notificación.
     * Si llegan sin estrategia, es un fallo de configuración (WARN).
     */
    private static final Set<Class<? extends OrderEvent>> NOTIFICATION_EVENTS = Set.of(
            DriverAssignedEvent.class,
            DriverArrivingEvent.class,
            OrderDeliveredEvent.class
    );

    private final List<NotificationMappingStrategy> strategies;
    private final NotificationCommandPort            publisher;

    public NotificationRouterService(List<NotificationMappingStrategy> strategies,
                                     NotificationCommandPort publisher) {
        this.strategies = List.copyOf(strategies);
        this.publisher  = publisher;
    }

    public void route(OrderEvent event) {
        if (event == null) throw new IllegalArgumentException("Order event must not be null");

        for (NotificationMappingStrategy strategy : strategies) {
            if (!strategy.supports(event)) continue;

            strategy.map(event).ifPresentOrElse(
                    publisher::publish,
                    () -> log.warn(
                            "La estrategia {} soporta el evento {} pero no produjo comando " +
                            "(orderId={}). Revisar la lógica de mapeo.",
                            strategy.getClass().getSimpleName(),
                            event.getClass().getSimpleName(),
                            event.orderId())
            );
            return;
        }

        // Ninguna estrategia soporta este evento
        if (NOTIFICATION_EVENTS.contains(event.getClass())) {
            log.warn("Ninguna estrategia maneja el evento {} (orderId={}). " +
                     "Verificar que las estrategias estén registradas como beans.",
                    event.getClass().getSimpleName(), event.orderId());
        } else {
            log.debug("Evento {} no requiere notificación, omitido (orderId={}).",
                    event.getClass().getSimpleName(), event.orderId());
        }
    }
}
