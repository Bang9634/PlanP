import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Checkbox } from "./ui/checkbox";
import { Progress } from "./ui/progress";
import { Calendar } from "./ui/calendar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { toast } from "sonner";
import {
  Plus,
  Calendar as CalendarIcon,
  CheckCircle2,
  XCircle,
  Trash2,
  Edit3,
  Clock,
  Target,
  Flame,
  TrendingUp,
  RefreshCw
} from "lucide-react";

interface Routine {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  category: string;
  icon?: string;
  createdAt: Date;
  active: boolean;
  completedDays: Date[];
  streak: number;
  targetFrequency: 'daily' | 'weekly'; // 매일 또는 주간
}

interface RoutineCompletion {
  routineId: string;
  date: Date;
  completed: boolean;
}

interface RoutineManagerProps {
  routines: Routine[];
  onRoutinesChange: (routines: Routine[]) => void;
  onAddActivity?: (activity: any) => void;
}

export function RoutineManager({ routines, onRoutinesChange, onAddActivity }: RoutineManagerProps) {
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [completions, setCompletions] = useState<RoutineCompletion[]>([]);
  const [todayCompleted, setTodayCompleted] = useState<Set<string>>(new Set());

  // 오늘 날짜
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 루틴 완료 처리
  const handleRoutineComplete = (routineId: string) => {
    const routine = routines.find(r => r.id === routineId);
    if (!routine) return;

    const isCompleted = todayCompleted.has(routineId);
    
    if (isCompleted) {
      // 완료 취소
      setTodayCompleted(prev => {
        const newSet = new Set(prev);
        newSet.delete(routineId);
        return newSet;
      });
      
      // 루틴 데이터에서 오늘 제거
      const updatedRoutines = routines.map(r => {
        if (r.id === routineId) {
          const newCompletedDays = r.completedDays.filter(date => 
            date.toDateString() !== today.toDateString()
          );
          return {
            ...r,
            completedDays: newCompletedDays,
            streak: calculateStreak(newCompletedDays)
          };
        }
        return r;
      });
      
      onRoutinesChange(updatedRoutines);
      toast.info(`${routine.title} 완료를 취소했습니다`);
    } else {
      // 완료 처리
      setTodayCompleted(prev => new Set(prev).add(routineId));
      
      // 루틴 데이터에 오늘 추가
      const updatedRoutines = routines.map(r => {
        if (r.id === routineId) {
          const newCompletedDays = [...r.completedDays, new Date(today)];
          return {
            ...r,
            completedDays: newCompletedDays,
            streak: calculateStreak(newCompletedDays)
          };
        }
        return r;
      });
      
      onRoutinesChange(updatedRoutines);
      
      // 활동 기록에 추가
      if (onAddActivity) {
        onAddActivity({
          id: `routine-${routineId}-${Date.now()}`,
          planTitle: routine.title,
          planCategory: routine.category,
          date: new Date(),
          completed: true,
          isRoutine: true,
          duration: routine.duration
        });
      }
      
      toast.success(`🎉 ${routine.title} 완료! 연속 ${calculateStreak([...routine.completedDays, today])}일째`);
    }
  };

  // 연속 일수 계산
  const calculateStreak = (completedDays: Date[]): number => {
    if (completedDays.length === 0) return 0;
    
    const sortedDates = completedDays
      .map(date => new Date(date))
      .sort((a, b) => b.getTime() - a.getTime());
    
    let streak = 0;
    let currentDate = new Date();
    currentDate.setHours(0, 0, 0, 0);
    
    for (const date of sortedDates) {
      const checkDate = new Date(date);
      checkDate.setHours(0, 0, 0, 0);
      
      if (checkDate.getTime() === currentDate.getTime()) {
        streak++;
        currentDate.setDate(currentDate.getDate() - 1);
      } else if (checkDate.getTime() === currentDate.getTime() + (24 * 60 * 60 * 1000)) {
        // 하루 전 날짜
        streak++;
        currentDate = new Date(checkDate);
        currentDate.setDate(currentDate.getDate() - 1);
      } else {
        break;
      }
    }
    
    return streak;
  };

  // 루틴 삭제
  const handleDeleteRoutine = (routineId: string) => {
    const routine = routines.find(r => r.id === routineId);
    if (routine && confirm(`"${routine.title}" 루틴을 삭제하시겠습니까?`)) {
      const updatedRoutines = routines.filter(r => r.id !== routineId);
      onRoutinesChange(updatedRoutines);
      toast.success('루틴이 삭제되었습니다');
    }
  };

  // 루틴 활성/비활성 토글
  const handleToggleRoutine = (routineId: string) => {
    const updatedRoutines = routines.map(r =>
      r.id === routineId ? { ...r, active: !r.active } : r
    );
    onRoutinesChange(updatedRoutines);
  };

  // 오늘 완료된 루틴 초기화
  useEffect(() => {
    const todayCompletedSet = new Set<string>();
    routines.forEach(routine => {
      const hasCompletedToday = routine.completedDays.some(date => 
        date.toDateString() === today.toDateString()
      );
      if (hasCompletedToday) {
        todayCompletedSet.add(routine.id);
      }
    });
    setTodayCompleted(todayCompletedSet);
  }, [routines]);

  // 특정 날짜의 루틴 완료 여부 확인
  const isCompletedOnDate = (routineId: string, date: Date): boolean => {
    const routine = routines.find(r => r.id === routineId);
    if (!routine) return false;
    
    return routine.completedDays.some(completedDate =>
      completedDate.toDateString() === date.toDateString()
    );
  };

  // 활성 루틴만 필터링
  const activeRoutines = routines.filter(r => r.active);
  const completedTodayCount = activeRoutines.filter(r => todayCompleted.has(r.id)).length;
  const todayProgress = activeRoutines.length > 0 ? (completedTodayCount / activeRoutines.length) * 100 : 0;

  return (
    <div className="space-y-6">
      {/* 오늘의 루틴 헤더 */}
      <Card className="border-primary/20 bg-gradient-to-r from-primary/5 to-purple-500/5">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Target className="w-5 h-5 text-primary" />
                오늘의 루틴
              </CardTitle>
              <CardDescription>
                {activeRoutines.length > 0 
                  ? `${completedTodayCount}/${activeRoutines.length} 완료`
                  : '등록된 루틴이 없습니다'
                }
              </CardDescription>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold text-primary">
                {Math.round(todayProgress)}%
              </div>
              <div className="text-sm text-muted-foreground">진행률</div>
            </div>
          </div>
          {activeRoutines.length > 0 && (
            <Progress value={todayProgress} className="mt-4" />
          )}
        </CardHeader>
      </Card>

      <Tabs defaultValue="today" className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="today">오늘의 루틴</TabsTrigger>
          <TabsTrigger value="all">모든 루틴</TabsTrigger>
          <TabsTrigger value="calendar">캘린더</TabsTrigger>
        </TabsList>

        {/* 오늘의 루틴 탭 */}
        <TabsContent value="today" className="space-y-4">
          {activeRoutines.length === 0 ? (
            <Card>
              <CardContent className="text-center py-12">
                <Target className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">루틴이 없습니다</h3>
                <p className="text-muted-foreground mb-4">
                  계획 상세 페이지에서 '루틴에 추가하기' 버튼을 눌러 루틴을 만들어보세요
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {activeRoutines.map((routine) => {
                const isCompleted = todayCompleted.has(routine.id);
                
                return (
                  <Card key={routine.id} className={`transition-all ${
                    isCompleted 
                      ? 'border-green-200 bg-green-50' 
                      : 'hover:shadow-md'
                  }`}>
                    <CardContent className="p-4">
                      <div className="flex items-center gap-4">
                        <Checkbox
                          checked={isCompleted}
                          onCheckedChange={() => handleRoutineComplete(routine.id)}
                          className="w-5 h-5"
                        />
                        
                        <div className="flex items-center gap-2 text-2xl">
                          {routine.icon || '🎯'}
                        </div>
                        
                        <div className="flex-1">
                          <h4 className={`font-medium ${
                            isCompleted ? 'line-through text-muted-foreground' : ''
                          }`}>
                            {routine.title}
                          </h4>
                          <div className="flex items-center gap-3 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {routine.duration}
                            </span>
                            {routine.streak > 0 && (
                              <span className="flex items-center gap-1 text-orange-600">
                                <Flame className="w-3 h-3" />
                                {routine.streak}일 연속
                              </span>
                            )}
                          </div>
                        </div>
                        
                        <div className="flex flex-wrap gap-1">
                          {routine.tags.slice(0, 2).map(tag => (
                            <Badge key={tag} variant="secondary" className="text-xs">
                              #{tag}
                            </Badge>
                          ))}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}

          {/* 오늘의 성과 */}
          {completedTodayCount > 0 && (
            <Card className="border-green-200 bg-green-50">
              <CardContent className="p-4">
                <div className="flex items-center gap-3">
                  <CheckCircle2 className="w-6 h-6 text-green-600" />
                  <div>
                    <h4 className="font-medium text-green-800">
                      오늘 {completedTodayCount}개 루틴 완료! 🎉
                    </h4>
                    <p className="text-sm text-green-600">
                      {todayProgress === 100 
                        ? '모든 루틴을 완료했어요! 정말 대단해요!' 
                        : '계속해서 나머지 루틴도 완료해보세요!'
                      }
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* 모든 루틴 탭 */}
        <TabsContent value="all" className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium">내 루틴 관리</h3>
            <Button variant="outline" size="sm" className="gap-2">
              <Plus className="w-4 h-4" />
              새 루틴 추가
            </Button>
          </div>

          {routines.length === 0 ? (
            <Card>
              <CardContent className="text-center py-12">
                <Target className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">아직 루틴이 없습니다</h3>
                <p className="text-muted-foreground">
                  좋아하는 활동을 루틴으로 만들어 매일 실행해보세요
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {routines.map((routine) => (
                <Card key={routine.id} className={
                  routine.active ? '' : 'opacity-60 border-dashed'
                }>
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start gap-3 flex-1">
                        <div className="text-2xl">{routine.icon || '🎯'}</div>
                        <div className="flex-1">
                          <h4 className="font-medium">{routine.title}</h4>
                          <p className="text-sm text-muted-foreground mb-2">
                            {routine.description}
                          </p>
                          
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {routine.duration}
                            </span>
                            <span className="flex items-center gap-1">
                              <Flame className="w-3 h-3" />
                              연속 {routine.streak}일
                            </span>
                            <span className="flex items-center gap-1">
                              <CheckCircle2 className="w-3 h-3" />
                              총 {routine.completedDays.length}회
                            </span>
                          </div>

                          <div className="flex gap-1 mt-2">
                            {routine.tags.map(tag => (
                              <Badge key={tag} variant="secondary" className="text-xs">
                                #{tag}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      </div>

                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleToggleRoutine(routine.id)}
                        >
                          {routine.active ? '일시정지' : '활성화'}
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDeleteRoutine(routine.id)}
                          className="text-red-600 hover:text-red-700"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* 캘린더 탭 */}
        <TabsContent value="calendar" className="space-y-4">
          <div className="grid gap-6 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CalendarIcon className="w-5 h-5" />
                    루틴 달성 캘린더
                  </CardTitle>
                  <CardDescription>
                    날짜를 클릭하여 해당 날의 루틴 기록을 확인하세요
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Calendar
                    mode="single"
                    selected={selectedDate}
                    onSelect={(date) => date && setSelectedDate(date)}
                    className="rounded-md border-0"
                    modifiers={{
                      completed: routines.flatMap(routine => 
                        routine.completedDays.map(date => new Date(date))
                      )
                    }}
                    modifiersClassNames={{
                      completed: "bg-green-100 text-green-800 font-bold"
                    }}
                  />
                </CardContent>
              </Card>
            </div>

            <div>
              <Card>
                <CardHeader>
                  <CardTitle>
                    {selectedDate.toLocaleDateString('ko-KR', {
                      month: 'long',
                      day: 'numeric',
                      weekday: 'short'
                    })}
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {routines.map((routine) => {
                    const completed = isCompletedOnDate(routine.id, selectedDate);
                    
                    return (
                      <div key={routine.id} className="flex items-center gap-3 p-2 rounded-lg border">
                        <div className={`p-1 rounded-full ${
                          completed 
                            ? 'bg-green-100 text-green-600' 
                            : 'bg-gray-100 text-gray-400'
                        }`}>
                          {completed ? (
                            <CheckCircle2 className="w-4 h-4" />
                          ) : (
                            <XCircle className="w-4 h-4" />
                          )}
                        </div>
                        <div className="flex-1">
                          <div className="font-medium text-sm">{routine.title}</div>
                          <div className="text-xs text-muted-foreground">
                            {routine.duration}
                          </div>
                        </div>
                      </div>
                    );
                  })}

                  {routines.length === 0 && (
                    <div className="text-center text-muted-foreground py-4">
                      루틴이 없습니다
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}