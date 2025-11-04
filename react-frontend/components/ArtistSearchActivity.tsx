import { useState } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { ArrowLeft, Search, Music, CheckCircle2 } from 'lucide-react';

interface ArtistSearchActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface Artist {
  id: string;
  name: string;
  newSongs: string[];
  image?: string;
}

// 샘플 가수 데이터
const sampleArtists: Artist[] = [
  {
    id: 'iu',
    name: '아이유(IU)',
    newSongs: ['Love wins all', 'Holssi', '아이와 나의 바다'],
    image: '🎤'
  },
  {
    id: 'bts',
    name: 'BTS',
    newSongs: ['Dynamite', 'Butter', 'Permission to Dance'],
    image: '🎵'
  },
  {
    id: 'newjeans',
    name: 'NewJeans',
    newSongs: ['Get Up', 'Super Shy', 'ETA'],
    image: '🌟'
  },
  {
    id: 'lesserafim',
    name: 'LE SSERAFIM',
    newSongs: ['UNFORGIVEN', 'Eve, Psyche & The Bluebeard', 'CRAZY'],
    image: '🔥'
  },
  {
    id: 'aespa',
    name: 'aespa',
    newSongs: ['Spicy', 'Better Things', 'Drama'],
    image: '✨'
  }
];

export function ArtistSearchActivity({ onBack, onComplete }: ArtistSearchActivityProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedArtist, setSelectedArtist] = useState<Artist | null>(null);
  const [showResults, setShowResults] = useState(false);

  const handleSearch = () => {
    if (searchTerm.trim()) {
      setShowResults(true);
    }
  };

  const handleArtistSelect = (artist: Artist) => {
    setSelectedArtist(artist);
  };

  const filteredArtists = sampleArtists.filter(artist =>
    artist.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const displayArtists = showResults && searchTerm ? filteredArtists : sampleArtists;

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="gap-2 mb-4">
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">가수 신곡 추천</h2>
        <p className="text-muted-foreground">
          좋아하는 가수를 검색하고 최신곡을 발견해보세요!
        </p>
      </div>

      {!selectedArtist ? (
        <div className="space-y-6">
          {/* 검색 영역 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Search className="w-5 h-5" />
                가수 검색
              </CardTitle>
              <CardDescription>
                가수 이름을 입력하거나 아래 추천 목록에서 선택해보세요
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex gap-2">
                <Input
                  placeholder="가수 이름을 입력하세요 (예: 아이유, BTS)"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                />
                <Button onClick={handleSearch}>검색</Button>
              </div>
            </CardContent>
          </Card>

          {/* 가수 목록 */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {displayArtists.map((artist) => (
              <Card 
                key={artist.id}
                className="cursor-pointer hover:bg-muted/50 transition-all duration-200 hover:scale-105"
                onClick={() => handleArtistSelect(artist)}
              >
                <CardContent className="p-6 text-center">
                  <div className="text-4xl mb-3">{artist.image}</div>
                  <h3 className="font-medium mb-2">{artist.name}</h3>
                  <p className="text-sm text-muted-foreground">
                    신곡 {artist.newSongs.length}곡 보기
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>

          {showResults && filteredArtists.length === 0 && (
            <div className="text-center py-8">
              <p className="text-muted-foreground">
                검색 결과가 없습니다. 다른 가수를 검색해보세요.
              </p>
            </div>
          )}
        </div>
      ) : (
        /* 선택된 가수의 신곡 표시 */
        <div className="space-y-6">
          <Card>
            <CardHeader className="text-center">
              <div className="text-6xl mb-4">{selectedArtist.image}</div>
              <CardTitle>{selectedArtist.name}의 최신곡</CardTitle>
              <CardDescription>
                따끈따끈한 신곡들을 확인해보세요!
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {selectedArtist.newSongs.map((song, index) => (
                  <div key={index} className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg">
                    <Music className="w-5 h-5 text-primary" />
                    <span className="flex-1 font-medium">{song}</span>
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={() => window.open(`https://www.youtube.com/results?search_query=${encodeURIComponent(selectedArtist.name + ' ' + song)}`, '_blank')}
                    >
                      듣기
                    </Button>
                  </div>
                ))}
              </div>

              <div className="text-center mt-8">
                <Button 
                  onClick={onComplete}
                  className="gap-2 bg-green-600 hover:bg-green-700"
                  size="lg"
                >
                  <CheckCircle2 className="w-5 h-5" />
                  활동 완료하기! 🎉
                </Button>
                <p className="text-sm text-muted-foreground mt-3">
                  새로운 음악을 발견했다면 완료해주세요!
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}