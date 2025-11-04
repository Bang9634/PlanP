import { useState, useEffect, useRef, useCallback } from "react";
import { Button } from "./ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Skeleton } from "./ui/skeleton";
import { PlanCard } from "./PlanCard";
import { 
  Clock, 
  Users, 
  Star, 
  RefreshCw, 
  Grid3X3, 
  LayoutGrid,
  Heart,
  Plus
} from "lucide-react";

interface Plan {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  participants?: string;
  rating?: number;
  icon?: string;
  category: string;
}

interface InfiniteRecommendationsProps {
  category?: string;
  onPlanClick: (plan: Plan) => void;
  onAddToRoutine?: (plan: Plan) => void;
  viewMode?: 'grid' | 'deck';
  onViewModeChange?: (mode: 'grid' | 'deck') => void;
}

// 카테고리별 대용량 계획 데이터 (실제로는 API에서 가져올 데이터)
const planDatabase: Record<string, Plan[]> = {
  music: [
    {
      id: 'music-1',
      title: '새로운 플레이리스트 만들기',
      description: '오늘 기분에 맞는 음악들로 나만의 플레이리스트를 만들어보세요',
      duration: '15-20분',
      difficulty: 'easy',
      tags: ['창의적', '혼자하기', '실내'],
      participants: '혼자',
      rating: 4.5,
      icon: '🎵',
      category: 'music'
    },
    {
      id: 'music-2',
      title: '좋아하는 가수 신곡 탐색',
      description: '최근에 나온 신곡들을 찾아보고 새로운 음악을 발견해보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['발견', '혼자하기', '실내'],
      participants: '혼자',
      rating: 4.3,
      icon: '🔍',
      category: 'music'
    },
    {
      id: 'music-3',
      title: '악기 연주 영상 보며 따라하기',
      description: '유튜브에서 간단한 악기 연주 영상을 보며 따라해보세요',
      duration: '30-45분',
      difficulty: 'medium',
      tags: ['학습', '연주', '실내'],
      participants: '혼자',
      rating: 4.1,
      icon: '🎸',
      category: 'music'
    },
    {
      id: 'music-4',
      title: '음악과 함께 집안일 하기',
      description: '신나는 음악을 틀고 집안일을 하면서 스트레스 해소하기',
      duration: '30-60분',
      difficulty: 'easy',
      tags: ['생산적', '실내', '활동적'],
      participants: '혼자',
      rating: 4.4,
      icon: '🧹',
      category: 'music'
    },
    {
      id: 'music-5',
      title: '카페에서 음악 감상하기',
      description: '좋아하는 카페에서 이어폰을 끼고 음악에 집중해보세요',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['힐링', '카페', '혼자하기'],
      participants: '혼자',
      rating: 4.6,
      icon: '☕',
      category: 'music'
    }
  ],
  daily: [
    {
      id: 'daily-1',
      title: '15분 아침 스트레칭',
      description: '몸을 깨우는 간단한 스트레칭으로 하루를 시작해보세요',
      duration: '15분',
      difficulty: 'easy',
      tags: ['건강', '아침', '실내'],
      participants: '혼자',
      rating: 4.7,
      icon: '🧘',
      category: 'daily'
    },
    {
      id: 'daily-2',
      title: '창가에서 커피 마시며 일기 쓰기',
      description: '따뜻한 커피와 함께 오늘의 기분을 일기에 적어보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['힐링', '기록', '실내'],
      participants: '혼자',
      rating: 4.5,
      icon: '📝',
      category: 'daily'
    },
    {
      id: 'daily-3',
      title: '5분 명상하기',
      description: '짧은 명상으로 마음을 정리하고 집중력을 높여보세요',
      duration: '5-10분',
      difficulty: 'easy',
      tags: ['명상', '힐링', '실내'],
      participants: '혼자',
      rating: 4.4,
      icon: '🧠',
      category: 'daily'
    },
    {
      id: 'daily-4',
      title: '방 정리하고 꾸미기',
      description: '주변 환경을 깔끔하게 정리하고 나만의 공간으로 꾸며보세요',
      duration: '30-60분',
      difficulty: 'medium',
      tags: ['정리', '꾸미기', '실내'],
      participants: '혼자',
      rating: 4.3,
      icon: '🏠',
      category: 'daily'
    },
    {
      id: 'daily-5',
      title: '요리 레시피 도전하기',
      description: '새로운 요리 레시피를 찾아서 직접 만들어보세요',
      duration: '45-90분',
      difficulty: 'medium',
      tags: ['요리', '창의적', '실내'],
      participants: '혼자',
      rating: 4.2,
      icon: '👨‍🍳',
      category: 'daily'
    }
  ],
  exercise: [
    {
      id: 'exercise-1',
      title: '홈트레이닝 20분',
      description: '유튜브 홈트레이닝 영상을 보며 운동해보세요',
      duration: '20-30분',
      difficulty: 'medium',
      tags: ['운동', '실내', '건강'],
      participants: '혼자',
      rating: 4.5,
      icon: '💪',
      category: 'exercise'
    },
    {
      id: 'exercise-2',
      title: '동네 한 바퀴 산책',
      description: '가벼운 산책으로 몸과 마음을 상쾌하게 만들어보세요',
      duration: '30-45분',
      difficulty: 'easy',
      tags: ['산책', '야외', '힐링'],
      participants: '혼자',
      rating: 4.6,
      icon: '🚶',
      category: 'exercise'
    },
    {
      id: 'exercise-3',
      title: '계단 오르내리기 운동',
      description: '집이나 아파트 계단을 이용한 간단한 유산소 운동',
      duration: '10-15분',
      difficulty: 'medium',
      tags: ['유산소', '실내', '간단'],
      participants: '혼자',
      rating: 4.2,
      icon: '🏃',
      category: 'exercise'
    }
  ]
  // 더 많은 카테고리와 계획들...
};

