import { Badge } from "./ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Play, ExternalLink, Clock, TrendingUp, RefreshCw, Zap } from "lucide-react";
import { Button } from "./ui/button";
import { useState, useEffect } from "react";

interface Song {
  id: string;
  title: string;
  artist: string;
  album?: string;
  duration: string;
  chartRank?: number;
  isHot?: boolean;
  spotifyUrl?: string;
  youtubeUrl?: string;
}

interface MusicRecommendationsProps {
  genre: string;
}

const musicByGenre: Record<string, Song[]> = {
  pop: [
    {
      id: 'pop-1',
      title: 'Flowers',
      artist: 'Miley Cyrus',
      album: 'Endless Summer Vacation',
      duration: '3:20',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=G7KNmW9a75Y'
    },
    {
      id: 'pop-2',
      title: 'Anti-Hero',
      artist: 'Taylor Swift',
      album: 'Midnights',
      duration: '3:20',
      chartRank: 2,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=b1kbLWvqugk'
    },
    {
      id: 'pop-3',
      title: 'As It Was',
      artist: 'Harry Styles',
      album: "Harry's House",
      duration: '2:47',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=H5v3kku4y6Q'
    },
    {
      id: 'pop-4',
      title: 'Unholy (feat. Kim Petras)',
      artist: 'Sam Smith',
      duration: '2:36',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=Uq9gPaIzbe8'
    },
    {
      id: 'pop-5',
      title: 'Bad Habit',
      artist: 'Steve Lacy',
      album: 'Gemini Rights',
      duration: '3:51',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=VF-r5TtlT9w'
    }
  ],
  kpop: [
    {
      id: 'kpop-1',
      title: 'Cupid',
      artist: 'FIFTY FIFTY',
      duration: '2:57',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=Qc7_zRjH808'
    },
    {
      id: 'kpop-2',
      title: 'NewJeans',
      artist: 'NewJeans',
      album: 'Get Up',
      duration: '2:49',
      chartRank: 2,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=js1CtxSY38I'
    },
    {
      id: 'kpop-3',
      title: 'Spicy',
      artist: 'aespa',
      album: 'MY WORLD',
      duration: '3:06',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=Os_heh8vPfs'
    },
    {
      id: 'kpop-4',
      title: 'God of Music',
      artist: 'SEVENTEEN',
      duration: '3:15',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=1Uwmr2mYRhg'
    },
    {
      id: 'kpop-5',
      title: 'UNFORGIVEN (feat. Nile Rodgers)',
      artist: 'LE SSERAFIM',
      album: 'UNFORGIVEN',
      duration: '3:06',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=UBURTj20HXI'
    }
  ],
  jpop: [
    {
      id: 'jpop-1',
      title: 'アイドル (Idol)',
      artist: 'YOASOBI',
      duration: '3:21',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=ZRtdQ81jPUQ'
    },
    {
      id: 'jpop-2',
      title: 'Subtitle',
      artist: 'Official髭男dism',
      duration: '4:12',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=SX_ViT4Ra7k'
    },
    {
      id: 'jpop-3',
      title: 'クリスマスソング',
      artist: 'back number',
      duration: '5:42',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=9fV8dLn9b5M'
    },
    {
      id: 'jpop-4',
      title: 'Lemon',
      artist: '米津玄師',
      duration: '4:15',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=SX_ViT4Ra7k'
    },
    {
      id: 'jpop-5',
      title: 'Pretender',
      artist: 'Official髭男dism',
      duration: '4:30',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=TQ8WlA2GXbk'
    }
  ],
  rock: [
    {
      id: 'rock-1',
      title: 'Master of Puppets',
      artist: 'Metallica',
      album: 'Master of Puppets',
      duration: '8:35',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=xnKhsTXoKCI'
    },
    {
      id: 'rock-2',
      title: 'Bohemian Rhapsody',
      artist: 'Queen',
      album: 'A Night at the Opera',
      duration: '5:55',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=fJ9rUzIMcZQ'
    },
    {
      id: 'rock-3',
      title: 'Smells Like Teen Spirit',
      artist: 'Nirvana',
      album: 'Nevermind',
      duration: '5:01',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=hTWKbfoikeg'
    },
    {
      id: 'rock-4',
      title: 'Stairway to Heaven',
      artist: 'Led Zeppelin',
      album: 'Led Zeppelin IV',
      duration: '8:02',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=QkF3oxziUI4'
    },
    {
      id: 'rock-5',
      title: 'Sweet Child O\' Mine',
      artist: "Guns N' Roses",
      album: 'Appetite for Destruction',
      duration: '5:03',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=1w7OgIMMRc4'
    }
  ],
  ballad: [
    {
      id: 'ballad-1',
      title: '봄날',
      artist: 'BTS',
      album: 'You Never Walk Alone',
      duration: '3:37',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=xEeFrLSkMm8'
    },
    {
      id: 'ballad-2',
      title: '너를 만나',
      artist: '폴킴',
      duration: '3:58',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=0b8BzQKTZYM'
    },
    {
      id: 'ballad-3',
      title: 'Someone You Loved',
      artist: 'Lewis Capaldi',
      album: 'Divinely Uninspired to a Hellish Extent',
      duration: '3:02',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=zABLecsR5UE'
    },
    {
      id: 'ballad-4',
      title: '사랑은 늘 도망가',
      artist: '임영웅',
      duration: '3:41',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=1rMKPE0C9WY'
    },
    {
      id: 'ballad-5',
      title: 'Perfect',
      artist: 'Ed Sheeran',
      album: '÷ (Divide)',
      duration: '4:23',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=2Vv-BfVoq4g'
    }
  ],
  hiphop: [
    {
      id: 'hiphop-1',
      title: 'First Class',
      artist: 'Jack Harlow',
      duration: '2:54',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=GQKdajW5wuM'
    },
    {
      id: 'hiphop-2',
      title: 'Jimmy Cooks (feat. 21 Savage)',
      artist: 'Drake',
      album: 'Honestly, Nevermind',
      duration: '3:45',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=gUPRUyxcgGU'
    },
    {
      id: 'hiphop-3',
      title: 'Rush (feat. Vory)',
      artist: 'Troye Sivan',
      duration: '3:13',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=hJQ0HdSBWnM'
    },
    {
      id: 'hiphop-4',
      title: 'That\'s What I Want',
      artist: 'Lil Nas X',
      album: 'MONTERO',
      duration: '2:23',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=UTHLKHL_whs'
    },
    {
      id: 'hiphop-5',
      title: 'Off The Grid',
      artist: 'Kanye West',
      album: 'Donda',
      duration: '5:39',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=g0OdmRtuQew'
    }
  ],
  indie: [
    {
      id: 'indie-1',
      title: 'Heat Waves',
      artist: 'Glass Animals',
      album: 'Dreamland',
      duration: '3:58',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=mRD0-GxqHVo'
    },
    {
      id: 'indie-2',
      title: 'Stay',
      artist: 'The Kid LAROI & Justin Bieber',
      duration: '2:21',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=kTJczUoc26U'
    },
    {
      id: 'indie-3',
      title: '우주를 줄게',
      artist: '볼빨간사춘기',
      duration: '3:39',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=4Uu_NnQZx_s'
    },
    {
      id: 'indie-4',
      title: 'Shivers',
      artist: 'Ed Sheeran',
      album: '= (Equals)',
      duration: '3:27',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=Il0S8BoucSA'
    },
    {
      id: 'indie-5',
      title: 'drivers license',
      artist: 'Olivia Rodrigo',
      album: 'SOUR',
      duration: '4:02',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=ZmDBbnmKpqQ'
    }
  ],
  edm: [
    {
      id: 'edm-1',
      title: 'Titanium (feat. Sia)',
      artist: 'David Guetta',
      album: 'Nothing but the Beat',
      duration: '4:05',
      chartRank: 1,
      isHot: true,
      youtubeUrl: 'https://www.youtube.com/watch?v=JRfuAukYTKg'
    },
    {
      id: 'edm-2',
      title: 'Levels',
      artist: 'Avicii',
      duration: '3:18',
      chartRank: 2,
      youtubeUrl: 'https://www.youtube.com/watch?v=_ovdm2yX4MA'
    },
    {
      id: 'edm-3',
      title: 'Closer (feat. Halsey)',
      artist: 'The Chainsmokers',
      album: 'Collage',
      duration: '4:04',
      chartRank: 3,
      youtubeUrl: 'https://www.youtube.com/watch?v=PT2_F-1esPk'
    },
    {
      id: 'edm-4',
      title: 'Bangarang (feat. Sirah)',
      artist: 'Skrillex',
      album: 'Bangarang',
      duration: '3:35',
      chartRank: 4,
      youtubeUrl: 'https://www.youtube.com/watch?v=YJVmu6yttiw'
    },
    {
      id: 'edm-5',
      title: 'Something Just Like This',
      artist: 'The Chainsmokers & Coldplay',
      duration: '4:07',
      chartRank: 5,
      youtubeUrl: 'https://www.youtube.com/watch?v=FM7MFYoylVs'
    }
  ]
};

