package smsworker.service;

import dtos.Names;
import dtos.NotificationCommand;
import dtos.NotificationPayloadRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsSenderServiceTest {

    private SmsSenderService service;

    @BeforeEach
    void setUp() {
        service = new SmsSenderService();
    }

    @Test
    void shouldProcessClientSms() {

        
        NotificationPayloadRecord payload =
                new NotificationPayloadRecord(
                        "order-001",
                        "customer-001",
                        "Your order has been confirmed"
                );

        NotificationCommand command =
                NotificationCommand.of(
                        Names.ROUTING_CLIENT_SMS,
                        payload
                );

        assertDoesNotThrow(() -> service.send(command));
    }

    @Test
    void shouldRejectDriverPush() {

        
        NotificationPayloadRecord payload =
                new NotificationPayloadRecord(
                        "order-001",
                        "driver-001",
                        "New delivery assigned"
                );

        NotificationCommand command =
                NotificationCommand.of(
                        Names.ROUTING_DRIVER_PUSH,
                        payload
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> service.send(command)
        );
    }

    @Test
    void shouldRejectNullCommand() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.send(null)
        );
    }
}
