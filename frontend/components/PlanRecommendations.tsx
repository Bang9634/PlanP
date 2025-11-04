import { PlanCard } from "./PlanCard";
import { TravelDestinationSelector } from "./TravelDestinationSelector";
import { MusicGenreSelector } from "./MusicGenreSelector";
import { MusicRecommendations } from "./MusicRecommendations";
import { Button } from "./ui/button";
import { Shuffle } from "lucide-react";
import { useState, useEffect } from "react";

interface Plan {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  participants?: string;
  rating?: number;
}

interface PlanRecommendationsProps {
  category: string;
  onPlanClick?: (plan: Plan) => void;
  refreshTrigger?: number;
}

const planData: Record<string, Plan[]> = {
  music: [],  // 장르별로 동적 생성
  daily: [
    {
      id: '4',
      title: '15분 아침 루틴',
      description: '간단한 스트레칭과 물 한 잔으로 하루 시작하기',
      duration: '15분',
      difficulty: 'easy',
      tags: ['스트레칭', '물', '아침'],
      rating: 4.9
    },
    {
      id: '5',
      title: '동네 산책로 탐험',
      description: '평소 가지 않던 골목길이나 공원 산책로 걸어보기',
      duration: '45분',
      difficulty: 'easy',
      tags: ['산책', '탐험', '운동'],
      participants: '혼자 추천',
      rating: 4.7
    },
    {
      id: '6',
      title: '간단한 요리 도전',
      description: '유튜브 보며 15분 안에 만들 수 있는 요리 하나 도전해보기',
      duration: '30분',
      difficulty: 'medium',
      tags: ['요리', '유튜브', '간단'],
      rating: 4.3
    }
  ],
  travel: [],  // 목적지별로 동적 생성
  study: [
    {
      id: '10',
      title: '25분 포모도로 공부법',
      description: '25분 집중 + 5분 휴식으로 하나의 주제 깊이 파보기',
      duration: '30분',
      difficulty: 'easy',
      tags: ['포모도로', '집중', '효율'],
      rating: 4.7
    },
    {
      id: '11',
      title: 'TED 강연 하나 보기',
      description: '관심 있는 분야의 TED 강연 하나 보고 핵심 내용 정리하기',
      duration: '20분',
      difficulty: 'easy',
      tags: ['TED', '영감', '정리'],
      rating: 4.8
    },
    {
      id: '12',
      title: '온라인 클래스 체험',
      description: '클래스101, 패스트캠퍼스 등에서 무료 체험 강의 들어보기',
      duration: '1시간',
      difficulty: 'medium',
      tags: ['온라인', '클래스', '체험'],
      rating: 4.4
    }
  ],
  hobby: [
    {
      id: '13',
      title: '스마트폰 사진 편집',
      description: 'VSCO, 스냅시드 같은 앱으로 사진 편집 기술 배우기',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['사진', '편집', '스마트폰'],
      rating: 4.6
    },
    {
      id: '14',
      title: '미니 가드닝',
      description: '작은 화분에 허브나 다육식물 키우기 시작하기',
      duration: '30분',
      difficulty: 'easy',
      tags: ['가드닝', '식물', '허브'],
      rating: 4.5
    },
    {
      id: '15',
      title: '간단한 DIY 프로젝트',
      description: '유튜브 보며 폰케이스 꾸미기나 책갈피 만들기 같은 소품 제작',
      duration: '1-2시간',
      difficulty: 'medium',
      tags: ['DIY', '만들기', '소품'],
      rating: 4.3
    }
  ],
  social: [
    {
      id: '16',
      title: '새로운 모임 참가',
      description: '관심사가 비슷한 사람들이 모이는 소모임이나 클럽 찾아보기',
      duration: '2-3시간',
      difficulty: 'medium',
      tags: ['모임', '네트워킹', '취미'],
      participants: '5-10명',
      rating: 4.4
    },
    {
      id: '17',
      title: '친구에게 편지 쓰기',
      description: '오랫동안 연락하지 못한 친구에게 손편지나 긴 메시지 보내기',
      duration: '30분',
      difficulty: 'easy',
      tags: ['편지', '친구', '감사'],
      rating: 4.9
    },
    {
      id: '18',
      title: '봉사활동 체험',
      description: '동물보호소, 푸드뱅크 등에서 단발성 봉사활동 참여하기',
      duration: '3-4시간',
      difficulty: 'medium',
      tags: ['봉사', '나눔', '체험'],
      participants: '그룹 활동',
      rating: 4.7
    }
  ],
  culture: [
    {
      id: '19',
      title: '무료 전시회 관람',
      description: '갤러리나 문화센터에서 열리는 무료 전시회 관람하기',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['전시회', '예술', '무료'],
      participants: '혼자 추천',
      rating: 4.6
    },
    {
      id: '20',
      title: '영화 마라톤',
      description: '하나의 주제나 감독으로 영화 2-3편 연속 시청하기',
      duration: '4-6시간',
      difficulty: 'easy',
      tags: ['영화', '마라톤', '주제'],
      rating: 4.4
    },
    {
      id: '21',
      title: '도서관 탐험',
      description: '새로운 도서관 방문해서 특별한 공간이나 프로그램 체험하기',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['도서관', '독서', '탐험'],
      rating: 4.5
    }
  ],
  exercise: [
    {
      id: '22',
      title: '홈트레이닝 시작',
      description: '유튜브 운동 영상 하나 골라서 따라하기',
      duration: '20-30분',
      difficulty: 'easy',
      tags: ['홈트', '유튜브', '시작'],
      rating: 4.7
    },
    {
      id: '23',
      title: '새로운 운동 체험',
      description: '클라이밍, 요가, 필라테스 등 평소 안 해본 운동 체험해보기',
      duration: '1시간',
      difficulty: 'medium',
      tags: ['체험', '클라이밍', '요가'],
      participants: '혼자 또는 친구와',
      rating: 4.5
    },
    {
      id: '24',
      title: '동네 러닝 코스 탐험',
      description: '평소와 다른 러닝 코스를 개척하며 가볍게 뛰어보기',
      duration: '30-45분',
      difficulty: 'medium',
      tags: ['러닝', '탐험', '코스'],
      rating: 4.6
    }
  ]
};

