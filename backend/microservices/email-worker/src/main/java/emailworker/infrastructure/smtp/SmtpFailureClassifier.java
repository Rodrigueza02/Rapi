package emailworker.infrastructure.smtp;

import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

@Component
public class SmtpFailureClassifier {

    private static final String[] PERMANENT_SIGNALS = {
        "550", "551", "552", "553", "554",
        "Invalid Addresses",
        "rejected"
    };

    public boolean isPermanent(MailSendException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        for (String signal : PERMANENT_SIGNALS) {
            if (msg.contains(signal)) return true;
        }
        return false;
    }
}
