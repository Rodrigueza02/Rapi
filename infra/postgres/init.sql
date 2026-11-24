-- ============================================================
-- init.sql  —  Inicialización de la base de datos rappistream
-- Se ejecuta una sola vez al crear el volumen de Postgres.
-- ============================================================

-- ── Tabla de usuarios (auth-service) ────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id              UUID          PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    password_hash   VARCHAR(255)  NOT NULL,
    rol             VARCHAR(20)   NOT NULL DEFAULT 'CLIENTE',
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_rol
        CHECK (rol IN ('ADMIN', 'CLIENTE', 'REPARTIDOR'))
);

-- ── Seed: usuario administrador por defecto ──────────────────
-- Contraseña: admin123  (BCrypt strength 10)
-- !! Cambiar en producción !!
INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo)
VALUES (
    gen_random_uuid(),
    'Admin',
    'admin@deliveryflow.com',
    '$2a$10$tXXUHf69/PgMyR6FwmZxuOxfJdQsUqU5.yVp2tlhv1RrImK56FSC.',
    'ADMIN',
    TRUE
)
ON CONFLICT (email) DO NOTHING;
