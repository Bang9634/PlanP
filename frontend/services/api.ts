declare global {
  interface Window {
    APP_CONFIG?: {
      API_BASE_URL: string;
      DEBUG: boolean;
    };
  }
}

/**
 * API Base URL 결정 (타입 안전)
 */
const getApiBaseUrl = (): string => {
  // 1. 런타임 설정 우선 (config.js)
  if (typeof window !== 'undefined' && window.APP_CONFIG?.API_BASE_URL) {
    console.log('🔧 API URL 소스: 런타임 config.js');
    return window.APP_CONFIG.API_BASE_URL;
  }
  
  // 2. Vite 환경변수 (타입 안전)
  if (import.meta.env.VITE_API_URL) {
    console.log('🔧 API URL 소스: Vite 환경변수 -', import.meta.env.VITE_API_URL);
    return import.meta.env.VITE_API_URL;
  }
  
  // 3. 환경별 자동 감지
  if (typeof window !== 'undefined') {
    const hostname = window.location.hostname;
    const protocol = window.location.protocol;
    const port = window.location.port;
    
    // 로컬 개발환경 (포트 3000)
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      if (port === '3000' || !port) {
        console.log('🔧 API URL 소스: 로컬 개발환경 (3000→8080)');
        return 'http://localhost:8080/api';
      }
    }
    
    // 프로덕션 환경 (포트 80 → 8080)
    console.log('🔧 API URL 소스: 프로덕션 환경 (80→8080)');
    return `${protocol}//${hostname}:8080/api`;
  }
  
  // 4. 기본값 (로컬)
  console.log('🔧 API URL 소스: 기본값');
  return 'http://localhost:8080/api';
};

/**
 * 디버그 모드 확인 (타입 안전)
 */
const isDebugMode = (): boolean => {
  if (typeof window !== 'undefined' && window.APP_CONFIG?.DEBUG !== undefined) {
    return window.APP_CONFIG.DEBUG;
  }
  
  // Vite 환경변수 타입 안전하게 확인
  const debugEnv = import.meta.env.VITE_DEBUG;
  const isDev = import.meta.env.DEV;
  
  return debugEnv === 'true' || isDev;
};

const API_BASE_URL = getApiBaseUrl();
const DEBUG = isDebugMode();

// 개발환경에서만 상세 로깅 (타입 안전)
if (DEBUG) {
  console.group('🌐 PlanP API 설정');
  console.log('현재 URL:', typeof window !== 'undefined' ? window.location.href : 'Server');
  console.log('프론트엔드 포트:', typeof window !== 'undefined' ? (window.location.port || '기본포트') : 'N/A');
  console.log('API Base URL:', API_BASE_URL);
  console.log('Environment:', import.meta.env.MODE);
  console.log('Production:', import.meta.env.PROD);
  console.log('Development:', import.meta.env.DEV);
  console.log('Debug Flag:', import.meta.env.VITE_DEBUG);
  console.groupEnd();
}

// 인터페이스 정의
export interface SignupRequest {
  userId: string;
  password: string;
  name: string;
  email: string;
}

export interface SignupResponse {
  success: boolean;
  message: string;
  userId?: string;
}

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  user?: {
    userId: string;
    name: string;
    email: string;
  };
}

export class ApiService {
  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    
    try {
      if (DEBUG) {
        console.log(`🌐 API 요청: ${options.method || 'GET'} ${url}`, {
          headers: options.headers,
          body: options.body
        });
      }
      
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          ...options.headers,
        },
        credentials: 'include',
        ...options,
      });

      if (!response.ok) {
        const errorText = await response.text();
        const error = new Error(`HTTP ${response.status}: ${errorText || response.statusText}`);
        
        if (DEBUG) {
          console.error(`❌ API 에러: ${url}`, {
            status: response.status,
            statusText: response.statusText,
            error: errorText
          });
        }
        
        throw error;
      }

      const data = await response.json();
      
      if (DEBUG) {
        console.log(`✅ API 응답: ${url}`, data);
      }
      
      return data;
      
    } catch (error) {
      if (error instanceof TypeError && error.message.includes('fetch')) {
        const networkError = new Error(
          `서버에 연결할 수 없습니다. 백엔드 서버(${API_BASE_URL})가 실행 중인지 확인해주세요.`
        );
        
        if (DEBUG) {
          console.error(`🔌 네트워크 오류 (${endpoint}):`, {
            originalError: error.message,
            apiUrl: API_BASE_URL,
            endpoint: endpoint
          });
        }
        
        throw networkError;
      }
      
      if (DEBUG) {
        console.error(`❌ API 에러 (${endpoint}):`, error);
      }
      
      throw error;
    }
  }

  // 사용자 관리 API
  async signup(data: SignupRequest): Promise<SignupResponse> {
    return this.request<SignupResponse>('/users/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async login(data: LoginRequest): Promise<LoginResponse> {
    return this.request<LoginResponse>('/users/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async logout(): Promise<{ success: boolean; message: string }> {
    return this.request('/users/logout', {
      method: 'POST',
    });
  }

  async checkUserId(userId: string): Promise<{ available: boolean; message: string }> {
    return this.request(`/users/check-id?userId=${encodeURIComponent(userId)}`);
  }

  async checkEmail(email: string): Promise<{ available: boolean; message: string }> {
    return this.request(`/users/check-email?email=${encodeURIComponent(email)}`);
  }

  // Health Check
  async healthCheck(): Promise<string> {
    const healthUrl = API_BASE_URL.replace('/api', '') + '/health';
    
    if (DEBUG) {
      console.log(`🏥 Health Check: ${healthUrl}`);
    }
    
    const response = await fetch(healthUrl);
    if (!response.ok) {
      throw new Error(`서버 상태 확인 실패: ${response.status}`);
    }
    return response.text();
  }

  // 현재 설정 정보 반환 (디버깅용)
  getConfig() {
    return {
      apiBaseUrl: API_BASE_URL,
      debug: DEBUG,
      environment: import.meta.env.MODE,
      production: import.meta.env.PROD,
      development: import.meta.env.DEV,
      viteDebug: import.meta.env.VITE_DEBUG,
    };
  }
}

export const apiService = new ApiService();

// 개발환경에서만 전역 객체에 추가 (디버깅용)
if (DEBUG && typeof window !== 'undefined') {
  (window as any).apiService = apiService;
  console.log('🔧 전역 객체 등록: window.apiService - 콘솔에서 apiService.getConfig() 실행 가능');
}