# 플랜P (Plan P) 🎯

> 무계획성 MBTI P 타입을 위한 즉석 계획 추천 플랫폼

[![Frontend CI](https://github.com/username/PlanP/workflows/Frontend%20CI/badge.svg)](https://github.com/username/PlanP/actions)
[![Backend CI](https://github.com/username/PlanP/workflows/Backend%20CI/badge.svg)](https://github.com/username/PlanP/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📖 프로젝트 소개

플랜P는 계획 세우기가 어려운 P 타입을 위한 **AI 기반 즉석 계획 추천 서비스**입니다. 

"오늘 뭐 할까?" 고민이 많은 당신을 위해 8개 카테고리에서 개인 맞춤형 활동을 추천하고, 루틴 관리와 성취 시스템으로 꾸준한 실행을 도와드립니다.

### 🎯 핵심 가치
- **즉각성**: 5초 내로 시작할 수 있는 계획 추천
- **개인화**: AI 기반 사용자 맞춤 추천 시스템  
- **심플함**: 복잡하지 않은 직관적 UI/UX
- **재미**: 게임화된 성취 시스템

## ✨ 주요 기능

| 기능 | 설명 | 상태 |
|------|------|------|
| 🎲 **랜덤 추천** | 8개 카테고리별 즉석 활동 추천 | ✅ |
| 🤖 **AI 맞춤 추천** | 개인 선호도 기반 맞춤형 계획 | ✅ |
| 📊 **루틴 관리** | 일일/주간 활동 관리 및 추적 | ✅ |
| 🏆 **성취 시스템** | 완료 기록 및 레벨업 시스템 | ✅ |
| 👤 **계정 관리** | 회원가입/로그인/프로필 관리 | ✅ |
| 📱 **반응형 디자인** | 모바일/태블릿/데스크톱 지원 | ✅ |

### 🗂️ 추천 카테고리
- 🎵 **음악** - 신곡 발견, 장르 탐색, 플레이리스트 생성
- 🏠 **일상** - 홈트레이닝, 요리, 정리정돈
- ✈️ **여행** - 국내외 여행지 추천, 당일치기 코스
- 📚 **공부** - 언어학습, 온라인 강의, 독서 계획
- 🎨 **취미** - 사진촬영, 그림그리기, 수공예
- 👥 **사교** - 모임 기획, 네트워킹 활동
- 🎭 **문화** - 영화감상, 전시관람, 공연예술
- 💪 **운동** - 홈트, 야외활동, 스포츠

## 🛠️ 기술 스택

### Backend
- **언어**: Java 21
- **프레임워크**: Java HTTP Server (내장)
- **데이터베이스**: MySQL 8.0
- **빌드 도구**: Maven
- **라이브러리**: Gson, jBCrypt, SLF4J+Logback

### Frontend  
- **언어**: TypeScript
- **프레임워크**: React 18 + Vite
- **스타일링**: Tailwind CSS
- **UI 라이브러리**: ShadCN UI
- **아이콘**: Lucide React
- **차트**: Recharts

### DevOps & 배포
- **CI/CD**: nCloud Source Deploy & Source Build
- **웹서버**: tomcat10
- **환경관리**: 환경변수 기반 설정

## 🚀 빠른 시작

### 전체 프로젝트 실행

```bash
# 저장소 클론
git clone https://github.com/username/PlanP.git
cd PlanP

# 백엔드 서버 실행 (포트 8080)
cd backend/planp
mvn clean package
java -jar target/planp-backend.jar

# 프론트엔드 서버 실행 (포트 3000) - 새 터미널
cd ../../frontend  
npm ci
npm run dev
```

### 개별 실행

#### 🔧 백엔드 서버
```bash
cd backend/planp

# 의존성 설치 및 빌드
mvn clean package

# 서버 실행 (기본 포트: 8080)
java -jar target/planp-backend.jar

# 커스텀 포트로 실행  
java -jar target/planp-backend.jar 9090

# 환경변수와 함께 실행
export PLANP_HOST=0.0.0.0
export PLANP_PORT=8080
java -jar target/planp-backend.jar
```

#### 🎨 프론트엔드 서버
```bash
cd frontend

# 의존성 설치
npm ci

# 개발 서버 실행
npm run dev

# 프로덕션 빌드
npm run build

# 빌드 프리뷰
npm run preview
```

### 환경 설정

1. **환경변수 파일 생성**
   ```bash
   # frontend/.env.local 생성
   cp frontend/.env.example frontend/.env.local
   
   # 실제 서버 IP 설정
   VITE_API_URL=http://YOUR_SERVER_IP:8080/api
   ```

2. **데이터베이스 설정** (백엔드)
   ```bash
   # MySQL 연결 정보 설정
   export MYSQL_HOST=localhost
   export MYSQL_PORT=3306
   export MYSQL_DATABASE=planp_db
   export MYSQL_USERNAME=your_username
   export MYSQL_PASSWORD=your_password
   ```

## 📁 프로젝트 구조

```
PlanP/
├── 📂 backend/planp/           # Java 백엔드
│   ├── src/main/java/
│   │   └── com/drhong/
│   │       ├── Main.java       # 서버 엔트리포인트  
│   │       ├── controller/     # REST API 컨트롤러
│   │       ├── service/        # 비즈니스 로직
│   │       ├── dao/           # 데이터 액세스 레이어
│   │       ├── model/         # 데이터 모델
│   │       ├── dto/           # 데이터 전송 객체
│   │       ├── server/        # HTTP 서버 설정
│   │       └── util/          # 유틸리티 클래스
│   ├── pom.xml               # Maven 설정
│   └── target/               # 빌드 결과물
├── 📂 frontend/               # React 프론트엔드  
│   ├── src/
│   │   ├── App.tsx           # 메인 앱 컴포넌트
│   │   └── main.tsx          # 앱 엔트리포인트
│   ├── components/           # React 컴포넌트
│   │   ├── ui/              # ShadCN UI 컴포넌트
│   │   ├── CategorySelector.tsx
│   │   ├── Header.tsx
│   │   ├── LoginForm.tsx
│   │   └── ...
│   ├── services/            # API 서비스
│   │   └── api.ts          # 백엔드 API 클라이언트
│   ├── package.json        # 패키지 정보
│   └── dist/              # 빌드 결과물
└── README.md              # 프로젝트 문서
```

## 🌐 API 문서

### 사용자 관리
- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인  
- `GET /api/users/check-id?userId=...` - ID 중복 확인
- `GET /api/users/check-email?email=...` - 이메일 중복 확인

### 시스템
- `GET /health` - 서버 상태 확인

## 🚢 배포

### 수동 배포
```bash
# 백엔드 JAR 파일 생성
cd backend/planp && mvn clean package

# 프론트엔드 빌드  
cd frontend && npm run build

# 서버에 파일 업로드
scp backend/planp/target/planp-backend.jar user@server:/opt/planp/
scp -r frontend/dist/* user@server:/var/lib/tomcat10/webapps/ROOT/
```

### 개발 가이드라인
- **코드 스타일**: ESLint + Prettier (프론트엔드), Google Java Style (백엔드)
- **커밋 메시지**: [Conventional Commits](https://www.conventionalcommits.org/) 형식
- **브랜치 전략**: Git Flow
- **테스트**: 모든 PR에 테스트 코드 포함 필수

## 📋 할 일 목록

- [ ] AI 추천 알고리즘 고도화
- [ ] 소셜 로그인 (Google, Kakao) 연동
- [ ] 푸시 알림 시스템  
- [ ] 모바일 앱 버전 (React Native)
- [ ] 다국어 지원 (i18n)
- [ ] 다크 모드 지원

## 📄 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE) 하에 배포됩니다.

## 👥 기여자

<a href="https://github.com/username/PlanP/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=username/PlanP" />
</a>

---

<p align="center">
  <b>무계획의 매력을 즐기세요 ✨</b><br>
  Made with ❤️ by PlanP Team
</p>