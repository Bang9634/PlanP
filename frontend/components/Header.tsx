import { Button } from "./ui/button";
import { User, LogIn, UserPlus, Target, BarChart3 } from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger, DropdownMenuSeparator } from "./ui/dropdown-menu";

interface HeaderProps {
  isLoggedIn: boolean;
  currentView: string;
  onViewChange: (view: string) => void;
  onLogout: () => void;
  currentUser?: string | null;
}

export function Header({ isLoggedIn, currentView, onViewChange, onLogout, currentUser }: HeaderProps) {

  return (
    <header className="w-full border-b bg-background">
      <div className="container mx-auto px-4 py-3">
        <div className="flex items-center justify-between">
          {/* 로고/홈 버튼 */}
          <Button 
            variant="ghost" 
            onClick={() => onViewChange('home')}
            className="hover:bg-transparent"
          >
            <span className="text-lg">플랜P</span>
          </Button>

          {/* 로그인/회원가입/내계정 버튼들 */}
          <div className="flex items-center gap-2">
            {!isLoggedIn ? (
              <>
                <Button 
                  variant="ghost" 
                  size="sm"
                  onClick={() => onViewChange('login')}
                  className="gap-2"
                >
                  <LogIn className="w-4 h-4" />
                  로그인
                </Button>
                <Button 
                  variant="ghost" 
                  size="sm"
                  onClick={() => onViewChange('signup')}
                  className="gap-2"
                >
                  <UserPlus className="w-4 h-4" />
                  회원가입
                </Button>
              </>
            ) : (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="gap-2">
                    <User className="w-4 h-4" />
                    {currentUser}님
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <DropdownMenuItem onClick={() => onViewChange('account')}>
                    <BarChart3 className="w-4 h-4 mr-2" />
                    내 계정 & 통계
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => onViewChange('routines')}>
                    <Target className="w-4 h-4 mr-2" />
                    루틴 관리
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={onLogout}>
                    <LogIn className="w-4 h-4 mr-2" />
                    로그아웃
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            )}

            {!isLoggedIn && (
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => onViewChange('login')}
                className="gap-2"
              >
                <User className="w-4 h-4" />
                내 계정
              </Button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}