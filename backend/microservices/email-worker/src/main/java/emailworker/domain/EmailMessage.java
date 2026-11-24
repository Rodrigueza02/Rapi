package emailworker.domain;

import java.util.Objects;

public record EmailMessage(
        String commandId,
        String to,
        String from,
        String replyTo,
        String subject,
        String body,
        String htmlBody,
        EmailNotificationType type,
        String orderId
) {
    public EmailMessage {
        Objects.requireNonNull(commandId, "commandId requerido");
        Objects.requireNonNull(to,        "destinatario requerido");
        Objects.requireNonNull(from,      "remitente requerido");
        Objects.requireNonNull(replyTo,   "reply-to requerido");
        Objects.requireNonNull(subject,   "asunto requerido");
        Objects.requireNonNull(body,      "cuerpo requerido");
        Objects.requireNonNull(type,      "tipo requerido");
        if (commandId.isBlank()) throw new IllegalArgumentException("commandId no puede estar vacío");
        if (to.isBlank())        throw new IllegalArgumentException("destinatario no puede estar vacío");
        if (subject.isBlank())   throw new IllegalArgumentException("asunto no puede estar vacío");
        if (body.isBlank())      throw new IllegalArgumentException("cuerpo no puede estar vacío");
    }
}
