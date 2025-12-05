import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Lightbulb, ArrowLeft } from "lucide-react";

interface LoginFormProps {
    // LoginForm은 API를 호출하지 않고 사용자가 입력한 값만 App에게 전달한다.
    onLogin: (id: string, password: string) => void;
    onSignupClick: () => void;
    onBackToHome: () => void;
}

export function LoginForm({ onLogin, onSignupClick, onBackToHome }: LoginFormProps) {
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

    const handleGoogleLogin = () => {

    }
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