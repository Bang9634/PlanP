import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, Globe, Clock, Star, Target, Book } from 'lucide-react';

interface LanguageLearningActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface LanguageCourse {
  language: string;
  level: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  platform: string;
  features: string[];
  goal: string;
  dailyTime: string;
}

const languageQuestions = [
  {
    id: 'language',
    question: '어떤 언어를 배우고 싶으신가요?',
    options: [
      { value: 'english', label: '영어', emoji: '🇺🇸' },
      { value: 'japanese', label: '일본어', emoji: '🇯🇵' },
      { value: 'chinese', label: '중국어', emoji: '🇨🇳' },
      { value: 'spanish', label: '스페인어', emoji: '🇪🇸' },
      { value: 'french', label: '프랑스어', emoji: '🇫🇷' },
      { value: 'korean', label: '한국어 (외국인용)', emoji: '🇰🇷' }
    ]
  },
  {
    id: 'level',
    question: '현재 언어 실력은 어느 정도인가요?',
    options: [
      { value: 'beginner', label: '완전 초보자', emoji: '🌱' },
      { value: 'intermediate', label: '기초 회화 가능', emoji: '📚' },
      { value: 'advanced', label: '고급 과정 원함', emoji: '🎓' }
    ]
  },
  {
    id: 'goal',
    question: '언어 학습의 목표는 무엇인가요?',
    options: [
      { value: 'travel', label: '여행에서 사용', emoji: '✈️' },
      { value: 'business', label: '업무/취업', emoji: '💼' },
      { value: 'exam', label: '시험 준비', emoji: '📝' },
      { value: 'hobby', label: '취미/교양', emoji: '🎨' }
    ]
  }
];

const courses: Record<string, LanguageCourse[]> = {
  'english-beginner-travel': [
    {
      language: '영어',
      level: '초급',
      title: '여행 영어 마스터',
      description: '해외여행에서 꼭 필요한 실용 영어 표현들을 배워보세요',
      duration: '4주 과정',
      difficulty: 'beginner',
      platform: '듀오링고, 토크톡',
      features: ['기본 인사말', '음식 주문하기', '길 물어보기', '쇼핑 표현', '응급상황 대처'],
      goal: '자신감 있는 해외여행',
      dailyTime: '15-20분'
    }
  ],
  'japanese-beginner-hobby': [
    {
      language: '일본어',
      level: '초급',
      title: '일본어 첫걸음',
      description: '히라가나부터 기초 회화까지 차근차근 배워보세요',
      duration: '6주 과정',
      difficulty: 'beginner',
      platform: '링구아, 이지톡',
      features: ['히라가나/가타카나', '기본 인사', '숫자와 시간', '일상 대화', '애니메이션 표현'],
      goal: '일본 문화 이해하기',
      dailyTime: '20-30분'
    }
  ],
  'chinese-intermediate-business': [
    {
      language: '중국어',
      level: '중급',
      title: '비즈니스 중국어',
      description: '업무에서 사용하는 실용적인 중국어를 익혀보세요',
      duration: '8주 과정',
      difficulty: 'intermediate',
      platform: 'HSK 온라인, 차이니즈팟',
      features: ['비즈니스 이메일', '회의 참여', '프레젠테이션', '협상 표현', 'HSK 4급 대비'],
      goal: '업무 활용 가능',
      dailyTime: '30-40분'
    }
  ]
};

export function LanguageLearningActivity({ onBack, onComplete }: LanguageLearningActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<LanguageCourse[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [languageQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < languageQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = async (finalAnswers: Record<string, string>) => {
    setIsGenerating(true);
    
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    const key = `${finalAnswers.language}-${finalAnswers.level}-${finalAnswers.goal}`;
    const matchingCourses = courses[key] || [];
    
    if (matchingCourses.length === 0) {
      // 기본 추천 제공
      const allCourses = Object.values(courses).flat();
      setRecommendations(allCourses.slice(0, 2));
    } else {
      setRecommendations(matchingCourses);
    }
    
    setIsGenerating(false);
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
  };

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'beginner': return 'bg-green-100 text-green-700';
      case 'intermediate': return 'bg-yellow-100 text-yellow-700';
      case 'advanced': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getDifficultyText = (difficulty: string) => {
    switch (difficulty) {
      case 'beginner': return '초급';
      case 'intermediate': return '중급';
      case 'advanced': return '고급';
      default: return difficulty;
    }
  };

  if (isGenerating) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="mb-8">
          <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
            <ArrowLeft className="w-4 h-4" />
            뒤로가기
          </Button>
          <h2 className="mb-2">🗣️ 언어 학습 추천</h2>
        </div>

        <Card>
          <CardContent className="py-16">
            <div className="text-center space-y-4">
              <div className="animate-pulse text-6xl mb-4">🌍</div>
              <h3>당신에게 맞는 언어 학습 과정을 찾고 있어요</h3>
              <p className="text-muted-foreground">
                목표와 수준에 맞는 최적의 학습 방법을 분석하고 있습니다...
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
          <h2 className="mb-2">🗣️ 맞춤 언어 학습 과정</h2>
          <p className="text-muted-foreground">
            당신의 목표에 맞는 언어 학습 방법을 찾았어요!
          </p>
        </div>

        <div className="space-y-6 mb-8">
          {recommendations.map((course, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Globe className="w-5 h-5 text-primary" />
                      {course.title}
                    </CardTitle>
                    <CardDescription className="mt-2">
                      {course.description}
                    </CardDescription>
                  </div>
                  <Badge className={getDifficultyColor(course.difficulty)}>
                    {getDifficultyText(course.difficulty)}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* 기본 정보 */}
                  <div className="flex items-center gap-6 text-sm">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      <span>{course.duration}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Target className="w-4 h-4" />
                      <span>하루 {course.dailyTime}</span>
                    </div>
                    <Badge variant="outline">{course.language} {course.level}</Badge>
                  </div>

                  {/* 학습 목표 */}
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                    <h4 className="font-medium text-sm mb-1 flex items-center gap-2 text-blue-800">
                      🎯 학습 목표
                    </h4>
                    <p className="text-sm text-blue-700">{course.goal}</p>
                  </div>

                  {/* 주요 학습 내용 */}
                  <div className="bg-muted/50 rounded-lg p-4">
                    <h4 className="font-medium mb-3 flex items-center gap-2">
                      📚 주요 학습 내용
                    </h4>
                    <div className="grid gap-2 md:grid-cols-2">
                      {course.features.map((feature, fIndex) => (
                        <div key={fIndex} className="flex items-center gap-2 text-sm">
                          <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
                          {feature}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* 추천 플랫폼 */}
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-medium text-sm mb-1 flex items-center gap-1">
                        💻 추천 플랫폼
                      </h4>
                      <p className="text-sm text-muted-foreground">{course.platform}</p>
                    </div>
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span className="text-sm text-muted-foreground">추천</span>
                    </div>
                  </div>

                  <Button className="w-full gap-2">
                    <Book className="w-4 h-4" />
                    학습 시작하기
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
              🔄 다른 언어로 다시 찾기
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
        <h2 className="mb-2">🗣️ 언어 학습 추천</h2>
        <p className="text-muted-foreground">
          당신에게 맞는 언어 학습 방법을 찾아드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-primary h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / languageQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{languageQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center">
            {languageQuestions[currentStep].question}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {languageQuestions[currentStep].options.map((option) => (
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