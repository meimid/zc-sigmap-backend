-- liquibase formatted sql

-- changeset sigmap:17496719590117-1

CREATE TABLE IF NOT EXISTS notification (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         VARCHAR(255)    NOT NULL,
    title           VARCHAR(500)    NOT NULL,
    message         TEXT,
    source          VARCHAR(50)     NOT NULL,
    type            VARCHAR(50)     NOT NULL,
    priority        VARCHAR(20)     NOT NULL DEFAULT 'NORMAL',
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at         DATETIME        NULL,
    reference_id    VARCHAR(255)    NULL,
    reference_type  VARCHAR(100)    NULL,
    action_url      VARCHAR(1000)   NULL,
    metadata        JSON            NULL,
    created_at      DATETIME        NULL,
    created_by      VARCHAR(255)    NULL,
    updated_at      DATETIME        NULL,
    updated_by      VARCHAR(255)    NULL,
    deleted_at      DATETIME        NULL,
    deleted_by      VARCHAR(255)    NULL,
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_notification_user_id       ON notification (user_id);
CREATE INDEX idx_notification_source        ON notification (source);
CREATE INDEX idx_notification_is_read       ON notification (is_read);
CREATE INDEX idx_notification_created_at    ON notification (created_at);
CREATE INDEX idx_notification_user_unread   ON notification (user_id, is_read, is_deleted);