// 랜덤하게 계획들을 섞어서 반환하는 함수
const getRandomPlans = (category?: string, count: number = 5): Plan[] => {
  let allPlans: Plan[] = [];
  
  if (category && planDatabase[category]) {
    allPlans = [...planDatabase[category]];
  } else {
    // 모든 카테고리에서 계획 가져오기
    allPlans = Object.values(planDatabase).flat();
  }
  
  // 배열 섞기
  for (let i = allPlans.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [allPlans[i], allPlans[j]] = [allPlans[j], allPlans[i]];
  }
  
  return allPlans.slice(0, count);
};

export function InfiniteRecommendations({ 
  category, 
  onPlanClick, 
  onAddToRoutine,
  viewMode = 'grid',
  onViewModeChange 
}: InfiniteRecommendationsProps) {
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(0);
  const observer = useRef<IntersectionObserver>();

  // 마지막 카드 요소를 참조하는 콜백
  const lastPlanElementRef = useCallback((node: HTMLDivElement) => {
    if (loading) return;
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        loadMorePlans();
      }
    });
    if (node) observer.current.observe(node);
  }, [loading, hasMore]);

  // 더 많은 계획들을 로드하는 함수
  const loadMorePlans = useCallback(async () => {
    if (loading) return;
    
    setLoading(true);
    
    // 실제 API 호출을 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 800));
    
    const newPlans = getRandomPlans(category, 4);
    
    setPlans(prev => [...prev, ...newPlans]);
    setPage(prev => prev + 1);
    
    // 30개 이상이면 더 이상 로드하지 않음 (실제로는 서버에서 판단)
    if (plans.length + newPlans.length >= 30) {
      setHasMore(false);
    }
    
    setLoading(false);
  }, [category, loading, plans.length]);

  // 초기 계획들 로드 및 카테고리 변경 시 초기화
  useEffect(() => {
    setPlans([]);
    setPage(0);
    setHasMore(true);
    const initialPlans = getRandomPlans(category, 5);
    setPlans(initialPlans);
  }, [category]);

  // 새로고침 함수
  const refreshPlans = () => {
    setPlans([]);
    setPage(0);
    setHasMore(true);
    const newPlans = getRandomPlans(category, 5);
    setPlans(newPlans);
  };

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-medium">
            {category ? `${getCategoryName(category)} 추천` : '모든 추천'} ✨
          </h2>
          <p className="text-sm text-muted-foreground">
            스크롤하면 더 많은 계획이 나타납니다
          </p>
        </div>
        
        <div className="flex items-center gap-2">
          {/* 뷰 모드 토글 */}
          {onViewModeChange && (
            <div className="flex items-center border rounded-md">
              <Button
                variant={viewMode === 'grid' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => onViewModeChange('grid')}
                className="rounded-r-none"
              >
                <LayoutGrid className="w-4 h-4" />
              </Button>
              <Button
                variant={viewMode === 'deck' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => onViewModeChange('deck')}
                className="rounded-l-none"
              >
                <Grid3X3 className="w-4 h-4" />
              </Button>
            </div>
          )}
          
          {/* 새로고침 버튼 */}
          <Button
            variant="outline"
            size="sm"
            onClick={refreshPlans}
            className="gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            새로고침
          </Button>
        </div>
      </div>

      {/* 카드 그리드 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {plans.map((plan, index) => (
          <div
            key={`${plan.id}-${index}`}
            ref={index === plans.length - 1 ? lastPlanElementRef : null}
          >
            <EnhancedPlanCard
              plan={plan}
              onClick={() => onPlanClick(plan)}
              onAddToRoutine={onAddToRoutine ? () => onAddToRoutine(plan) : undefined}
            />
          </div>
        ))}
      </div>

      {/* 로딩 스켈레톤 */}
      {loading && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[...Array(4)].map((_, index) => (
            <Card key={index} className="animate-pulse">
              <CardHeader className="pb-3">
                <Skeleton className="h-5 w-3/4" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-2/3" />
              </CardHeader>
              <CardContent>
                <div className="flex gap-2 mb-3">
                  <Skeleton className="h-5 w-12" />
                  <Skeleton className="h-5 w-16" />
                  <Skeleton className="h-5 w-14" />
                </div>
                <Skeleton className="h-4 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* 더 이상 로드할 항목이 없을 때 */}
      {!hasMore && plans.length > 0 && (
        <div className="text-center py-8">
          <p className="text-muted-foreground">
            모든 추천을 확인했어요! 🎉
          </p>
          <Button
            variant="outline"
            onClick={refreshPlans}
            className="mt-4 gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            처음부터 다시 보기
          </Button>
        </div>
      )}
    </div>
  );
}

// 향상된 계획 카드 컴포넌트
function EnhancedPlanCard({ 
  plan, 
  onClick, 
  onAddToRoutine 
}: { 
  plan: Plan;
  onClick: () => void;
  onAddToRoutine?: () => void;
}) {
  const difficultyColors = {
    easy: 'bg-green-100 text-green-700',
    medium: 'bg-yellow-100 text-yellow-700',
    hard: 'bg-red-100 text-red-700'
  };

  const difficultyText = {
    easy: '쉬움',
    medium: '보통',
    hard: '어려움'
  };

  return (
    <Card className="hover:shadow-lg transition-all duration-300 cursor-pointer group relative overflow-hidden">
      {/* 배경 그라데이션 */}
      <div className="absolute inset-0 bg-gradient-to-br from-transparent to-muted/20 opacity-0 group-hover:opacity-100 transition-opacity" />
      
      <CardHeader className="pb-3 relative">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2">
            {plan.icon && (
              <span className="text-2xl">{plan.icon}</span>
            )}
            <CardTitle 
              className="group-hover:text-primary transition-colors cursor-pointer"
              onClick={onClick}
            >
              {plan.title}
            </CardTitle>
          </div>
          {plan.rating && (
            <div className="flex items-center gap-1 shrink-0">
              <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
              <span className="text-sm">{plan.rating}</span>
            </div>
          )}
        </div>
        <CardDescription className="mt-2 line-clamp-2">
          {plan.description}
        </CardDescription>
      </CardHeader>
      
      <CardContent className="relative">
        <div className="flex flex-wrap gap-1 mb-3">
          {plan.tags.slice(0, 3).map((tag) => (
            <Badge key={tag} variant="secondary" className="text-xs">
              #{tag}
            </Badge>
          ))}
          {plan.tags.length > 3 && (
            <Badge variant="secondary" className="text-xs">
              +{plan.tags.length - 3}
            </Badge>
          )}
        </div>
        
        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1">
              <Clock className="w-4 h-4 text-muted-foreground" />
              <span className="text-muted-foreground">{plan.duration}</span>
            </div>
            {plan.participants && (
              <div className="flex items-center gap-1">
                <Users className="w-4 h-4 text-muted-foreground" />
                <span className="text-muted-foreground">{plan.participants}</span>
              </div>
            )}
          </div>
          <Badge className={difficultyColors[plan.difficulty]}>
            {difficultyText[plan.difficulty]}
          </Badge>
        </div>

        {/* 액션 버튼들 */}
        <div className="flex gap-2 mt-4 opacity-0 group-hover:opacity-100 transition-opacity">
          <Button 
            size="sm" 
            onClick={onClick}
            className="flex-1"
          >
            자세히 보기
          </Button>
          {onAddToRoutine && (
            <Button 
              size="sm" 
              variant="outline"
              onClick={(e) => {
                e.stopPropagation();
                onAddToRoutine();
              }}
              className="gap-1"
            >
              <Plus className="w-3 h-3" />
              루틴
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
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