package emailworker.port;

import emailworker.domain.EmailMessage;
import emailworker.exception.EmailSendException;

public interface EmailSenderPort {

    void send(EmailMessage message) throws EmailSendException;
}
