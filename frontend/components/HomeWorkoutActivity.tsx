import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { ArrowLeft, Play, Clock, CheckCircle2, Target } from 'lucide-react';

interface HomeWorkoutActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface WorkoutTarget {
  id: string;
  name: string;
  description: string;
  emoji: string;
  color: string;
  workouts: Workout[];
}

interface Workout {
  title: string;
  duration: string;
  level: string;
  youtubeQuery: string;
  description: string;
}

const workoutTargets: WorkoutTarget[] = [
  {
    id: 'upper-body',
    name: '상체 운동',
    description: '팔, 어깨, 가슴, 등 근력 강화',
    emoji: '💪',
    color: 'bg-red-100 text-red-700',
    workouts: [
      {
        title: '초보자 상체 홈트레이닝',
        duration: '15분',
        level: '초급',
        youtubeQuery: '초보자 상체 홈트 15분',
        description: '팔굽혀펴기, 플랭크 등 기본 동작'
      },
      {
        title: '덤벨 없는 상체 운동',
        duration: '20분',
        level: '중급',
        youtubeQuery: '덤벨 없는 상체 운동 20분',
        description: '맨몸으로 하는 효과적인 상체 운동'
      },
      {
        title: '강화된 상체 트레이닝',
        duration: '25분',
        level: '고급',
        youtubeQuery: '상체 근력 운동 25분',
        description: '다양한 변형 동작으로 근력 향상'
      }
    ]
  },
  {
    id: 'lower-body',
    name: '하체 운동',
    description: '다리, 엉덩이 근력 및 라인 만들기',
    emoji: '🦵',
    color: 'bg-blue-100 text-blue-700',
    workouts: [
      {
        title: '하체 라인 만들기',
        duration: '15분',
        level: '초급',
        youtubeQuery: '하체 라인 운동 15분',
        description: '스쿼트, 런지로 예쁜 하체 라인'
      },
      {
        title: '엉덩이 근력 강화',
        duration: '20분',
        level: '중급',
        youtubeQuery: '엉덩이 근력 운동 20분',
        description: '힙업을 위한 집중 운동'
      },
      {
        title: '하체 종합 트레이닝',
        duration: '30분',
        level: '고급',
        youtubeQuery: '하체 종합 운동 30분',
        description: '전체 하체 근력과 지구력 향상'
      }
    ]
  },
  {
    id: 'core',
    name: '코어 운동',
    description: '복부, 허리 코어 근력 강화',
    emoji: '🔥',
    color: 'bg-orange-100 text-orange-700',
    workouts: [
      {
        title: '뱃살 빼는 코어 운동',
        duration: '10분',
        level: '초급',
        youtubeQuery: '뱃살 빼는 운동 10분',
        description: '복부 지방 감소를 위한 기본 동작'
      },
      {
        title: '코어 근력 강화',
        duration: '15분',
        level: '중급',
        youtubeQuery: '코어 근력 운동 15분',
        description: '복근과 허리 근력 동시 강화'
      },
      {
        title: '고강도 복근 운동',
        duration: '20분',
        level: '고급',
        youtubeQuery: '고강도 복근 운동 20분',
        description: '강도 높은 복근 조각 만들기'
      }
    ]
  },
  {
    id: 'full-body',
    name: '전신 운동',
    description: '전체 근육을 고르게 사용하는 운동',
    emoji: '🏃',
    color: 'bg-green-100 text-green-700',
    workouts: [
      {
        title: '전신 순환 운동',
        duration: '20분',
        level: '초급',
        youtubeQuery: '전신 순환 운동 20분',
        description: '전체 근육을 가볍게 활성화'
      },
      {
        title: '전신 근력 운동',
        duration: '25분',
        level: '중급',
        youtubeQuery: '전신 근력 운동 25분',
        description: '상하체 모든 근육 균형 발달'
      },
      {
        title: '고강도 전신 트레이닝',
        duration: '30분',
        level: '고급',
        youtubeQuery: 'HIIT 전신 운동 30분',
        description: '체력과 근력 동시 향상'
      }
    ]
  }
];

