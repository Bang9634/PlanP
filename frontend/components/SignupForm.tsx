import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Lightbulb } from "lucide-react";
import { apiService, SignupRequest } from "../services/api";

interface SignupFormProps {
  onSignup: (id: string, password: string, confirmPassword: string) => void;
  onLoginClick: () => void;
}

export function SignupForm({ onSignup, onLoginClick }: SignupFormProps) {
  const [formData, setFormData] = useState({
    id: "",
    name: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [loading, setLoading] = useState(false);

  const handleInputChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const { id, name, email, password, confirmPassword } = formData;

    // 유효성 검사
    if (!id || !name || !email || !password || !confirmPassword) {
      alert("모든 필드를 입력해주세요.");
      return;
    }
    
    if (password !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    setLoading(true);

    try {
      const signupData: SignupRequest = {
        userId: id,
        password: password,
        name: name,
        email: email
      };

      // 백엔드 API 호출
      const result = await apiService.signup(signupData);

      if (result.success) {
        alert("회원가입이 완료되었습니다!");
        onSignup(id, password, confirmPassword); // 기존 콜백 호출
      } else {
        alert(`회원가입 실패: ${result.message}`);
      }
    } catch (error) {
      console.error('회원가입 오류:', error);
      alert('서버와의 연결에 문제가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
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

        {/* 회원가입 폼 */}
        <Card>
          <CardHeader>
            <CardTitle>회원가입</CardTitle>
            <CardDescription>새 계정을 만들어보세요</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="signup-id">아이디</Label>
                <Input
                  id="signup-id"
                  type="text"
                  value={formData.id}
                  onChange={handleInputChange('id')}
                  placeholder="사용할 아이디를 입력하세요"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="signup-name">이름</Label>
                <Input
                  id="signup-name"
                  type="text"
                  value={formData.name}
                  onChange={handleInputChange('name')}
                  placeholder="이름을 입력하세요"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="signup-email">이메일</Label>
                <Input
                  id="signup-email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange('email')}
                  placeholder="이메일을 입력하세요"
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="signup-password">비밀번호</Label>
                <Input
                  id="signup-password"
                  type="password"
                  value={formData.password}
                  onChange={handleInputChange('password')}
                  placeholder="비밀번호를 입력하세요"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirm-password">비밀번호 확인</Label>
                <Input
                  id="confirm-password"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleInputChange('confirmPassword')}
                  placeholder="비밀번호를 다시 입력하세요"
                  required
                />
              </div>

              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? "처리 중..." : "회원가입"}
              </Button>
            </form>

            <div className="mt-6">
              <div className="text-center">
                <span className="text-sm text-muted-foreground">
                  이미 계정이 있으신가요?
                </span>
              </div>
              <Button 
                variant="outline" 
                className="w-full mt-2"
                onClick={onLoginClick}
              >
                로그인
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}