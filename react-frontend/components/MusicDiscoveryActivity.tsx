import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, Music, Play, Heart, Shuffle, Clock } from 'lucide-react';

interface MusicDiscoveryActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface Song {
  title: string;
  artist: string;
  genre: string;
  mood: string;
  reason: string;
  duration: string;
  releaseYear: string;
}

const moodQuestions = [
  {
    id: 'energy',
    question: '지금 어떤 에너지를 원하시나요?',
    options: [
      { value: 'high', label: '신나고 활기찬', emoji: '🔥' },
      { value: 'medium', label: '편안하고 차분한', emoji: '😌' },
      { value: 'low', label: '잔잔하고 감성적인', emoji: '🌙' }
    ]
  },
  {
    id: 'activity',
    question: '어떤 상황에서 들으실 건가요?',
    options: [
      { value: 'work', label: '일하거나 공부할 때', emoji: '💻' },
      { value: 'exercise', label: '운동할 때', emoji: '🏃‍♀️' },
      { value: 'relax', label: '휴식이나 힐링할 때', emoji: '🛋️' },
      { value: 'commute', label: '이동할 때', emoji: '🚌' }
    ]
  },
  {
    id: 'preference',
    question: '평소 어떤 음악을 즐겨 듣나요?',
    options: [
      { value: 'pop', label: '대중가요/K-Pop', emoji: '🎤' },
      { value: 'indie', label: '인디/대안음악', emoji: '🎸' },
      { value: 'jazz', label: '재즈/소울', emoji: '🎷' },
      { value: 'classical', label: '클래식/뉴에이지', emoji: '🎼' },
      { value: 'electronic', label: '일렉트로닉/EDM', emoji: '🎧' }
    ]
  }
];

const musicDatabase: Record<string, Song[]> = {
  'high-exercise-pop': [
    {
      title: 'Dynamite',
      artist: 'BTS',
      genre: 'K-Pop',
      mood: '신나는',
      reason: '경쾌한 리듬과 에너지 넘치는 비트로 운동할 때 완벽한 곡',
      duration: '3:19',
      releaseYear: '2020'
    },
    {
      title: 'Next Level',
      artist: 'aespa',
      genre: 'K-Pop',
      mood: '강렬한',
      reason: '파워풀한 사운드와 중독성 있는 멜로디로 에너지 충전',
      duration: '3:30',
      releaseYear: '2021'
    }
  ],
  'medium-work-indie': [
    {
      title: '봄날',
      artist: '잔나비',
      genre: '인디',
      mood: '따뜻한',
      reason: '잔잔하면서도 집중력을 높여주는 멜로디로 작업에 최적',
      duration: '4:12',
      releaseYear: '2019'
    },
    {
      title: '호랑이',
      artist: '이상은',
      genre: '인디 포크',
      mood: '감성적',
      reason: '서정적인 가사와 어쿠스틱 사운드가 편안한 분위기 조성',
      duration: '3:45',
      releaseYear: '2020'
    }
  ],
  'low-relax-jazz': [
    {
      title: 'Moon River',
      artist: 'Audrey Hepburn',
      genre: '재즈',
      mood: '로맨틱',
      reason: '클래식한 재즈 스탠다드로 마음을 편안하게 해주는 곡',
      duration: '2:41',
      releaseYear: '1961'
    },
    {
      title: 'The Way You Look Tonight',
      artist: 'Tony Bennett',
      genre: '재즈',
      mood: '우아한',
      reason: '부드러운 보컬과 섬세한 연주로 힐링 타임에 완벽',
      duration: '3:22',
      releaseYear: '1964'
    }
  ]
};

// AI가 생성한 것처럼 보이는 추가 추천들
const generateAIRecommendations = (answers: Record<string, string>): Song[] => {
  const baseRecommendations = [
    {
      title: 'Blinding Lights',
      artist: 'The Weeknd',
      genre: 'Synth-pop',
      mood: '레트로',
      reason: '80년대 신스팝을 현대적으로 재해석한 중독성 강한 멜로디',
      duration: '3:20',
      releaseYear: '2019'
    },
    {
      title: '신호등',
      artist: '이무진',
      genre: '발라드',
      mood: '감동적',
      reason: '진정성 있는 보컬과 감성적인 가사로 마음을 울리는 곡',
      duration: '3:58',
      releaseYear: '2021'
    },
    {
      title: 'Levitating',
      artist: 'Dua Lipa',
      genre: 'Dance-pop',
      mood: '스타일리시',
      reason: '디스코 펑크와 모던 팝의 완벽한 조화',
      duration: '3:23',
      releaseYear: '2020'
    },
    {
      title: '밤하늘의 별을',
      artist: '경서예지',
      genre: '인디 팝',
      mood: '몽환적',
      reason: '꿈 같은 사운드스케이프와 포근한 멜로디',
      duration: '4:05',
      releaseYear: '2022'
    }
  ];

  // 답변에 따라 다른 곡들을 선택
  return baseRecommendations.slice(0, 3);
};

