import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, MapPin, Clock, Star, Camera } from 'lucide-react';

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

const destinations: Record<string, TravelDestination[]> = {
  'healing-day-seoul': [
    {
      name: '북한산 둘레길',
      location: '서울 성북구',
      description: '도심 속에서 즐기는 자연 산책로, 스트레스 해소에 완벽',
      tags: ['자연', '걷기', '힐링'],
      duration: '3-4시간',
      difficulty: 'easy',
      highlights: ['북한산 자연 경관', '다양한 둘레길 코스', '도심 접근성'],
      season: '사계절',
      transport: '지하철 + 도보'
    },
    {
      name: '서울숲',
      location: '서울 성동구',
      description: '도심 속 대형 공원에서 피크닉과 산책을 즐겨보세요',
      tags: ['공원', '피크닉', '가족'],
      duration: '2-3시간',
      difficulty: 'easy',
      highlights: ['넓은 잔디밭', '곤충식물원', '한강 전망'],
      season: '봄, 가을 추천',
      transport: '지하철 + 도보'
    }
  ],
  'adventure-weekend-gangwon': [
    {
      name: '평창 알펜시아',
      location: '강원도 평창군',
      description: '사계절 리조트에서 다양한 액티비티를 즐겨보세요',
      tags: ['리조트', '액티비티', '겨울스포츠'],
      duration: '1박 2일',
      difficulty: 'medium',
      highlights: ['스키/스노보드', '슬라이딩센터', '자연 경관'],
      season: '겨울 추천',
      transport: 'KTX + 셔틀버스'
    }
  ],
  'culture-long-gyeongsang': [
    {
      name: '경주 역사문화지구',
      location: '경상북도 경주시',
      description: '천년 고도 경주에서 신라 문화를 체험해보세요',
      tags: ['역사', '문화재', '교육'],
      duration: '2박 3일',
      difficulty: 'easy',
      highlights: ['불국사', '석굴암', '첨성대', '안압지'],
      season: '사계절',
      transport: 'KTX + 버스'
    }
  ],
  'nature-day-chungcheong': [
    {
      name: '태안 안면도',
      location: '충청남도 태안군',
      description: '서해안의 아름다운 해변과 자연을 만끽하세요',
      tags: ['바다', '해변', '일몰'],
      duration: '당일치기',
      difficulty: 'easy',
      highlights: ['꽃지해수욕장', '할미할아버지바위', '일몰 명소'],
      season: '여름, 가을 추천',
      transport: '자차 또는 버스'
    }
  ]
};

export function DomesticTravelActivity({ onBack, onComplete }: DomesticTravelActivityProps) {
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