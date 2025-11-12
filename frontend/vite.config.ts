import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000, 
    // 개발 환경에서 프론트엔드는 3000번 포트 사용. 배포시에는 80번포트로 변경 요망.
    open: true,
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});
