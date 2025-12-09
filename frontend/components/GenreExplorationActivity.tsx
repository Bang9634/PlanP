import { useState, useEffect, useRef } from 'react';
import { Button } from './ui/button';
import { ArrowLeft, RefreshCw, Sparkles, Lock } from 'lucide-react';
import MusicService, { Track } from '../services/MusicService';
import { TrackList } from './TrackList';
import { useMusicPlayer } from '../hooks/useMusicPlayer';
import { AuthService } from '../services/AuthService';

interface GenreExplorationActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

const genres = [
  { id: 'KPOP', name: 'K-POP', emoji: '🇰🇷', color: 'bg-purple-100 hover:bg-purple-200 text-purple-700' },
  { id: 'POP', name: 'POP', emoji: '🎤', color: 'bg-pink-100 hover:bg-pink-200 text-pink-700' },
  { id: 'ROCK', name: 'ROCK', emoji: '🎸', color: 'bg-orange-100 hover:bg-orange-200 text-orange-700' },
  { id: 'HIPHOP', name: 'HIP-HOP', emoji: '🎧', color: 'bg-yellow-100 hover:bg-yellow-200 text-yellow-700' },
  { id: 'JAZZ', name: 'JAZZ', emoji: '🎷', color: 'bg-blue-100 hover:bg-blue-200 text-blue-700' },
  { id: 'CLASSICAL', name: 'CLASSICAL', emoji: '🎼', color: 'bg-indigo-100 hover:bg-indigo-200 text-indigo-700' },
  { id: 'ELECTRONIC', name: 'EDM', emoji: '⚡', color: 'bg-cyan-100 hover:bg-cyan-200 text-cyan-700' },
  { id: 'RNB', name: 'R&B', emoji: '💿', color: 'bg-rose-100 hover:bg-rose-200 text-rose-700' },
  { id: 'ALTERNATIVE', name: '얼터너티브', emoji: '🎹', color: 'bg-emerald-100 hover:bg-emerald-200 text-emerald-700' },
  { id: 'FOLK', name: '포크', emoji: '🪕', color: 'bg-amber-100 hover:bg-amber-200 text-amber-700' },
];

