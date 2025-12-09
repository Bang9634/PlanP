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
  success: boolean; // .회원가입 성공 여부
  message: string; // 성공/실패 메세지
  userId?: string; // 생성된 사용자 ID(success 시)
  // 회원가입후 생성된 토큰을 받음
  accessToken?: string;
  refreshToken?: string;
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
  timestamp?: number;
  data?: { // data 객체 추가
    userId: string;
    name: string;
    email?: string;
    accessToken: string;
    refreshToken?: string;
  };
}

export interface GoogleLoginRequest {
    accessToken: string; // Google OAuth Access Token
}

export interface GoogleLoginResponse extends LoginResponse {}

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


    // 회원가입 API
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

    // 4) 로그인 API
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await this.request<LoginResponse>('/users/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }, false);

    // 로그인 성공 시 토큰 저장
    if (response.success && response.data) {
      const { accessToken, refreshToken, userId, name, email } = response.data;

      console.log('✅ 토큰 저장 시도:', {
        access: accessToken?.substring(0, 20) + '...',
        refresh: refreshToken?.substring(0, 20) + '...'
      });

      AuthService.saveTokens({
        accessToken: accessToken,
        refreshToken: refreshToken,
      });

      AuthService.saveUserInfo({
        userId,
        name,
        email: email || '',
      });
      console.log('💾 localStorage 확인:', {
        access: localStorage.getItem('planp_access_token'),
        user: localStorage.getItem('planp_user_info')
      });
    }

    return response;
  }

    /**
     * Google OAuth Access Token을 백엔드로 보내 PlanP JWT를 획득하는 API
     * @param data GoogleLoginRequest (accessToken 포함)
     * @returns GoogleLoginResponse (PlanP JWT 및 사용자 정보 포함)
     */
    async googleLogin(data: GoogleLoginRequest): Promise<GoogleLoginResponse> {
        console.log(" Google Access Token 기반 로그인 API 호출: /users/auth/google");

        // request 메서드를 사용하여 백엔드 엔드포인트 호출
        const response = await this.request<GoogleLoginResponse>('/users/auth/google', {
            method: 'POST',
            body: JSON.stringify(data),
            // 인증이 필요한 요청이 아님
        }, false);
        console.debug('성공 여부:', response.success, '토큰:', response.accessToken);

        // 로그인 성공 시 응답으로 받은 PlanP JWT 저장 (기존 로그인 로직 재사용)
        if (response.success && response.accessToken) {
          console.debug('토큰 저장 시도');
            AuthService.saveTokens({
                accessToken: response.accessToken,
                refreshToken: response.refreshToken,
            });

            // 사용자 정보 저장
            if (response.user) {
              console.debug('사용자 정보 저장 시도');
                AuthService.saveUserInfo({
                    userId: response.user.userId,
                    name: response.user.name,
                    email: response.user.email,
                });
            }
        }

        return response;
    }

    // 5) 로그아웃 API
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

    /**
   * AI 기반 장르별 음악 추천 (Gemini)
   * 
   * @param genre 음악 장르 (예: KPOP, ROCK)
   * @param count 추천받을 곡 수 (기본: 15)
   * @returns 추천된 곡 제목 리스트 ("Artist - Song" 형식)
   */
  async getAIRecommendationsByGenre(genre: string, count: number = 3): Promise<string[]> {
    try {
      const response = await this.request<{
        success: boolean;
        message: string;
        data: {
          genre: string;
          count: number;
          songs: string[];
        };
      }>(`/music/recommend-by-genre?genre=${encodeURIComponent(genre)}&count=${count}`, {}, true);

      if (response.success && response.data) {
        console.log(`✅ 백엔드 AI 추천 성공: ${response.data.songs.length}곡`);
        return response.data.songs;
      }

      console.warn('⚠️ AI 추천 응답 형식 오류:', response);
      return [];
      
    } catch (error) {
      console.error('❌ AI 음악 추천 실패:', error);
      return [];
    }
  }

  /**
   * AI 기반 음악 발견 (컨텍스트 분석)
   * 
   * @param energy 에너지 레벨 (high, medium, low)
   * @param activity 활동 상황 (work, exercise, relax, commute)
   * @param preference 선호 장르 (pop, indie, jazz, classical, electronic)
   * @param count 추천받을 곡 수
   */
  async discoverMusic(
    energy: string, 
    activity: string, 
    preference: string, 
    count: number = 10
  ): Promise<string[]> {
    try {
      const response = await this.request<{
        success: boolean;
        message: string;
        data: {
          energy: string;
          activity: string;
          preference: string;
          count: number;
          songs: string[];
        };
      }>(
        `/music/discover?energy=${encodeURIComponent(energy)}&activity=${encodeURIComponent(activity)}&preference=${encodeURIComponent(preference)}&count=${count}`,
        {},
        true
      );

      if (response.success && response.data) {
        console.log(`✅ AI 음악 발견 성공: ${response.data.songs.length}곡`);
        return response.data.songs;
      }

      console.warn('⚠️ AI 음악 발견 응답 형식 오류:', response);
      return [];
      
    } catch (error) {
      console.error('❌ AI 음악 발견 실패:', error);
      return [];
    }
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