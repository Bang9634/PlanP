import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, ChefHat, Clock, Users, Flame } from 'lucide-react';

interface CookingActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface Recipe {
  name: string;
  description: string;
  difficulty: 'easy' | 'medium' | 'hard';
  cookingTime: string;
  servings: string;
  ingredients: string[];
  steps: string[];
  tips: string;
  category: string;
}

const cookingQuestions = [
  {
    id: 'difficulty',
    question: '요리 실력은 어느 정도인가요?',
    options: [
      { value: 'beginner', label: '초보자 (라면 정도)', emoji: '🍜' },
      { value: 'intermediate', label: '중급자 (기본 요리)', emoji: '👨‍🍳' },
      { value: 'advanced', label: '고급자 (복잡한 요리)', emoji: '👨‍🍳‍⭐' }
    ]
  },
  {
    id: 'time',
    question: '요리할 시간은 얼마나 있나요?',
    options: [
      { value: 'quick', label: '30분 이내', emoji: '⚡' },
      { value: 'medium', label: '30분 - 1시간', emoji: '⏰' },
      { value: 'long', label: '1시간 이상 천천히', emoji: '🕐' }
    ]
  },
  {
    id: 'type',
    question: '어떤 종류의 음식을 만들고 싶나요?',
    options: [
      { value: 'korean', label: '한식', emoji: '🥢' },
      { value: 'western', label: '양식', emoji: '🍝' },
      { value: 'asian', label: '아시안 퓨전', emoji: '🍲' },
      { value: 'dessert', label: '디저트/베이킹', emoji: '🧁' }
    ]
  }
];

const recipes: Record<string, Recipe[]> = {
  'beginner-quick-korean': [
    {
      name: '참치마요덮밥',
      description: '간단하면서도 맛있는 참치마요덮밥으로 든든한 한 끼',
      difficulty: 'easy',
      cookingTime: '15분',
      servings: '1인분',
      ingredients: ['밥 1공기', '참치캔 1개', '마요네즈 2큰술', '간장 1작은술', '김 약간', '계란 1개'],
      steps: [
        '계란을 반숙으로 삶아주세요 (6-7분)',
        '참치캔의 기름을 빼고 마요네즈, 간장과 섞어주세요',
        '따뜻한 밥 위에 참치마요를 올려주세요',
        '반숙 계란을 올리고 김을 뿌려 완성!'
      ],
      tips: '참치에 다진 양파나 옥수수를 추가하면 더 맛있어요',
      category: '간단 한식'
    }
  ],
  'intermediate-medium-western': [
    {
      name: '크림 파스타',
      description: '부드럽고 진한 크림소스가 일품인 파스타',
      difficulty: 'medium',
      cookingTime: '45분',
      servings: '2인분',
      ingredients: ['파스타면 200g', '생크림 200ml', '마늘 3쪽', '베이컨 100g', '파마산 치즈', '올리브오일', '소금, 후추'],
      steps: [
        '파스타면을 소금물에 삶아주세요',
        '팬에 올리브오일을 두르고 마늘, 베이컨을 볶아주세요',
        '생크림을 넣고 끓이다가 파마산 치즈를 추가하세요',
        '삶은 파스타를 넣고 소스와 잘 버무려주세요',
        '소금, 후추로 간을 맞춰 완성!'
      ],
      tips: '파스타 삶은 물을 조금 넣으면 소스가 더 부드러워져요',
      category: '양식'
    }
  ],
  'beginner-quick-dessert': [
    {
      name: '초콜릿 머그케이크',
      description: '전자레인지로 3분 만에 만드는 초콜릿 케이크',
      difficulty: 'easy',
      cookingTime: '5분',
      servings: '1인분',
      ingredients: ['밀가루 4큰술', '설탕 4큰술', '코코아가루 2큰술', '베이킹파우더 1/4작은술', '우유 3큰술', '식용유 3큰술'],
      steps: [
        '머그컵에 모든 재료를 넣고 잘 섞어주세요',
        '덩어리가 없도록 골고루 저어주세요',
        '전자레인지에 1분 30초 돌려주세요',
        '꺼내서 확인 후 필요하면 30초 더 돌려주세요',
        '바닐라아이스크림이나 생크림 올려서 드세요!'
      ],
      tips: '초콜릿칩을 추가하면 더 맛있어요',
      category: '간단 디저트'
    }
  ]
};

