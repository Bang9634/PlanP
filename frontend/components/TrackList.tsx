import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Play, ExternalLink, Clock } from 'lucide-react';
import { Track } from '../services/MusicService';

interface TrackListProps {
  tracks: Track[];
  showRank?: boolean;
  showHotBadge?: boolean;
  onPlayPreview: (url: string) => void;
}

export function TrackList({ 
  tracks, 
  showRank = false, 
  showHotBadge = false,
  onPlayPreview 
}: TrackListProps) {
  return (
    <div className="grid gap-4">
      {tracks.map((track, index) => (
        <Card key={track.id} className="hover:shadow-md transition-shadow">
          <CardContent className="p-4">
            <div className="flex items-center gap-4">
              {/* 순위 (옵션) */}
              {showRank && (
                <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary text-primary-foreground text-sm font-medium flex-shrink-0">
                  {index + 1}
                </div>
              )}

              {/* 앨범 커버 */}
              <img 
                src={track.albumCover} 
                alt={track.album}
                className="w-16 h-16 rounded object-cover flex-shrink-0"
              />

              {/* 곡 정보 */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <h3 className="font-medium truncate">{track.title}</h3>
                  {showHotBadge && index < 3 && (
                    <Badge variant="destructive" className="text-xs flex-shrink-0">
                      HOT
                    </Badge>
                  )}
                </div>
                <p className="text-sm text-muted-foreground truncate">{track.artist}</p>
                <p className="text-xs text-muted-foreground truncate">{track.album}</p>
              </div>

              {/* 재생 시간 */}
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <Clock className="w-3 h-3" />
                {Math.floor(track.duration / 60)}:{String(track.duration % 60).padStart(2, '0')}
              </div>

              {/* 버튼 */}
              <div className="flex gap-2 flex-shrink-0">
                {track.preview && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => onPlayPreview(track.preview)}
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
  );
}