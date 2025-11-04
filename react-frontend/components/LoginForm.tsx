import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Lightbulb } from "lucide-react";

interface LoginFormProps {
  onLogin: (id: string, password: string) => void;
  onSignupClick: () => void;
}

export function LoginForm({ onLogin, onSignupClick }: LoginFormProps) {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (id && password) {
      onLogin(id, password);
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
            로그인하고 나만의 계획을 저장해보세요
          </p>
        </div>

        {/* 로그인 폼 */}
        <Card>
          <CardHeader>
            <CardTitle>로그인</CardTitle>
            <CardDescription>
              계정 정보를 입력해주세요
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="id">아이디</Label>
                <Input
                  id="id"
                  type="text"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                  placeholder="아이디를 입력하세요"
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password">비밀번호</Label>
                <Input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="비밀번호를 입력하세요"
                  required
                />
              </div>

              <Button type="submit" className="w-full">
                로그인
              </Button>
            </form>

            <div className="mt-6">
              <div className="text-center">
                <span className="text-sm text-muted-foreground">
                  계정이 없으신가요?
                </span>
              </div>
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