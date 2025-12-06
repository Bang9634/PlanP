import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { Calendar } from "./ui/calendar";
import {
    Trophy,
    Target,
    Calendar as CalendarIcon,
    CheckCircle2,
    XCircle,
    Award,
    Activity,
    Clock,
    BarChart3,
    PieChart
} from "lucide-react";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    PieChart as RechartsPieChart,
    Pie,
    Cell
} from "recharts";

import { apiService } from "../services/api";

// 프로필
export interface UserProfile {
    userId: string;
    name: string;
    email: string;
    level?: number;
    points?: number;
}
// 활동기록
export interface ActivityRecord {
    id: string;
    title: string;
    category: string;
    date: string;        // API는 대부분 string(ISO date)
    completed: boolean;
    duration?: string;
    isRoutine?: boolean;
}
// 통계
export interface ActivityStatistics {
    totalActivities: number;
    completedActivities: number;
    currentStreak: number;
    longestStreak: number;
    favoriteCategory: string;
}

// 성취도
export interface Achievement {
    id: string;
    title: string;
    description: string;
    icon: string;
    earned: boolean;
    earnedDate?: string;
    progress?: number;
    target?: number;
}

export interface CalendarDayActivity {
    date: string; // YYYY-MM-DD
    activities: {
        id: string;
        title: string;
        category: string;
        completed: boolean;
    }[];
}

interface MyAccountPageProps {
    onBack: () => void;
}

export function MyAccountPage({ onBack }: MyAccountPageProps) {
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [activityHistory, setActivityHistory] = useState<ActivityRecord[]>([]);
    const [statistics, setStatistics] = useState<ActivityStatistics | null>(null);
    const [achievements, setAchievements] = useState<Achievement[]>([]);
    const [calendar, setCalendar] = useState<CalendarDayActivity[]>([]);

    //  API에서 가져온 사용자 프로필 저장
    useEffect(() => {
        async function loadAllData() {
            try {
                const now = new Date();
                const year = now.getFullYear();
                const month = now.getMonth() + 1;

                // 사용자 계정 페이지 데이터 일괄 로딩
                // /users/me              : 기본 프로필
                // /users/me/activity     : 활동 이력
                // /users/me/statistics   : 통계 정보
                // /users/me/achievements : 보유 뱃지
                // /users/me/calendar     : 월간 활동 캘린더

                const [
                    profileRes, activityRes, statsRes, achievementsRes, calendarRes,] =
                    await Promise.all([
                    apiService.getMyProfile(),
                    apiService.getMyActivityHistory(),
                    apiService.getMyStatistics(),
                    apiService.getMyAchievements(),
                    apiService.getMyCalendar(year, month),
                ]);

                setProfile(profileRes);
                setActivityHistory(activityRes);
                setStatistics(statsRes);
                setAchievements(achievementsRes);
                setCalendar(calendarRes);
            } catch (err) {
                console.error("❌ MyAccountPage 데이터 로딩 실패:", err);
            }
        }

        loadAllData();
    }, []);

    // ⭐ 로딩 UI (프로필 불러오기 전)
    if (!profile) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <p>내 정보를 불러오는 중...</p>
            </div>
        );
    }

    //------------------------------------------------------------------------------------------------------------------
    return (
        <div className="min-h-screen bg-background">
            {/* 헤더 */}
            <div className="border-b bg-card">
                <div className="container mx-auto px-4 py-4">
                    <Button variant="ghost" onClick={onBack} className="mb-4">
                        ← 뒤로가기
                    </Button>

                    {/* 프로필 섹션 */}
                    <div className="flex items-center gap-6 mb-6">
                        <Avatar className="w-20 h-20">
                            <AvatarImage src="" />
                            <AvatarFallback className="text-xl">
                                {profile.name.charAt(0).toUpperCase()}
                            </AvatarFallback>
                        </Avatar>

                        <div className="flex-1">
                            <h1 className="text-2xl font-medium mb-2">{profile.name}님</h1>

                            <div className="flex items-center gap-6 text-sm text-muted-foreground">
                                <div className="flex items-center gap-1">
                                    <Trophy className="w-4 h-4 text-yellow-500" />
                                    <span>레벨 {userStats.level}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Target className="w-4 h-4 text-green-500" />
                                    <span>{userStats.points} 포인트</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <Activity className="w-4 h-4 text-blue-500" />
                                    <span>총 {userStats.totalActivities}개 활동</span>
                                </div>
                            </div>

                            {/* 레벨 진행률 */}
                            <div className="mt-3">
                                <div className="flex justify-between text-xs mb-1">
                                    <span>다음 레벨까지</span>
                                    <span>{Math.round(levelProgress)}%</span>
                                </div>
                                <Progress value={levelProgress} className="h-2" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 이하 모든 UI는 기존 코드 그대로 */}
            {/* ---- 중략: 활동 기록 / 통계 / 성취 / 캘린더 UI 그대로 유지 ---- */}
        </div>
    );
}

// 카테고리 이름 헬퍼
function getCategoryName(category: string): string {
    const names: Record<string, string> = {
        music: "음악",
        daily: "일상",
        travel: "여행",
        study: "공부",
        hobby: "취미",
        social: "사교",
        culture: "문화",
        exercise: "운동"
    };
    return names[category] || category;
}