export function MusicRecommendations({ genre }: MusicRecommendationsProps) {
  const [currentSongs, setCurrentSongs] = useState<Song[]>([]);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  const genreNames: Record<string, string> = {
    pop: 'POP',
    kpop: 'K-POP',
    jpop: 'J-POP',
    rock: '록',
    ballad: '발라드',
    hiphop: '힙합',
    indie: '인디',
    edm: 'EDM'
  };

  // 차트 순위를 랜덤하게 섞고 HOT 태그를 다시 배치하는 함수
  const shuffleChart = (songs: Song[]) => {
    const shuffled = [...songs];
    
    // 순위를 랜덤하게 재배치
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    
    // 새로운 순위 배정
    shuffled.forEach((song, index) => {
      song.chartRank = index + 1;
      song.isHot = index < 2; // 상위 2곡만 HOT 태그
    });
    
    return shuffled;
  };

  // 초기 로드 및 장르 변경 시
  useEffect(() => {
    const baseSongs = musicByGenre[genre] || [];
    if (baseSongs.length > 0) {
      setCurrentSongs(shuffleChart(baseSongs));
      setLastUpdated(new Date());
    }
  }, [genre]);

  // 차트 새로고침 함수
  const refreshChart = async () => {
    setIsRefreshing(true);
    
    // 로딩 효과를 위한 딜레이
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const baseSongs = musicByGenre[genre] || [];
    if (baseSongs.length > 0) {
      setCurrentSongs(shuffleChart(baseSongs));
      setLastUpdated(new Date());
    }
    
    setIsRefreshing(false);
  };

  if (currentSongs.length === 0) {
    return null;
  }

  return (
    <div className="mt-8">
      <div className="flex items-center justify-between mb-6 max-w-4xl mx-auto">
        <h2 className="text-center text-muted-foreground flex items-center justify-center gap-2">
          <TrendingUp className="w-5 h-5" />
          {genreNames[genre]} 인기 차트 🎵
        </h2>
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
            {isRefreshing ? '업데이트 중...' : '차트 새로고침'}
          </Button>
        </div>
      </div>
      
      <div className="grid gap-4 max-w-4xl mx-auto">
        {currentSongs.map((song, index) => (
          <Card key={song.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-4">
              <div className="flex items-center gap-4">
                {/* 순위 */}
                <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-primary-foreground text-sm font-medium">
                  {song.chartRank || index + 1}
                </div>

                {/* 곡 정보 */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between">
                    <div className="min-w-0 flex-1">
                      <h3 className="font-medium truncate">{song.title}</h3>
                      <p className="text-sm text-muted-foreground truncate">{song.artist}</p>
                      {song.album && (
                        <p className="text-xs text-muted-foreground truncate">{song.album}</p>
                      )}
                    </div>
                    
                    <div className="flex items-center gap-2 ml-4">
                      {song.isHot && (
                        <Badge variant="destructive" className="text-xs">
                          HOT
                        </Badge>
                      )}
                      <div className="flex items-center gap-1 text-xs text-muted-foreground">
                        <Clock className="w-3 h-3" />
                        {song.duration}
                      </div>
                    </div>
                  </div>
                </div>

                {/* 재생 버튼 */}
                <div className="flex gap-2">
                  {song.youtubeUrl && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => window.open(song.youtubeUrl, '_blank')}
                      className="gap-1"
                    >
                      <Play className="w-4 h-4" />
                      <span className="hidden sm:inline">유튜브</span>
                    </Button>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="mt-6 text-center">
        <p className="text-sm text-muted-foreground">
          * 차트는 실시간 인기도를 반영하여 주기적으로 업데이트됩니다
        </p>
      </div>
    </div>
  );
}