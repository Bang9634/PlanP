const API_BASE_URL = 'http://localhost:8080/api';

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