import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Input } from './ui/input';
import { ArrowLeft, Search, Play, Clock, ExternalLink } from 'lucide-react';
import MusicService, { Track } from '../services/MusicService';

interface ArtistSearchActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

export function ArtistSearchActivity({ onBack, onComplete }: ArtistSearchActivityProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [tracks, setTracks] = useState<Track[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentAudio, setCurrentAudio] = useState<HTMLAudioElement | null>(null);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    setLoading(true);

    try {
      const results = await MusicService.searchArtist(searchQuery, 20);
      setTracks(results);
    } catch (error) {
      console.error('아티스트 검색 실패:', error);
    } finally {
      setLoading(false);
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

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">🎤 좋아하는 아티스트 찾기</h2>
        <p className="text-muted-foreground">
          아티스트 이름을 검색하고 최신 곡들을 들어보세요
        </p>
      </div>

      {/* 검색 */}
      <div className="flex gap-2 mb-6">
        <Input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="아티스트 이름 검색 (예: 아이유, BTS, Taylor Swift)"
          className="flex-1"
        />
        <Button onClick={handleSearch} className="gap-2">
          <Search className="w-4 h-4" />
          검색
        </Button>
      </div>

      {/* 로딩 */}
      {loading && (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">검색 중...</p>
        </div>
      )}

      {/* 트랙 목록 */}
      {!loading && tracks.length > 0 && (
        <>
          <div className="grid gap-4 mb-8">
            {tracks.map((track) => (
              <Card key={track.id} className="hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex items-center gap-4">
                    <img 
                      src={track.albumCover} 
                      alt={track.album}
                      className="w-16 h-16 rounded object-cover"
                    />
                    <div className="flex-1 min-w-0">
                      <h3 className="font-medium truncate">{track.title}</h3>
                      <p className="text-sm text-muted-foreground truncate">{track.artist}</p>
                      <p className="text-xs text-muted-foreground truncate">{track.album}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-muted-foreground">
                        <Clock className="w-3 h-3 inline mr-1" />
                        {Math.floor(track.duration / 60)}:{String(track.duration % 60).padStart(2, '0')}
                      </span>
                    </div>
                    <div className="flex gap-2">
                      {track.preview && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => playPreview(track.preview)}
                        >
                          <Play className="w-4 h-4" />
                        </Button>
                      )}
                      {track.itunesUrl && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => window.open(track.itunesUrl, '_blank')}
                        >
                          <ExternalLink className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="text-center">
            <Button onClick={onComplete} size="lg">
              ✅ 활동 완료하기
            </Button>
          </div>
        </>
      )}

      {!loading && tracks.length === 0 && searchQuery && (
        <div className="text-center py-12">
          <p className="text-muted-foreground">
            검색 결과가 없습니다. 다른 아티스트를 검색해보세요.
          </p>
        </div>
      )}

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