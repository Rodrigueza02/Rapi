-- Seed: usuario administrador de referencia para entorno LOCAL / CI.
-- EN PRODUCCIÓN (AWS): este script NO debe ejecutarse.
--   El administrador real ya existe en la base de datos de producción.
--   Protección: ON CONFLICT (email) DO NOTHING — no sobreescribe registros existentes.
--
-- Para aplicar manualmente en local:
--   psql -U rappi -d rappistream -f 002_seed_admin.sql
--
-- Contraseña de referencia: admin123  (BCrypt strength 10)
-- CAMBIAR en cualquier entorno real antes de exponer el servicio.

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