export function CookingActivity({ onBack, onComplete }: CookingActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [recommendations, setRecommendations] = useState<Recipe[]>([]);

  const handleAnswer = (value: string) => {
    const newAnswers = { ...answers, [cookingQuestions[currentStep].id]: value };
    setAnswers(newAnswers);

    if (currentStep < cookingQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = (finalAnswers: Record<string, string>) => {
    const key = `${finalAnswers.difficulty}-${finalAnswers.time}-${finalAnswers.type}`;
    const matchingRecipes = recipes[key] || [];
    
    if (matchingRecipes.length === 0) {
      // 기본 추천 제공
      const allRecipes = Object.values(recipes).flat();
      setRecommendations(allRecipes.slice(0, 2));
    } else {
      setRecommendations(matchingRecipes);
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
          <h2 className="mb-2">👨‍🍳 맞춤 레시피 추천</h2>
          <p className="text-muted-foreground">
            당신의 요리 실력과 시간에 맞는 레시피를 찾았어요!
          </p>
        </div>

        <div className="space-y-6 mb-8">
          {recommendations.map((recipe, index) => (
            <Card key={index} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <ChefHat className="w-5 h-5 text-primary" />
                      {recipe.name}
                    </CardTitle>
                    <CardDescription className="mt-2">
                      {recipe.description}
                    </CardDescription>
                  </div>
                  <Badge className={getDifficultyColor(recipe.difficulty)}>
                    {getDifficultyText(recipe.difficulty)}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  {/* 기본 정보 */}
                  <div className="flex items-center gap-6 text-sm">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      <span>{recipe.cookingTime}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4" />
                      <span>{recipe.servings}</span>
                    </div>
                    <Badge variant="outline">{recipe.category}</Badge>
                  </div>

                  {/* 재료 */}
                  <div className="bg-muted/50 rounded-lg p-4">
                    <h4 className="font-medium mb-3 flex items-center gap-2">
                      🛒 필요한 재료
                    </h4>
                    <div className="grid gap-1 md:grid-cols-2">
                      {recipe.ingredients.map((ingredient, iIndex) => (
                        <div key={iIndex} className="flex items-center gap-2 text-sm">
                          <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
                          {ingredient}
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* 조리 과정 */}
                  <div>
                    <h4 className="font-medium mb-3 flex items-center gap-2">
                      <Flame className="w-4 h-4" />
                      조리 과정
                    </h4>
                    <div className="space-y-3">
                      {recipe.steps.map((step, sIndex) => (
                        <div key={sIndex} className="flex gap-3">
                          <span className="flex-shrink-0 w-6 h-6 bg-primary/10 text-primary rounded-full flex items-center justify-center text-sm font-medium">
                            {sIndex + 1}
                          </span>
                          <p className="text-sm pt-0.5">{step}</p>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* 꿀팁 */}
                  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                    <h4 className="font-medium mb-2 flex items-center gap-2 text-yellow-800">
                      💡 요리 꿀팁
                    </h4>
                    <p className="text-sm text-yellow-700">{recipe.tips}</p>
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
              🔄 다른 레시피 찾기
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
        <h2 className="mb-2">👨‍🍳 새로운 요리 도전</h2>
        <p className="text-muted-foreground">
          간단한 질문에 답해주시면 맞춤 레시피를 추천해드려요
        </p>
      </div>

      <div className="mb-6">
        <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
          <span>진행률</span>
          <div className="flex-1 bg-muted rounded-full h-2">
            <div 
              className="bg-primary h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentStep + 1) / cookingQuestions.length) * 100}%` }}
            />
          </div>
          <span>{currentStep + 1}/{cookingQuestions.length}</span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-center">
            {cookingQuestions[currentStep].question}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 md:grid-cols-2">
            {cookingQuestions[currentStep].options.map((option) => (
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