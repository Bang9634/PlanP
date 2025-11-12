import { useState } from 'react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { ArrowLeft, Music, CheckCircle2, Play } from 'lucide-react';

interface GenreExplorationActivityProps {
  onBack: () => void;
  onComplete: () => void;
}

interface Genre {
  id: string;
  name: string;
  description: string;
  emoji: string;
  color: string;
  sampleSongs: string[];
}

const genres: Genre[] = [
  {
    id: 'jazz',
    name: '재즈 (Jazz)',
    description: '부드럽고 즉흥적인 멜로디',
    emoji: '🎷',
    color: 'bg-amber-100 text-amber-700',
    sampleSongs: ['Take Five - Dave Brubeck', 'Fly Me to the Moon - Frank Sinatra', 'Autumn Leaves - Bill Evans']
  },
  {
    id: 'electronic',
    name: '일렉트로닉 (Electronic)',
    description: '신스사이저와 비트의 조화',
    emoji: '🎛️',
    color: 'bg-purple-100 text-purple-700',
    sampleSongs: ['Midnight City - M83', 'Strobe - Deadmau5', 'Opus - Eric Prydz']
  },
  {
    id: 'indie',
    name: '인디 (Indie)',
    description: '독창적이고 감성적인 사운드',
    emoji: '🎸',
    color: 'bg-green-100 text-green-700',
    sampleSongs: ['Somebody Else - The 1975', 'Young Folks - Peter Bjorn and John', 'Float On - Modest Mouse']
  },
  {
    id: 'classical',
    name: '클래식 (Classical)',
    description: '오케스트라의 웅장한 선율',
    emoji: '🎼',
    color: 'bg-blue-100 text-blue-700',
    sampleSongs: ['Canon in D - Pachelbel', 'Für Elise - Beethoven', 'The Four Seasons - Vivaldi']
  },
  {
    id: 'reggae',
    name: '레게 (Reggae)',
    description: '자메이카의 리듬감 넘치는 음악',
    emoji: '🌴',
    color: 'bg-yellow-100 text-yellow-700',
    sampleSongs: ['Three Little Birds - Bob Marley', 'No Woman No Cry - Bob Marley', 'Is This Love - Bob Marley']
  },
  {
    id: 'folk',
    name: '포크 (Folk)',
    description: '어쿠스틱 기타의 따뜻한 선율',
    emoji: '🪕',
    color: 'bg-orange-100 text-orange-700',
    sampleSongs: ['The Sound of Silence - Simon & Garfunkel', 'Blowin in the Wind - Bob Dylan', 'Big Yellow Taxi - Joni Mitchell']
  }
];

export function GenreExplorationActivity({ onBack, onComplete }: GenreExplorationActivityProps) {
  const [selectedGenre, setSelectedGenre] = useState<Genre | null>(null);
  const [currentStep, setCurrentStep] = useState<'select' | 'explore'>('select');

  const handleGenreSelect = (genre: Genre) => {
    setSelectedGenre(genre);
    setCurrentStep('explore');
  };

  const handleBackToGenres = () => {
    setCurrentStep('select');
    setSelectedGenre(null);
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <Button 
          variant="ghost" 
          onClick={currentStep === 'select' ? onBack : handleBackToGenres} 
          className="gap-2 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          뒤로가기
        </Button>
        <h2 className="mb-2">
          {currentStep === 'select' ? '새로운 장르 탐색' : `${selectedGenre?.name} 탐색`}
        </h2>
        <p className="text-muted-foreground">
          {currentStep === 'select' 
            ? '평소 안 듣던 음악 장르에 도전해보세요!' 
            : '이 장르의 대표적인 곡들을 들어보세요'}
        </p>
      </div>

      {currentStep === 'select' ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {genres.map((genre) => (
            <Card 
              key={genre.id}
              className="cursor-pointer hover:bg-muted/50 transition-all duration-200 hover:scale-105"
              onClick={() => handleGenreSelect(genre)}
            >
              <CardContent className="p-6">
                <div className="text-center mb-4">
                  <div className="text-4xl mb-3">{genre.emoji}</div>
                  <h3 className="font-medium mb-2">{genre.name}</h3>
                  <p className="text-sm text-muted-foreground">
                    {genre.description}
                  </p>
                </div>
                <div className={`text-center py-2 px-3 rounded-lg text-sm ${genre.color}`}>
                  탐색하기
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : selectedGenre && (
        <div className="space-y-6">
          <Card>
            <CardHeader className="text-center">
              <div className="text-6xl mb-4">{selectedGenre.emoji}</div>
              <CardTitle>{selectedGenre.name}</CardTitle>
              <CardDescription>
                {selectedGenre.description}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <h4 className="font-medium mb-4 text-center">추천 곡 목록</h4>
              <div className="space-y-3">
                {selectedGenre.sampleSongs.map((song, index) => (
                  <div key={index} className="flex items-center gap-3 p-4 bg-muted/50 rounded-lg">
                    <div className={`p-2 rounded-full ${selectedGenre.color}`}>
                      <Music className="w-4 h-4" />
                    </div>
                    <span className="flex-1 font-medium">{song}</span>
                    <Button 
                      size="sm" 
                      variant="outline"
                      className="gap-2"
                      onClick={() => window.open(`https://www.youtube.com/results?search_query=${encodeURIComponent(song)}`, '_blank')}
                    >
                      <Play className="w-3 h-3" />
                      듣기
                    </Button>
                  </div>
                ))}
              </div>

              <div className="mt-8 p-4 bg-gradient-to-r from-primary/5 to-purple-500/5 rounded-lg border border-primary/10">
                <h4 className="font-medium mb-2">💡 탐색 팁</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• 각 곡을 최소 30초 이상 들어보세요</li>
                  <li>• 마음에 드는 곡이 있다면 비슷한 아티스트를 찾아보세요</li>
                  <li>• 이 장르의 특징적인 악기나 리듬에 주목해보세요</li>
                </ul>
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
                  새로운 장르를 탐색했다면 완료해주세요!
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}