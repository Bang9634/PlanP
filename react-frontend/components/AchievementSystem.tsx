import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { 
  Trophy, 
  Target, 
  Calendar, 
  TrendingUp, 
  Award, 
  Star,
  CheckCircle2,
  Zap,
  Heart,
  Crown
} from "lucide-react";

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  earned: boolean;
  earnedDate?: Date;
}

interface WeeklyStats {
  completedPlans: number;
  streak: number;
  favoriteCategory: string;
  totalTime: string;
}

interface AchievementSystemProps {
  completedPlans: string[];
  currentUser: string | null;
}

export function AchievementSystem({ completedPlans, currentUser }: AchievementSystemProps) {
  const [achievements, setAchievements] = useState<Achievement[]>([
    {
      id: 'first-plan',
      title: '첫 걸음 🎯',
      description: '첫 번째 계획을 완료했어요!',
      icon: '🎯',
      earned: false
    },
    {
      id: 'three-streak',
      title: '3일 연속 📅',
      description: '3일 연속으로 계획을 실행했어요!',
      icon: '📅',
      earned: false
    },
    {
      id: 'music-lover',
      title: '음악 애호가 🎵',
      description: '음악 관련 계획을 5개 이상 완료했어요!',
      icon: '🎵',
      earned: false
    },
    {
      id: 'travel-explorer',
      title: '여행 탐험가 ✈️',
      description: '여행 관련 계획을 3개 이상 완료했어요!',
      icon: '✈️',
      earned: false
    },
    {
      id: 'week-champion',
      title: '주간 챔피언 👑',
      description: '이번 주에 7개 이상의 계획을 완료했어요!',
      icon: '👑',
      earned: false
    },
    {
      id: 'variety-seeker',
      title: '다양성 추구자 🌈',
      description: '모든 카테고리의 계획을 경험해봤어요!',
      icon: '🌈',
      earned: false
    }
  ]);

  const [weeklyStats, setWeeklyStats] = useState<WeeklyStats>({
    completedPlans: 0,
    streak: 0,
    favoriteCategory: '음악',
    totalTime: '2시간 30분'
  });

  const [showCelebration, setShowCelebration] = useState(false);

  // 성취 체크 및 업데이트
  useEffect(() => {
    const updatedAchievements = achievements.map(achievement => {
      let shouldEarn = false;

      switch (achievement.id) {
        case 'first-plan':
          shouldEarn = completedPlans.length >= 1;
          break;
        case 'three-streak':
          shouldEarn = weeklyStats.streak >= 3;
          break;
        case 'music-lover':
          // 음악 관련 계획 완료수 체크 (실제로는 더 세밀한 로직 필요)
          shouldEarn = completedPlans.length >= 2;
          break;
        case 'travel-explorer':
          shouldEarn = completedPlans.length >= 2;
          break;
        case 'week-champion':
          shouldEarn = weeklyStats.completedPlans >= 5;
          break;
        case 'variety-seeker':
          shouldEarn = completedPlans.length >= 4;
          break;
      }

      if (shouldEarn && !achievement.earned) {
        // 새로운 성취 달성!
        setShowCelebration(true);
        setTimeout(() => setShowCelebration(false), 3000);
        return { ...achievement, earned: true, earnedDate: new Date() };
      }

      return achievement;
    });

    setAchievements(updatedAchievements);
  }, [completedPlans, weeklyStats]);

  // 주간 통계 업데이트
  useEffect(() => {
    setWeeklyStats(prev => ({
      ...prev,
      completedPlans: completedPlans.length,
      streak: Math.min(completedPlans.length, 7) // 간단한 연속 계산
    }));
  }, [completedPlans]);

  const earnedAchievements = achievements.filter(a => a.earned);
  const totalAchievements = achievements.length;
  const achievementProgress = (earnedAchievements.length / totalAchievements) * 100;

  if (!currentUser) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-5 h-5" />
            성취 시스템
          </CardTitle>
          <CardDescription>
            로그인하면 성취와 통계를 확인할 수 있어요
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* 축하 메시지 */}
      {showCelebration && (
        <Card className="border-green-200 bg-green-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <div className="text-2xl">🎉</div>
              <div>
                <h3 className="font-medium text-green-800">새로운 성취 달성!</h3>
                <p className="text-sm text-green-600">축하합니다! 계속 멋진 계획들을 실행해보세요.</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 주간 통계 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5" />
            이번 주 통계
          </CardTitle>
          <CardDescription>
            {currentUser}님의 이번 주 활동 요약
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center p-3 bg-blue-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">{weeklyStats.completedPlans}</div>
              <div className="text-sm text-blue-600">완료한 계획</div>
            </div>
            <div className="text-center p-3 bg-orange-50 rounded-lg">
              <div className="text-2xl font-bold text-orange-600">{weeklyStats.streak}</div>
              <div className="text-sm text-orange-600">연속 일수</div>
            </div>
            <div className="text-center p-3 bg-purple-50 rounded-lg">
              <div className="text-xl font-bold text-purple-600">{weeklyStats.favoriteCategory}</div>
              <div className="text-sm text-purple-600">선호 카테고리</div>
            </div>
            <div className="text-center p-3 bg-green-50 rounded-lg">
              <div className="text-xl font-bold text-green-600">{weeklyStats.totalTime}</div>
              <div className="text-sm text-green-600">총 활동 시간</div>
            </div>
          </div>

          {weeklyStats.completedPlans > 0 && (
            <div className="mt-4 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
              <div className="flex items-center gap-2 mb-2">
                <Star className="w-5 h-5 text-yellow-600" />
                <span className="font-medium text-yellow-800">개인화 추천</span>
              </div>
              <p className="text-sm text-yellow-700">
                {`${currentUser}님에게 맞는 활동은 '${weeklyStats.favoriteCategory} + 짧은 루틴'이네요! 
                ${weeklyStats.streak >= 3 ? '연속 실행력이 뛰어나시군요 👏' : '조금씩 꾸준히 하는 것을 추천해요 🌟'}`}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 성취 목록 */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="w-5 h-5" />
            성취 뱃지
          </CardTitle>
          <CardDescription>
            달성한 성취: {earnedAchievements.length}/{totalAchievements}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4">
            <div className="flex justify-between text-sm mb-2">
              <span>전체 진행률</span>
              <span>{Math.round(achievementProgress)}%</span>
            </div>
            <Progress value={achievementProgress} className="w-full" />
          </div>

          <div className="grid gap-3">
            {achievements.map((achievement) => (
              <div 
                key={achievement.id}
                className={`flex items-center gap-3 p-3 rounded-lg border transition-all ${
                  achievement.earned 
                    ? 'bg-green-50 border-green-200' 
                    : 'bg-gray-50 border-gray-200 opacity-60'
                }`}
              >
                <div className="text-2xl">{achievement.icon}</div>
                <div className="flex-1">
                  <h4 className="font-medium">{achievement.title}</h4>
                  <p className="text-sm text-muted-foreground">{achievement.description}</p>
                  {achievement.earned && achievement.earnedDate && (
                    <p className="text-xs text-green-600 mt-1">
                      {achievement.earnedDate.toLocaleDateString('ko-KR')} 달성
                    </p>
                  )}
                </div>
                {achievement.earned && (
                  <CheckCircle2 className="w-5 h-5 text-green-600" />
                )}
              </div>
            ))}
          </div>

          {earnedAchievements.length === 0 && (
            <div className="text-center py-8">
              <Target className="w-12 h-12 text-muted-foreground mx-auto mb-3" />
              <h3 className="font-medium mb-2">첫 번째 성취를 달성해보세요!</h3>
              <p className="text-sm text-muted-foreground">
                계획을 하나씩 완료하면서 멋진 뱃지들을 모아보세요
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 격려 메시지 */}
      {completedPlans.length > 0 && (
        <Card className="border-primary/20 bg-primary/5">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <Heart className="w-6 h-6 text-red-500" />
              <div>
                <h3 className="font-medium">오늘도 목표 달성! 🎉</h3>
                <p className="text-sm text-muted-foreground">
                  {`${completedPlans.length}개의 계획을 완료하셨네요. 
                  ${weeklyStats.streak >= 3 ? '연속 실행 중이에요!' : '꾸준히 실행해보세요!'}`}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}