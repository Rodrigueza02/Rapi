package notificationrouter.ports;

import dtos.NotificationCommand;

public interface NotificationCommandPort {

    void publish(NotificationCommand command);
}
