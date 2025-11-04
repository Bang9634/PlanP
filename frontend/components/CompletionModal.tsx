import { useEffect, useState } from 'react';
import { Dialog, DialogContent, DialogTitle, DialogDescription } from './ui/dialog';
import { Button } from './ui/button';
import { Trophy, Sparkles, Home } from 'lucide-react';

interface CompletionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onHomeReturn: () => void;
}

export function CompletionModal({ isOpen, onClose, onHomeReturn }: CompletionModalProps) {
  const [showAnimation, setShowAnimation] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setShowAnimation(true);
    } else {
      setShowAnimation(false);
    }
  }, [isOpen]);

  const handleHomeReturn = () => {
    onClose();
    onHomeReturn();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md border-0 bg-gradient-to-br from-green-50 to-blue-50">
        <DialogTitle className="sr-only">활동 완료 축하</DialogTitle>
        <DialogDescription className="sr-only">
          계획을 성공적으로 완료했음을 알리는 축하 메시지입니다.
        </DialogDescription>
        <div className="text-center py-8">
          {/* 애니메이션 효과 */}
          <div className={`transition-all duration-1000 ${showAnimation ? 'scale-100 opacity-100' : 'scale-50 opacity-0'}`}>
            <div className="relative mb-6">
              <div className="text-8xl mb-4">🎉</div>
              {/* 반짝이는 효과 */}
              <div className="absolute top-0 left-1/2 transform -translate-x-1/2 -translate-y-2">
                <Sparkles className="w-6 h-6 text-yellow-500 animate-pulse" />
              </div>
              <div className="absolute bottom-4 left-1/4 transform -translate-x-1/2">
                <Sparkles className="w-4 h-4 text-purple-500 animate-pulse delay-300" />
              </div>
              <div className="absolute bottom-4 right-1/4 transform translate-x-1/2">
                <Sparkles className="w-5 h-5 text-pink-500 animate-pulse delay-700" />
              </div>
            </div>
          </div>

          {/* 메시지 */}
          <div className={`transition-all duration-1000 delay-500 ${showAnimation ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}`}>
            <h2 className="text-2xl font-bold text-green-700 mb-2">
              축하합니다!
            </h2>
            <p className="text-lg text-green-600 mb-4">
              계획을 완료하셨네요.
            </p>
            <div className="flex items-center justify-center gap-2 mb-6">
              <Trophy className="w-5 h-5 text-yellow-500" />
              <p className="text-green-600 font-medium">
                성취감이 쌓였어요!
              </p>
              <Trophy className="w-5 h-5 text-yellow-500" />
            </div>
          </div>

          {/* 액션 버튼 */}
          <div className={`transition-all duration-1000 delay-1000 ${showAnimation ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}`}>
            <div className="space-y-3">
              <Button 
                onClick={handleHomeReturn}
                className="w-full gap-2 bg-gradient-to-r from-green-500 to-blue-500 hover:from-green-600 hover:to-blue-600 text-white border-0 shadow-lg"
                size="lg"
              >
                <Home className="w-5 h-5" />
                홈으로 돌아가기
              </Button>
              
              <p className="text-sm text-muted-foreground">
                더 많은 재미있는 계획들이 기다리고 있어요! ✨
              </p>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}