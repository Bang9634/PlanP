import { useState } from 'react';

export function useMusicPlayer() {
  const [currentAudio, setCurrentAudio] = useState<HTMLAudioElement | null>(null);

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

  return {
    currentAudio,
    playPreview,
    stopPreview,
  };
}