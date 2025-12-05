/**
 * JWT 토큰 및 사용자 인증 관리 서비스
 * 
 * localStorage를 사용하여 토큰을 저장/관리합니다.
 * 
 * @example
 * // 로그인 성공 시
 * AuthService.saveTokens({ accessToken: "eyJhbGc...", refreshToken: "..." });
 * 
 * // API 요청 시
 * const token = AuthService.getAccessToken();
 * 
 * // 로그아웃 시
 * AuthService.logout();
 */

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
}

export interface UserInfo {
  userId: string;
  name: string;
  email: string;
}

export class AuthService {
  private static readonly ACCESS_TOKEN_KEY = 'planp_access_token';
  private static readonly REFRESH_TOKEN_KEY = 'planp_refresh_token';
  private static readonly USER_INFO_KEY = 'planp_user_info';

  /**
   * 토큰 저장 (로그인 성공 시)
   */
  static saveTokens(tokens: AuthTokens): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, tokens.accessToken);
    
    if (tokens.refreshToken) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, tokens.refreshToken);
    }
    
    console.log('토큰 저장 완료');
  }

  /**
   * 액세스 토큰 가져오기
   */
  static getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * 리프레시 토큰 가져오기
   */
  static getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * 사용자 정보 저장
   */
  static saveUserInfo(userInfo: UserInfo): void {
    localStorage.setItem(this.USER_INFO_KEY, JSON.stringify(userInfo));
    console.log('사용자 정보 저장:', userInfo);
  }

  /**
   * 사용자 정보 가져오기
   */
  static getUserInfo(): UserInfo | null {
    const userInfo = localStorage.getItem(this.USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  }

  /**
   * 로그인 여부 확인
   */
  static isAuthenticated(): boolean {
    const token = this.getAccessToken();
    
    if (!token) {
      return false;
    }
    
    // 토큰 만료 확인
    if (this.isTokenExpired(token)) {
      console.warn('토큰이 만료되었습니다');
      this.logout();
      return false;
    }
    
    return true;
  }

  /**
   * 로그아웃 (모든 토큰 삭제)
   */
  static logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_INFO_KEY);
    console.log('로그아웃 완료');
  }

  /**
   * 토큰 만료 확인 (JWT 디코딩)
   */
  static isTokenExpired(token: string): boolean {
    try {
      // JWT는 "header.payload.signature" 구조
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000; // 초를 밀리초로 변환
      
      return Date.now() >= exp;
    } catch (error) {
      console.error('토큰 디코딩 실패:', error);
      return true;
    }
  }

  /**
   * 토큰에서 사용자 ID 추출
   */
  static getUserIdFromToken(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.userId || null;
    } catch (error) {
      console.error('토큰에서 사용자 ID 추출 실패:', error);
      return null;
    }
  }
}