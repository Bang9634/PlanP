import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, Film, Star, Clock, Users } from 'lucide-react';

interface MovieRecommendationActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface Movie {
  title: string;
  genre: string;
  year: string;
  rating: number;
  runtime: string;
  description: string;
  director: string;
  cast: string[];
  reason: string;
  mood: string;
  platform: string;
}

const movieQuestions = [
  {
    id: 'mood',
    question: '지금 어떤 기분이신가요?',
    options: [
      { value: 'fun', label: '재미있고 신나는', emoji: '😄' },
      { value: 'emotional', label: '감동적이고 눈물나는', emoji: '😭' },
      { value: 'thrilling', label: '긴장감 넘치는', emoji: '😱' },
      { value: 'romantic', label: '달콤하고 로맨틱한', emoji: '💕' },
      { value: 'thoughtful', label: '생각해볼만한', emoji: '🤔' }
    ]
  },
  {
    id: 'genre',
    question: '어떤 장르를 선호하시나요?',
    options: [
      { value: 'comedy', label: '코미디', emoji: '😂' },
      { value: 'drama', label: '드라마', emoji: '🎭' },
      { value: 'action', label: '액션/어드벤처', emoji: '💥' },
      { value: 'horror', label: '공포/스릴러', emoji: '👻' },
      { value: 'romance', label: '로맨스', emoji: '💖' },
      { value: 'scifi', label: 'SF/판타지', emoji: '🚀' }
    ]
  },
  {
    id: 'time',
    question: '시청할 시간은 얼마나 있나요?',
    options: [
      { value: 'short', label: '1시간 반 이내', emoji: '⏰' },
      { value: 'medium', label: '2시간 정도', emoji: '🕐' },
      { value: 'long', label: '시간 상관없어요', emoji: '🎬' }
    ]
  }
];

const movies: Record<string, Movie[]> = {
  'fun-comedy-medium': [
    {
      title: '극한직업',
      genre: '코미디',
      year: '2019',
      rating: 9.2,
      runtime: '111분',
      description: '마약 수사 중 치킨집을 운영하게 된 형사들의 좌충우돌 코미디',
      director: '이병헌',
      cast: ['류승룡', '이하늬', '진선규', '이동휘'],
      reason: '한국형 코미디의 정수를 보여주는 웃음 보장 영화',
      mood: '유쾌한',
      platform: '넷플릭스, 웨이브'
    }
  ],
  'emotional-drama-long': [
    {
      title: '기생충',
      genre: '드라마',
      year: '2019',
      rating: 9.5,
      runtime: '132분',
      description: '계층 간의 갈등을 그린 봉준호 감독의 아카데미 수상작',
      director: '봉준호',
      cast: ['송강호', '이선균', '조여정', '최우식'],
      reason: '사회적 메시지와 완벽한 연출이 어우러진 걸작',
      mood: '생각하게 하는',
      platform: '넷플릭스'
    }
  ],
  'thrilling-action-medium': [
    {
      title: '존 윅',
      genre: '액션',
      year: '2014',
      rating: 8.7,
      runtime: '101분',
      description: '개를 죽인 자들에게 복수하는 전설적인 킬러의 이야기',
      director: '채드 스타헬스키',
      cast: ['키아누 리브스', '윌렘 데포'],
      reason: '세련된 액션과 간결한 스토리의 완벽한 조화',
      mood: '스타일리시한',
      platform: '넷플릭스, 왓챠'
    }
  ],
  'romantic-romance-medium': [
    {
      title: '건축학개론',
      genre: '로맨스',
      year: '2012',
      rating: 8.8,
      runtime: '118분',
      description: '첫사랑의 추억을 그린 감성적인 로맨스 영화',
      director: '이용주',
      cast: ['엄태웅', '한가인', '이제훈', '수지'],
      reason: '누구나 가진 첫사랑의 추억을 아름답게 그린 작품',
      mood: '감성적인',
      platform: '왓챠, 티빙'
    }
  ]
};

