import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],

  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@/components": path.resolve(__dirname, "./components"),
      "@/services": path.resolve(__dirname, "./services"),
      "@/styles": path.resolve(__dirname, "./styles"),
    },
  },
  server: {
    port: 3000, 
    // 개발 환경에서 프론트엔드는 3000번 포트 사용. 배포시에는 80번포트로 변경 요망.
    host: 'localhost',
    open: true,

    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('프록시 오류: ', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('프록시 요청: ', req.method, req.url);
          });
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});
