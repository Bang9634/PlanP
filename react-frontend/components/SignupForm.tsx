import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Lightbulb } from "lucide-react";

interface SignupFormProps {
  onSignup: (id: string, password: string, confirmPassword: string) => void;
  onLoginClick: () => void;
}

export function SignupForm({ onSignup, onLoginClick }: SignupFormProps) {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (id && password && confirmPassword) {
      if (password !== confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }
      onSignup(id, password, confirmPassword);
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
            <CardDescription>
              새 계정을 만들어보세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="signup-id">아이디</Label>
                <Input
                  id="signup-id"
                  type="text"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                  placeholder="사용할 아이디를 입력하세요"
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="signup-password">비밀번호</Label>
                <Input
                  id="signup-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="비밀번호를 입력하세요"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirm-password">비밀번호 확인</Label>
                <Input
                  id="confirm-password"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="비밀번호를 다시 입력하세요"
                  required
                />
              </div>

              <Button type="submit" className="w-full">
                회원가입
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