export function MovieRecommendationActivity({ onBack, onComplete }: MovieRecommendationActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<Movie[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [movieQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < movieQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = async (finalAnswers: Record<string, string>) => {
    setIsGenerating(true);
    
    // AI 추천 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    const key = `${finalAnswers.mood}-${finalAnswers.genre}-${finalAnswers.time}`;
    const matchingMovies = movies[key] || [];
    
    if (matchingMovies.length === 0) {
      // 기본 추천 제공
      const allMovies = Object.values(movies).flat();
      setRecommendations(allMovies.slice(0, 2));
    } else {
      setRecommendations(matchingMovies);
    }
    
    setIsGenerating(false);
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
  };

  const getMoodColor = (mood: string) => {
    const colors: Record<string, string> = {
      '유쾌한': 'bg-yellow-100 text-yellow-700',
      '생각하게 하는': 'bg-purple-100 text-purple-700',
      '스타일리시한': 'bg-gray-100 text-gray-700',
      '감성적인': 'bg-pink-100 text-pink-700'
    };
    return colors[mood] || 'bg-blue-100 text-blue-700';
  };

  if (isGenerating) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="mb-8">
          <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <h2 className="mb-2">🎬 영화 추천</h2>
        </div>

        <Card>
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="animate-pulse text-6xl mb-4">🎭</div>
              <h3>당신에게 완벽한 영화를 찾고 있어요</h3>
              <p className="text-muted-foreground">
                수많은 영화 중에서 지금 당신의 기분에 딱 맞는 작품을 골라내고 있습니다...
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (recommendations.length > 0) {
    return (
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <h2 className="mb-2">🎬 맞춤 영화 추천</h2>
          <p className="text-muted-foreground">
            당신의 취향에 맞는 완벽한 영화를 찾았어요!
          </p>
        </div>

        <div className="space-y-6 mb-8">
          {recommendations.map((movie, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Film className="w-5 h-5 text-primary" />
                      {movie.title}
                    </CardTitle>
                    <CardDescription className="mt-2">
                      {movie.description}
                    </CardDescription>
                  </div>
                  <Badge className={getMoodColor(movie.mood)}>
                    {movie.mood}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* 기본 정보 */}
                  <div className="flex items-center gap-4 text-sm">
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span>{movie.rating}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="w-4 h-4" />
                      <span>{movie.runtime}</span>
                    </div>
                    <Badge variant="outline">{movie.genre}</Badge>
                    <span className="text-muted-foreground">{movie.year}</span>
                  </div>

                  {/* 출연진 */}
                  <div>
                    <h4 className="font-medium text-sm mb-2">🎭 감독 & 출연진</h4>
                    <p className="text-sm text-muted-foreground">
                      <strong>감독:</strong> {movie.director}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      <strong>출연:</strong> {movie.cast.join(', ')}
                    </p>
                  </div>

                  {/* 추천 이유 */}
                  <div className="bg-muted/50 rounded-lg p-3">
                    <h4 className="font-medium text-sm mb-2 flex items-center gap-1">
                      💡 추천 이유
                    </h4>
                    <p className="text-sm text-muted-foreground">{movie.reason}</p>
                  </div>

                  {/* 시청 플랫폼 */}
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-medium text-sm mb-1">📺 시청 가능한 곳</h4>
                      <p className="text-sm text-muted-foreground">{movie.platform}</p>
                    </div>
                    <Button size="sm" className="gap-1">
                      <Film className="w-4 h-4" />
                      지금 보기
                    </Button>
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
              🔄 다른 영화 추천받기
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
        <h2 className="mb-2">🎬 영화/드라마 추천</h2>
        <p className="text-muted-foreground">
          당신의 기분에 맞는 완벽한 작품을 추천해드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-primary h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / movieQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{movieQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center">
            {movieQuestions[currentStep].question}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {movieQuestions[currentStep].options.map((option) => (
              <Button
                key={option.value}
                variant="outline"
                onClick={() => handleAnswer(option.value)}
                className="h-auto p-4 flex flex-col items-center gap-2 hover:bg-primary/5"
              >
                <span className="text-2xl">{option.emoji}</span>
                <span className="text-center">{option.label}</span>
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}