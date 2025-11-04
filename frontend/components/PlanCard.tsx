import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Clock, Users, Star } from "lucide-react";

interface PlanCardProps {
  id: string;
  title: string;
  description: string;
  duration: string;
  difficulty: 'easy' | 'medium' | 'hard';
  tags: string[];
  participants?: string;
  rating?: number;
  onClick?: (planId: string) => void;
}

export function PlanCard({ id, title, description, duration, difficulty, tags, participants, rating, onClick }: PlanCardProps) {
  const difficultyColors = {
    easy: 'bg-green-100 text-green-700',
    medium: 'bg-yellow-100 text-yellow-700',
    hard: 'bg-red-100 text-red-700'
  };

  const difficultyText = {
    easy: '쉬움',
    medium: '보통',
    hard: '어려움'
  };

  return (
    <Card 
      className="hover:shadow-lg transition-shadow cursor-pointer group"
      onClick={() => onClick?.(id)}
    >
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <CardTitle className="group-hover:text-primary transition-colors">{title}</CardTitle>
          {rating && (
            <div className="flex items-center gap-1">
              <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
              <span className="text-sm">{rating}</span>
            </div>
          )}
        </div>
        <CardDescription className="mt-2">{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex flex-wrap gap-2 mb-3">
          {tags.map((tag) => (
            <Badge key={tag} variant="secondary" className="text-xs">
              {tag}
            </Badge>
          ))}
        </div>
        
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1">
              <Clock className="w-4 h-4" />
              <span>{duration}</span>
            </div>
            {participants && (
              <div className="flex items-center gap-1">
                <Users className="w-4 h-4" />
                <span>{participants}</span>
              </div>
            )}
          </div>
          <Badge className={difficultyColors[difficulty]}>
            {difficultyText[difficulty]}
          </Badge>
        </div>
      </CardContent>
    </Card>
  );
}