export function GenreExplorationActivity({ onBack, onComplete }: GenreExplorationActivityProps) {
  const [isAuthChecking, setIsAuthChecking] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [selectedGenre, setSelectedGenre] = useState<string | null>(null);
  const [tracks, setTracks] = useState<Track[]>([]);
  const [loading, setLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());
  
  const { currentAudio, playPreview, stopPreview } = useMusicPlayer();

  const hasLoadedInitial = useRef(false);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = AuthService.isAuthenticated();
      setIsAuthenticated(authenticated);
      setIsAuthChecking(false);
      
      if (!authenticated) {
        console.warn('⚠️ 로그인이 필요한 기능입니다');
      }
    };

    checkAuth();
  }, []);

  const loadTracks = async (genreId: string) => {
    if (!isAuthenticated) {
      console.warn('⚠️ 로그인이 필요합니다');
      return;
    }

    setLoading(true);

    try {
      console.log(`🎸 AI 장르별 검색: ${genreId}`);
      
      const results = await MusicService.getTracksByGenreWithAI(genreId, 20);
      
      if (results.length > 0) {
        setTracks(results);
      } else {
        console.warn('검색 결과 없음, 인기 차트 로드');
        const fallback = await MusicService.getTopTracks(20);
        setTracks(fallback);
      }
      
      setLastUpdated(new Date());
    } catch (error) {
      console.error('검색 실패:', error);
      
      if (error instanceof Error && error.message.includes('인증')) {
        AuthService.logout();
        setIsAuthenticated(false);
        alert('세션이 만료되었습니다. 다시 로그인해주세요.');
        onBack();
        return;
      }
      
      const fallback = await MusicService.getTopTracks(20);
      setTracks(fallback);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated && !isAuthChecking && !hasLoadedInitial.current) {
      console.log('🎵 초기 장르 로드: KPOP');
      hasLoadedInitial.current = true;
      setSelectedGenre('KPOP');
      loadTracks('KPOP');
    }
  }, [isAuthenticated, isAuthChecking]);

  useEffect(() => {
    return () => {
      if (currentAudio) {
        currentAudio.pause();
      }
    };
  }, [currentAudio]);

  const handleGenreSelect = async (genreId: string) => {
    if (!isAuthenticated) {
      alert('🔒 로그인이 필요한 기능입니다');
      return;
    }

    setSelectedGenre(genreId);
    await loadTracks(genreId);
  };

  const refreshChart = async () => {
    if (!selectedGenre || !isAuthenticated) return;
    
    setIsRefreshing(true);
    await new Promise(resolve => setTimeout(resolve, 500));
    await loadTracks(selectedGenre);
    setIsRefreshing(false);
  };

  if (isAuthChecking) {
    return (
      <div className="max-w-6xl mx-auto text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
        <p className="text-muted-foreground">인증 확인 중...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="max-w-6xl mx-auto text-center py-12">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        
        <div className="flex flex-col items-center gap-4 mt-12">
          <Lock className="w-16 h-16 text-muted-foreground" />
          <h2 className="text-2xl font-bold">로그인이 필요합니다</h2>
          <p className="text-muted-foreground">
            장르별 음악 탐색은 로그인 후 이용하실 수 있습니다.
          </p>
          <Button onClick={onBack} size="lg" className="mt-4">
            돌아가기
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* 헤더 */}
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <div className="flex items-center justify-between">
          <div>
            <h2 className="mb-2 flex items-center gap-2">
              🎸 장르별 음악 탐색
              <Sparkles className="w-5 h-5 text-yellow-500 animate-pulse" />
            </h2>
            <p className="text-muted-foreground">
              Gemini AI가 추천하는 장르별 인기곡 🤖✨
            </p>
          </div>
          
          {/* 컨트롤 버튼 */}
          {selectedGenre && (
            <div className="flex items-center gap-3">
              <span className="text-xs text-muted-foreground">
                {lastUpdated.toLocaleTimeString('ko-KR', { 
                  hour: '2-digit', 
                  minute: '2-digit' 
                })} 업데이트
              </span>
              <Button
                onClick={refreshChart}
                disabled={isRefreshing}
                variant="outline"
                size="sm"
                className="gap-2"
              >
                <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />
                새로고침
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* 장르 선택 */}
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-3 mb-8">
        {genres.map((genre) => (
          <Button
            key={genre.id}
            variant={selectedGenre === genre.id ? 'default' : 'outline'}
            onClick={() => handleGenreSelect(genre.id)}
            className={`h-auto p-4 flex flex-col items-center gap-2 transition-all ${
              selectedGenre === genre.id 
                ? '' 
                : genre.color
            }`}
          >
            <span className="text-2xl">{genre.emoji}</span>
            <span className="text-sm">{genre.name}</span>
          </Button>
        ))}
      </div>

      {/* 로딩 */}
      {loading && (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">
            🤖 Gemini AI가 {genres.find(g => g.id === selectedGenre)?.name} 추천곡을 찾고 있어요...
          </p>
        </div>
      )}

      {/* 트랙 목록 */}
      {!loading && tracks.length > 0 && (
        <>
          <div className="mb-4">
            <h3 className="text-lg font-semibold flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-yellow-500" />
              {genres.find(g => g.id === selectedGenre)?.name} AI 추천 곡 🤖
            </h3>
          </div>

          <TrackList 
            tracks={tracks} 
            showRank={true}
            showHotBadge={true}
            onPlayPreview={playPreview}
          />

          <div className="text-center mt-8">
            <Button onClick={onComplete} size="lg" className="gap-2">
              ✅ 활동 완료하기
            </Button>
            <p className="text-sm text-muted-foreground mt-3">
              💡 Gemini AI + iTunes Search API로 맞춤 추천을 제공합니다
            </p>
          </div>
        </>
      )}

      {/* 재생 중 정지 버튼 */}
      {currentAudio && (
        <div className="fixed bottom-6 right-6 z-50">
          <Button
            onClick={stopPreview}
            className="px-6 py-3 bg-red-600 text-white rounded-full shadow-lg hover:bg-red-700 gap-2"
          >
            ⏹ 재생 정지
          </Button>
        </div>
      )}
    </div>
  );
}