import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, Camera, Clock, Star, Eye, Lightbulb } from 'lucide-react';

interface PhotographyActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface PhotoChallenge {
  title: string;
  theme: string;
  description: string;
  difficulty: 'easy' | 'medium' | 'hard';
  duration: string;
  tips: string[];
  techniques: string[];
  equipment: string;
  inspiration: string;
  hashtags: string[];
}

const photographyQuestions = [
  {
    id: 'experience',
    question: '사진 촬영 경험은 어느 정도인가요?',
    options: [
      { value: 'beginner', label: '초보자 (폰카메라)', emoji: '📱' },
      { value: 'intermediate', label: '중급자 (기본 카메라)', emoji: '📷' },
      { value: 'advanced', label: '고급자 (전문 장비)', emoji: '📸' }
    ]
  },
  {
    id: 'subject',
    question: '어떤 피사체를 촬영하고 싶나요?',
    options: [
      { value: 'portrait', label: '인물/셀카', emoji: '👤' },
      { value: 'landscape', label: '풍경/자연', emoji: '🌄' },
      { value: 'street', label: '일상/거리', emoji: '🏙️' },
      { value: 'food', label: '음식', emoji: '🍜' },
      { value: 'macro', label: '소품/클로즈업', emoji: '🔍' }
    ]
  },
  {
    id: 'mood',
    question: '어떤 느낌의 사진을 원하시나요?',
    options: [
      { value: 'bright', label: '밝고 화사한', emoji: '☀️' },
      { value: 'moody', label: '분위기 있는', emoji: '🌙' },
      { value: 'vintage', label: '빈티지/레트로', emoji: '📼' },
      { value: 'minimal', label: '미니멀/깔끔한', emoji: '⚪' }
    ]
  }
];

const challenges: Record<string, PhotoChallenge[]> = {
  'beginner-portrait-bright': [
    {
      title: '셀카 마스터 도전',
      theme: '자연광 인물 사진',
      description: '창가의 자연광을 활용해서 아름다운 셀카와 인물 사진을 찍어보세요',
      difficulty: 'easy',
      duration: '30분',
      tips: [
        '창문 옆에서 촬영하세요',
        '얼굴을 45도 각도로 돌려보세요',
        '눈높이에서 찍으면 자연스러워요',
        '배경을 단순하게 정리하세요'
      ],
      techniques: ['자연광 활용', '구도 잡기', '각도 찾기'],
      equipment: '스마트폰 또는 기본 카메라',
      inspiration: '인스타그램의 자연스러운 일상 사진들',
      hashtags: ['#셀카', '#자연광', '#일상사진', '#portrait']
    }
  ],
  'intermediate-landscape-moody': [
    {
      title: '골든아워 풍경 사진',
      theme: '분위기 있는 풍경',
      description: '일출이나 일몰 시간대의 부드러운 빛으로 감성적인 풍경 사진을 촬영해보세요',
      difficulty: 'medium',
      duration: '1-2시간',
      tips: [
        '일출 30분 전부터 준비하세요',
        '전경에 흥미로운 요소를 배치하세요',
        '실루엣 효과를 활용해보세요',
        '구름이 있는 날이 더 드라마틱해요'
      ],
      techniques: ['골든아워 활용', '3분할 구도', '전경-중경-배경', '실루엣 촬영'],
      equipment: '카메라, 삼각대 (선택사항)',
      inspiration: '지브리 애니메이션의 배경 같은 몽환적 풍경',
      hashtags: ['#골든아워', '#풍경사진', '#일몰', '#분위기사진']
    }
  ],
  'beginner-food-bright': [
    {
      title: '맛있어 보이는 음식 사진',
      theme: '푸드 포토그래피',
      description: '일상의 음식을 맛있고 예쁘게 촬영하는 방법을 배워보세요',
      difficulty: 'easy',
      duration: '20분',
      tips: [
        '위에서 내려다보며 찍어보세요',
        '자연광이 들어오는 곳에서 촬영하세요',
        '음식 주변을 깔끔하게 정리하세요',
        '작은 소품을 함께 배치해보세요'
      ],
      techniques: ['탑다운 앵글', '플랫레이', '자연광 활용', '소품 활용'],
      equipment: '스마트폰',
      inspiration: '카페나 레스토랑의 인스타그램 피드',
      hashtags: ['#푸드포토', '#음식사진', '#일상', '#맛스타그램']
    }
  ]
};

