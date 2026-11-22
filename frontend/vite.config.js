import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Vite reenvía /api/* al backend correspondiente durante desarrollo.
 * El auth-service corre en :8087 — ajusta si cambias el puerto.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': {
        target:      'http://localhost:8087',
        changeOrigin: true,
      },
      '/api/order': {
        target:      'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/dashboard': {
        target:      'http://localhost:8086',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
  },
});