export function HomeWorkoutActivity({ onBack, onComplete }: HomeWorkoutActivityProps) {
  const [selectedTarget, setSelectedTarget] = useState<WorkoutTarget | null>(null);
  const [currentStep, setCurrentStep] = useState<'select' | 'workout'>('select');

  const handleTargetSelect = (target: WorkoutTarget) => {
    setSelectedTarget(target);
    setCurrentStep('workout');
  };

  const handleBackToTargets = () => {
    setCurrentStep('select');
    setSelectedTarget(null);
  };

  const openWorkoutVideo = (workout: Workout) => {
    window.open(`https://www.youtube.com/results?search_query=${encodeURIComponent(workout.youtubeQuery)}`, '_blank');
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <Button 
          variant="ghost" 
          onClick={currentStep === 'select' ? onBack : handleBackToTargets} 
          className="gap-2 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">
          {currentStep === 'select' ? '홈트레이닝' : `${selectedTarget?.name}`}
        </h2>
        <p className="text-muted-foreground">
          {currentStep === 'select' 
            ? '집에서 할 수 있는 운동 부위를 선택해주세요' 
            : '운동 강도와 시간에 맞는 루틴을 선택하세요'}
        </p>
      </div>

      {currentStep === 'select' ? (
        <div className="grid gap-4 md:grid-cols-2">
          {workoutTargets.map((target) => (
            <Card 
              key={target.id}
              className="cursor-pointer hover:bg-muted/50 transition-all duration-200 hover:scale-105"
              onClick={() => handleTargetSelect(target)}
            >
              <CardContent className="p-6">
                <div className="text-center mb-4">
                  <div className="text-4xl mb-3">{target.emoji}</div>
                  <h3 className="font-medium mb-2">{target.name}</h3>
                  <p className="text-sm text-muted-foreground">
                    {target.description}
                  </p>
                </div>
                <div className={`text-center py-2 px-3 rounded-lg text-sm ${target.color}`}>
                  운동 시작하기
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : selectedTarget && (
        <div className="space-y-6">
          <Card>
            <CardHeader className="text-center">
              <div className="text-6xl mb-4">{selectedTarget.emoji}</div>
              <CardTitle>{selectedTarget.name}</CardTitle>
              <CardDescription>
                {selectedTarget.description}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <h4 className="font-medium mb-4 text-center">운동 루틴 선택</h4>
              <div className="space-y-4">
                {selectedTarget.workouts.map((workout, index) => (
                  <Card key={index} className="border-2 hover:border-primary/30 transition-colors">
                    <CardContent className="p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h5 className="font-medium">{workout.title}</h5>
                        <div className="flex gap-2">
                          <span className={`px-2 py-1 rounded text-xs ${
                            workout.level === '초급' ? 'bg-green-100 text-green-700' :
                            workout.level === '중급' ? 'bg-yellow-100 text-yellow-700' :
                            'bg-red-100 text-red-700'
                          }`}>
                            {workout.level}
                          </span>
                        </div>
                      </div>
                      
                      <p className="text-sm text-muted-foreground mb-3">
                        {workout.description}
                      </p>
                      
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Clock className="w-4 h-4" />
                            {workout.duration}
                          </span>
                          <span className="flex items-center gap-1">
                            <Target className="w-4 h-4" />
                            {workout.level}
                          </span>
                        </div>
                        <Button 
                          onClick={() => openWorkoutVideo(workout)}
                          className="gap-2"
                          size="sm"
                        >
                          <Play className="w-4 h-4" />
                          운동 시작
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              <div className="mt-8 p-4 bg-gradient-to-r from-primary/5 to-purple-500/5 rounded-lg border border-primary/10">
                <h4 className="font-medium mb-2">💡 운동 팁</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• 운동 전후 스트레칭을 꼭 해주세요</li>
                  <li>• 물을 충분히 준비해두세요</li>
                  <li>• 무리하지 말고 본인 페이스에 맞춰 진행하세요</li>
                  <li>• 운동 중 불편함이 있으면 즉시 중단하세요</li>
                </ul>
              </div>

              <div className="text-center mt-8">
                <Button 
                  onClick={onComplete}
                  className="gap-2 bg-green-600 hover:bg-green-700"
                  size="lg"
                >
                  <CheckCircle2 className="w-5 h-5" />
                  활동 완료하기! 🎉
                </Button>
                <p className="text-sm text-muted-foreground mt-3">
                  운동을 마쳤다면 완료해주세요!
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}