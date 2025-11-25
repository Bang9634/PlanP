import { useState } from 'react';
import { CategorySelector } from '../components/CategorySelector';
import { SubCategorySelector } from '../components/SubCategorySelector';
import { ArtistSearchActivity } from '../components/ArtistSearchActivity';
import { GenreExplorationActivity } from '../components/GenreExplorationActivity';
import { MusicDiscoveryActivity } from '../components/MusicDiscoveryActivity';
import { HomeWorkoutActivity } from '../components/HomeWorkoutActivity';
import { DomesticTravelActivity } from '../components/DomesticTravelActivity';
import { InternationalTravelActivity } from '../components/InternationalTravelActivity';
import { CookingActivity } from '../components/CookingActivity';
import { MovieRecommendationActivity } from '../components/MovieRecommendationActivity';
import { LanguageLearningActivity } from '../components/LanguageLearningActivity';
import { PhotographyActivity } from '../components/PhotographyActivity';
import { CompletionModal } from '../components/CompletionModal';
import { AchievementSystem } from '../components/AchievementSystem';
import { Header } from '../components/Header';
import { LoginForm } from '../components/LoginForm';
import { SignupForm } from '../components/SignupForm';
import { MyAccountPage } from '../components/MyAccountPage';
import { RoutineManager } from '../components/RoutineManager';
import { Lightbulb, Shuffle, Trophy, Target } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import {
    apiService,
    SignupRequest,
    LoginRequest,
    LoginResponse
} from "../services/api";


// 루틴 인터페이스
interface Routine {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  category: string;
  icon?: string;
  createdAt: Date;
  active: boolean;
  completedDays: Date[];
  streak: number;
  targetFrequency: 'daily' | 'weekly';
}

// 계획 인터페이스
interface Plan {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  participants?: string;
  rating?: number;
  icon?: string;
  category: string;
}

