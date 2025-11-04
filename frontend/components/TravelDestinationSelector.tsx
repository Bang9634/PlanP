import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { MapPin, Search, Globe } from "lucide-react";

interface TravelDestinationSelectorProps {
  selectedDestination: string | null;
  onDestinationSelect: (destination: string) => void;
}

const domesticDestinations = [
  { id: 'seoul-gangnam', country: '한국', city: '서울/강남', fullName: '서울/강남' },
  { id: 'seoul-hongdae', country: '한국', city: '서울/홍대', fullName: '서울/홍대' },
  { id: 'busan-gwangalli', country: '한국', city: '부산/광안리', fullName: '부산/광안리' },
  { id: 'busan-haeundae', country: '한국', city: '부산/해운대', fullName: '부산/해운대' },
  { id: 'jeju-seogwipo', country: '한국', city: '제주/서귀포', fullName: '제주/서귀포' },
  { id: 'jeju-jejucity', country: '한국', city: '제주/제주시', fullName: '제주/제주시' },
  { id: 'gangneung-city', country: '한국', city: '강릉/시내', fullName: '강릉/시내' },
  { id: 'gyeongju-bulguksa', country: '한국', city: '경주/불국사', fullName: '경주/불국사' },
];

const internationalDestinations = [
  { id: 'japan-tokyo', country: '일본', city: '도쿄/시부야', fullName: '일본/도쿄/시부야' },
  { id: 'japan-osaka', country: '일본', city: '오사카/도톤보리', fullName: '일본/오사카/도톤보리' },
  { id: 'thailand-bangkok', country: '태국', city: '방콕/시암', fullName: '태국/방콕/시암' },
  { id: 'thailand-phuket', country: '태국', city: '푸켓/파통', fullName: '태국/푸켓/파통' },
  { id: 'vietnam-danang', country: '베트남', city: '다낭/한시장', fullName: '베트남/다낭/한시장' },
  { id: 'vietnam-hochiminh', country: '베트남', city: '호치민/벤탄시장', fullName: '베트남/호치민/벤탄시장' },
  { id: 'china-beijing', country: '중국', city: '베이징/왕푸징', fullName: '중국/베이징/왕푸징' },
  { id: 'usa-newyork', country: '미국', city: '뉴욕/맨하탄', fullName: '미국/뉴욕/맨하탄' },
];

export function TravelDestinationSelector({ selectedDestination, onDestinationSelect }: TravelDestinationSelectorProps) {
  const [customCountry, setCustomCountry] = useState("");
  const [customCity, setCustomCity] = useState("");

  const handleCustomDestinationSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (customCountry.trim() && customCity.trim()) {
      const fullDestination = `${customCountry.trim()}/${customCity.trim()}`;
      onDestinationSelect(fullDestination);
      setCustomCountry("");
      setCustomCity("");
    }
  };

  return (
    <div className="space-y-8">
      {/* 국내 인기 목적지 */}
      <div>
        <h3 className="mb-4 text-center text-muted-foreground flex items-center justify-center gap-2">
          <MapPin className="w-5 h-5" />
          국내 인기 여행지 🇰🇷
        </h3>
        <div className="flex flex-wrap justify-center gap-2 max-w-4xl mx-auto">
          {domesticDestinations.map((destination) => (
            <Button
              key={destination.id}
              variant="outline"
              onClick={() => onDestinationSelect(destination.fullName)}
              className={`
                flex items-center gap-2 transition-all
                ${selectedDestination === destination.fullName 
                  ? 'bg-blue-100 border-blue-300 text-blue-700' 
                  : 'hover:bg-blue-50'
                }
              `}
            >
              <div className="text-left">
                <div>{destination.city}</div>
                <div className="text-xs text-muted-foreground">{destination.country}</div>
              </div>
            </Button>
          ))}
        </div>
      </div>

      {/* 해외 인기 목적지 */}
      <div>
        <h3 className="mb-4 text-center text-muted-foreground flex items-center justify-center gap-2">
          <Globe className="w-5 h-5" />
          해외 인기 여행지 🌍
        </h3>
        <div className="flex flex-wrap justify-center gap-2 max-w-4xl mx-auto">
          {internationalDestinations.map((destination) => (
            <Button
              key={destination.id}
              variant="outline"
              onClick={() => onDestinationSelect(destination.fullName)}
              className={`
                flex items-center gap-2 transition-all
                ${selectedDestination === destination.fullName 
                  ? 'bg-green-100 border-green-300 text-green-700' 
                  : 'hover:bg-green-50'
                }
              `}
            >
              <div className="text-left">
                <div>{destination.city}</div>
                <div className="text-xs text-muted-foreground">{destination.country}</div>
              </div>
            </Button>
          ))}
        </div>
      </div>

      {/* 구분선 */}
      <div className="flex items-center gap-4">
        <div className="flex-1 border-t border-border"></div>
        <span className="text-sm text-muted-foreground">또는</span>
        <div className="flex-1 border-t border-border"></div>
      </div>

      {/* 직접 입력 */}
      <div>
        <h3 className="mb-4 text-center text-muted-foreground">
          원하는 목적지 직접 입력 ✏️
        </h3>
        <form onSubmit={handleCustomDestinationSubmit} className="max-w-lg mx-auto">
          <div className="grid grid-cols-2 gap-2 mb-2">
            <div>
              <Label htmlFor="country" className="text-sm">국가/지역</Label>
              <Input
                id="country"
                type="text"
                value={customCountry}
                onChange={(e) => setCustomCountry(e.target.value)}
                placeholder="예: 프랑스, 스페인..."
                required
              />
            </div>
            <div>
              <Label htmlFor="city" className="text-sm">도시/지역</Label>
              <Input
                id="city"
                type="text"
                value={customCity}
                onChange={(e) => setCustomCity(e.target.value)}
                placeholder="예: 파리, 바르셀로나..."
                required
              />
            </div>
          </div>
          <Button type="submit" className="w-full gap-2">
            <Search className="w-4 h-4" />
            목적지 검색
          </Button>
        </form>
      </div>
    </div>
  );
}