export function PhotographyActivity({ onBack, onComplete }: PhotographyActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<PhotoChallenge[]>([]);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [photographyQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < photographyQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = (finalAnswers: Record<string, string>) => {
    const key = `${finalAnswers.experience}-${finalAnswers.subject}-${finalAnswers.mood}`;
    const matchingChallenges = challenges[key] || [];
    
    if (matchingChallenges.length === 0) {
      // 기본 추천 제공
      const allChallenges = Object.values(challenges).flat();
      setRecommendations(allChallenges.slice(0, 2));
    } else {
      setRecommendations(matchingChallenges);
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

  const getDifficultyText = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return '쉬움';
      case 'medium': return '보통';
      case 'hard': return '어려움';
      default: return difficulty;
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
          <h2 className="mb-2">📸 맞춤 사진 촬영 도전</h2>
          <p className="text-muted-foreground">
            당신의 실력과 취향에 맞는 사진 촬영 도전과제를 찾았어요!
          </p>
        </div>

        <div className="space-y-6 mb-8">
          {recommendations.map((challenge, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Camera className="w-5 h-5 text-primary" />
                      {challenge.title}
                    </CardTitle>
                    <CardDescription className="mt-2">
                      {challenge.description}
                    </CardDescription>
                  </div>
                  <Badge className={getDifficultyColor(challenge.difficulty)}>
                    {getDifficultyText(challenge.difficulty)}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* 기본 정보 */}
                  <div className="flex items-center gap-6 text-sm">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      <span>{challenge.duration}</span>
                    </div>
                    <Badge variant="outline">{challenge.theme}</Badge>
                  </div>

                  {/* 촬영 팁 */}
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                    <h4 className="font-medium mb-3 flex items-center gap-2 text-blue-800">
                      💡 촬영 팁
                    </h4>
                    <div className="space-y-2">
                      {challenge.tips.map((tip, tIndex) => (
                        <div key={tIndex} className="flex items-start gap-2 text-sm text-blue-700">
                          <span className="w-1.5 h-1.5 bg-blue-500 rounded-full mt-2 flex-shrink-0"></span>
                          {tip}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* 촬영 기법 */}
                  <div className="bg-muted/50 rounded-lg p-4">
                    <h4 className="font-medium mb-3 flex items-center gap-2">
                      🎯 배울 수 있는 기법
                    </h4>
                    <div className="flex flex-wrap gap-2">
                      {challenge.techniques.map((technique, techIndex) => (
                        <Badge key={techIndex} variant="secondary" className="text-xs">
                          {technique}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  {/* 장비 정보 */}
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <h4 className="font-medium text-sm mb-2 flex items-center gap-1">
                        📷 필요 장비
                      </h4>
                      <p className="text-sm text-muted-foreground">{challenge.equipment}</p>
                    </div>
                    <div>
                      <h4 className="font-medium text-sm mb-2 flex items-center gap-1">
                        <Eye className="w-4 h-4" />
                        영감 소스
                      </h4>
                      <p className="text-sm text-muted-foreground">{challenge.inspiration}</p>
                    </div>
                  </div>

                  {/* 해시태그 */}
                  <div>
                    <h4 className="font-medium text-sm mb-2">📱 추천 해시태그</h4>
                    <div className="flex flex-wrap gap-1">
                      {challenge.hashtags.map((tag, tagIndex) => (
                        <span key={tagIndex} className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">
                          {tag}
                        </span>
                      ))}
                    </div>
                  </div>

                  <Button className="w-full gap-2">
                    <Camera className="w-4 h-4" />
                    지금 촬영하러 가기!
                  </Button>
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
              🔄 다른 스타일로 다시 찾기
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
        <h2 className="mb-2">📸 사진 촬영 도전</h2>
        <p className="text-muted-foreground">
          당신에게 맞는 사진 촬영 도전과제를 찾아드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-primary h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / photographyQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{photographyQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center">
            {photographyQuestions[currentStep].question}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {photographyQuestions[currentStep].options.map((option) => (
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