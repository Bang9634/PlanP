/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_APP_NAME: string
  readonly VITE_APP_VERSION: string
  readonly VITE_NODE_ENV: string
  readonly VITE_DEBUG: string
  readonly VITE_DEV_PORT: string
  readonly VITE_GOOGLE_MAPS_API_KEY?: string
  readonly VITE_ANALYTICS_ID?: string
  
  // Vite 내장 환경변수
  readonly MODE: string
  readonly DEV: boolean
  readonly PROD: boolean
  readonly SSR: boolean
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}