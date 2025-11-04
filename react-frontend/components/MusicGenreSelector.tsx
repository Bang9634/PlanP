import { Button } from "./ui/button";
import { Music, Heart, Zap, Globe, Mic, Piano, Guitar, Volume2 } from "lucide-react";

interface MusicGenreSelectorProps {
  selectedGenre: string | null;
  onGenreSelect: (genre: string) => void;
}

const musicGenres = [
  { id: 'pop', name: 'POP', icon: Music, color: 'bg-pink-100 hover:bg-pink-200 text-pink-700', description: '대중적인 팝 음악' },
  { id: 'kpop', name: 'K-POP', icon: Heart, color: 'bg-red-100 hover:bg-red-200 text-red-700', description: '한국 아이돌/팝' },
  { id: 'jpop', name: 'J-POP', icon: Globe, color: 'bg-orange-100 hover:bg-orange-200 text-orange-700', description: '일본 팝 음악' },
  { id: 'rock', name: '록', icon: Zap, color: 'bg-purple-100 hover:bg-purple-200 text-purple-700', description: '록/하드록' },
  { id: 'ballad', name: '발라드', icon: Piano, color: 'bg-blue-100 hover:bg-blue-200 text-blue-700', description: '감성 발라드' },
  { id: 'hiphop', name: '힙합', icon: Mic, color: 'bg-gray-100 hover:bg-gray-200 text-gray-700', description: '랩/힙합' },
  { id: 'indie', name: '인디', icon: Guitar, color: 'bg-green-100 hover:bg-green-200 text-green-700', description: '인디/얼터너티브' },
  { id: 'edm', name: 'EDM', icon: Volume2, color: 'bg-indigo-100 hover:bg-indigo-200 text-indigo-700', description: '일렉트로닉 댄스' },
];

export function MusicGenreSelector({ selectedGenre, onGenreSelect }: MusicGenreSelectorProps) {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-4 text-center text-muted-foreground">
          어떤 장르의 음악을 들어볼까요? 🎵
        </h3>
        <div className="flex flex-wrap justify-center gap-3 max-w-3xl mx-auto">
          {musicGenres.map((genre) => {
            const Icon = genre.icon;
            return (
              <Button
                key={genre.id}
                variant="outline"
                onClick={() => onGenreSelect(genre.id)}
                className={`
                  flex flex-col items-center gap-2 px-4 py-6 h-auto min-h-[80px] transition-all
                  ${selectedGenre === genre.id 
                    ? 'border-current shadow-md scale-105' 
                    : 'border-transparent hover:border-gray-200'
                  }
                  ${genre.color}
                `}
              >
                <Icon className="w-6 h-6" />
                <div className="text-center">
                  <div className="font-medium">{genre.name}</div>
                  <div className="text-xs text-muted-foreground mt-1">
                    {genre.description}
                  </div>
                </div>
              </Button>
            );
          })}
        </div>
      </div>
    </div>
  );
}