-- MIGRACIÓN 003 — compatible, no destructiva. LOCAL ÚNICAMENTE.
-- Ejecutar con: docker exec -i rappi-stream-actual-postgres-1 psql -U rappi -d rappistream < 003_add_state_columns.sql
-- Respaldo previo generado: backup_before_003_20261123_132835.sql

BEGIN;

-- ── 1. orders: agregar prepared_at ──────────────────────────────────────────
ALTER TABLE orders ADD COLUMN IF NOT EXISTS prepared_at TIMESTAMPTZ;

-- ── 2. order_history: eliminar CHECK que limita a 3 estados ─────────────────
-- El constraint se llama order_history_state_check (verificado con \d order_history)
ALTER TABLE order_history DROP CONSTRAINT IF EXISTS order_history_state_check;

-- ── 3. order_history: agregar columnas de datos del pedido ───────────────────
ALTER TABLE order_history ADD COLUMN IF NOT EXISTS customer_id      TEXT;
ALTER TABLE order_history ADD COLUMN IF NOT EXISTS delivery_address TEXT;
ALTER TABLE order_history ADD COLUMN IF NOT EXISTS total_amount     NUMERIC(12,2);
ALTER TABLE order_history ADD COLUMN IF NOT EXISTS items_json       TEXT;

-- ── 4. Verificación de integridad ────────────────────────────────────────────
-- Registros existentes preservados
DO $$
DECLARE
  cnt_orders  INTEGER;
  cnt_history INTEGER;
BEGIN
  SELECT COUNT(*) INTO cnt_orders  FROM orders;
  SELECT COUNT(*) INTO cnt_history FROM order_history;
  RAISE NOTICE 'orders: %, order_history: %', cnt_orders, cnt_history;
  IF cnt_orders < 11 THEN
    RAISE EXCEPTION 'Verificacion fallida: se esperaban >= 11 orders, encontrados %', cnt_orders;
  END IF;
END $$;

COMMIT;
