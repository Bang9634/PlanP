import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "./ui/card";
import { Lightbulb, CheckCircle, Mail, ArrowLeft } from "lucide-react";
import { apiService } from "../services/api";

interface SignupFormProps {
    // SignupForm은 회원가입 API를 직접 호출하지 않고
    // App에게 입력값만 전달하는 구조
    onSignup: (
        id: string,
        password: string,
        confirmPassword: string,
        name: string,
        email: string
    ) => void;
    onBackToHome: () => void;
    onLoginClick: () => void;
}

export function SignupForm({ onSignup, onLoginClick, onBackToHome }: SignupFormProps) {

        // 폼 입력값 관리
        // Form은 UI 요소이므로 입력 상태를 관리하는 것이 책임
        // signup API 요청은 App.tsx가 담당

    const [formData, setFormData] = useState({
        id: "",
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
    });


        // 이메일 인증 상태
        // 이메일 인증은 "UI 상에서 이루어지는 이벤트"이므로
        //SignupForm 내부에 존재하는 것이 맞다.
        //인증 코드 입력과 버튼 활성화/비활성화는 모두 UI 상태이기 때문

    const [verificationCode, setVerificationCode] = useState("");
    const [isCodeSent, setIsCodeSent] = useState(false);
    const [isEmailVerified, setIsEmailVerified] = useState(false);

        // 입력 변경 핸들러
        // 모든 필드에서 같은 방식으로 setFormData를 위해 사용

    const handleInputChange =
        (field: keyof typeof formData) =>
            (e: React.ChangeEvent<HTMLInputElement>) => {
                setFormData((prev) => ({ ...prev, [field]: e.target.value }));
            };


        // 이메일 인증 코드 전송 API
        // 왜 SignupForm 내부에 있어야 하는가?
        // 이 로직은 회원가입 로직과 별개인 “UI 기반 이벤트”이다.
        // 사용자가 !코드 전송! 버튼을 누르는 즉각적 UI 동작이며
        //  Form 내부에서 제어하는 것이 자연스럽다. ㅇㅈ?
        // App.tsx는 전체 페이지 전환 및 회원가입 결과 책임을 가짐.
        // 결국 App.tsx는 전역 책임을 가지는 것인데.
        //  이메일 인증은 그런 전역 책임이 아님 → Form에 두는 게 맞음.
        // 반박 시 주재홍

    const handleSendCode = async () => {
        if (!formData.email) {
            alert("이메일을 입력해주세요.");
            return;
        }

        try {
            const res = await apiService.sendEmailCode(formData.email);

            if (res.success) {
                setIsCodeSent(true);
                alert("인증 코드가 이메일로 전송되었습니다!");
            } else {
                alert(res.message);
            }
        } catch (err) {
            console.error("sendEmailCode 오류:", err);
            alert("이메일 전송 중 오류가 발생했습니다.");
        }
    };


        // 이메일 인증 코드 검증 API
        // "왜 signup API는 Form에서 빼는데 이건 Form에 있어도 되나?"
        // 인증 코드 입력 → Form UI 입력과 강하게 연결됨
        // 인증 버튼을 누르면 바로 UI 피드백이 필요함
        // signup API는 회원가입 전체 흐름(App)이 책임져야 하지만,
        // 이메일 인증은 Form UI의 일부로서 여기 남는 것이 맞다.

    const handleVerifyCode = async () => {
        if (!verificationCode) {
            alert("인증 코드를 입력해주세요.");
            return;
        }

        try {
            const res = await apiService.verifyEmailCode(
                formData.email,
                verificationCode
            );

            if (res.success) {
                setIsEmailVerified(true);
                alert("이메일 인증이 완료되었습니다!");
            } else {
                alert(res.message);
            }
        } catch (err) {
            console.error("verifyEmailCode 오류:", err);
            alert("인증 확인 중 오류가 발생했습니다.");
        }
    };


       // 최종 폼 제출
       // 매우 중요
       // 여기서 회원가입 API를 호출하지 않는다!
       // Form은 단지 입력된 회원 정보를 App에게 전달만 한다.
       // signup API 호출은 App.tsx에서 일어난다.
       // 이 구조(B 방식)는 Form의 책임(입력/UI)과
       // App의 책임(비즈니스 로직/API 호출)을 분리하는 가장 좋은 구조.

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const { id, name, email, password, confirmPassword } = formData;

        if (!id || !name || !email || !password || !confirmPassword) {
            alert("모든 필드를 입력해주세요.");
            return;
        }

        if (!isEmailVerified) {
            alert("이메일 인증을 완료해주세요.");
            return;
        }

        if (password !== confirmPassword) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        // Form은 여기서 API 요청을 하지 않는다.
        // 단지 App.tsx에게 입력값을 전달하여 signup API를 호출하게 한다.
        onSignup(id, password, confirmPassword, name, email);
    };

        //JSX 렌더링
        //Form UI + 이메일 인증 UI 모두 포함
        //회원가입 버튼은 App.tsx에서 처리되므로 단순히 submit 역할만 수행

    return (
        // Flex 중앙 정렬 구조를 유지하고, 'relative'를 추가하여 버튼의 기준점으로 설정
        <div className="min-h-screen bg-background flex items-center justify-center px-4 relative">

            {/* ⬅️ 뒤로가기 버튼: 전체 컨테이너의 좌측 상단에 절대 위치(absolute)로 배치 */}
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
                        회원가입하고 나만의 계획을 저장해보세요
                    </p>
                </div>

                <Card>
                    <CardHeader>
                        <CardTitle>회원가입</CardTitle>
                        <CardDescription>새 계정을 만들어보세요</CardDescription>
                    </CardHeader>

                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            {/* 아이디 */}
                            <div className="space-y-2">
                                <Label htmlFor="signup-id">아이디</Label>
                                <Input
                                    id="signup-id"
                                    value={formData.id}
                                    onChange={handleInputChange("id")}
                                    placeholder="아이디를 입력하세요"
                                    required
                                />
                            </div>

                            {/* 이름 */}
                            <div className="space-y-2">
                                <Label htmlFor="signup-name">이름</Label>
                                <Input
                                    id="signup-name"
                                    value={formData.name}
                                    onChange={handleInputChange("name")}
                                    placeholder="이름을 입력하세요"
                                    required
                                />
                            </div>

                            {/* 이메일 + 코드 전송 버튼 */}
                            <div className="space-y-2">
                                <Label htmlFor="signup-email">이메일</Label>

                                <div className="flex gap-2">
                                    <Input
                                        id="signup-email"
                                        value={formData.email}
                                        onChange={handleInputChange("email")}
                                        placeholder="이메일을 입력하세요"
                                        disabled={isEmailVerified}
                                        required
                                    />

                                    {!isEmailVerified && (
                                        <Button type="button" variant="outline" onClick={handleSendCode}>
                                            <Mail className="w-4 h-4 mr-1" />
                                            코드 전송
                                        </Button>
                                    )}

                                    {isEmailVerified && (
                                        <Button type="button" disabled className="bg-green-500 text-white">
                                            <CheckCircle className="w-4 h-4" />
                                            인증 완료
                                        </Button>
                                    )}
                                </div>
                            </div>

                            {/* 인증 코드 입력 */}
                            {isCodeSent && !isEmailVerified && (
                                <div className="space-y-2">
                                    <Label htmlFor="verification-code">인증 코드</Label>
                                    <div className="flex gap-2">
                                        <Input
                                            id="verification-code"
                                            value={verificationCode}
                                            onChange={(e) => setVerificationCode(e.target.value)}
                                            placeholder="6자리 코드를 입력하세요"
                                            maxLength={6}
                                            required
                                        />
                                        <Button type="button" onClick={handleVerifyCode}>
                                            인증 확인
                                        </Button>
                                    </div>
                                </div>
                            )}

                            {/* 비밀번호 */}
                            <div className="space-y-2">
                                <Label htmlFor="signup-password">비밀번호</Label>
                                <Input
                                    id="signup-password"
                                    type="password"
                                    value={formData.password}
                                    onChange={handleInputChange("password")}
                                    placeholder="비밀번호를 입력하세요"
                                    required
                                />
                            </div>

                            {/* 비밀번호 확인 */}
                            <div className="space-y-2">
                                <Label htmlFor="confirm-password">비밀번호 확인</Label>
                                <Input
                                    id="confirm-password"
                                    type="password"
                                    value={formData.confirmPassword}
                                    onChange={handleInputChange("confirmPassword")}
                                    placeholder="비밀번호를 다시 입력하세요"
                                    required
                                />
                            </div>

                            {/* 회원가입 제출 */}
                            <Button type="submit" className="w-full">
                                회원가입
                            </Button>
                        </form>

                        {/* 로그인 이동 버튼 */}
                        <div className="mt-6 text-center">
                            <span className="text-sm text-muted-foreground">이미 계정이 있으신가요?</span>
                            <Button variant="outline" className="w-full mt-2" onClick={onLoginClick}>
                                로그인
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