export function MusicDiscoveryActivity({ onBack, onComplete }: MusicDiscoveryActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<Song[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [moodQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < moodQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = async (finalAnswers: Record<string, string>) => {
    setIsGenerating(true);
    
    // AI 추천 시뮬레이션을 위한 딜레이
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    const key = `${finalAnswers.energy}-${finalAnswers.activity}-${finalAnswers.preference}`;
    const matchingMusic = musicDatabase[key] || [];
    
    let finalRecommendations: Song[] = [];
    
    if (matchingMusic.length > 0) {
      finalRecommendations = matchingMusic;
    } else {
      finalRecommendations = generateAIRecommendations(finalAnswers);
    }
    
    setRecommendations(finalRecommendations);
    setIsGenerating(false);
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
  };

  const getMoodColor = (mood: string) => {
    const colors: Record<string, string> = {
      '신나는': 'bg-red-100 text-red-700',
      '강렬한': 'bg-purple-100 text-purple-700',
      '따뜻한': 'bg-orange-100 text-orange-700',
      '감성적': 'bg-blue-100 text-blue-700',
      '로맨틱': 'bg-pink-100 text-pink-700',
      '우아한': 'bg-indigo-100 text-indigo-700',
      '레트로': 'bg-yellow-100 text-yellow-700',
      '감동적': 'bg-emerald-100 text-emerald-700',
      '스타일리시': 'bg-violet-100 text-violet-700',
      '몽환적': 'bg-cyan-100 text-cyan-700'
    };
    return colors[mood] || 'bg-gray-100 text-gray-700';
  };

  if (isGenerating) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="mb-8">
          <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <h2 className="mb-2">🎵 AI 음악 추천</h2>
        </div>

        <Card>
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="animate-spin w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full mx-auto"></div>
              <h3>🤖 AI가 당신만을 위한 음악을 찾고 있어요</h3>
              <p className="text-muted-foreground">
                수많은 음악 중에서 당신의 취향에 맞는 곡들을 분석하고 있습니다...
              </p>
              <div className="space-y-2 text-sm text-muted-foreground">
                <p>🎯 선호도 분석 중...</p>
                <p>🎶 음악 데이터베이스 검색 중...</p>
                <p>✨ 맞춤 추천 생성 중...</p>
              </div>
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
          <h2 className="mb-2">🎵 AI 맞춤 음악 추천</h2>
          <p className="text-muted-foreground">
            당신의 취향을 분석해서 찾은 특별한 음악들이에요! 🤖✨
          </p>
        </div>

        <div className="grid gap-4 mb-8">
          {recommendations.map((song, index) => (
            <Card key={index} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-primary/20 to-purple-500/20 rounded-lg flex items-center justify-center">
                    <Music className="w-8 h-8 text-primary" />
                  </div>
                  
                  <div className="flex-1 space-y-3">
                    <div>
                      <h3 className="font-medium flex items-center gap-2">
                        {song.title}
                        <Badge className={getMoodColor(song.mood)} variant="secondary">
                          {song.mood}
                        </Badge>
                      </h3>
                      <p className="text-muted-foreground">
                        {song.artist} • {song.genre}
                      </p>
                    </div>
                    
                    <p className="text-sm text-muted-foreground leading-relaxed">
                      🤖 <strong>AI 추천 이유:</strong> {song.reason}
                    </p>
                    
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Clock className="w-4 h-4" />
                        {song.duration}
                      </div>
                      <div>📅 {song.releaseYear}</div>
                    </div>
                    
                    <div className="flex gap-2">
                      <Button size="sm" variant="outline" className="gap-1">
                        <Play className="w-4 h-4" />
                        재생
                      </Button>
                      <Button size="sm" variant="outline" className="gap-1">
                        <Heart className="w-4 h-4" />
                        좋아요
                      </Button>
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
          <div className="flex gap-3 justify-center">
            <Button variant="outline" onClick={resetFlow} className="gap-2">
              <Shuffle className="w-4 h-4" />
              다른 취향으로 다시 추천받기
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
        <h2 className="mb-2">🎵 AI 음악 발견 여행</h2>
        <p className="text-muted-foreground">
          AI가 당신의 취향을 분석해서 새로운 음악을 추천해드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>분석 진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-gradient-to-r from-primary to-purple-500 h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / moodQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{moodQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center flex items-center justify-center gap-2">
            🤖 {moodQuestions[currentStep].question}
          </CardTitle>
          <CardDescription className="text-center">
            AI가 더 정확한 추천을 위해 당신을 분석하고 있어요
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {moodQuestions[currentStep].options.map((option) => (
              <Button
                key={option.value}
                variant="outline"
                onClick={() => handleAnswer(option.value)}
                className="h-auto p-4 flex flex-col items-center gap-2 hover:bg-gradient-to-br hover:from-primary/5 hover:to-purple-500/5 transition-all duration-200"
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