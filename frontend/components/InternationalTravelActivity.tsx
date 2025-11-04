import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, MapPin, Clock, Star, Plane, DollarSign } from 'lucide-react';

interface InternationalTravelActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface TravelDestination {
  name: string;
  country: string;
  description: string;
  tags: string[];
  duration: string;
  budget: 'low' | 'medium' | 'high';
  highlights: string[];
  bestSeason: string;
  difficulty: string;
  currency: string;
  timeZone: string;
}

const travelQuestions = [
  {
    id: 'budget',
    question: '여행 예산은 어느 정도 생각하고 계신가요?',
    options: [
      { value: 'low', label: '100만원 이하', emoji: '💰' },
      { value: 'medium', label: '100-300만원', emoji: '💳' },
      { value: 'high', label: '300만원 이상', emoji: '💎' }
    ]
  },
  {
    id: 'style',
    question: '어떤 스타일의 여행을 선호하시나요?',
    options: [
      { value: 'culture', label: '문화/역사 탐방', emoji: '🏛️' },
      { value: 'nature', label: '자연/경치 감상', emoji: '🏔️' },
      { value: 'city', label: '도시 탐험', emoji: '🏙️' },
      { value: 'beach', label: '휴양/바다', emoji: '🏖️' },
      { value: 'food', label: '음식 탐방', emoji: '🍜' }
    ]
  },
  {
    id: 'distance',
    question: '비행시간은 얼마나 괜찮으신가요?',
    options: [
      { value: 'near', label: '3시간 이내 (동아시아)', emoji: '✈️' },
      { value: 'medium', label: '6시간 이내 (동남아시아)', emoji: '🛫' },
      { value: 'far', label: '10시간 이상도 OK', emoji: '🌏' }
    ]
  }
];

const destinations: Record<string, TravelDestination[]> = {
  'low-culture-near': [
    {
      name: '교토',
      country: '일본',
      description: '천년 고도의 전통문화와 아름다운 사찰들을 만나보세요',
      tags: ['전통문화', '사찰', '정원', '음식'],
      duration: '3-4일',
      budget: 'low',
      highlights: ['후시미 이나리 신사', '기요미즈데라', '아라시야마 대나무숲', '기온 거리'],
      bestSeason: '봄, 가을',
      difficulty: '쉬움',
      currency: '엔(JPY)',
      timeZone: '+0시간'
    }
  ],
  'medium-nature-medium': [
    {
      name: '바다 나우',
      country: '베트남',
      description: '아름다운 해변과 자연경관이 어우러진 휴양지',
      tags: ['해변', '자연', '휴양', '액티비티'],
      duration: '4-5일',
      budget: 'medium',
      highlights: ['미케 해변', '골든브릿지', '바나힐', '호이안 구시가지'],
      bestSeason: '2-8월',
      difficulty: '보통',
      currency: '동(VND)',
      timeZone: '-2시간'
    }
  ],
  'high-city-far': [
    {
      name: '뉴욕',
      country: '미국',
      description: '세계 최고의 메트로폴리탄에서 도시의 매력을 만끽하세요',
      tags: ['도시', '문화', '쇼핑', '뮤지컬'],
      duration: '5-7일',
      budget: 'high',
      highlights: ['타임스스퀘어', '센트럴파크', '자유의 여신상', '브로드웨이'],
      bestSeason: '4-6월, 9-11월',
      difficulty: '보통',
      currency: '달러(USD)',
      timeZone: '-14시간'
    }
  ],
  'low-food-near': [
    {
      name: '타이베이',
      country: '대만',
      description: '야시장과 다양한 먹거리로 유명한 미식의 천국',
      tags: ['음식', '야시장', '문화', '쇼핑'],
      duration: '3-4일',
      budget: 'low',
      highlights: ['시린 야시장', '지우펀', '용산사', '타이베이 101'],
      bestSeason: '10-4월',
      difficulty: '쉬움',
      currency: '대만달러(TWD)',
      timeZone: '-1시간'
    }
  ],
  'medium-beach-medium': [
    {
      name: '보라카이',
      country: '필리핀',
      description: '세계적으로 유명한 화이트 비치에서 완벽한 휴양을 즐기세요',
      tags: ['해변', '휴양', '액티비티', '일몰'],
      duration: '4-5일',
      budget: 'medium',
      highlights: ['화이트 비치', '윌리스 록', '아일랜드 호핑', '선셋 세일링'],
      bestSeason: '11-5월',
      difficulty: '쉬움',
      currency: '페소(PHP)',
      timeZone: '-1시간'
    }
  ]
};

