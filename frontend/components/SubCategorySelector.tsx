import { Button } from "./ui/button";
import { ArrowLeft } from "lucide-react";

interface SubCategoryItem {
  id: string;
  name: string;
  description: string;
  emoji: string;
}

interface SubCategorySelectorProps {
  category: string;
  onSubCategorySelect: (subCategory: string) => void;
  onBack: () => void;
}

const subCategories: Record<string, SubCategoryItem[]> = {
  music: [
    { id: 'artist-new-songs', name: '가수 신곡 추천', description: '좋아하는 가수의 최신곡 발견하기', emoji: '🎤' },
    { id: 'genre-exploration', name: '새로운 장르 탐색', description: '평소 안 듣던 장르 도전해보기', emoji: '🎧' },
    { id: 'playlist-creation', name: '플레이리스트 만들기', description: '테마별 나만의 플레이리스트 제작', emoji: '📝' },
    { id: 'music-discovery', name: '음악 발견 여행', description: 'AI 추천으로 새로운 음악 찾기', emoji: '🔍' },
    { id: 'live-music', name: '라이브 음악 감상', description: '공연이나 라이브 영상 찾아보기', emoji: '🎵' }
  ],
  daily: [
    { id: 'cooking', name: '새로운 요리', description: '간단한 새 레시피 도전해보기', emoji: '👨‍🍳' },
    { id: 'organization', name: '정리정돈', description: '공간을 깔끔하게 정리하기', emoji: '📦' },
    { id: 'self-care', name: '셀프케어', description: '나를 위한 시간 갖기', emoji: '💆' },
    { id: 'reading', name: '독서', description: '새로운 책이나 아티클 읽기', emoji: '📖' },
    { id: 'journaling', name: '일기쓰기', description: '하루를 돌아보고 기록하기', emoji: '✏️' }
  ],
  travel: [
    { id: 'domestic-travel', name: '국내여행', description: '우리나라 명소와 숨은 보석 찾기', emoji: '🇰🇷' },
    { id: 'international-travel', name: '해외여행', description: '세계 각국의 매력적인 여행지', emoji: '🌍' },
    { id: 'day-trip', name: '당일치기 여행', description: '가까운 곳에서 즐기는 짧은 여행', emoji: '🚗' },
    { id: 'food-tour', name: '맛집 투어', description: '지역별 특색 있는 맛집 탐방', emoji: '🍜' },
    { id: 'nature-trip', name: '자연 여행', description: '산, 바다, 숲에서 힐링하기', emoji: '🏔️' }
  ],
  study: [
    { id: 'language-learning', name: '언어 학습', description: '새로운 외국어 배우기', emoji: '🗣️' },
    { id: 'tech-skills', name: '새로운 기술 배우기', description: '프로그래밍, 디자인 등 IT 스킬', emoji: '💻' },
    { id: 'online-courses', name: '온라인 강의', description: '관심 분야 강의 수강하기', emoji: '📚' },
    { id: 'certification', name: '자격증 공부', description: '취업이나 이직에 도움되는 자격증', emoji: '📜' },
    { id: 'book-study', name: '전문서적 읽기', description: '깊이 있는 지식 쌓기', emoji: '📖' }
  ],
  hobby: [
    { id: 'drawing-art', name: '그림/그리기', description: '창작의 즐거움을 느껴보세요', emoji: '🎨' },
    { id: 'photography', name: '사진 촬영', description: '순간을 아름답게 담아보기', emoji: '📸' },
    { id: 'music-instrument', name: '악기 연주', description: '새로운 악기 배우기', emoji: '🎹' },
    { id: 'collecting', name: '수집 활동', description: '나만의 컬렉션 만들기', emoji: '🏺' },
    { id: 'diy-crafts', name: 'DIY/만들기', description: '손으로 직접 만드는 재미', emoji: '🔨' },
    { id: 'gaming', name: '게임', description: '새로운 게임 도전하기', emoji: '🎮' },
    { id: 'gardening', name: '원예/식물 키우기', description: '초록 친구들과 함께하기', emoji: '🌱' }
  ],
  social: [
    { id: 'meet-friends', name: '친구 만나기', description: '오랜만에 친구와 시간 보내기', emoji: '👫' },
    { id: 'new-people', name: '새로운 사람들과 만나기', description: '모임이나 동호회 참여하기', emoji: '🤝' },
    { id: 'online-community', name: '온라인 커뮤니티', description: '관심사가 같은 사람들과 소통', emoji: '💬' },
    { id: 'volunteer', name: '봉사활동', description: '의미있는 일에 참여하기', emoji: '❤️' },
    { id: 'networking', name: '네트워킹', description: '새로운 인맥 만들기', emoji: '🌐' }
  ],
  culture: [
    { id: 'movie-drama', name: '영화/드라마', description: '새로운 작품 감상하기', emoji: '🎬' },
    { id: 'exhibition', name: '전시회 관람', description: '온/오프라인 전시 둘러보기', emoji: '🖼️' },
    { id: 'performance', name: '공연 관람', description: '연극, 뮤지컬, 콘서트 찾아보기', emoji: '🎭' },
    { id: 'museum', name: '박물관 탐방', description: '역사와 문화 배우기', emoji: '🏛️' },
    { id: 'cultural-experience', name: '문화 체험', description: '전통문화나 새로운 문화 경험', emoji: '🎨' }
  ],
  exercise: [
    { id: 'home-workout', name: '홈트레이닝', description: '집에서 할 수 있는 운동 루틴', emoji: '🏠' },
    { id: 'outdoor-activity', name: '야외 운동', description: '산책, 조깅, 공원 운동', emoji: '🌳' },
    { id: 'yoga-meditation', name: '요가/명상', description: '몸과 마음을 편안하게', emoji: '🧘' },
    { id: 'dance-fitness', name: '댄스/피트니스', description: '즐겁게 몸을 움직이기', emoji: '💃' },
    { id: 'stretching', name: '스트레칭', description: '간단한 몸풀기와 유연성 향상', emoji: '🤸' }
  ]
};

const getCategoryTitle = (category: string): string => {
  const titles: Record<string, string> = {
    music: '음악',
    daily: '일상',
    travel: '여행',
    study: '공부',
    hobby: '취미',
    social: '사교',
    culture: '문화',
    exercise: '운동'
  };
  return titles[category] || category;
};

export function SubCategorySelector({ category, onSubCategorySelect, onBack }: SubCategorySelectorProps) {
  const items = subCategories[category] || [];
  
  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">{getCategoryTitle(category)} 카테고리</h2>
        <p className="text-muted-foreground">
          어떤 종류의 {getCategoryTitle(category)} 활동을 해보시겠어요?
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {items.map((item) => (
          <Button
            key={item.id}
            variant="outline"
            onClick={() => onSubCategorySelect(item.id)}
            className="h-auto p-6 flex flex-col items-start gap-3 hover:bg-muted/50 transition-all duration-200 hover:scale-105"
          >
            <div className="text-3xl">{item.emoji}</div>
            <div className="text-left">
              <h3 className="font-medium mb-1">{item.name}</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                {item.description}
              </p>
            </div>
          </Button>
        ))}
      </div>
    </div>
  );
}