import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import {
    Trophy,
    Target,
    Activity,
    ArrowLeft,
    Mail,
    User,
    Key,
    UserMinus,
    Shield
} from "lucide-react";
import { AuthService } from "../services/AuthService";
import { apiService, MyAccountResponse,DeleteAccountResponse } from "../services/api";

interface MyAccountPageProps {
    onBackToHome: () => void;
    onLogout: () => void;
}

export function MyAccountPage({ onBackToHome, onLogout }: MyAccountPageProps) {
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [profile, setProfile] = useState<{
        level: number;
        points: number;
    } | null>(null);
    const [myAccount, setMyAccount] = useState<{
       userId: string;
        name: string;
        email: string;
        isGoogleAccount: boolean;
        googleId?: string;
    } | null>(null);

    useEffect(() => {
        const loadAccount = async() => {
             try {
                setIsLoading(true);
                setError(null);

                // ✅ 백엔드에서 사용자 정보 가져오기
                const myAccountResponse: MyAccountResponse = await apiService.getMyAccount();

                console.log('✅ 사용자 정보 조회 성공:', myAccountResponse);

                // 응답 데이터를 상태에 저장
                setMyAccount({
                    userId: myAccountResponse.data.userId,
                    name: myAccountResponse.data.name,
                    email: myAccountResponse.data.email,
                    isGoogleAccount: myAccountResponse.data.isGoogleAccount, 
                    googleId: myAccountResponse.data.googleId, // 백엔드에서 제공하지 않으면 undefined
                });

            } catch (err) {
                console.error('사용자 정보 조회 실패:', err);
                setError('사용자 정보를 불러올 수 없습니다');

                // 폴백: 로컬 스토리지에서 기본 정보 로드
                const userInfo = AuthService.getUserInfo();
                if (userInfo) {
                    setMyAccount({
                        userId: userInfo.userId,
                        name: userInfo.name,
                        email: userInfo.email,
                        isGoogleAccount: false,
                        googleId: undefined,
                    });
                }
            } finally {
                setIsLoading(false);
            }
        };

        setProfile({
            level: 1,
            points: 0,
        });
        loadAccount();
    }, []);

    const handlePasswordChange = () => {
        // TODO: 비밀번호 변경 모달 열기
        alert('비밀번호 변경 기능은 준비 중입니다.');
    };

    const handleAccountDelete = async () => {
        if (!profile) return;
    
        // 1차 확인
        const confirmed = window.confirm(
            '정말로 회원탈퇴 하시겠습니까?\n\n' +
            '⚠️ 모든 데이터가 삭제되며 복구할 수 없습니다.\n' +
            '⚠️ 작성한 계획, 활동 기록 등이 모두 삭제됩니다.'
        );

        if (!confirmed) return;
        
        try {
            // 일반 계정인 경우 비밀번호 확인
            let password: string | undefined;
            
            if (!myAccount.isGoogleAccount) {
                password = window.prompt('본인 확인을 위해 비밀번호를 입력해주세요:');
                
                if (password === null) {
                    // 사용자가 취소 버튼 클릭
                    return;
                }
                
                if (!password || password.trim().length === 0) {
                    alert('비밀번호를 입력해주세요.');
                    return;
                }
            } else {
                // Google 계정 최종 확인
                const googleConfirm = window.confirm(
                    'Google 계정 탈퇴를 진행합니다.\n' +
                    '정말 탈퇴하시겠습니까?'
                );
                
                if (!googleConfirm) return;
            } 
            
            // API 호출
            const deleteAccountResponse = await apiService.deleteMyAccount(password);
            
            // 성공 처리
            if (deleteAccountResponse.success == true) {
                alert('회원탈퇴가 완료되었습니다.\n그동안 이용해주셔서 감사합니다.');
                // 로그아웃 처리
                AuthService.logout();
                // 홈으로 이동
                onLogout();
            } else {
                alert(deleteAccountResponse.message);
            }
        } catch (error) {
            console.error('회원탈퇴 실패:', error);
            
            if (error instanceof Error) {
                if (error.message.includes('비밀번호')) {
                    alert('❌ 비밀번호가 일치하지 않습니다.');
                } else if (error.message.includes('인증')) {
                    alert('❌ 인증에 실패했습니다. 다시 로그인해주세요.');
                    AuthService.logout();
                    onBackToHome();
                } else {
                    alert('❌ 회원탈퇴에 실패했습니다.\n잠시 후 다시 시도해주세요.');
                }
            } else {
                alert('❌ 회원탈퇴 중 오류가 발생했습니다.');
            }
        }

    };


    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                    <p className="text-muted-foreground">내 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }
       // 에러 발생
    if (error && !myAccount) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <p className="text-destructive mb-4">{error}</p>
                    <Button onClick={onBackToHome}>홈으로 돌아가기</Button>
                </div>
            </div>
        );
    }
    if (!profile) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <p className="text-muted-foreground mb-4">사용자 정보를 찾을 수 없습니다</p>
                    <Button onClick={onBackToHome}>홈으로 돌아가기</Button>
                </div>
            </div>
        );
    }

    // 레벨 진행률 계산
    const levelProgress = ((profile.points % 500) / 500) * 100;

    return (
        <div className="min-h-screen bg-background">
            {/* 헤더 */}
            <div className="border-b bg-card">
                <div className="container mx-auto px-4 py-4">
                    <Button variant="ghost" onClick={onBackToHome} className="gap-2 mb-4">
                        <ArrowLeft className="w-4 h-4" />
                        뒤로가기
                    </Button>

                    {/* 에러 알림 (프로필은 있지만 API 실패한 경우) */}
                    {error && (
                        <div className="mb-4 p-3 bg-yellow-50 dark:bg-yellow-950/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
                            <p className="text-sm text-yellow-700 dark:text-yellow-400">
                                ⚠️ {error} (로컬 캐시 데이터 표시 중)
                            </p>
                        </div>
                    )}

                    {/* 프로필 섹션 */}
                    <div className="flex items-center gap-6 mb-6">
                        <Avatar className="w-20 h-20">
                            <AvatarImage src="" />
                            <AvatarFallback className="text-xl bg-primary/10">
                                {myAccount.name.charAt(0).toUpperCase()}
                            </AvatarFallback>
                        </Avatar>

                        <div className="flex-1">
                            <h1 className="text-2xl font-medium mb-2">{myAccount.name}님</h1>

                            <div className="flex items-center gap-6 text-sm text-muted-foreground">
                                <div className="flex items-center gap-1">
                                    <Trophy className="w-4 h-4 text-yellow-500" />
                                    <span>레벨 {profile.level}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Target className="w-4 h-4 text-green-500" />
                                    <span>{profile.points} 포인트</span>
                                </div>
                            </div>

                            {/* 레벨 진행률 */}
                            <div className="mt-3">
                                <div className="flex justify-between text-xs mb-1">
                                    <span>다음 레벨까지</span>
                                    <span>{Math.round(levelProgress)}%</span>
                                </div>
                                <Progress value={levelProgress} className="h-2" />
                                <p className="text-xs text-muted-foreground mt-1">
                                    {500 - (profile.points % 500)} 포인트 남음
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 메인 콘텐츠 */}
            <div className="container mx-auto px-4 py-8">
                {/* 계정 정보 섹션 */}
                <Card className="mb-8">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Shield className="w-5 h-5 text-primary" />
                            계정 정보
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        {/* 기본 정보 그리드 */}
                        <div className="grid gap-4 md:grid-cols-2">
                            {/* 이름 */}
                            <div className="flex items-start gap-3 p-4 rounded-lg bg-muted/50">
                                <User className="w-5 h-5 text-blue-500 mt-0.5" />
                                <div className="flex-1">
                                    <p className="text-sm text-muted-foreground mb-1">이름</p>
                                    <p className="font-medium">{myAccount.name}</p>
                                </div>
                            </div>

                            {/* 아이디 */}
                            <div className="flex items-start gap-3 p-4 rounded-lg bg-muted/50">
                                <Activity className="w-5 h-5 text-green-500 mt-0.5" />
                                <div className="flex-1">
                                    <p className="text-sm text-muted-foreground mb-1">아이디</p>
                                    <p className="font-medium font-mono text-sm">{myAccount.userId}</p>
                                </div>
                            </div>

                            {/* 이메일 */}
                            <div className="flex items-start gap-3 p-4 rounded-lg bg-muted/50">
                                <Mail className="w-5 h-5 text-purple-500 mt-0.5" />
                                <div className="flex-1">
                                    <p className="text-sm text-muted-foreground mb-1">이메일</p>
                                    <p className="font-medium break-all">{myAccount.email}</p>
                                </div>
                            </div>

                            {/* 계정 유형 */}
                            <div className="flex items-start gap-3 p-4 rounded-lg bg-muted/50">
                                {myAccount.isGoogleAccount ? (
                                    <svg className="w-5 h-5 mt-0.5" viewBox="0 0 24 24">
                                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                    </svg>
                                ) : (
                                    <Key className="w-5 h-5 text-orange-500 mt-0.5" />
                                )}
                                <div className="flex-1">
                                    <p className="text-sm text-muted-foreground mb-1">계정 유형</p>
                                    <Badge variant="secondary" className={
                                        myAccount.isGoogleAccount 
                                            ? "bg-blue-100 text-blue-700 hover:bg-blue-200" 
                                            : "bg-purple-100 text-purple-700 hover:bg-purple-200"
                                    }>
                                        {myAccount.isGoogleAccount ? 'Google 소셜 계정' : '일반 계정'}
                                    </Badge>
                                </div>
                            </div>
                        </div>

                        {/* Google ID (Google 계정인 경우만 표시) */}
                        {myAccount.isGoogleAccount && myAccount.googleId && (
                            <div className="flex items-start gap-3 p-4 rounded-lg bg-blue-50 dark:bg-blue-950/20 border border-blue-200 dark:border-blue-800">
                                <svg className="w-5 h-5 mt-0.5" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                                <div className="flex-1">
                                    <p className="text-sm text-muted-foreground mb-1">Google 고유 ID</p>
                                    <p className="font-mono text-sm font-medium break-all text-blue-700 dark:text-blue-400">
                                        {myAccount.googleId}
                                    </p>
                                    <p className="text-xs text-muted-foreground mt-1">
                                        🔒 Google에서 제공하는 고유 식별자입니다
                                    </p>
                                </div>
                            </div>
                        )}

                        {/* 구분선 */}
                        <div className="border-t pt-6">
                            <p className="text-sm text-muted-foreground mb-4">계정 관리</p>
                            
                            {/* 버튼 그룹 */}
                            <div className="flex flex-wrap gap-3">
                                {/* 비밀번호 변경 (일반 계정만) */}
                                {!myAccount.isGoogleAccount && (
                                    <Button 
                                        variant="outline" 
                                        className="gap-2"
                                        onClick={handlePasswordChange}
                                    >
                                        <Key className="w-4 h-4" />
                                        비밀번호 변경
                                    </Button>
                                )}

                                {/* Google 계정 안내 */}
                                {myAccount.isGoogleAccount && (
                                    <div className="flex-1 p-3 rounded-lg bg-blue-50 dark:bg-blue-950/20 border border-blue-200 dark:border-blue-800">
                                        <p className="text-sm text-blue-700 dark:text-blue-400">
                                            🔐 Google 계정은 Google에서 비밀번호를 관리합니다
                                        </p>
                                    </div>
                                )}

                                {/* 회원탈퇴 */}
                                <Button 
                                    variant="destructive" 
                                    className="gap-2"
                                    onClick={handleAccountDelete}
                                >
                                    <UserMinus className="w-4 h-4" />
                                    회원탈퇴
                                </Button>
                            </div>
                        </div>
                    </CardContent>
                </Card>


                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {/* 활동 통계 카드 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Activity className="w-5 h-5 text-primary" />
                                활동 통계
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-4">
                                <div className="flex justify-between items-center">
                                    <span className="text-sm text-muted-foreground">총 활동</span>
                                    <Badge variant="secondary">15개</Badge>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-sm text-muted-foreground">완료율</span>
                                    <Badge variant="secondary">87%</Badge>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-sm text-muted-foreground">연속 일수</span>
                                    <Badge variant="secondary">🔥 7일</Badge>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    {/* 최근 활동 카드 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Target className="w-5 h-5 text-green-500" />
                                최근 활동
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-3">
                                <div className="flex items-center gap-2 text-sm">
                                    <span className="text-lg">🎵</span>
                                    <span className="flex-1">장르별 음악 탐색</span>
                                    <Badge variant="outline" className="text-xs">오늘</Badge>
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                    <span className="text-lg">🏃</span>
                                    <span className="flex-1">홈 운동</span>
                                    <Badge variant="outline" className="text-xs">어제</Badge>
                                </div>
                                <div className="flex items-center gap-2 text-sm">
                                    <span className="text-lg">🍳</span>
                                    <span className="flex-1">요리 배우기</span>
                                    <Badge variant="outline" className="text-xs">2일 전</Badge>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    {/* 성취 배지 카드 */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Trophy className="w-5 h-5 text-yellow-500" />
                                획득 배지
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-3 gap-3">
                                <div className="flex flex-col items-center gap-1">
                                    <div className="w-12 h-12 rounded-full bg-yellow-100 flex items-center justify-center text-2xl">
                                        🏆
                                    </div>
                                    <span className="text-xs text-center">첫 시작</span>
                                </div>
                                <div className="flex flex-col items-center gap-1">
                                    <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-2xl">
                                        🎵
                                    </div>
                                    <span className="text-xs text-center">음악 애호가</span>
                                </div>
                                <div className="flex flex-col items-center gap-1">
                                    <div className="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center text-2xl">
                                        🔥
                                    </div>
                                    <span className="text-xs text-center">7일 연속</span>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>

                {/* 안내 메시지 */}
                <div className="mt-8 p-6 bg-muted/50 rounded-lg text-center">
                    <p className="text-sm text-muted-foreground">
                        💡 더 많은 활동을 완료하면 레벨이 올라가고 새로운 배지를 획득할 수 있어요!
                    </p>
                </div>
            </div>
        </div>
    );
}