export function InternationalTravelActivity({ onBack, onComplete }: InternationalTravelActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<TravelDestination[]>([]);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [travelQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < travelQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = (finalAnswers: Record<string, string>) => {
    const key = `${finalAnswers.budget}-${finalAnswers.style}-${finalAnswers.distance}`;
    const matchingDestinations = destinations[key] || [];
    
    // 만약 정확한 매치가 없다면 비슷한 옵션들을 찾음
    if (matchingDestinations.length === 0) {
      const allDestinations = Object.values(destinations).flat();
      const filtered = allDestinations.filter(dest => {
        return dest.budget === finalAnswers.budget || 
               dest.tags.some(tag => tag.includes(finalAnswers.style));
      });
      setRecommendations(filtered.slice(0, 3));
    } else {
      setRecommendations(matchingDestinations);
    }
    
    // 만약 여전히 없다면 기본 추천 제공
    if (matchingDestinations.length === 0 && recommendations.length === 0) {
      const defaultRecommendations = Object.values(destinations).flat().slice(0, 2);
      setRecommendations(defaultRecommendations);
    }
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
  };

  const getBudgetColor = (budget: string) => {
    switch (budget) {
      case 'low': return 'bg-green-100 text-green-700';
      case 'medium': return 'bg-yellow-100 text-yellow-700';
      case 'high': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getBudgetText = (budget: string) => {
    switch (budget) {
      case 'low': return '저예산';
      case 'medium': return '중예산';
      case 'high': return '고예산';
      default: return budget;
    }
  };

  if (recommendations.length > 0) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <h2 className="mb-2">🌍 맞춤 해외여행 추천</h2>
          <p className="text-muted-foreground">
            당신의 여행 스타일에 맞는 해외 여행지를 찾았어요!
          </p>
        </div>

        <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2 mb-8">
          {recommendations.map((destination, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Plane className="w-5 h-5 text-primary" />
                      {destination.name}
                    </CardTitle>
                    <CardDescription className="flex items-center gap-2 mt-1">
                      🌍 {destination.country}
                    </CardDescription>
                  </div>
                  <Badge className={getBudgetColor(destination.budget)}>
                    {getBudgetText(destination.budget)}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground mb-4">
                  {destination.description}
                </p>
                
                <div className="space-y-3">
                  <div className="flex items-center gap-2 text-sm">
                    <Clock className="w-4 h-4" />
                    <span>{destination.duration}</span>
                    <span className="text-muted-foreground">|</span>
                    <span>{destination.difficulty}</span>
                  </div>
                  
                  <div className="flex items-center gap-4 text-sm text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <DollarSign className="w-4 h-4" />
                      <span>{destination.currency}</span>
                    </div>
                    <div>
                      🕐 {destination.timeZone}
                    </div>
                  </div>
                  
                  <div className="flex flex-wrap gap-1">
                    {destination.tags.map((tag, tagIndex) => (
                      <Badge key={tagIndex} variant="secondary" className="text-xs">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                  
                  <div className="bg-muted/50 rounded-lg p-3">
                    <h4 className="font-medium text-sm mb-2 flex items-center gap-1">
                      <MapPin className="w-4 h-4" />
                      주요 관광지
                    </h4>
                    <ul className="text-sm text-muted-foreground space-y-1">
                      {destination.highlights.map((highlight, hIndex) => (
                        <li key={hIndex} className="flex items-center gap-2">
                          <span className="w-1 h-1 bg-primary rounded-full"></span>
                          {highlight}
                        </li>
                      ))}
                    </ul>
                  </div>
                  
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">
                      🌸 여행 적기: {destination.bestSeason}
                    </span>
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span className="text-muted-foreground">추천</span>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="text-center space-y-4">
          <Button onClick={onComplete} size="lg" className="gap-2">
            ✅ 활동 완료하기!
          </Button>
          <div>
            <Button variant="outline" onClick={resetFlow} className="gap-2">
              🔄 다른 조건으로 다시 찾기
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // 질문 단계
  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">🌍 해외여행 추천</h2>
        <p className="text-muted-foreground">
          몇 가지 질문에 답해주시면 맞춤 해외 여행지를 추천해드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-primary h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / travelQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{travelQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center">
            {travelQuestions[currentStep].question}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {travelQuestions[currentStep].options.map((option) => (
              <Button
                key={option.value}
                variant="outline"
                onClick={() => handleAnswer(option.value)}
                className="h-auto p-4 flex flex-col items-center gap-2 hover:bg-primary/5"
              >
                <span className="text-2xl">{option.emoji}</span>
                <span>{option.label}</span>
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}