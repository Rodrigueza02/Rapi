package dtos;

public final class Names {

    private Names() {}

    // ---------- Kafka ----------
    public static final String ORDER_EVENTS_TOPIC = "order_events";

    // ---------- RabbitMQ: cola de trabajo (comandos) ----------
    public static final String NEW_ORDERS_QUEUE    = "new_orders_queue";
    public static final String CANCEL_ORDERS_QUEUE = "cancel_orders_queue";

    // ---------- RabbitMQ: topic exchange (notificaciones) ----------
    public static final String NOTIFICATIONS_EXCHANGE = "notifications";

    public static final String ROUTING_CLIENT_SMS = "notify.client.sms";
    public static final String ROUTING_DRIVER_PUSH = "notify.driver.push";

    public static final String BINDING_CLIENT_ALL = "notify.client.*";
    public static final String BINDING_DRIVER_ALL = "notify.driver.*";

    public static final String SMS_QUEUE  = "sms_queue";
    public static final String PUSH_QUEUE = "push_queue";

    // ---------- Tipos de evento (campo "event_type" de cada mensaje Kafka) ----------
    public static final String EVENT_ORDER_PREPARED  = "ORDER_PREPARED";
    public static final String EVENT_DRIVER_ASSIGNED = "DRIVER_ASSIGNED";
    public static final String EVENT_DRIVER_ARRIVING = "DRIVER_ARRIVING";
    public static final String EVENT_ORDER_DELIVERED = "ORDER_DELIVERED";
    public static final String EVENT_ORDER_CANCELLED = "ORDER_CANCELLED";
}
