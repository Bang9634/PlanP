declare global {
  interface Window {
    APP_CONFIG?: {
      API_BASE_URL: string;
    };
  }
}

const getApiBaseUrl = (): string => {
  // 런타임 설정 확인
  if (typeof window !== 'undefined' && window.APP_CONFIG) {
    return window.APP_CONFIG.API_BASE_URL;
  }
  
  // 환경변수 또는 기본값
  return process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
}

const API_BASE_URL = getApiBaseUrl();

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

export class ApiService {
  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async signup(data: SignupRequest): Promise<SignupResponse> {
    return this.request<SignupResponse>('/users/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async checkUserId(userId: string): Promise<{ available: boolean; message: string }> {
    return this.request(`/users/check-id?userId=${encodeURIComponent(userId)}`);
  }

  async checkEmail(email: string): Promise<{ available: boolean; message: string }> {
    return this.request(`/users/check-email?email=${encodeURIComponent(email)}`);
  }
}

export const apiService = new ApiService();