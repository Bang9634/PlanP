import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { ArrowLeft, Music, Play, Heart, Shuffle, Clock, ExternalLink } from 'lucide-react';
import MusicService, { Track } from '../services/MusicService';

interface MusicDiscoveryActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

const moodQuestions = [
  {
    id: 'energy',
    question: '지금 어떤 에너지를 원하시나요?',
    options: [
      { value: 'high', label: '신나고 활기찬', emoji: '🔥', keywords: 'upbeat energetic party' },
      { value: 'medium', label: '편안하고 차분한', emoji: '😌', keywords: 'chill relax calm' },
      { value: 'low', label: '잔잔하고 감성적인', emoji: '🌙', keywords: 'emotional ballad slow' }
    ]
  },
  {
    id: 'activity',
    question: '어떤 상황에서 들으실 건가요?',
    options: [
      { value: 'work', label: '일하거나 공부할 때', emoji: '💻', keywords: 'study focus concentration' },
      { value: 'exercise', label: '운동할 때', emoji: '🏃‍♀️', keywords: 'workout motivation gym' },
      { value: 'relax', label: '휴식이나 힐링할 때', emoji: '🛋️', keywords: 'healing meditation spa' },
      { value: 'commute', label: '이동할 때', emoji: '🚌', keywords: 'road trip travel' }
    ]
  },
  {
    id: 'preference',
    question: '평소 어떤 음악을 즐겨 듣나요?',
    options: [
      { value: 'pop', label: '대중가요/K-Pop', emoji: '🎤', keywords: 'kpop korean pop' },
      { value: 'indie', label: '인디/대안음악', emoji: '🎸', keywords: 'indie alternative rock' },
      { value: 'jazz', label: '재즈/소울', emoji: '🎷', keywords: 'jazz soul smooth' },
      { value: 'classical', label: '클래식/뉴에이지', emoji: '🎼', keywords: 'classical piano instrumental' },
      { value: 'electronic', label: '일렉트로닉/EDM', emoji: '🎧', keywords: 'electronic edm house' }
    ]
  }
];

export function MusicDiscoveryActivity({ onBack, onComplete }: MusicDiscoveryActivityProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [answers, setAnswers] = useState<Record<string, any>>({});
  const [recommendations, setRecommendations] = useState<Track[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [currentAudio, setCurrentAudio] = useState<HTMLAudioElement | null>(null);

  const handleAnswer = (value: string, keywords: string) => {
    const newAnswers = { 
      ...answers, 
      [moodQuestions[currentStep].id]: { value, keywords } 
    };
    setAnswers(newAnswers);

    if (currentStep < moodQuestions.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      generateRecommendations(newAnswers);
    }
  };

  const generateRecommendations = async (finalAnswers: Record<string, any>) => {
    setIsGenerating(true);
    
    try {
      // 모든 키워드 조합
      const allKeywords = Object.values(finalAnswers)
        .map((answer: any) => answer.keywords)
        .join(' ');
      
      console.log('🤖 AI 추천 키워드:', allKeywords);
      
      // iTunes API로 검색
      const tracks = await MusicService.searchTracks(allKeywords, 10);
      
      // 결과가 없으면 개별 키워드로 재시도
      if (tracks.length === 0) {
        const backupTracks = await MusicService.searchTracks(
          finalAnswers.preference.keywords, 
          10
        );
        setRecommendations(backupTracks);
      } else {
        setRecommendations(tracks);
      }
      
    } catch (error) {
      console.error('추천 생성 실패:', error);
      // 실패 시 K-POP 인기곡으로 대체
      const fallbackTracks = await MusicService.getTopTracks(10);
      setRecommendations(fallbackTracks);
    } finally {
      setIsGenerating(false);
    }
  };

  const playPreview = (previewUrl: string) => {
    if (currentAudio) {
      currentAudio.pause();
    }

    const audio = new Audio(previewUrl);
    audio.play();
    setCurrentAudio(audio);

    audio.addEventListener('ended', () => {
      setCurrentAudio(null);
    });
  };

  const stopPreview = () => {
    if (currentAudio) {
      currentAudio.pause();
      setCurrentAudio(null);
    }
  };

  const resetFlow = () => {
    setCurrentStep(0);
    setAnswers({});
    setRecommendations([]);
    stopPreview();
  };

  const getMoodColor = (genre: string = '') => {
    const colors: Record<string, string> = {
      'Pop': 'bg-red-100 text-red-700',
      'K-Pop': 'bg-purple-100 text-purple-700',
      'Rock': 'bg-orange-100 text-orange-700',
      'Hip-Hop/Rap': 'bg-yellow-100 text-yellow-700',
      'Dance': 'bg-pink-100 text-pink-700',
      'Electronic': 'bg-blue-100 text-blue-700',
      'Jazz': 'bg-indigo-100 text-indigo-700',
      'Classical': 'bg-violet-100 text-violet-700',
      'Alternative': 'bg-emerald-100 text-emerald-700',
    };
    return colors[genre] || 'bg-gray-100 text-gray-700';
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
                iTunes에서 수백만 곡 중 당신의 취향에 맞는 곡들을 검색하고 있습니다...
              </p>
              <div className="space-y-2 text-sm text-muted-foreground">
                <p>🎯 선호도 분석 중...</p>
                <p>🎶 iTunes 데이터베이스 검색 중...</p>
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
          {recommendations.map((song) => (
            <Card key={song.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start gap-4">
                  {/* 앨범 커버 */}
                  <div className="flex-shrink-0">
                    <img 
                      src={song.albumCover} 
                      alt={song.album}
                      className="w-20 h-20 rounded-lg object-cover"
                    />
                  </div>
                  
                  <div className="flex-1 space-y-3">
                    <div>
                      <h3 className="font-medium flex items-center gap-2">
                        {song.title}
                        {song.genre && (
                          <Badge className={getMoodColor(song.genre)} variant="secondary">
                            {song.genre}
                          </Badge>
                        )}
                      </h3>
                      <p className="text-muted-foreground">
                        {song.artist} • {song.album}
                      </p>
                    </div>
                    
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <Clock className="w-4 h-4" />
                        {Math.floor(song.duration / 60)}:{String(song.duration % 60).padStart(2, '0')}
                      </div>
                      {song.releaseDate && (
                        <div>📅 {song.releaseDate}</div>
                      )}
                    </div>
                    
                    <div className="flex gap-2">
                      {song.preview && (
                        <Button 
                          size="sm" 
                          variant="outline" 
                          className="gap-1"
                          onClick={() => playPreview(song.preview)}
                        >
                          <Play className="w-4 h-4" />
                          30초 미리듣기
                        </Button>
                      )}
                      {song.itunesUrl && (
                        <Button 
                          size="sm" 
                          variant="outline" 
                          className="gap-1"
                          onClick={() => window.open(song.itunesUrl, '_blank')}
                        >
                          <ExternalLink className="w-4 h-4" />
                          iTunes
                        </Button>
                      )}
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

        {/* 재생 중 정지 버튼 */}
        {currentAudio && (
          <div className="fixed bottom-6 right-6">
            <Button
              onClick={stopPreview}
              className="px-6 py-3 bg-red-600 text-white rounded-full shadow-lg hover:bg-red-700"
            >
              ⏹ 정지
            </Button>
          </div>
        )}
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
          AI가 당신의 취향을 분석해서 iTunes에서 새로운 음악을 추천해드려요
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
                onClick={() => handleAnswer(option.value, option.keywords)}
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