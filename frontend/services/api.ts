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
// 벡엔드는 인터페이스를 통해 아래의 내용을 확인 가능함.
// 프론트에서 어떤 필드를 요구하는지
// 필드 타입은 어떠한 형태인지?
// 어떤 API가 어떤 데이터를 반환해야하는지
// 프론트의 요구사항 명세서와 같은 역할

// 회원가입 요청
export interface SignupRequest {
  userId: string;    // 사용자 로그인 ID
  password: string;  // 비밀번호 ( 서버에서 해싱)
  name: string;      // 사용자 이름
  email: string;     // 사용자 이메일
}
// 회원가입 응답
export interface SignupResponse {
  success: boolean;  // 회원가입 성공 여부
  message: string;   // 성공/실패 메세지
  userId?: string;   // 생성된 사용자 ID(success 시)
}
// 로그인 요청
export interface LoginRequest {
  userId: string;
  password: string;
}
// 로그인 응답
export interface LoginResponse {
  success: boolean;
  message: string;
  user?: {
    userId: string;
    name: string;
    email: string;
  };
}

// 6) 내정보 (UserProfile)
// /users/me API용
// 프론트 MyAccoutPage에 데이터 구성에 사용할 요소
export interface UserProfile {
    userId: string;     // ID
    name: string;       // 이름
    email: string;      // 이메일
    // 아래는 선택적 필드
    // 시간없으면 빼야함
    level?: number;     // 성취 레벨
    points?: number;    // 성취 포인트
}
// 7) 활동기록
export interface ActivityRecord {
    id: string; // 활동 ID
    title: string; // 활동 제목 (예시: 음악 감상)
    category: string; // 카테고리 (music, daily, health 등등?)
    date: string;       // 활동 수행일자 '2025-11-30' , 즉 완료 일자
    duration?: string; // 선택 : 활동에 소요된 기간
    isRoutine?: boolean; // 선택 : 루틴 기반 활동인지에 대한 여부
    completed: boolean; // 완료 여부 (이건 선택이 아닐거같긴한데?)
}
// 8) 통계
export interface ActivityStatistics {
    weekly: {
        day: string;  // 요일(월, 화, 수...)
        completed: number; // 완료한 활동 수
         missed: number; // 실패? 수행 못한 활동 수(건너뛴?)
    }[];
    categoryDistribution: {
        name: string; // 카테고리 이름
        value: number; // 비율 혹은 횟수
        color: string; // 차트 표시 색상
    }[];
    totalActivities: number; // 전체 활동 개수
    completedActivities: number; // 완료한 활동 개수
    currentStreak: number; // 현재 연속 성공 일수
    longestStreak: number; // 가장 길었던 연속 성공일
    favoriteCategory: string; // 가장 많이 한 카테고리
}

// 9) 뱃지(필요함? ㅅㅂ) // 12.03 이건 빼자 :(
export interface Achievement {
    id: string;
    title: string;
    description: string;
    icon: string;
    earned: boolean;
    earnedDate?: string; // '2025-11-30'
    progress?: number;
    target?: number;
}
// 10) 캘린더 (이건 필요할만도?)
export interface CalendarDayActivity {
    date: string; // '2025-11-30'
    activities: {
        id: string; // ID
        title: string; // 활동 제목
        category: string; // 활동 카테고리
        completed: boolean; // 완료여부
    }[];
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

  // 1) 이메일 전송 API
    async sendEmailCode(email: string): Promise<SignupResponse> {
        return this.request<SignupResponse>("/users/send-email-code", {
            method: "POST",
            body: JSON.stringify({ email }),
        });
    }
  // 2) 이메일 검증 API
    async verifyEmailCode(email: string, code: string): Promise<SignupResponse> {
        return this.request<SignupResponse>("/users/verify-email-code", {
            method: "POST",
            body: JSON.stringify({ email, code }),
        });
    }


    // 3) 사용자 관리 API
  async signup(data: SignupRequest): Promise<SignupResponse> {
    return this.request<SignupResponse>('/users/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

    // 4) 로그인 API
  async login(data: LoginRequest): Promise<LoginResponse> {
    return this.request<LoginResponse>('/users/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
    // 5) 로그아웄 API
  async logout(): Promise<{ success: boolean; message: string }> {
    return this.request('/users/logout', {
      method: 'POST',
    });
  }
    // 6) 내 정보 가져오는 API
    async getMyProfile(): Promise<UserProfile> {
        return this.request<UserProfile>("/users/me", {
            method: "GET",
        });
    }

    // 7) 내 활동기록 API
    async getMyActivityHistory(): Promise<ActivityRecord[]> {
        return this.request<ActivityRecord[]>("/users/me/activity-history", {
            method: "GET",
        });
    }
    // 8) 통계 API
    async getMyStatistics(): Promise<ActivityStatistics> {
        return this.request<ActivityStatistics>("/users/me/statistics", {
            method: "GET",
        });
    }
    // 9) 뱃지 API(진짜 필요하냐고?)
    async getMyAchievements(): Promise<Achievement[]> {
        return this.request<Achievement[]>("/users/me/achievements", {
            method: "GET",
        });
    }

    // 10) 캘린더 API
    async getMyCalendar(year: number, month: number): Promise<CalendarDayActivity[]> {
        const query = `?year=${year}&month=${month}`;
        return this.request<CalendarDayActivity[]>(`/users/me/calendar${query}`, {
            method: "GET",
        });
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