// 목적지별 여행 계획 데이터
const travelPlansByDestination: Record<string, Plan[]> = {
  '서울/강남': [
    {
      id: 'seoul-gangnam-1',
      title: '강남 스타일 투어',
      description: '강남역, 코엑스, 청담동 등 트렌디한 강남 핫플레이스 탐방',
      duration: '하루',
      difficulty: 'easy',
      tags: ['쇼핑', '카페', '트렌드'],
      participants: '친구와',
      rating: 4.7
    },
    {
      id: 'seoul-gangnam-2',
      title: '코엑스 아쿠아리움',
      description: '도심 속 바다 생물들과 만나는 특별한 체험',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['아쿠아리움', '데이트', '실내'],
      participants: '커플/가족',
      rating: 4.5
    }
  ],
  '서울/홍대': [
    {
      id: 'seoul-hongdae-1',
      title: '홍대 거리공연 투어',
      description: '홍대 앞 다양한 거리공연과 클럽 문화 체험하기',
      duration: '4-5시간',
      difficulty: 'medium',
      tags: ['거리공연', '홍대', '클럽'],
      participants: '친구들과',
      rating: 4.8
    },
    {
      id: 'seoul-hongdae-2',
      title: '홍대 독립서점 탐방',
      description: '개성 넘치는 홍대 일대 독립서점과 아지트 카페 투어',
      duration: '3-4시간',
      difficulty: 'easy',
      tags: ['독립서점', '카페', '문화'],
      participants: '혼자 또는 친구와',
      rating: 4.4
    }
  ],
  '부산/광안리': [
    {
      id: 'busan-gwangalli-1',
      title: '광안리 해변 야경',
      description: '광안대교 야경과 함께하는 해변 산책과 회 맛집 투어',
      duration: '3-4시간',
      difficulty: 'easy',
      tags: ['야경', '해변', '회'],
      participants: '커플/친구',
      rating: 4.9
    },
    {
      id: 'busan-gwangalli-2',
      title: '광안리 수변공원 조깅',
      description: '아침 일찍 광안리 수변공원에서 바다 보며 조깅하기',
      duration: '1-2시간',
      difficulty: 'medium',
      tags: ['조깅', '운동', '바다'],
      participants: '혼자',
      rating: 4.6
    }
  ],
  '부산/해운대': [
    {
      id: 'busan-haeundae-1',
      title: '해운대 해변 일출',
      description: '아름다운 해운대 해변에서 일출 감상하고 아침 산책',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['일출', '해변', '산책'],
      participants: '누구나',
      rating: 4.8
    },
    {
      id: 'busan-haeundae-2',
      title: '해운대 스카이캡슐',
      description: '해운대 해수욕장을 한눈에 내려다보는 스카이캡슐 체험',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['스카이캡슐', '전망', '체험'],
      participants: '커플/가족',
      rating: 4.5
    }
  ],
  '일본/도쿄/시부야': [
    {
      id: 'tokyo-shibuya-1',
      title: '시부야 스크램블 교차로',
      description: '세계에서 가장 유명한 교차로에서 인증샷 찍기',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['교차로', '인증샷', '관광'],
      participants: '누구나',
      rating: 4.7
    },
    {
      id: 'tokyo-shibuya-2',
      title: '하라주쿠 스트리트 패션',
      description: '독특한 일본 스트리트 패션과 원宿 문화 체험',
      duration: '3-4시간',
      difficulty: 'easy',
      tags: ['패션', '하라주쿠', '쇼핑'],
      participants: '친구와',
      rating: 4.6
    }
  ],
  '태국/방콕/시암': [
    {
      id: 'bangkok-siam-1',
      title: '시암 파라곤 쇼핑',
      description: '동남아 최대 쇼핑몰에서 쇼핑과 태국 전통 음식 체험',
      duration: '반나절',
      difficulty: 'easy',
      tags: ['쇼핑몰', '음식', '쇼핑'],
      participants: '친구/가족',
      rating: 4.5
    },
    {
      id: 'bangkok-siam-2',
      title: '짜오프라야 강 크루즈',
      description: '방콕의 생명줄 짜오프라야 강에서 선상 디너 크루즈',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['크루즈', '강', '디너'],
      participants: '커플 추천',
      rating: 4.8
    }
  ],
  '베트남/다낭/한시장': [
    {
      id: 'danang-hanmarket-1',
      title: '한시장 야시장 투어',
      description: '다낭 대표 야시장에서 현지 길거리 음식 체험',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['야시장', '길거리음식', '현지'],
      participants: '친구들과',
      rating: 4.7
    },
    {
      id: 'danang-hanmarket-2',
      title: '용다리 화염쇼',
      description: '다낭의 상징 용다리에서 주말 화염쇼 관람',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['용다리', '화염쇼', '야경'],
      participants: '가족/커플',
      rating: 4.6
    }
  ],
  '서울': [
    {
      id: 'seoul-1',
      title: '한강 피크닉',
      description: '한강공원에서 치킨과 맥주로 즐기는 여유로운 피크닉',
      duration: '3-4시간',
      difficulty: 'easy',
      tags: ['한강', '피크닉', '치맥'],
      participants: '친구들과',
      rating: 4.8
    },
    {
      id: 'seoul-2',
      title: '북촌 한옥마을 산책',
      description: '전통 한옥과 현대적 카페가 어우러진 북촌 탐험하기',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['한옥', '전통', '사진'],
      participants: '혼자 또는 친구와',
      rating: 4.6
    },
    {
      id: 'seoul-3',
      title: '홍대 거리공연 투어',
      description: '홍대 앞 다양한 거리공연과 클럽 문화 체험하기',
      duration: '4-5시간',
      difficulty: 'medium',
      tags: ['거리공연', '홍대', '클럽'],
      participants: '친구들과',
      rating: 4.5
    }
  ],
  '부산': [
    {
      id: 'busan-1',
      title: '해운대 해변 산책',
      description: '아름다운 해운대 해변에서 일출/일몰 감상하기',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['해변', '일출', '산책'],
      participants: '누구나',
      rating: 4.9
    },
    {
      id: 'busan-2',
      title: '감천문화마을 탐험',
      description: '알록달록한 색깔의 집들과 골목길 아트 투어',
      duration: '3-4시간',
      difficulty: 'medium',
      tags: ['문화마을', '아트', '포토존'],
      participants: '친구와 추천',
      rating: 4.7
    },
    {
      id: 'busan-3',
      title: '자갈치시장 맛집투어',
      description: '신선한 해산물과 부산 로컬 음식 맛보기',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['해산물', '시장', '맛집'],
      participants: '친구들과',
      rating: 4.6
    }
  ],
  '제주도': [
    {
      id: 'jeju-1',
      title: '성산일출봉 트레킹',
      description: '제주 대표 명소에서 장관의 일출 감상하기',
      duration: '3-4시간',
      difficulty: 'medium',
      tags: ['일출', '트레킹', '명소'],
      participants: '혼자 또는 친구와',
      rating: 4.8
    },
    {
      id: 'jeju-2',
      title: '한라봉 체험농장',
      description: '제주 특산물 한라봉 따기 체험과 시식',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['한라봉', '체험', '농장'],
      participants: '가족/친구와',
      rating: 4.5
    },
    {
      id: 'jeju-3',
      title: '우도 자전거 투어',
      description: '작고 아름다운 우도에서 자전거로 섬 일주하기',
      duration: '하루',
      difficulty: 'medium',
      tags: ['우도', '자전거', '섬'],
      participants: '친구와 추천',
      rating: 4.9
    }
  ],
  '강릉': [
    {
      id: 'gangneung-1',
      title: '정동진 해변 일출',
      description: '기차역에서 가장 가까운 바다에서 일출 감상',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['정동진', '일출', '기차'],
      participants: '누구나',
      rating: 4.8
    },
    {
      id: 'gangneung-2',
      title: '안목해변 커피거리',
      description: '바다 뷰와 함께 즐기는 강릉 로스터리 카페 투어',
      duration: '3-4시간',
      difficulty: 'easy',
      tags: ['커피', '바다뷰', '카페'],
      participants: '친구와',
      rating: 4.7
    },
    {
      id: 'gangneung-3',
      title: '오죽헌 문화체험',
      description: '율곡 이이의 생가에서 한국 전통문화 체험하기',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['전통문화', '역사', '체험'],
      rating: 4.4
    }
  ],
  '경주': [
    {
      id: 'gyeongju-1',
      title: '불국사 템플스테이',
      description: '천년 고찰에서 하룻밤 머물며 명상과 차 체험',
      duration: '1박2일',
      difficulty: 'medium',
      tags: ['템플스테이', '명상', '불교'],
      participants: '혼자 추천',
      rating: 4.6
    },
    {
      id: 'gyeongju-2',
      title: '첨성대 야경투어',
      description: '신라시대 천문대 첨성대와 주변 유적지 야경 감상',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['야경', '역사', '천문대'],
      participants: '친구와',
      rating: 4.5
    },
    {
      id: 'gyeongju-3',
      title: '대릉원 한복체험',
      description: '신라 왕릉에서 한복 입고 전통 포토 촬영',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['한복', '왕릉', '포토'],
      participants: '친구/커플',
      rating: 4.8
    }
  ],
  '전주': [
    {
      id: 'jeonju-1',
      title: '전주 한옥마을 맛집투어',
      description: '비빔밥과 한정식 등 전주 대표 음식 체험하기',
      duration: '하루',
      difficulty: 'easy',
      tags: ['한옥마을', '비빔밥', '맛집'],
      participants: '친구들과',
      rating: 4.9
    },
    {
      id: 'jeonju-2',
      title: '한지 공예 체험',
      description: '전통 한지로 만드는 소품 만들기 원데이클래스',
      duration: '2시간',
      difficulty: 'medium',
      tags: ['한지', '공예', '체험'],
      rating: 4.4
    },
    {
      id: 'jeonju-3',
      title: '전동성당 미사 참석',
      description: '아름다운 서양식 성당에서 평온한 시간 보내기',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['성당', '미사', '건축'],
      rating: 4.3
    }
  ],
  '여수': [
    {
      id: 'yeosu-1',
      title: '여수 밤바다 유람선',
      description: '아름다운 여수 밤바다를 유람선으로 감상하기',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['밤바다', '유람선', '야경'],
      participants: '커플/가족',
      rating: 4.8
    },
    {
      id: 'yeosu-2',
      title: '오동도 동백꽃 산책',
      description: '동백꽃이 피는 섬에서 로맨틱한 산책로 걷기',
      duration: '2-3시간',
      difficulty: 'easy',
      tags: ['동백꽃', '섬', '산책'],
      participants: '커플 추천',
      rating: 4.7
    },
    {
      id: 'yeosu-3',
      title: '여수 케이블카',
      description: '바다 위를 지나는 케이블카로 여수 전망 감상',
      duration: '1시간',
      difficulty: 'easy',
      tags: ['케이블카', '전망', '바다'],
      rating: 4.6
    }
  ],
  '안동': [
    {
      id: 'andong-1',
      title: '하회마을 탈춤 공연',
      description: '유네스코 세계문화유산에서 전통 탈춤 관람하기',
      duration: '3시간',
      difficulty: 'easy',
      tags: ['하회마을', '탈춤', '전통'],
      rating: 4.5
    },
    {
      id: 'andong-2',
      title: '안동소주 양조장 투어',
      description: '전통 방식으로 만드는 안동소주 제조과정 견학',
      duration: '2시간',
      difficulty: 'easy',
      tags: ['소주', '양조장', '전통'],
      participants: '성인만',
      rating: 4.4
    },
    {
      id: 'andong-3',
      title: '월영교 야경 산책',
      description: '아름다운 목재 다리에서 낙동강 야경 감상',
      duration: '1-2시간',
      difficulty: 'easy',
      tags: ['야경', '다리', '강'],
      participants: '커플 추천',
      rating: 4.6
    }
  ]
};

