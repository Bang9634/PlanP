import { useState } from "react";
import { Button } from "./ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Checkbox } from "./ui/checkbox";
import { Progress } from "./ui/progress";
import { 
  ArrowLeft, 
  Clock, 
  Users, 
  Star, 
  Calendar, 
  Bell, 
  CheckCircle2, 
  Play,
  ExternalLink,
  Trophy,
  Target,
  Lightbulb
} from "lucide-react";

interface PlanDetailProps {
  plan: {
    id: string;
    title: string;
    description: string;
    duration: string;
    difficulty: 'easy' | 'medium' | 'hard';
    tags: string[];
    participants?: string;
    rating?: number;
  };
  onBack: () => void;
  onComplete: (planId: string) => void;
}



export function PlanDetailPage({ plan, onBack, onComplete }: PlanDetailProps) {
  const [showScheduleOptions, setShowScheduleOptions] = useState(false);
  const [selectedTime, setSelectedTime] = useState<string>('');

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return 'bg-green-100 text-green-700';
      case 'medium': return 'bg-yellow-100 text-yellow-700';
      case 'hard': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getDifficultyText = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return '쉬움';
      case 'medium': return '보통';
      case 'hard': return '어려움';
      default: return '보통';
    }
  };

  const getDetailedGuide = (planTitle: string) => {
    // 계획별 상세 가이드 (실제로는 DB에서 가져올 데이터)
    const guides: Record<string, string[]> = {
      '새로운 플레이리스트 만들기': [
        '1. 스포티파이나 유튜브 뮤직 앱을 열어주세요',
        '2. "새 플레이리스트 만들기" 버튼을 찾아 클릭하세요',
        '3. 플레이리스트 이름을 정해주세요 (예: "오늘의 기분", "집중할 때 듣는 음악")',
        '4. 좋아하는 곡들을 하나씩 추가해보세요 (최소 10곡 추천)',
        '5. 친구들과 공유하거나 혼자만의 비밀 플레이리스트로 설정하세요'
      ],
      '한강 피크닉': [
        '1. 날씨를 확인하고 맑은 날을 선택하세요',
        '2. 피크닉 용품을 준비하세요: 돗자리, 음식, 물',
        '3. 한강공원 중 접근성이 좋은 곳을 선택하세요 (여의도, 반포, 뚝섬)',
        '4. 치킨과 맥주를 미리 주문하거나 현장에서 구매하세요',
        '5. 친구들과 함께 여유로운 시간을 보내세요'
      ],
      '15분 아침 루틴': [
        '1. 알람을 평소보다 15분 일찍 맞춰주세요',
        '2. 일어나자마자 침대에서 간단한 스트레칭 (3분)',
        '3. 세면 후 미지근한 물 한 잔 마시기 (2분)',
        '4. 오늘 할 일 3가지를 종이에 적어보기 (5분)',  
        '5. 긍정적인 마음가짐으로 하루 시작하기 (5분)'
      ]
    };

    return guides[planTitle] || [
      '1. 이 활동에 대해 더 자세히 알아보세요',
      '2. 필요한 준비물이나 조건을 확인하세요',
      '3. 적절한 시간과 장소를 정하세요',
      '4. 계획을 실행에 옮겨보세요',
      '5. 경험을 기록하고 다음에 더 잘할 방법을 생각해보세요'
    ];
  };

  const getRelatedLinks = (planTitle: string) => {
    const links: Record<string, Array<{name: string, url: string}>> = {
      '새로운 플레이리스트 만들기': [
        { name: '스포티파이 웹플레이어', url: 'https://open.spotify.com' },
        { name: '유튜브 뮤직', url: 'https://music.youtube.com' },
        { name: '플레이리스트 만들기 팁', url: '#' }
      ],
      '한강 피크닉': [
        { name: '한강공원 안내', url: 'https://hangang.seoul.go.kr' },
        { name: '날씨 확인', url: 'https://weather.naver.com' },
        { name: '치킨 배달 앱', url: '#' }
      ]
    };

    return links[planTitle] || [];
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <div className="border-b bg-card">
        <div className="container mx-auto px-4 py-4">
          <Button 
            variant="ghost" 
            onClick={onBack}
            className="gap-2 mb-4"
          >
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h1 className="mb-2">{plan.title}</h1>
              <p className="text-muted-foreground mb-4">
                {plan.description}
              </p>
              
              <div className="flex flex-wrap gap-2 mb-4">
                <Badge variant="outline" className="gap-1">
                  <Clock className="w-3 h-3" />
                  {plan.duration}
                </Badge>
                <Badge variant="outline" className={getDifficultyColor(plan.difficulty)}>
                  <Target className="w-3 h-3" />
                  {getDifficultyText(plan.difficulty)}
                </Badge>
                {plan.participants && (
                  <Badge variant="outline" className="gap-1">
                    <Users className="w-3 h-3" />
                    {plan.participants}
                  </Badge>
                )}
                {plan.rating && (
                  <Badge variant="outline" className="gap-1">
                    <Star className="w-3 h-3" />
                    {plan.rating}
                  </Badge>
                )}
              </div>

              <div className="flex flex-wrap gap-1">
                {plan.tags.map((tag, index) => (
                  <Badge key={index} variant="secondary" className="text-xs">
                    #{tag}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid gap-8 lg:grid-cols-3">
          {/* 왼쪽: 실행 가이드 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 구체적 실행 가이드 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Lightbulb className="w-5 h-5" />
                  실행 가이드
                </CardTitle>
                <CardDescription>
                  단계별로 따라하면 쉽게 완성할 수 있어요
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {getDetailedGuide(plan.title).map((step, index) => (
                    <div key={index} className="flex gap-3 p-3 rounded-lg bg-muted/50">
                      <div className="flex items-center justify-center w-6 h-6 rounded-full bg-primary text-primary-foreground text-sm flex-shrink-0">
                        {index + 1}
                      </div>
                      <p className="text-sm">{step}</p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* 관련 링크 및 자료 */}
            {getRelatedLinks(plan.title).length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <ExternalLink className="w-5 h-5" />
                    관련 링크 & 자료
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-2">
                    {getRelatedLinks(plan.title).map((link, index) => (
                      <Button 
                        key={index} 
                        variant="outline" 
                        className="justify-start gap-2"
                        onClick={() => window.open(link.url, '_blank')}
                      >
                        <ExternalLink className="w-4 h-4" />
                        {link.name}
                      </Button>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          {/* 오른쪽: 일정화 & 체크리스트 */}
          <div className="space-y-6">
            {/* 일정화 버튼 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="w-5 h-5" />
                  일정 설정
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button 
                  className="w-full gap-2"
                  onClick={() => alert('지금 바로 시작하세요! 🚀')}
                >
                  <Play className="w-4 h-4" />
                  지금 바로 하기
                </Button>
                
                <Button 
                  variant="outline" 
                  className="w-full gap-2"
                  onClick={() => setShowScheduleOptions(!showScheduleOptions)}
                >
                  <Bell className="w-4 h-4" />
                  특정 시간 알림 받기
                </Button>

                {showScheduleOptions && (
                  <div className="space-y-2 p-3 border rounded-lg">
                    <label className="text-sm">알림 시간 선택</label>
                    <input 
                      type="datetime-local" 
                      className="w-full p-2 border rounded text-sm"
                      value={selectedTime}
                      onChange={(e) => setSelectedTime(e.target.value)}
                    />
                    <Button 
                      size="sm" 
                      className="w-full"
                      onClick={() => alert(`${selectedTime}에 알림이 설정되었습니다! 📱`)}
                    >
                      알림 설정하기
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* 활동 완료 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Trophy className="w-5 h-5" />
                  활동 완료
                </CardTitle>
                <CardDescription>
                  이 활동을 완료하셨나요?
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Button 
                  className="w-full gap-2 bg-green-600 hover:bg-green-700"
                  onClick={() => onComplete(plan.id)}
                >
                  <CheckCircle2 className="w-4 h-4" />
                  활동 완료하기! 🎉
                </Button>
                <p className="text-xs text-muted-foreground text-center mt-3">
                  완료하시면 포인트를 획득하고 성취 기록에 저장됩니다
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}