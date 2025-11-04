# 플랜P (Plan P)

무계획성 MBTI P 타입을 위한 계획 추천 어플리케이션

## 프로젝트 소개

플랜P는 계획 세우기가 어려운 P 타입을 위한 즉석 계획 추천 서비스입니다. 
8개의 카테고리(음악, 일상, 여행, 공부, 취미, 사교, 문화, 운동)에서 다양한 활동을 추천받고, 
AI 기반 개인 맞춤 추천 시스템을 통해 자신에게 딱 맞는 계획을 찾을 수 있습니다.

## 주요 기능

- 🎯 **8개 카테고리** - 음악, 일상, 여행, 공부, 취미, 사교, 문화, 운동
- 🎲 **랜덤 추천** - 선택 장애가 있다면 랜덤으로 추천받기
- 🤖 **AI 맞춤 추천** - 질문 기반 개인화 추천 시스템
- 📊 **루틴 관리** - 매일 실행할 활동 관리 및 진행상황 추적
- 🏆 **성취 시스템** - 완료한 계획 기록 및 성취감 쌓기
- 👤 **계정 관리** - 로그인/회원가입 시스템

## 백엔드 기술 스택
- **Backend**: Java
- **DataBase**: mySQL

## 프론트엔드 기술 스택
- **Frontend**: React 18 + TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: ShadCN UI
- **Icons**: Lucide React
- **Frontend Build Tool**: Vite
- **Charts**: Recharts

## 시작하기

### 설치

```bash
# 의존성 설치
npm install
```

### 프론트엔드 서버 실행

```bash
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어 확인하세요.

### 프로덕션 빌드

```bash
npm run build
```

빌드된 파일은 `dist` 폴더에 생성됩니다.

### 프리뷰

```bash
npm run preview
```

빌드된 프로덕션 버전을 로컬에서 미리 볼 수 있습니다.

## 프로젝트 구조

```
plan-p/
├── src/
│   ├── main.tsx               # 앱 엔트리포인트
│   └── App.tsx                # 메인 앱 컴포넌트 (복사본)
├── components/                # React 컴포넌트
│   ├── ui/                   # ShadCN UI 컴포넌트
│   ├── figma/               # Figma 관련 컴포넌트
│   ├── CategorySelector.tsx
│   ├── SubCategorySelector.tsx
│   ├── ArtistSearchActivity.tsx
│   ├── GenreExplorationActivity.tsx
│   ├── MusicDiscoveryActivity.tsx
│   ├── HomeWorkoutActivity.tsx
│   ├── DomesticTravelActivity.tsx
│   ├── InternationalTravelActivity.tsx
│   ├── CookingActivity.tsx
│   ├── MovieRecommendationActivity.tsx
│   ├── LanguageLearningActivity.tsx
│   ├── PhotographyActivity.tsx
│   ├── Header.tsx
│   ├── LoginForm.tsx
│   ├── SignupForm.tsx
│   ├── MyAccountPage.tsx
│   ├── RoutineManager.tsx
│   ├── AchievementSystem.tsx
│   ├── CompletionModal.tsx
│   └── ...
├── styles/
│   └── globals.css           # 글로벌 스타일 & Tailwind
├── guidelines/
│   └── Guidelines.md         # 개발 가이드라인
├── App.tsx                   # 메인 앱 컴포넌트
├── index.html               # HTML 템플릿
├── package.json            # 프로젝트 메타데이터
├── vite.config.ts         # Vite 설정
├── tailwind.config.js    # Tailwind 설정
├── postcss.config.js    # PostCSS 설정
├── tsconfig.json        # TypeScript 설정
├── .gitignore          # Git 제외 파일
├── .eslintrc.json     # ESLint 설정
├── README.md          # 프로젝트 소개
└── README_SETUP.md    # 상세 설치 가이드
```

## 주요 컴포넌트

### 카테고리 관련
- `CategorySelector` - 8개 메인 카테고리 선택
- `SubCategorySelector` - 각 카테고리의 하위 항목 선택

### 활동 컴포넌트
- `ArtistSearchActivity` - 가수별 신곡 추천
- `GenreExplorationActivity` - 음악 장르 탐색
- `MusicDiscoveryActivity` - AI 음악 발견
- `HomeWorkoutActivity` - 홈트레이닝
- `DomesticTravelActivity` - 국내 여행 추천
- `InternationalTravelActivity` - 해외 여행 추천
- `CookingActivity` - 요리 레시피 추천
- `MovieRecommendationActivity` - 영화 추천
- `LanguageLearningActivity` - 언어 학습
- `PhotographyActivity` - 사진 촬영 가이드

### 시스템 컴포넌트
- `Header` - 네비게이션 헤더
- `RoutineManager` - 루틴 관리 시스템
- `AchievementSystem` - 성취 추적 시스템
- `CompletionModal` - 활동 완료 축하 모달

## 개발 가이드

- 자세한 개발 가이드라인: [Guidelines.md](guidelines/Guidelines.md)
- 상세 설치 및 설정 가이드: [README_SETUP.md](README_SETUP.md)

### VSCode에서 실행하기

1. **의존성 설치**
   ```bash
   npm install
   ```

2. **개발 서버 실행**
   ```bash
   npm run dev
   ```

3. **프로덕션 빌드**
   ```bash
   npm run build
   ```

빌드된 파일은 `dist/` 폴더에 생성되며, 정적 호스팅 서비스(Vercel, Netlify 등)에 배포할 수 있습니다.

## 디자인 원칙

- **심플함**: 복잡하지 않고 직관적인 UI
- **재미**: 부담스럽지 않은 즐거운 사용자 경험
- **즉각성**: 빠르게 시작할 수 있는 계획들
- **개인화**: 사용자 맞춤 추천 시스템

## 라이선스

MIT License

## 기여하기

기여는 언제나 환영합니다! Pull Request를 보내주세요.

---

**무계획의 매력을 즐기세요 ✨**
