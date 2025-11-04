import { useState, useEffect } from "react";
import { CardDeck } from "./CardDeck";

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

interface DeckRecommendationsProps {
  category?: string;
  onPlanSelect: (plan: Plan) => void;
  onPlanReject: (plan: Plan) => void;
  onAddToRoutine?: (plan: Plan) => void;
}

// 카테고리별 대용량 계획 데이터
const planDatabase: Record<string, Plan[]> = {
  music: [
    {
      id: 'music-1',
      title: '새로운 플레이리스트 만들기',
      description: '오늘 기분에 맞는 음악들로 나만의 플레이리스트를 만들어보세요',
      duration: '15-20분',
      difficulty: 'easy',
      tags: ['창의적', '혼자하기', '실내'],
      participants: '혼자',
      rating: 4.5,
      icon: '🎵',
      category: 'music'
    },
    {
      id: 'music-2',
      title: '좋아하는 가수 신곡 탐색',
      description: '최근에 나온 신곡들을 찾아보고 새로운 음악을 발견해보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['발견', '혼자하기', '실내'],
      participants: '혼자',
      rating: 4.3,
      icon: '🔍',
      category: 'music'
    },
    {
      id: 'music-3',
      title: '악기 연주 영상 보며 따라하기',
      description: '유튜브에서 간단한 악기 연주 영상을 보며 따라해보세요',
      duration: '30-45분',
      difficulty: 'medium',
      tags: ['학습', '연주', '실내'],
      participants: '혼자',
      rating: 4.1,
      icon: '🎸',
      category: 'music'
    },
    {
      id: 'music-4',
      title: '음악과 함께 집안일 하기',
      description: '신나는 음악을 틀고 집안일을 하면서 스트레스 해소하기',
      duration: '30-60분',
      difficulty: 'easy',
      tags: ['생산적', '실내', '활동적'],
      participants: '혼자',
      rating: 4.4,
      icon: '🧹',
      category: 'music'
    },
    {
      id: 'music-5',
      title: '카페에서 음악 감상하기',
      description: '좋아하는 카페에서 이어폰을 끼고 음악에 집중해보세요',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['힐링', '카페', '혼자하기'],
      participants: '혼자',
      rating: 4.6,
      icon: '☕',
      category: 'music'
    }
  ],
  daily: [
    {
      id: 'daily-1',
      title: '15분 아침 스트레칭',
      description: '몸을 깨우는 간단한 스트레칭으로 하루를 시작해보세요',
      duration: '15분',
      difficulty: 'easy',
      tags: ['건강', '아침', '실내'],
      participants: '혼자',
      rating: 4.7,
      icon: '🧘',
      category: 'daily'
    },
    {
      id: 'daily-2',
      title: '창가에서 커피 마시며 일기 쓰기',
      description: '따뜻한 커피와 함께 오늘의 기분을 일기에 적어보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['힐링', '기록', '실내'],
      participants: '혼자',
      rating: 4.5,
      icon: '📝',
      category: 'daily'
    },
    {
      id: 'daily-3',
      title: '5분 명상하기',
      description: '짧은 명상으로 마음을 정리하고 집중력을 높여보세요',
      duration: '5-10분',
      difficulty: 'easy',
      tags: ['명상', '힐링', '실내'],
      participants: '혼자',
      rating: 4.4,
      icon: '🧠',
      category: 'daily'
    },
    {
      id: 'daily-4',
      title: '방 정리하고 꾸미기',
      description: '주변 환경을 깔끔하게 정리하고 나만의 공간으로 꾸며보세요',
      duration: '30-60분',
      difficulty: 'medium',
      tags: ['정리', '꾸미기', '실내'],
      participants: '혼자',
      rating: 4.3,
      icon: '🏠',
      category: 'daily'
    },
    {
      id: 'daily-5',
      title: '요리 레시피 도전하기',
      description: '새로운 요리 레시피를 찾아서 직접 만들어보세요',
      duration: '45-90분',
      difficulty: 'medium',
      tags: ['요리', '창의적', '실내'],
      participants: '혼자',
      rating: 4.2,
      icon: '👨‍🍳',
      category: 'daily'
    }
  ],
  exercise: [
    {
      id: 'exercise-1',
      title: '홈트레이닝 20분',
      description: '유튜브 홈트레이닝 영상을 보며 운동해보세요',
      duration: '20-30분',
      difficulty: 'medium',
      tags: ['운동', '실내', '건강'],
      participants: '혼자',
      rating: 4.5,
      icon: '💪',
      category: 'exercise'
    },
    {
      id: 'exercise-2',
      title: '동네 한 바퀴 산책',
      description: '가벼운 산책으로 몸과 마음을 상쾌하게 만들어보세요',
      duration: '30-45분',
      difficulty: 'easy',
      tags: ['산책', '야외', '힐링'],
      participants: '혼자',
      rating: 4.6,
      icon: '🚶',
      category: 'exercise'
    },
    {
      id: 'exercise-3',
      title: '계단 오르내리기 운동',
      description: '집이나 아파트 계단을 이용한 간단한 유산소 운동',
      duration: '10-15분',
      difficulty: 'medium',
      tags: ['유산소', '실내', '간단'],
      participants: '혼자',
      rating: 4.2,
      icon: '🏃',
      category: 'exercise'
    }
  ],
  travel: [
    {
      id: 'travel-1',
      title: '근처 카페 탐방하기',
      description: '가본 적 없는 동네 카페를 찾아서 새로운 분위기를 만끽해보세요',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['탐방', '카페', '새로운경험'],
      participants: '혼자 또는 친구와',
      rating: 4.4,
      icon: '☕',
      category: 'travel'
    },
    {
      id: 'travel-2',
      title: '도시 야경 명소 방문',
      description: '저녁에 아름다운 야경을 감상할 수 있는 장소를 찾아가보세요',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['야경', '데이트', '사진'],
      participants: '혼자 또는 연인과',
      rating: 4.7,
      icon: '🌃',
      category: 'travel'
    }
  ],
  study: [
    {
      id: 'study-1',
      title: '새로운 언어 20분 학습',
      description: '듀오링고나 언어 학습 앱으로 새로운 언어에 도전해보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['언어', '학습', '앱'],
      participants: '혼자',
      rating: 4.3,
      icon: '🗣️',
      category: 'study'
    },
    {
      id: 'study-2',
      title: 'TED 강연 시청하기',
      description: '관심 있는 주제의 TED 강연을 보며 새로운 지식을 얻어보세요',
      duration: '15-20분',
      difficulty: 'easy',
      tags: ['지식', '영감', '학습'],
      participants: '혼자',
      rating: 4.5,
      icon: '📚',
      category: 'study'
    }
  ],
  hobby: [
    {
      id: 'hobby-1',
      title: '간단한 그림 그리기',
      description: '종이와 펜으로 주변의 사물이나 풍경을 그려보세요',
      duration: '30-60분',
      difficulty: 'easy',
      tags: ['그림', '창의적', '실내'],
      participants: '혼자',
      rating: 4.2,
      icon: '🎨',
      category: 'hobby'
    },
    {
      id: 'hobby-2',
      title: '손편지 써보기',
      description: '가족이나 친구에게 정성스런 손편지를 써보세요',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['편지', '감성', '소통'],
      participants: '혼자',
      rating: 4.6,
      icon: '✉️',
      category: 'hobby'
    }
  ],
  social: [
    {
      id: 'social-1',
      title: '오랜 친구에게 안부 연락하기',
      description: '최근에 연락하지 못한 친구에게 안부를 물어보세요',
      duration: '30-60분',
      difficulty: 'easy',
      tags: ['소통', '우정', '연락'],
      participants: '2명',
      rating: 4.8,
      icon: '📞',
      category: 'social'
    },
    {
      id: 'social-2',
      title: '온라인 게임 친구와 즐기기',
      description: '친구들과 함께 온라인 게임을 하며 즐거운 시간 보내기',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['게임', '친구', '온라인'],
      participants: '2명 이상',
      rating: 4.4,
      icon: '🎮',
      category: 'social'
    }
  ],
  culture: [
    {
      id: 'culture-1',
      title: '온라인 박물관 투어',
      description: '구글 아트앤컬처로 세계 유명 박물관을 가상으로 둘러보세요',
      duration: '45-60분',
      difficulty: 'easy',
      tags: ['박물관', '문화', '예술'],
      participants: '혼자',
      rating: 4.3,
      icon: '🏛️',
      category: 'culture'
    },
    {
      id: 'culture-2',
      title: '다큐멘터리 시청하기',
      description: '관심 있는 주제의 다큐멘터리를 보며 견문을 넓혀보세요',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['다큐', '지식', '학습'],
      participants: '혼자',
      rating: 4.5,
      icon: '📺',
      category: 'culture'
    }
  ]
};

