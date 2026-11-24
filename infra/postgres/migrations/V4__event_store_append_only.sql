-- MIGRACIÓN V4 — Event Store append-only para order_history
-- Compatible y no destructiva. LOCAL ÚNICAMENTE.
-- NO ejecutar en AWS sin aprobación explícita.
--
-- Propósito: convertir order_history en un append-only Event Store donde
-- cada fila representa un único evento de dominio.
--
-- Cambios:
--   1. Agrega event_type   VARCHAR(40)  NOT NULL DEFAULT 'LEGACY'
--   2. Agrega sequence_num INTEGER      NOT NULL DEFAULT 0
--   3. Elimina cualquier UNIQUE sobre order_id que impida múltiples filas
--      para el mismo pedido (solo si existe dicho constraint).
--   4. Crea índice compuesto (order_id, sequence_num) para replay eficiente.
--
-- Compatibilidad: las filas históricas quedan con event_type='LEGACY'
-- y sequence_num=0. No se inventan ni modifican datos existentes.

BEGIN;

-- 1. Nuevas columnas (idempotente con IF NOT EXISTS)
ALTER TABLE order_history
    ADD COLUMN IF NOT EXISTS event_type   VARCHAR(40) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN IF NOT EXISTS sequence_num INTEGER     NOT NULL DEFAULT 0;

-- 2. Eliminar unique constraint sobre order_id si existe
--    (el nombre puede variar; buscamos por columna)
DO $$
DECLARE
    cname TEXT;
BEGIN
    SELECT tc.constraint_name INTO cname
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
         ON ccu.constraint_name = tc.constraint_name
    WHERE tc.table_name = 'order_history'
      AND tc.constraint_type = 'UNIQUE'
      AND ccu.column_name = 'order_id'
    LIMIT 1;

    IF cname IS NOT NULL THEN
        EXECUTE 'ALTER TABLE order_history DROP CONSTRAINT ' || quote_ident(cname);
        RAISE NOTICE 'Eliminado constraint UNIQUE: %', cname;
    ELSE
        RAISE NOTICE 'No existe UNIQUE sobre order_id en order_history — sin cambios';
    END IF;
END $$;

-- 3. Índice compuesto para replay eficiente por pedido
CREATE INDEX IF NOT EXISTS idx_order_history_replay
    ON order_history(order_id, sequence_num ASC, occurred_at ASC);

COMMIT;
