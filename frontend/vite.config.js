import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * En desarrollo Vite reenvía /api/* al microservicio correspondiente.
 *
 * auth-service          → :8087
 * order-api             → :8081
 * dashboard-aggregator  → :8085  (orders, stats, round-robin)
 * dashboard-backend     → :8086  (WebSocket ws://localhost:8086/ws)
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': {
        target:       'http://localhost:8087',
        changeOrigin: true,
      },
      '/api/order': {
        target:       'http://localhost:8081',
        changeOrigin: true,
      },
      // dashboard/orders, dashboard/stats, dashboard/round-robin
      '/api/dashboard': {
        target:       'http://localhost:8085',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
  },
});