// 랜덤하게 계획들을 섞어서 반환하는 함수
const getRandomPlans = (category?: string, count: number = 20): Plan[] => {
  let allPlans: Plan[] = [];
  
  if (category && planDatabase[category]) {
    allPlans = [...planDatabase[category]];
  } else {
    // 모든 카테고리에서 계획 가져오기
    allPlans = Object.values(planDatabase).flat();
  }
  
  // 배열 섞기
  for (let i = allPlans.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [allPlans[i], allPlans[j]] = [allPlans[j], allPlans[i]];
  }
  
  return allPlans.slice(0, count);
};

export function DeckRecommendations({ 
  category, 
  onPlanSelect, 
  onPlanReject, 
  onAddToRoutine 
}: DeckRecommendationsProps) {
  const [plans, setPlans] = useState<Plan[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);

  // 카테고리 변경 시 새로운 계획들 생성
  useEffect(() => {
    const newPlans = getRandomPlans(category, 20);
    setPlans(newPlans);
    setCurrentIndex(0);
  }, [category]);

  // 더 많은 계획이 필요할 때
  const handleNeedMorePlans = () => {
    const additionalPlans = getRandomPlans(category, 10);
    setPlans(prev => [...prev, ...additionalPlans]);
  };

  return (
    <CardDeck
      plans={plans}
      onPlanSelect={onPlanSelect}
      onPlanReject={onPlanReject}
      onAddToRoutine={onAddToRoutine}
      onNeedMorePlans={handleNeedMorePlans}
    />
  );
}