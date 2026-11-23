-- Seed: usuario administrador por defecto
-- Contraseña: admin123  (BCrypt strength 10)
-- Cambiar en producción.

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
