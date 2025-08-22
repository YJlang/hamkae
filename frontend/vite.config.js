import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
          utils: ['axios']
        }
      }
    }
  },
  server: {
    port: 5173,
    host: true
  },
  preview: {
    port: 4173,
    host: true
  },
  define: {
    // 빌드 시 환경변수 설정
    'import.meta.env.VITE_API_BASE_URL': JSON.stringify('https://hamkae.sku-sku.com'),
    'import.meta.env.VITE_KAKAO_JS_KEY': JSON.stringify('aa165a705be839a4702887b43b0c83b0')
  }
})
