import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Lightbulb, ArrowLeft} from "lucide-react"; // Chrome 아이콘 추가

// [수정된 부분] window 객체에 Google SDK가 로드된 타입 정의
declare global {
    interface Window {
        google: any; // Google Identity Services 객체
    }
}

// [추가된 부분] Google Client ID 정의
// 실제 값은 환경 변수에서 가져오는 것을 권장합니다.
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || "116180938606-0v635n7h76s9b84k1e71rlfjnh596ksv.apps.googleusercontent.com";

interface LoginFormProps {
    // LoginForm은 API를 호출하지 않고 사용자가 입력한 값만 App에게 전달한다.
    onLogin: (id: string, password: string) => void;
    onSignupClick: () => void;
    onBackToHome: () => void;
    // [추가된 부분] Google 로그인 성공 시 Access Token을 App.tsx로 전달할 콜백 함수
    onGoogleLoginSuccess: (accessToken: string) => void;
}

export function LoginForm({ onLogin, onSignupClick, onBackToHome, onGoogleLoginSuccess }: LoginFormProps) {
    const [id, setId] = useState("");
    const [password, setPassword] = useState("");


    // 로그인 입력값 제출 (API 호출 없음)
    // LoginForm의 책임은 UI 이벤트 → 입력 전달까지
    // API 요청은 App.tsx의 handleLogin()이 담당

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!id || !password) {
            alert("아이디와 비밀번호를 입력하세요.");
            return;
        }

        // App.tsx에게 입력값을 넘김 → 여기서 API 호출하게 됨
        onLogin(id, password);
    };

    //  [수정된 부분] 기존 handleGoogleLogin 함수를 Access Token 획득 로직으로 대체
    const handleGoogleLoginClick = () => {
        // 1. Google SDK 로드 및 Client ID 확인
        if (typeof window.google === 'undefined' || !GOOGLE_CLIENT_ID) {
            alert('Google SDK가 로드되지 않았거나 Client ID가 설정되지 않았습니다.');
            return;
        }

        // 2. Access Token을 요청하는 클라이언트 초기화 (ID Token 방식 아님)
        const client = window.google.accounts.oauth2.initTokenClient({
            client_id: GOOGLE_CLIENT_ID,
            // 백엔드의 UserInfo API 호출에 필요한 스코프 지정
            scope: 'https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile',
            callback: handleAccessTokenResponse, // 토큰 획득 후 처리할 함수
        });

        // 3. Google 팝업 요청 시작 (Access Token 획득)
        client.requestAccessToken();
    };

    // [추가된 부분] Access Token 응답 처리 콜백 함수
    const handleAccessTokenResponse = (tokenResponse: { access_token?: string; error?: string }) => {
        if (tokenResponse.error) {
            console.error("Google Access Token 획득 실패:", tokenResponse.error);
            alert('Google 로그인에 실패했습니다. 다시 시도해 주세요.');
            return;
        }

        const accessToken = tokenResponse.access_token;
        if (accessToken) {
            // 4. 상위 컴포넌트(App.tsx)로 Access Token 전달
            onGoogleLoginSuccess(accessToken);
        }
    };

    return (
        <div className="min-h-screen bg-background flex items-center justify-center px-4 relative">

            {/* 뒤로가기 버튼 */}
            <div className="absolute top-0 left-0 p-4">
                <Button variant="ghost" onClick={onBackToHome}>
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    뒤로가기
                </Button>
            </div>

            <div className="w-full max-w-md">
                {/* 로고 */}
                <div className="text-center mb-8">
                    <div className="flex items-center justify-center gap-2 mb-4">
                        <Lightbulb className="w-8 h-8 text-yellow-500" />
                        <h1 className="text-3xl">플랜P</h1>
                    </div>
                    <p className="text-muted-foreground">
                        로그인하고 나만의 계획을 시작해보세요
                    </p>
                </div>

                <Card>
                    <CardHeader>
                        <CardTitle>로그인</CardTitle>
                        <CardDescription>계정 정보를 입력하세요</CardDescription>
                    </CardHeader>

                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">

                            {/* 아이디 */}
                            <div className="space-y-2">
                                <Label htmlFor="login-id">아이디</Label>
                                <Input
                                    id="login-id"
                                    value={id}
                                    onChange={(e) => setId(e.target.value)}
                                    placeholder="아이디 입력"
                                    required
                                />
                            </div>

                            {/* 비밀번호 */}
                            <div className="space-y-2">
                                <Label htmlFor="login-password">비밀번호</Label>
                                <Input
                                    id="login-password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="비밀번호 입력"
                                    required
                                />
                            </div>

                            {/* 로그인 버튼 */}
                            <Button type="submit" className="w-full">
                                로그인
                            </Button>
                        </form>

                        {/* ⭐ [추가된 부분] 소셜 로그인 구분선 */}
                        <div className="relative my-6">
                            <div className="absolute inset-0 flex items-center">
                                <span className="w-full border-t" />
                            </div>
                            <div className="relative flex justify-center text-xs uppercase">
                                <span className="bg-background px-2 text-muted-foreground">
                                    또는 소셜 로그인
                                </span>
                            </div>
                        </div>

                        {/* Google 로그인 버튼 */}
                        <Button
                            type="button" // HTML Form 제출을 방지하기 위해 type="button"을 명시합니다.
                            variant="outline"
                            className="w-full mt-4" // 기존 디자인과 통일성을 위해 className="w-full" 유지 및 margin 추가
                            onClick={handleGoogleLoginClick} // 기존 Access Token 획득 함수 연결
                        >
                            <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                                <path
                                    fill="#4285F4"
                                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                                />
                                <path
                                    fill="#34A853"
                                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                                />
                                <path
                                    fill="#FBBC05"
                                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                                />
                                <path
                                    fill="#EA4335"
                                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                                />
                            </svg>
                            구글 계정으로 로그인
                        </Button>


                        {/* 회원가입 이동 */}
                        <div className="mt-6">
                            <p className="text-center text-sm text-muted-foreground">
                                아직 계정이 없으신가요?
                            </p>
                            <Button
                                variant="outline"
                                className="w-full mt-2"
                                onClick={onSignupClick}
                            >
                                회원가입
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}