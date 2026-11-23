import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],

    server: {
      port: 5173,

      proxy: {
        '/api/auth': {
          target: env.AUTH_PROXY_TARGET || 'http://localhost:8087',
          changeOrigin: true,
        },

        '/api/order': {
          target: env.ORDER_PROXY_TARGET || 'http://localhost:8081',
          changeOrigin: true,
        },

        '/api/dashboard': {
          target: env.DASHBOARD_PROXY_TARGET || 'http://localhost:8085',
          changeOrigin: true,
        },

        '/api/emails': {
          target: env.DASHBOARD_PROXY_TARGET || 'http://localhost:8085',
          changeOrigin: true,
        },

        // Proxy WebSocket en desarrollo — Vite reenvía la conexión al dashboard-backend.
        // En producción esta ruta la maneja Nginx (nginx.aws.conf).
        '/ws': {
          target: env.WS_PROXY_TARGET || 'http://localhost:8086',
          changeOrigin: true,
          ws: true,
        },
      },
    },

    test: {
      environment: 'jsdom',
    },
  };
});
