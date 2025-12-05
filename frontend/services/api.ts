declare global {
  interface Window {
    APP_CONFIG?: {
      API_BASE_URL: string;
      DEBUG: boolean;
    };
  }
}
import {AuthService} from './AuthService';

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
  // 회원가입후 생성된 토큰을 받음
  accessToken?: string;
  refreshToken?: string;
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
  // 로그인후 생성된 토큰을 받음
  accessToken?: string;
  refreshToken?: string;
}


export class ApiService {
// 세번째 매개변수 requireAuth는 인증 상태를 요구하느냐를 의미함
// 기본값은 false로 로그인, 회원가입과 같은 api를 호출할때는 로그인 상태가 아니기에 false,
// 로그아웃이나 내 정보 보기와 같은 api를 호출할 때는 로그인 상태를 요구하기에 true가 되어야함.
  private async request<T>(endpoint: string, options: RequestInit = {}, requiresAuth: boolean = false ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    
    try {
      // 헤더 구성
      const headers: HeadersInit = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...options.headers,
      };

      // 인증이 필요한 요청인 경우 토큰 추가
      if (requiresAuth) {
        const token = AuthService.getAccessToken();
        
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
          
          if (DEBUG) {
            console.log('🔑 Authorization 헤더 추가:', `Bearer ${token.substring(0, 20)}...`);
          }
        } else {
          console.warn('⚠️ 인증이 필요한 요청이지만 토큰이 없습니다');
          
          // 로그인 페이지로 리다이렉트
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
          
          throw new Error('인증이 필요합니다. 로그인해주세요.');
        }
      }

      if (DEBUG) {
        console.log(`🌐 API 요청: ${options.method || 'GET'} ${url}`, {
          headers,
          body: options.body,
          requiresAuth
        });
      }
      
      const response = await fetch(url, {
        headers,
        credentials: 'include',
        ...options,
      });

      // 401 에러 처리 (인증 실패)
      if (response.status === 401) {
        console.warn('🔒 401 Unauthorized - 로그아웃 처리');
        
        AuthService.logout();
        
        if (typeof window !== 'undefined') {
          alert('인증이 만료되었습니다. 다시 로그인해주세요.');
          window.location.href = '/login';
        }
        
        throw new Error('인증이 만료되었습니다');
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
  // 이메일 전송 API
    async sendEmailCode(email: string): Promise<SignupResponse> {
        console.log("📨 이메일 인증코드 전송 API 호출");
        return this.request<SignupResponse>("/users/send-email-code", {
            method: "POST",
            body: JSON.stringify({ email }),
        }, false);
    }

    async verifyEmailCode(email: string, code: string): Promise<SignupResponse> {
        console.log("🔍 이메일 인증코드 검증 API 호출");
        return this.request<SignupResponse>("/users/verify-email-code", {
            method: "POST",
            body: JSON.stringify({ email, code }),
        }, false);
    }


    // 사용자 관리 API
  async signup(data: SignupRequest): Promise<SignupResponse> {
    const response = await this.request<SignupResponse>('/users/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    }, false);

    // 회원가입 성공 시 토큰 저장
    if (response.success && response.accessToken) {
      AuthService.saveTokens({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      });

      if (response.userId) {
        // 사용자 정보는 로그인 후 따로 조회하거나, 응답에 포함시켜야 함
        AuthService.saveUserInfo({
          userId: response.userId,
          name: '',  // 백엔드 응답에 추가 필요
          email: '',
        });
      }
    }
    return response;
  }

  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await this.request<LoginResponse>('/users/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }, false);

    // 로그인 성공 시 토큰 저장
    if (response.success && response.accessToken) {
      AuthService.saveTokens({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
      });

      if (response.user) {
        AuthService.saveUserInfo({
          userId: response.user.userId,
          name: response.user.name,
          email: response.user.email,
        });
      }
    }

    return response;
  }

  async logout(): Promise<{ success: boolean; message: string }> {
    const response = await this.request('/users/logout', {
      method: 'POST',
    }, true);

    // 로그아웃 성공 시 로컬 토큰 삭제
    if (response.success) {
      AuthService.logout();
    }

    return response;
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