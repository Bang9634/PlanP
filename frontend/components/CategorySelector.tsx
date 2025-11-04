import { Button } from "./ui/button";
import { Music, Coffee, Heart, Camera, Dumbbell, Plane, BookOpen, Palette } from "lucide-react";

interface CategorySelectorProps {
  selectedCategory: string | null;
  onCategorySelect: (category: string) => void;
}

const categories = [
  { id: 'music', name: '음악', icon: Music, color: 'bg-purple-100 hover:bg-purple-200 text-purple-700' },
  { id: 'daily', name: '일상', icon: Coffee, color: 'bg-amber-100 hover:bg-amber-200 text-amber-700' },
  { id: 'travel', name: '여행', icon: Plane, color: 'bg-blue-100 hover:bg-blue-200 text-blue-700' },
  { id: 'study', name: '공부', icon: BookOpen, color: 'bg-green-100 hover:bg-green-200 text-green-700' },
  { id: 'hobby', name: '취미', icon: Palette, color: 'bg-orange-100 hover:bg-orange-200 text-orange-700' },
  { id: 'social', name: '사교', icon: Heart, color: 'bg-pink-100 hover:bg-pink-200 text-pink-700' },
  { id: 'culture', name: '문화', icon: Camera, color: 'bg-indigo-100 hover:bg-indigo-200 text-indigo-700' },
  { id: 'exercise', name: '운동', icon: Dumbbell, color: 'bg-red-100 hover:bg-red-200 text-red-700' },
];

export function CategorySelector({ selectedCategory, onCategorySelect }: CategorySelectorProps) {
  return (
    <div className="flex flex-wrap justify-center gap-3 max-w-2xl mx-auto">
      {categories.map((category) => {
        const Icon = category.icon;
        return (
          <Button
            key={category.id}
            variant="ghost"
            onClick={() => onCategorySelect(category.id)}
            className={`
              flex items-center gap-2 px-4 py-6 rounded-lg border-2 transition-all
              ${selectedCategory === category.id 
                ? 'border-current shadow-md' 
                : 'border-transparent hover:border-gray-200'
              }
              ${category.color}
            `}
          >
            <Icon className="w-5 h-5" />
            <span>{category.name}</span>
          </Button>
        );
      })}
    </div>
  );
}