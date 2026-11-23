package aggregator.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendEmailRequest(
        @NotBlank @Email
        String recipientEmail,

        @NotBlank @Size(max = 255)
        String subject,

        @NotBlank
        String bodyText,

        String orderId
) {}
