import { useState, useRef, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { 
  Clock, 
  Users, 
  Star, 
  Heart, 
  X, 
  RotateCcw,
  Plus,
  ChevronRight,
  ChevronLeft
} from "lucide-react";
import { motion, AnimatePresence, PanInfo } from "framer-motion";

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

interface CardDeckProps {
  plans: Plan[];
  onPlanSelect: (plan: Plan) => void;
  onPlanReject: (plan: Plan) => void;
  onAddToRoutine?: (plan: Plan) => void;
  onNeedMorePlans?: () => void;
}

export function CardDeck({ 
  plans, 
  onPlanSelect, 
  onPlanReject, 
  onAddToRoutine,
  onNeedMorePlans 
}: CardDeckProps) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [dragDirection, setDragDirection] = useState<'left' | 'right' | null>(null);
  const constraintsRef = useRef(null);

  const currentPlan = plans[currentIndex];
  const remainingCards = plans.length - currentIndex;

  // 다음 카드로 이동
  const nextCard = () => {
    if (currentIndex < plans.length - 1) {
      setCurrentIndex(prev => prev + 1);
    } else if (onNeedMorePlans) {
      onNeedMorePlans();
    }
  };

  // 이전 카드로 이동
  const prevCard = () => {
    if (currentIndex > 0) {
      setCurrentIndex(prev => prev - 1);
    }
  };

  // 카드 선택 (좋아요)
  const handleLike = () => {
    if (currentPlan) {
      onPlanSelect(currentPlan);
      nextCard();
    }
  };

  // 카드 거부 (싫어요)
  const handleDislike = () => {
    if (currentPlan) {
      onPlanReject(currentPlan);
      nextCard();
    }
  };

  // 루틴에 추가
  const handleAddToRoutine = () => {
    if (currentPlan && onAddToRoutine) {
      onAddToRoutine(currentPlan);
    }
  };

  // 드래그 핸들러
  const handleDragEnd = (event: any, info: PanInfo) => {
    const threshold = 150;
    
    if (info.offset.x > threshold) {
      // 오른쪽으로 스와이프 - 좋아요
      handleLike();
    } else if (info.offset.x < -threshold) {
      // 왼쪽으로 스와이프 - 싫어요
      handleDislike();
    }
    
    setDragDirection(null);
  };

  const handleDrag = (event: any, info: PanInfo) => {
    if (info.offset.x > 50) {
      setDragDirection('right');
    } else if (info.offset.x < -50) {
      setDragDirection('left');
    } else {
      setDragDirection(null);
    }
  };

  // 카드가 부족할 때 더 로드
  useEffect(() => {
    if (remainingCards <= 2 && onNeedMorePlans) {
      onNeedMorePlans();
    }
  }, [currentIndex, remainingCards, onNeedMorePlans]);

  if (!currentPlan) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <div className="text-6xl mb-4">🎉</div>
        <h3 className="text-xl font-medium mb-2">모든 카드를 확인했어요!</h3>
        <p className="text-muted-foreground mb-6">새로운 추천을 받아보세요</p>
        <Button onClick={() => setCurrentIndex(0)} className="gap-2">
          <RotateCcw className="w-4 h-4" />
          처음부터 다시 보기
        </Button>
      </div>
    );
  }

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
    <div className="relative max-w-md mx-auto">
      {/* 진행 표시기 */}
      <div className="flex items-center justify-between mb-6">
        <span className="text-sm text-muted-foreground">
          {currentIndex + 1} / {plans.length}
        </span>
        <div className="flex gap-1">
          {[...Array(Math.min(plans.length, 5))].map((_, index) => (
            <div
              key={index}
              className={`w-2 h-2 rounded-full transition-colors ${
                index <= currentIndex ? 'bg-primary' : 'bg-muted'
              }`}
            />
          ))}
        </div>
        <span className="text-sm text-muted-foreground">
          {remainingCards}개 남음
        </span>
      </div>

      {/* 카드 덱 영역 */}
      <div className="relative h-[500px] perspective-1000" ref={constraintsRef}>
        <AnimatePresence mode="wait">
          {/* 배경 카드들 (다음 카드 미리보기) */}
          {plans.slice(currentIndex + 1, currentIndex + 3).map((plan, index) => (
            <motion.div
              key={`bg-${plan.id}`}
              className="absolute inset-0"
              initial={{ scale: 0.95 - (index * 0.05), y: index * 5 }}
              animate={{ scale: 0.95 - (index * 0.05), y: index * 5 }}
              style={{ zIndex: -index - 1 }}
            >
              <Card className="h-full opacity-60">
                <CardHeader>
                  <CardTitle className="text-lg">{plan.title}</CardTitle>
                </CardHeader>
              </Card>
            </motion.div>
          ))}

          {/* 메인 카드 */}
          <motion.div
            key={currentPlan.id}
            className="absolute inset-0 cursor-grab active:cursor-grabbing"
            drag="x"
            dragConstraints={constraintsRef}
            dragElastic={0.2}
            onDrag={handleDrag}
            onDragEnd={handleDragEnd}
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ 
              scale: 1, 
              opacity: 1,
              rotate: dragDirection === 'right' ? 5 : dragDirection === 'left' ? -5 : 0
            }}
            exit={{ scale: 0.9, opacity: 0 }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            style={{ zIndex: 10 }}
          >
            <Card className={`h-full transition-all duration-200 ${
              dragDirection === 'right' 
                ? 'border-green-400 shadow-green-200 shadow-lg' 
                : dragDirection === 'left' 
                ? 'border-red-400 shadow-red-200 shadow-lg'
                : 'hover:shadow-lg'
            }`}>
              {/* 스와이프 힌트 */}
              {dragDirection && (
                <div className={`absolute top-4 right-4 p-2 rounded-full ${
                  dragDirection === 'right' 
                    ? 'bg-green-500 text-white' 
                    : 'bg-red-500 text-white'
                }`}>
                  {dragDirection === 'right' ? (
                    <Heart className="w-6 h-6" />
                  ) : (
                    <X className="w-6 h-6" />
                  )}
                </div>
              )}

              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    {currentPlan.icon && (
                      <span className="text-3xl">{currentPlan.icon}</span>
                    )}
                    <div>
                      <CardTitle className="text-xl">{currentPlan.title}</CardTitle>
                      {currentPlan.rating && (
                        <div className="flex items-center gap-1 mt-1">
                          <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                          <span className="text-sm">{currentPlan.rating}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="space-y-4">
                <CardDescription className="text-base leading-relaxed">
                  {currentPlan.description}
                </CardDescription>

                {/* 태그들 */}
                <div className="flex flex-wrap gap-2">
                  {currentPlan.tags.map((tag) => (
                    <Badge key={tag} variant="secondary" className="text-sm">
                      #{tag}
                    </Badge>
                  ))}
                </div>

                {/* 정보 */}
                <div className="flex items-center justify-between pt-2">
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">{currentPlan.duration}</span>
                    </div>
                    {currentPlan.participants && (
                      <div className="flex items-center gap-2">
                        <Users className="w-4 h-4 text-muted-foreground" />
                        <span className="text-sm text-muted-foreground">{currentPlan.participants}</span>
                      </div>
                    )}
                  </div>
                  <Badge className={difficultyColors[currentPlan.difficulty]}>
                    {difficultyText[currentPlan.difficulty]}
                  </Badge>
                </div>

                {/* 액션 버튼들 */}
                <div className="flex gap-3 pt-4">
                  {onAddToRoutine && (
                    <Button 
                      variant="outline" 
                      size="sm" 
                      onClick={handleAddToRoutine}
                      className="gap-2"
                    >
                      <Plus className="w-4 h-4" />
                      루틴 추가
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          </motion.div>
        </AnimatePresence>
      </div>

      {/* 하단 액션 버튼들 */}
      <div className="flex items-center justify-center gap-4 mt-6">
        <Button 
          variant="outline" 
          size="lg"
          onClick={handleDislike}
          className="gap-2 bg-red-50 hover:bg-red-100 border-red-200 text-red-700"
        >
          <X className="w-5 h-5" />
          Pass
        </Button>

        <Button 
          variant="outline" 
          size="sm"
          onClick={prevCard}
          disabled={currentIndex === 0}
          className="gap-1"
        >
          <ChevronLeft className="w-4 h-4" />
        </Button>

        <Button 
          variant="outline" 
          size="sm"
          onClick={nextCard}
          className="gap-1"
        >
          <ChevronRight className="w-4 h-4" />
        </Button>

        <Button 
          size="lg"
          onClick={handleLike}
          className="gap-2 bg-green-500 hover:bg-green-600 text-white"
        >
          <Heart className="w-5 h-5" />
          좋아요
        </Button>
      </div>

      {/* 스와이프 힌트 */}
      <div className="text-center mt-4">
        <p className="text-xs text-muted-foreground">
          ← 스와이프하여 넘기기 | 스와이프하여 선택하기 →
        </p>
      </div>
    </div>
  );
}