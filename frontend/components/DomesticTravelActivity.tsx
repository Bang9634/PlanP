import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, MapPin, Clock, Star, Camera } from 'lucide-react';
import { AuthService } from '../services/AuthService';
import { apiService } from '../services/api';

interface DomesticTravelActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface TravelDestination {
  name: string;
  location: string;
  description: string;
  tags: string[];
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  highlights: string[];
  season: string;
  transport: string;
}

const travelQuestions = [
  {
    id: 'mood',
    question: '어떤 기분의 여행을 원하시나요?',
    options: [
      { value: 'healing', label: '힐링/휴식', emoji: '🧘‍♀️' },
      { value: 'adventure', label: '모험/액티비티', emoji: '🎢' },
      { value: 'culture', label: '문화/역사', emoji: '🏛️' },
      { value: 'nature', label: '자연 감상', emoji: '🌿' }
    ]
  },
  {
    id: 'duration',
    question: '여행 기간은 어느 정도를 생각하시나요?',
    options: [
      { value: 'day', label: '당일치기', emoji: '☀️' },
      { value: 'weekend', label: '1박 2일', emoji: '🌙' },
      { value: 'long', label: '2박 3일 이상', emoji: '🗓️' }
    ]
  },
  {
    id: 'region',
    question: '어느 지역을 선호하시나요?',
    options: [
      { value: 'seoul', label: '서울/경기', emoji: '🏙️' },
      { value: 'gangwon', label: '강원도', emoji: '🏔️' },
      { value: 'jeju', label: '제주도', emoji: '🌊' },
      { value: 'gyeongsang', label: '경상도', emoji: '🏯' },
      { value: 'jeolla', label: '전라도', emoji: '🌾' },
      { value: 'chungcheong', label: '충청도', emoji: '🏞️' }
    ]
  }
];

export function DomesticTravelActivity({ onBack, onComplete }: DomesticTravelActivityProps) {
  const [isAuthChecking, setIsAuthChecking] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<TravelDestination[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = AuthService.isAuthenticated();
      setIsAuthenticated(authenticated);
      setIsAuthChecking(false);
      
      if (!authenticated) {
        console.warn('⚠️ 로그인이 필요한 기능입니다');
      }
    };

    checkAuth();
  }, []);


  const handleAnswer = (value: string) => {
    if (!isAuthenticated) {
      alert('🔒 로그인이 필요한 기능입니다');
      return;
    }
    const newAnswers = { ...answers, [travelQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < travelQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = (finalAnswers: Record<string, string>) => {
    const key = `${finalAnswers.mood}-${finalAnswers.duration}-${finalAnswers.region}`;
    const matchingDestinations = destinations[key] || [];
    
    // 만약 정확한 매치가 없다면 비슷한 옵션들을 찾음
    if (matchingDestinations.length === 0) {
      const allDestinations = Object.values(destinations).flat();
      const filtered = allDestinations.filter(dest => {
        return dest.tags.some(tag => 
          tag.includes(finalAnswers.mood) || 
          dest.duration.includes(finalAnswers.duration === 'day' ? '시간' : '박')
        );
      });
      setRecommendations(filtered.slice(0, 3));
    } else {
      setRecommendations(matchingDestinations);
    }
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
  };

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return 'bg-green-100 text-green-700';
      case 'medium': return 'bg-yellow-100 text-yellow-700';
      case 'hard': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
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
          <h2 className="mb-2">🇰🇷 맞춤 국내여행 추천</h2>
          <p className="text-muted-foreground">
            당신의 취향에 맞는 국내 여행지를 찾았어요!
          </p>
        </div>

        <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2 mb-8">
          {recommendations.map((destination, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <MapPin className="w-5 h-5 text-primary" />
                      {destination.name}
                    </CardTitle>
                    <CardDescription className="flex items-center gap-2 mt-1">
                      📍 {destination.location}
                    </CardDescription>
                  </div>
                  <Badge className={getDifficultyColor(destination.difficulty)}>
                    {destination.difficulty === 'easy' ? '쉬움' : 
                     destination.difficulty === 'medium' ? '보통' : '어려움'}
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
                    <span>{destination.transport}</span>
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
                      <Camera className="w-4 h-4" />
                      주요 볼거리
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
                      🌸 {destination.season}
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
        <h2 className="mb-2">🇰🇷 국내여행 추천</h2>
        <p className="text-muted-foreground">
          몇 가지 질문에 답해주시면 맞춤 여행지를 추천해드려요
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