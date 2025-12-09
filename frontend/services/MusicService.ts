import { apiService } from './api';

const ITUNES_API_BASE = 'https://itunes.apple.com';

export interface Track {
  id: number;
  title: string;
  artist: string;
  artistPicture?: string;
  album: string;
  albumCover: string;
  albumCoverLarge?: string;
  preview: string;
  duration: number;
  releaseDate?: string;
  genre?: string;
  itunesUrl?: string;
}

class MusicService {
  private readonly MOOD_KEYWORDS: { [key: string]: string } = {
    'CHILL': 'chill relax',
    'PARTY': 'party dance upbeat',
    'WORKOUT': 'workout motivation energy',
    'FOCUS': 'study focus concentration',
    'HAPPY': 'happy cheerful joy',
    'SAD': 'sad emotional melancholy',
    'ROMANTIC': 'love romantic',
    'ENERGETIC': 'energetic powerful',
  };

  /**
   * AI 추천 + iTunes 검색 통합
   */
  async getTracksByGenreWithAI(genre: string, limit: number = 20): Promise<Track[]> {
    try {
      console.log(`🤖 Gemini AI 추천 시작: ${genre}`);

      // 1. 백엔드 Gemini AI에서 곡 제목 추천받기
      const aiRecommendations = await apiService.getAIRecommendationsByGenre(genre, 15);

      if (aiRecommendations.length === 0) {
        console.warn('⚠️ AI 추천 결과 없음, 기본 검색으로 전환');
        return this.searchTracks(genre, limit);
      }

      console.log(`✅ Gemini AI 추천 ${aiRecommendations.length}곡:`, aiRecommendations);

      // 2. 추천받은 곡들을 iTunes에서 검색
      const searchPromises = aiRecommendations.map(songTitle =>
        this.searchSingleTrack(songTitle)
      );

      const results = await Promise.allSettled(searchPromises);

      // 3. 성공한 검색 결과만 모으기
      const tracks: Track[] = results
        .filter((result): result is PromiseFulfilledResult<Track | null> =>
          result.status === 'fulfilled' && result.value !== null
        )
        .map(result => result.value!);

      console.log(`🎵 iTunes 검색 완료: ${tracks.length}/${aiRecommendations.length} 곡 찾음`);

      // 4. 결과가 부족하면 기본 검색으로 보충
      if (tracks.length < limit / 2) {
        console.log(`⚠️ iTunes 검색 결과 부족 (${tracks.length}곡), 기본 검색으로 보충`);
        const additionalTracks = await this.searchTracks(genre, limit - tracks.length);
        tracks.push(...additionalTracks);
      }

      return tracks.slice(0, limit);

    } catch (error) {
      console.error('❌ AI 기반 검색 실패, 기본 검색으로 전환:', error);
      return this.searchTracks(genre, limit);
    }
  }

  /**
   * 단일 곡 제목으로 iTunes 검색
   */
  private async searchSingleTrack(songTitle: string): Promise<Track | null> {
    try {
      const searchQuery = songTitle.trim();
      const url = `${ITUNES_API_BASE}/search?term=${encodeURIComponent(searchQuery)}&media=music&entity=song&limit=1&country=KR`;

      const response = await fetch(url);
      
      if (!response.ok) {
        console.warn(`iTunes 검색 실패 (${response.status}): ${songTitle}`);
        return null;
      }
      
      const data = await response.json();

      if (data.results && data.results.length > 0) {
        const tracks = this.parseTracksResponse({ results: [data.results[0]] });
        return tracks.length > 0 ? tracks[0] : null;
      }

      console.warn(`⚠️ iTunes 검색 결과 없음: ${songTitle}`);
      return null;
      
    } catch (error) {
      console.warn(`❌ iTunes 검색 실패: ${songTitle}`, error);
      return null;
    }
  }

  /**
   * 트랙 검색 (기본)
   */
  async searchTracks(query: string, limit: number = 20): Promise<Track[]> {
    try {
      const url = `${ITUNES_API_BASE}/search?term=${encodeURIComponent(query)}&media=music&entity=song&limit=${limit}&country=KR`;
      
      console.log('🎵 iTunes 검색:', query);
      
      const response = await fetch(url);
      
      if (!response.ok) {
        console.error('iTunes API 호출 실패:', response.status);
        return [];
      }
      
      const data = await response.json();
      return this.parseTracksResponse(data);
      
    } catch (error) {
      console.error('트랙 검색 실패:', error);
      return [];
    }
  }

  /**
   * 아티스트 검색
   */
  async searchArtist(artistName: string, limit: number = 20): Promise<Track[]> {
    try {
      const url = `${ITUNES_API_BASE}/search?term=${encodeURIComponent(artistName)}&media=music&entity=song&attribute=artistTerm&limit=${limit}&country=KR`;
      
      console.log('🎤 아티스트 검색:', artistName);
      
      const response = await fetch(url);
      const data = await response.json();
      return this.parseTracksResponse(data);
      
    } catch (error) {
      console.error('아티스트 검색 실패:', error);
      return [];
    }
  }

  /**
   * 장르별 검색 (폴백용)
   */
  async getTracksByGenre(genre: string, limit: number = 20): Promise<Track[]> {
    return this.searchTracks(genre, limit);
  }

  /**
   * 분위기별 추천
   */
  async getTracksByMood(mood: string, limit: number = 20): Promise<Track[]> {
    const keyword = this.MOOD_KEYWORDS[mood.toUpperCase()] || mood;
    console.log('😊 분위기별 검색:', mood, '→', keyword);
    return this.searchTracks(keyword, limit);
  }

  /**
   * 인기 차트 (K-POP) - ✅ 추가
   */
  async getTopTracks(limit: number = 20): Promise<Track[]> {
    console.log('🔥 인기 차트 로딩');
    return this.searchTracks('kpop 2024 popular', limit);
  }

  /**
   * JSON 응답 파싱
   */
  private parseTracksResponse(data: any): Track[] {
    if (!data.results || !Array.isArray(data.results)) {
      return [];
    }
    
    return data.results
      .filter((item: any) => item.kind === 'song' && item.previewUrl)
      .map((item: any) => ({
        id: item.trackId,
        title: item.trackName,
        artist: item.artistName,
        artistPicture: item.artistViewUrl,
        album: item.collectionName || '',
        albumCover: item.artworkUrl100 || item.artworkUrl60 || '',
        albumCoverLarge: item.artworkUrl100?.replace('100x100', '600x600') || '',
        preview: item.previewUrl || '',
        duration: Math.floor((item.trackTimeMillis || 0) / 1000),
        releaseDate: item.releaseDate ? new Date(item.releaseDate).toLocaleDateString('ko-KR') : undefined,
        genre: item.primaryGenreName,
        itunesUrl: item.trackViewUrl,
      }));
  }
}

export default new MusicService();