export function PlanRecommendations({ category, onPlanClick, refreshTrigger }: PlanRecommendationsProps) {
  const [selectedDestination, setSelectedDestination] = useState<string | null>(null);
  const [selectedGenre, setSelectedGenre] = useState<string | null>(null);
  const [currentPlans, setCurrentPlans] = useState<Plan[]>([]);

  // 계획을 랜덤하게 섞는 함수
  const shufflePlans = (plans: Plan[]) => {
    const shuffled = [...plans];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled.slice(0, 3); // 최대 3개까지만 보여주기
  };

  // 카테고리나 refreshTrigger 변경 시 계획 새로고침
  useEffect(() => {
    const plans = planData[category] || [];
    if (plans.length > 0) {
      setCurrentPlans(shufflePlans(plans));
    }
  }, [category, refreshTrigger]);

  // 음악 카테고리인 경우
  if (category === 'music') {
    return (
      <div className="mt-8">
        <div className="mb-8">
          <MusicGenreSelector
            selectedGenre={selectedGenre}
            onGenreSelect={setSelectedGenre}
          />
        </div>

        {selectedGenre && (
          <MusicRecommendations genre={selectedGenre} />
        )}
      </div>
    );
  }

  // 여행 카테고리인 경우
  if (category === 'travel') {
    return (
      <div className="mt-8">
        <div className="mb-8">
          <TravelDestinationSelector
            selectedDestination={selectedDestination}
            onDestinationSelect={setSelectedDestination}
          />
        </div>

        {selectedDestination && (
          <div>
            <h2 className="mb-6 text-center text-muted-foreground">
              {selectedDestination}에서 이런 계획은 어떠세요? ✈️
            </h2>
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {(travelPlansByDestination[selectedDestination] || []).map((plan) => (
                <PlanCard
                  key={plan.id}
                  id={plan.id}
                  title={plan.title}
                  description={plan.description}
                  duration={plan.duration}
                  difficulty={plan.difficulty}
                  tags={plan.tags}
                  participants={plan.participants}
                  rating={plan.rating}
                  onClick={() => onPlanClick?.(plan)}
                />
              ))}
            </div>
          </div>
        )}
      </div>
    );
  }

  // 기타 카테고리인 경우
  const plans = currentPlans.length > 0 ? currentPlans : (planData[category] || []);
  
  if (plans.length === 0) {
    return null;
  }

  const handleRefreshPlans = () => {
    const allPlans = planData[category] || [];
    if (allPlans.length > 0) {
      setCurrentPlans(shufflePlans(allPlans));
    }
  };

  return (
    <div className="mt-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-center text-muted-foreground">
          이런 계획은 어떠세요? 🎯
        </h2>
        <Button
          onClick={handleRefreshPlans}
          variant="outline"
          size="sm"
          className="gap-2"
        >
          <Shuffle className="w-4 h-4" />
          다른 추천 보기
        </Button>
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {plans.map((plan) => (
          <PlanCard
            key={plan.id}
            id={plan.id}
            title={plan.title}
            description={plan.description}
            duration={plan.duration}
            difficulty={plan.difficulty}
            tags={plan.tags}
            participants={plan.participants}
            rating={plan.rating}
            onClick={() => onPlanClick?.(plan)}
          />
        ))}
      </div>
    </div>
  );
}