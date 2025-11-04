import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { Calendar } from "./ui/calendar";
import {
  User,
  Trophy,
  Target,
  Calendar as CalendarIcon,
  CheckCircle2,
  XCircle,
  TrendingUp,
  Award,
  Star,
  Activity,
  Clock,
  BarChart3,
  PieChart
} from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart as RechartsPieChart, Pie, Cell } from "recharts";

interface ActivityRecord {
  id: string;
  planTitle: string;
  planCategory: string;
  date: Date;
  completed: boolean;
  duration?: string;
  isRoutine?: boolean;
}

interface UserStats {
  level: number;
  points: number;
  totalActivities: number;
  completedActivities: number;
  currentStreak: number;
  longestStreak: number;
  favoriteCategory: string;
}

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  earned: boolean;
  earnedDate?: Date;
  progress?: number;
  target?: number;
}

interface MyAccountPageProps {
  currentUser: string;
  completedPlans: string[];
  routines: any[];
  onBack: () => void;
}

export function MyAccountPage({ currentUser, completedPlans, routines, onBack }: MyAccountPageProps) {
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());
  const [activityHistory, setActivityHistory] = useState<ActivityRecord[]>([]);
  const [userStats, setUserStats] = useState<UserStats>({
    level: 5,
    points: 1250,
    totalActivities: 28,
    completedActivities: 23,
    currentStreak: 5,
    longestStreak: 12,
    favoriteCategory: '음악'
  });

  const [achievements, setAchievements] = useState<Achievement[]>([
    {
      id: 'first-activity',
      title: '첫 걸음',
      description: '첫 번째 활동을 완료했어요!',
      icon: '🎯',
      earned: true,
      earnedDate: new Date('2024-01-15')
    },
    {
      id: 'streak-3',
      title: '3일 연속',
      description: '3일 연속으로 활동을 완료했어요!',
      icon: '🔥',
      earned: true,
      earnedDate: new Date('2024-01-18')
    },
    {
      id: 'streak-7',
      title: '일주일 챌린지',
      description: '7일 연속으로 활동을 완료했어요!',
      icon: '⚡',
      earned: true,
      earnedDate: new Date('2024-01-22')
    },
    {
      id: 'activity-10',
      title: '활동가',
      description: '총 10개의 활동을 완료했어요!',
      icon: '🏃',
      earned: true,
      earnedDate: new Date('2024-01-20')
    },
    {
      id: 'music-lover',
      title: '음악 애호가',
      description: '음악 카테고리 활동을 5회 완료했어요!',
      icon: '🎵',
      earned: true,
      earnedDate: new Date('2024-01-25')
    },
    {
      id: 'routine-master',
      title: '루틴 마스터',
      description: '3개 이상의 루틴을 생성했어요!',
      icon: '📅',
      earned: false,
      progress: 2,
      target: 3
    },
    {
      id: 'explorer',
      title: '탐험가',
      description: '모든 카테고리의 활동을 경험해봤어요!',
      icon: '🌟',
      earned: false,
      progress: 6,
      target: 8
    }
  ]);

  // 모든 활동 기록 생성 (샘플 데이터)
  useEffect(() => {
    const sampleActivities: ActivityRecord[] = [
      {
        id: '1',
        planTitle: '새로운 플레이리스트 만들기',
        planCategory: 'music',
        date: new Date('2024-01-27'),
        completed: true,
        duration: '20분',
        isRoutine: false
      },
      {
        id: '2',
        planTitle: '15분 아침 스트레칭',
        planCategory: 'daily',
        date: new Date('2024-01-27'),
        completed: true,
        duration: '15분',
        isRoutine: true
      },
      {
        id: '3',
        planTitle: '동네 한 바퀴 산책',
        planCategory: 'exercise',
        date: new Date('2024-01-26'),
        completed: true,
        duration: '30분',
        isRoutine: false
      },
      {
        id: '4',
        planTitle: '5분 명상하기',
        planCategory: 'daily',
        date: new Date('2024-01-26'),
        completed: false,
        isRoutine: true
      },
      {
        id: '5',
        planTitle: '카페에서 음악 감상하기',
        planCategory: 'music',
        date: new Date('2024-01-25'),
        completed: true,
        duration: '1시간 30분',
        isRoutine: false
      }
    ];
    setActivityHistory(sampleActivities);
  }, []);

  // 주간 통계 데이터
  const weeklyData = [
    { name: '월', 완료: 3, 미완료: 1 },
    { name: '화', 완료: 2, 미완료: 0 },
    { name: '수', 완료: 4, 미완료: 1 },
    { name: '목', 완료: 3, 미완료: 2 },
    { name: '금', 완료: 5, 미완료: 0 },
    { name: '토', 완료: 2, 미완료: 1 },
    { name: '일', 완료: 4, 미완료: 0 }
  ];

  // 카테고리별 활동 분포
  const categoryData = [
    { name: '음악', value: 8, color: '#8B5CF6' },
    { name: '일상', value: 6, color: '#10B981' },
    { name: '운동', value: 4, color: '#F59E0B' },
    { name: '공부', value: 3, color: '#EF4444' },
    { name: '문화', value: 2, color: '#3B82F6' }
  ];

  const completionRate = Math.round((userStats.completedActivities / userStats.totalActivities) * 100);
  const levelProgress = (userStats.points % 500) / 500 * 100; // 500포인트마다 레벨업

  const earnedAchievements = achievements.filter(a => a.earned);

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
                {currentUser.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>
            
            <div className="flex-1">
              <h1 className="text-2xl font-medium mb-2">{currentUser}님</h1>
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

          {/* 주요 통계 */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-2xl font-bold text-green-600">{userStats.completedActivities}</div>
                <div className="text-sm text-muted-foreground">완료한 활동</div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-2xl font-bold text-orange-600">{userStats.currentStreak}</div>
                <div className="text-sm text-muted-foreground">현재 연속 일수</div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-2xl font-bold text-purple-600">{completionRate}%</div>
                <div className="text-sm text-muted-foreground">완료율</div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4 text-center">
                <div className="text-xl font-bold text-blue-600">{userStats.favoriteCategory}</div>
                <div className="text-sm text-muted-foreground">선호 카테고리</div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      {/* 메인 콘텐츠 */}
      <div className="container mx-auto px-4 py-8">
        <Tabs defaultValue="history" className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="history">활동 기록</TabsTrigger>
            <TabsTrigger value="statistics">통계</TabsTrigger>
            <TabsTrigger value="achievements">성취</TabsTrigger>
            <TabsTrigger value="calendar">캘린더</TabsTrigger>
          </TabsList>

          {/* 활동 기록 탭 */}
          <TabsContent value="history" className="space-y-4">
            <h2 className="flex items-center gap-2">
              <Clock className="w-5 h-5" />
              최근 활동 기록
            </h2>
            
            <div className="space-y-3">
              {activityHistory.map((activity) => (
                <Card key={activity.id}>
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className={`p-2 rounded-full ${
                          activity.completed 
                            ? 'bg-green-100 text-green-600' 
                            : 'bg-red-100 text-red-600'
                        }`}>
                          {activity.completed ? (
                            <CheckCircle2 className="w-4 h-4" />
                          ) : (
                            <XCircle className="w-4 h-4" />
                          )}
                        </div>
                        
                        <div>
                          <h4 className="font-medium">{activity.planTitle}</h4>
                          <div className="flex items-center gap-3 text-sm text-muted-foreground">
                            <span>{activity.date.toLocaleDateString('ko-KR')}</span>
                            {activity.duration && <span>⏱ {activity.duration}</span>}
                            {activity.isRoutine && (
                              <Badge variant="outline" className="text-xs">루틴</Badge>
                            )}
                          </div>
                        </div>
                      </div>
                      
                      <Badge variant="secondary" className="text-xs">
                        {getCategoryName(activity.planCategory)}
                      </Badge>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>

          {/* 통계 탭 */}
          <TabsContent value="statistics" className="space-y-6">
            <div className="grid gap-6 lg:grid-cols-2">
              {/* 주간 활동 차트 */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BarChart3 className="w-5 h-5" />
                    이번 주 활동
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={weeklyData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="완료" fill="#10B981" />
                      <Bar dataKey="미완료" fill="#EF4444" />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* 카테고리별 분포 */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <PieChart className="w-5 h-5" />
                    카테고리별 활동
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={200}>
                    <RechartsPieChart>
                      <Pie
                        data={categoryData}
                        cx="50%"
                        cy="50%"
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                        label={({ name, value }) => `${name}: ${value}`}
                      >
                        {categoryData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </RechartsPieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </div>

            {/* 상세 통계 */}
            <Card>
              <CardHeader>
                <CardTitle>상세 통계</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 md:grid-cols-3">
                  <div className="p-4 bg-blue-50 rounded-lg">
                    <div className="text-sm text-blue-600 mb-1">평균 활동 시간</div>
                    <div className="text-2xl font-bold text-blue-700">32분</div>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg">
                    <div className="text-sm text-green-600 mb-1">최고 연속 기록</div>
                    <div className="text-2xl font-bold text-green-700">{userStats.longestStreak}일</div>
                  </div>
                  <div className="p-4 bg-purple-50 rounded-lg">
                    <div className="text-sm text-purple-600 mb-1">이번 달 활동</div>
                    <div className="text-2xl font-bold text-purple-700">18개</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* 성취 탭 */}
          <TabsContent value="achievements" className="space-y-4">
            <h2 className="flex items-center gap-2">
              <Award className="w-5 h-5" />
              성취 뱃지 ({earnedAchievements.length}/{achievements.length})
            </h2>
            
            <div className="grid gap-4 md:grid-cols-2">
              {achievements.map((achievement) => (
                <Card key={achievement.id} className={
                  achievement.earned 
                    ? 'border-green-200 bg-green-50' 
                    : 'border-gray-200'
                }>
                  <CardContent className="p-4">
                    <div className="flex items-center gap-3">
                      <div className={`text-3xl ${achievement.earned ? '' : 'grayscale opacity-50'}`}>
                        {achievement.icon}
                      </div>
                      <div className="flex-1">
                        <h4 className="font-medium">{achievement.title}</h4>
                        <p className="text-sm text-muted-foreground">
                          {achievement.description}
                        </p>
                        {achievement.earned && achievement.earnedDate && (
                          <p className="text-xs text-green-600 mt-1">
                            {achievement.earnedDate.toLocaleDateString('ko-KR')} 달성
                          </p>
                        )}
                        {!achievement.earned && achievement.progress && achievement.target && (
                          <div className="mt-2">
                            <div className="flex justify-between text-xs mb-1">
                              <span>진행률</span>
                              <span>{achievement.progress}/{achievement.target}</span>
                            </div>
                            <Progress 
                              value={(achievement.progress / achievement.target) * 100} 
                              className="h-2" 
                            />
                          </div>
                        )}
                      </div>
                      {achievement.earned && (
                        <CheckCircle2 className="w-5 h-5 text-green-600" />
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>

          {/* 캘린더 탭 */}
          <TabsContent value="calendar" className="space-y-4">
            <h2 className="flex items-center gap-2">
              <CalendarIcon className="w-5 h-5" />
              활동 캘린더
            </h2>
            
            <div className="grid gap-6 lg:grid-cols-3">
              <div className="lg:col-span-2">
                <Card>
                  <CardContent className="p-6">
                    <Calendar
                      mode="single"
                      selected={selectedDate}
                      onSelect={setSelectedDate}
                      className="rounded-md border-0"
                      classNames={{
                        day: "hover:bg-accent hover:text-accent-foreground",
                        day_selected: "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground",
                        day_today: "bg-accent text-accent-foreground",
                        day_outside: "text-muted-foreground opacity-50",
                        day_disabled: "text-muted-foreground opacity-50",
                        day_range_middle: "aria-selected:bg-accent aria-selected:text-accent-foreground",
                        day_hidden: "invisible",
                      }}
                    />
                  </CardContent>
                </Card>
              </div>
              
              <div>
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">
                      {selectedDate?.toLocaleDateString('ko-KR', {
                        month: 'long',
                        day: 'numeric'
                      })}
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {selectedDate && (
                        <div className="text-sm text-muted-foreground">
                          이 날의 활동 기록을 확인해보세요
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

// 카테고리 이름 헬퍼 함수
function getCategoryName(category: string): string {
  const names: Record<string, string> = {
    music: '음악',
    daily: '일상',
    travel: '여행',
    study: '공부',
    hobby: '취미',
    social: '사교',
    culture: '문화',
    exercise: '운동'
  };
  return names[category] || category;
}