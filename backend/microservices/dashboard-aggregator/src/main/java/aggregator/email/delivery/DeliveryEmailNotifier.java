package aggregator.email.delivery;

import aggregator.email.database.EmailInsertGateway;
import aggregator.email.publisher.EmailCommandPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DeliveryEmailNotifier {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEmailNotifier.class);

    private final DeliveryOrderJpaRepository orders;
    private final DeliveryUserJpaRepository  users;
    private final EmailInsertGateway         insertGateway;
    private final EmailCommandPublisher      publisher;
    private final DeliveryEmailBodyBuilder   bodyBuilder;
    private final String senderEmail;
    private final String replyToEmail;

    public DeliveryEmailNotifier(
            DeliveryOrderJpaRepository orders,
            DeliveryUserJpaRepository users,
            EmailInsertGateway insertGateway,
            EmailCommandPublisher publisher,
            DeliveryEmailBodyBuilder bodyBuilder,
            @Value("${app.email.sender:no-reply@deliveryflow.com}") String senderEmail,
            @Value("${app.email.reply-to:soporte@deliveryflow.com}") String replyToEmail) {
        this.orders       = orders;
        this.users        = users;
        this.insertGateway = insertGateway;
        this.publisher    = publisher;
        this.bodyBuilder  = bodyBuilder;
        this.senderEmail  = senderEmail;
        this.replyToEmail = replyToEmail;
    }

    public void notifyDelivered(String orderId) {
        DeliveryOrderRow order = orders.findById(orderId).orElse(null);
        if (order == null || !"DELIVERED".equals(order.getStatus())) return;
        DeliveryUserRow user = resolveUser(order.getCustomerId());
        if (user == null) return;

        String commandId = UUID.nameUUIDFromBytes(("ORDER_DELIVERED:" + orderId).getBytes()).toString();

        List<String> items = parseItemsJson(order.getItemsJson());
        String subject = "¡Tu pedido llegó con éxito! — DeliveryFlow";
        String body = bodyBuilder.buildText(user.getNombre(), orderId, items, order.getTotalAmount());
        String html = bodyBuilder.buildHtml(user.getNombre(), orderId, items, order.getTotalAmount());

        boolean inserted = insertGateway.insertPending(commandId, user.getEmail(), senderEmail,
                replyToEmail, subject, body, "ORDER_DELIVERED", orderId);
        if (!inserted) { log.info("Correo ya existe para orderId={}, no se republica", orderId); return; }
        publish(commandId, user.getEmail(), subject, body, html, orderId);
    }

    private void publish(String commandId, String recipient, String subject, String body, String html, String orderId) {
        try {
            publisher.publish(Map.of("command_id", commandId, "recipient_email", recipient,
                    "sender_email", senderEmail, "reply_to_email", replyToEmail,
                    "subject", subject, "body_text", body, "html_body", html,
                    "notification_type", "ORDER_DELIVERED", "order_id", orderId));
            log.info("Correo de entrega encolado: orderId={}", orderId);
        } catch (RuntimeException e) {
            log.error("PENDING registrado pero publicación falló (orderId={})", orderId, e);
        }
    }

    private DeliveryUserRow resolveUser(String customerId) {
        UUID uid = parseUuid(customerId);
        if (uid == null) { log.warn("customer_id={} no es UUID", customerId); return null; }
        DeliveryUserRow u = users.findById(uid).orElse(null);
        if (u == null || !u.isActivo() || isBlank(u.getEmail())) { log.warn("Usuario {} no válido", uid); return null; }
        return u;
    }

    private UUID parseUuid(String v) { try { return UUID.fromString(v); } catch (Exception e) { return null; } }
    private boolean isBlank(String s) { return s == null || s.isBlank(); }

    @SuppressWarnings("unchecked")
    private List<String> parseItemsJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, List.class); }
        catch (Exception e) { return List.of(); }
    }
}