export default function App() {
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedSubCategory, setSelectedSubCategory] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<string>('home');
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<string | null>(null);
  const [completedPlans, setCompletedPlans] = useState<string[]>([]);
  const [showCompletionModal, setShowCompletionModal] = useState<boolean>(false);
  const [routines, setRoutines] = useState<Routine[]>([
    {
      id: 'routine-1',
      title: '15분 아침 스트레칭',
      description: '몸을 깨우는 간단한 스트레칭으로 하루를 시작해보세요',
      duration: '15분',
      difficulty: 'easy',
      tags: ['건강', '아침', '실내'],
      category: 'daily',
      icon: '🧘',
      createdAt: new Date('2024-01-20'),
      active: true,
      completedDays: [
        new Date('2024-01-25'),
        new Date('2024-01-26'),
        new Date('2024-01-27')
      ],
      streak: 3,
      targetFrequency: 'daily'
    },
    {
      id: 'routine-2', 
      title: '5분 명상하기',
      description: '짧은 명상으로 마음을 정리하고 집중력을 높여보세요',
      duration: '5-10분',
      difficulty: 'easy',
      tags: ['명상', '힐링', '실내'],
      category: 'daily',
      icon: '🧠',
      createdAt: new Date('2024-01-22'),
      active: true,
      completedDays: [
        new Date('2024-01-26'),
        new Date('2024-01-27')
      ],
      streak: 2,
      targetFrequency: 'daily'
    }
  ]);

  const [activityHistory, setActivityHistory] = useState<any[]>([]);

  const handleRandomCategory = () => {
    const categories = ['music', 'daily', 'travel', 'study', 'hobby', 'social', 'culture', 'exercise'];
    const randomCategory = categories[Math.floor(Math.random() * categories.length)];
    setSelectedCategory(randomCategory);
    setSelectedSubCategory(null);
  };

  const handleCategorySelect = (category: string) => {
    setSelectedCategory(category);
    setSelectedSubCategory(null);
  };

  const handleSubCategorySelect = (subCategory: string) => {
    setSelectedSubCategory(subCategory);
  };

  const handleBackToHome = () => {
    setSelectedCategory(null);
    setSelectedSubCategory(null);
  };

  const handleBackToCategory = () => {
    setSelectedSubCategory(null);
  };

  const handleActivityComplete = () => {
    setCompletedPlans(prev => [...prev, `${selectedCategory}-${selectedSubCategory}-${Date.now()}`]);
    setShowCompletionModal(true);
  };

  const handleCompletionModalClose = () => {
    setShowCompletionModal(false);
    handleBackToHome();
  };



    const handleLogin = async (id: string, password: string) => {
        console.log("📨 로그인 요청:", { id, password });

        // 백엔드 DTO 형태로 request body 구성
        const loginData: LoginRequest = {
            userId: id,
            password: password,
        };

        try {
            const result: LoginResponse = await apiService.login(loginData);

            if (result.success) {
                // 로그인 성공
                alert(`🎉 ${result.user?.name || id}님 환영합니다!`);

                setIsLoggedIn(true);
                setCurrentUser(result.user?.userId || id); // 서버에서 받은 userId
                setCurrentView("home");
            } else {
                // 로그인 실패 메시지 반환
                alert(`❌ 로그인 실패: ${result.message}`);
            }
        } catch (error) {
            console.error("로그인 오류:", error);
            alert("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        }
    };

    const handleSignup = async (
        id: string,
        password: string,
        confirmPassword: string,
        name: string,
        email: string
    ) => {
        console.log("📨 회원가입 요청 데이터:", {
            id,
            password,
            confirmPassword,
            name,
            email,
        });

        // 📌 백엔드 DTO(SignupRequest)에 정확히 맞는 JSON 구조
        const signupData: SignupRequest = {
            userId: id,
            password: password,
            name: name,
            email: email,
        };

        try {
            const result = await apiService.signup(signupData);

            if (result.success) {
                alert("🎉 회원가입이 완료되었습니다!");
                setCurrentView("login"); // 🔥 회원가입 후 → 로그인 화면으로 이동
            } else {
                alert(`❌ 회원가입 실패: ${result.message}`);
            }
        } catch (error) {
            console.error("회원가입 오류:", error);
            alert("서버와 연결할 수 없습니다. 다시 시도해주세요.");
        }
    };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setCurrentUser(null);
    setCurrentView('home');
    setSelectedCategory(null);
  };

  // 로그인 화면
  if (currentView === 'login') {
    return (
      <LoginForm
        onLogin={handleLogin}
        onSignupClick={() => setCurrentView('signup')}
      />
    );
  }

  // 회원가입 화면
  if (currentView === 'signup') {
    return (
      <SignupForm
        onSignup={handleSignup}
        onLoginClick={() => setCurrentView('login')}
      />
    );
  }

  // 내 계정 페이지
  if (currentView === 'account' && isLoggedIn) {
    return (
      <MyAccountPage
        currentUser={currentUser || ''}
        completedPlans={completedPlans}
        routines={routines}
        onBack={handleBackToHome}
      />
    );
  }

  // 루틴 관리 페이지
  if (currentView === 'routines' && isLoggedIn) {
    return (
      <div className="min-h-screen bg-background">
        <div className="border-b bg-card">
          <div className="container mx-auto px-4 py-4">
            <Button variant="ghost" onClick={handleBackToHome} className="mb-4">
              ← 뒤로가기
            </Button>
            <h1 className="text-2xl font-medium mb-2">루틴 관리</h1>
            <p className="text-muted-foreground">매일 실행할 활동들을 관리하고 진행상황을 확인하세요</p>
          </div>
        </div>
        <div className="container mx-auto px-4 py-8">
          <RoutineManager
            routines={routines}
            onRoutinesChange={setRoutines}
            onAddActivity={(activity) => setActivityHistory(prev => [...prev, activity])}
          />
        </div>
      </div>
    );
  }



  // 메인 홈 화면
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <Header
        isLoggedIn={isLoggedIn}
        currentView={currentView}
        onViewChange={setCurrentView}
        onLogout={handleLogout}
        currentUser={currentUser}
      />

      {/* Main Content */}
      <div className="container mx-auto px-4 py-8">
        <div className="text-center mb-12">
          <div className="flex items-center justify-center gap-2 mb-4">
            <Lightbulb className="w-8 h-8 text-yellow-500" />
            <h1 className="text-3xl">플랜P</h1>
          </div>
          <p className="text-muted-foreground max-w-md mx-auto">
            무계획이 매력인 당신을 위한 즉석 계획 추천
          </p>
          <p className="text-sm text-muted-foreground mt-2">
            어떤 걸 할지 모르겠다면, 아래에서 카테고리를 선택해보세요
          </p>
          {isLoggedIn && currentUser && (
            <p className="text-sm text-primary mt-2">
              {currentUser}님, 환영합니다! 🎉
            </p>
          )}
        </div>

        {/* Category Selection */}
        <div className="mb-8">
          <CategorySelector 
            selectedCategory={selectedCategory}
            onCategorySelect={handleCategorySelect}
          />
        </div>

        {/* Random Button */}
        <div className="text-center mb-8">
          <div className="flex flex-col items-center gap-3">
            <Button
              onClick={handleRandomCategory}
              variant="default"
              size="lg"
              className="gap-2 bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 text-white border-0 shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105"
            >
              <Shuffle className="w-5 h-5" />
              🎲 그냥 아무거나 추천해줘!
            </Button>
            <p className="text-xs text-muted-foreground max-w-xs">
              선택 장애가 있다면? 랜덤으로 재미있는 계획을 받아보세요!
            </p>
          </div>
        </div>

        {/* 하위 카테고리 선택 영역 */}
        {selectedCategory && !selectedSubCategory && (
          <div className="mb-8">
            <SubCategorySelector
              category={selectedCategory}
              onSubCategorySelect={handleSubCategorySelect}
              onBack={handleBackToHome}
            />
          </div>
        )}

        {/* 활동 영역 */}
        {selectedCategory && selectedSubCategory && (
          <div className="mb-8">
            {/* 음악 카테고리 활동들 */}
            {selectedCategory === 'music' && selectedSubCategory === 'artist-new-songs' && (
              <ArtistSearchActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}
            {selectedCategory === 'music' && selectedSubCategory === 'genre-exploration' && (
              <GenreExplorationActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}
            {selectedCategory === 'music' && selectedSubCategory === 'music-discovery' && (
              <MusicDiscoveryActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 일상 카테고리 활동들 */}
            {selectedCategory === 'daily' && selectedSubCategory === 'cooking' && (
              <CookingActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 여행 카테고리 활동들 */}
            {selectedCategory === 'travel' && selectedSubCategory === 'domestic-travel' && (
              <DomesticTravelActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}
            {selectedCategory === 'travel' && selectedSubCategory === 'international-travel' && (
              <InternationalTravelActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 공부 카테고리 활동들 */}
            {selectedCategory === 'study' && selectedSubCategory === 'language-learning' && (
              <LanguageLearningActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 취미 카테고리 활동들 */}
            {selectedCategory === 'hobby' && selectedSubCategory === 'photography' && (
              <PhotographyActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 문화 카테고리 활동들 */}
            {selectedCategory === 'culture' && selectedSubCategory === 'movie-drama' && (
              <MovieRecommendationActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 운동 카테고리 활동들 */}
            {selectedCategory === 'exercise' && selectedSubCategory === 'home-workout' && (
              <HomeWorkoutActivity 
                onBack={handleBackToCategory}
                onComplete={handleActivityComplete}
              />
            )}

            {/* 아직 구현되지 않은 활동들 */}
            {((selectedCategory === 'music' && !['artist-new-songs', 'genre-exploration', 'music-discovery'].includes(selectedSubCategory)) ||
              (selectedCategory === 'daily' && !['cooking'].includes(selectedSubCategory)) ||
              (selectedCategory === 'travel' && !['domestic-travel', 'international-travel'].includes(selectedSubCategory)) ||
              (selectedCategory === 'study' && !['language-learning'].includes(selectedSubCategory)) ||
              (selectedCategory === 'hobby' && !['photography'].includes(selectedSubCategory)) ||
              (selectedCategory === 'culture' && !['movie-drama'].includes(selectedSubCategory)) ||
              (selectedCategory === 'exercise' && !['home-workout'].includes(selectedSubCategory)) ||
              (selectedCategory === 'social')) && (
              <div className="text-center py-16">
                <h3 className="mb-4">이 활동은 곧 출시될 예정입니다!</h3>
                <p className="text-muted-foreground mb-8">
                  더 많은 재미있는 활동들을 준비하고 있어요.
                </p>
                <Button onClick={handleBackToCategory}>다른 활동 선택하기</Button>
              </div>
            )}
          </div>
        )}

        {/* 로그인 사용자 전용 섹션 */}
        {isLoggedIn && (
          <div className="mt-16 space-y-12">
            {/* 오늘의 루틴 요약 */}
            {routines.filter(r => r.active).length > 0 && (
              <div className="max-w-2xl mx-auto">
                <div className="flex items-center justify-center gap-2 mb-6">
                  <Target className="w-6 h-6 text-primary" />
                  <h2>오늘의 루틴</h2>
                </div>
                <div className="bg-gradient-to-r from-primary/5 to-purple-500/5 rounded-lg p-6 border border-primary/10">
                  <div className="grid gap-4 md:grid-cols-2">
                    {routines.filter(r => r.active).slice(0, 4).map((routine) => {
                      const today = new Date();
                      today.setHours(0, 0, 0, 0);
                      const isCompletedToday = routine.completedDays.some(date => 
                        date.toDateString() === today.toDateString()
                      );
                      
                      return (
                        <div key={routine.id} className="flex items-center gap-3 p-3 bg-background/50 rounded-lg">
                          <div className="text-xl">{routine.icon || '🎯'}</div>
                          <div className="flex-1">
                            <h4 className="font-medium text-sm">{routine.title}</h4>
                            <div className="flex items-center gap-2 text-xs text-muted-foreground">
                              <span>{routine.duration}</span>
                              {routine.streak > 0 && (
                                <Badge variant="secondary" className="text-xs">
                                  🔥 {routine.streak}일 연속
                                </Badge>
                              )}
                            </div>
                          </div>
                          <div className={`w-3 h-3 rounded-full ${
                            isCompletedToday ? 'bg-green-500' : 'bg-gray-300'
                          }`} />
                        </div>
                      );
                    })}
                  </div>
                  <div className="text-center mt-4">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentView('routines')}
                      className="gap-2"
                    >
                      <Target className="w-4 h-4" />
                      루틴 관리하기
                    </Button>
                  </div>
                </div>
              </div>
            )}

            {/* Achievement System */}
            <div>
              <div className="flex items-center justify-center gap-2 mb-8">
                <Trophy className="w-6 h-6 text-yellow-500" />
                <h2>나의 성취</h2>
              </div>
              <AchievementSystem 
                completedPlans={completedPlans}
                currentUser={currentUser}
              />
            </div>
          </div>
        )}

        {/* Welcome Message when no category selected */}
        {!selectedCategory && (
          <div className="text-center py-16">
            <div className="max-w-md mx-auto">
              <h3 className="mb-4 text-muted-foreground">
                계획 세우기가 어려우신가요?
              </h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                재미있는 계획들을 준비했어요. 
                부담스럽지 않고 바로 시작할 수 있는 것들만 모았습니다. 
                카테고리를 선택하거나 랜덤 추천을 받아보세요!
              </p>
            </div>
          </div>
        )}

        {/* 완료 축하 모달 */}
        <CompletionModal 
          isOpen={showCompletionModal}
          onClose={() => setShowCompletionModal(false)}
          onHomeReturn={handleCompletionModalClose}
        />
      </div>

      {/* Footer */}
      <footer className="border-t mt-16">
        <div className="container mx-auto px-4 py-6">
          <p className="text-center text-sm text-muted-foreground">
            무계획의 매력을 즐기세요 ✨
          </p>
        </div>
      </footer>
    </div>  
  );
}