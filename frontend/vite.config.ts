import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      host: '127.0.0.1',
      port: 3015,
      strictPort: true,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8095',
          changeOrigin: true
        }
      }
    },
    preview: {
      host: '127.0.0.1',
      port: 3015,
      strictPort: true
    },
    build: {
      sourcemap: false,
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            element: ['element-plus'],
            echarts: ['echarts'],
            utils: ['axios', '@vueuse/core', 'lucide-vue-next']
          }
        }
      }
    }
  }
})
