CREATE TABLE IF NOT EXISTS email_notifications (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    command_id       VARCHAR(50)   NOT NULL,
    recipient_email  VARCHAR(150)  NOT NULL,
    sender_email     VARCHAR(150)  NOT NULL,
    reply_to_email   VARCHAR(150)  NOT NULL,
    subject          VARCHAR(255)  NOT NULL,
    body_text        TEXT          NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    order_id         VARCHAR(50),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    error_message    TEXT,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    sent_at          TIMESTAMPTZ,

    CONSTRAINT uk_email_command_id UNIQUE (command_id),

    CONSTRAINT ck_email_status CHECK (status IN (
        'PENDING', 'SENDING', 'SMTP_ACCEPTED', 'FAILED', 'UNCERTAIN'
    )),

    CONSTRAINT ck_email_recipient_format CHECK (
        recipient_email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'
    )
);

CREATE INDEX IF NOT EXISTS idx_email_status
    ON email_notifications(status);

CREATE INDEX IF NOT EXISTS idx_email_order_id
    ON email_notifications(order_id)
    WHERE order_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_email_created_at
    ON email_notifications(created_at DESC);
