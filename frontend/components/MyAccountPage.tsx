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
} from "lucide-react";
import { AuthService } from "../services/AuthService";

interface MyAccountPageProps {
    onBackToHome: () => void;
}

export function MyAccountPage({ onBackToHome }: MyAccountPageProps) {
    const [isLoading, setIsLoading] = useState(true);
    const [profile, setProfile] = useState<{
        userId: string;
        name: string;
        email: string;
        level: number;
        points: number;
    } | null>(null);

    useEffect(() => {
        // 로컬 스토리지에서 사용자 정보 로드
        const userInfo = AuthService.getUserInfo();
        
        if (userInfo) {
            // 임시 데이터 (백엔드 API 구현 전)
            setProfile({
                userId: userInfo.userId,
                name: userInfo.name,
                email: userInfo.email,
                level: 5,
                points: 1250,
            });
        }
        
        setIsLoading(false);
    }, []);

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

                    {/* 프로필 섹션 */}
                    <div className="flex items-center gap-6 mb-6">
                        <Avatar className="w-20 h-20">
                            <AvatarImage src="" />
                            <AvatarFallback className="text-xl bg-primary/10">
                                {profile.name.charAt(0).toUpperCase()}
                            </AvatarFallback>
                        </Avatar>

                        <div className="flex-1">
                            <h1 className="text-2xl font-medium mb-2">{profile.name}님</h1>

                            <div className="flex items-center gap-6 text-sm text-muted-foreground">
                                <div className="flex items-center gap-1">
                                    <Trophy className="w-4 h-4 text-yellow-500" />
                                    <span>레벨 {profile.level}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Target className="w-4 h-4 text-green-500" />
                                    <span>{profile.points} 포인트</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Activity className="w-4 h-4 text-blue-500" />
                                    <span>{profile.email}</span>
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