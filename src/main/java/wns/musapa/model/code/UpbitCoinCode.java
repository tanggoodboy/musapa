package wns.musapa.model.code;

import wns.musapa.model.CoinCode;

public enum UpbitCoinCode implements CoinCode {
    BTC("CRIX.UPBIT.KRW-BTC", "비트코인"),
    DASH("CRIX.UPBIT.KRW-DASH", "대시"),
    ETH("CRIX.UPBIT.KRW-ETH", "이더리움"),
    NEO("CRIX.UPBIT.KRW-NEO", "네오"),
    BCC("CRIX.UPBIT.KRW-BCC", "비트코인캐시"),
    MTL("CRIX.UPBIT.KRW-MTL", "메탈"),
    LTC("CRIX.UPBIT.KRW-LTC", "라이트코인"),
    STRAT("CRIX.UPBIT.KRW-STRAT", "스트라티스"),
    XRP("CRIX.UPBIT.KRW-XRP", "리플"),
    ETC("CRIX.UPBIT.KRW-ETC", "이더리움클래식"),
    OMG("CRIX.UPBIT.KRW-OMG", "오미세고"),
    SNT("CRIX.UPBIT.KRW-SNT", "스테이터스네트워크토큰"),
    WAVES("CRIX.UPBIT.KRW-WAVES", "웨이브"),
    PIVX("CRIX.UPBIT.KRW-PIVX", "피벡스"),
    XEM("CRIX.UPBIT.KRW-XEM", "뉴이코노미무브먼트"),
    ZEC("CRIX.UPBIT.KRW-ZEC", "지캐시"),
    XMR("CRIX.UPBIT.KRW-XMR", "모네로"),
    QTUM("CRIX.UPBIT.KRW-QTUM", "퀀텀"),
    LSK("CRIX.UPBIT.KRW-LSK", "리스크"),
    STEEM("CRIX.UPBIT.KRW-STEEM", "스팀"),
    XLM("CRIX.UPBIT.KRW-XLM", "스텔라루멘"),
    ADDR("CRIX.UPBIT.KRW-ARDR", "아더"),
    KMD("CRIX.UPBIT.KRW-KMD", "코모도"),
    ARK("CRIX.UPBIT.KRW-ARK", "아크"),
    STORJ("CRIX.UPBIT.KRW-STORJ", "스토리지"),
    GRS("CRIX.UPBIT.KRW-GRS", "그로스톨코인"),
    VTC("CRIX.UPBIT.KRW-VTC", "버트코인"),
    REP("CRIX.UPBIT.KRW-REP", "어거"),
    EMC2("CRIX.UPBIT.KRW-EMC2", "아인슈타이늄"),
    ADA("CRIX.UPBIT.KRW-ADA", "에이다"),
    SBD("CRIX.UPBIT.KRW-SBD", "스팀달러"),
    TIX("CRIX.UPBIT.KRW-TIX", "블록틱스"),
    POWR("CRIX.UPBIT.KRW-POWR", "파워렛저"),
    MER("CRIX.UPBIT.KRW-MER", "머큐리"),
    BTG("CRIX.UPBIT.KRW-BTG", "비트코인골드");

    String code;
    String korean;

    UpbitCoinCode(String code, String korean) {
        this.code = code;
        this.korean = korean;
    }

    public String getKorean() {
        return this.korean;
    }

    public String getCode() {
        return this.code;
    }

    public static String print() {
        StringBuilder sb = new StringBuilder();
        for (UpbitCoinCode code : UpbitCoinCode.values()) {
            sb.append(code.getKorean() + ": " + code.name()).append("\n");
        }
        return sb.toString();
    }

    public static UpbitCoinCode parseByName(String coinCode) {
        for (UpbitCoinCode code : UpbitCoinCode.values()) {
            if (code.name().equalsIgnoreCase(coinCode)) {
                return code;
            }
        }
        return null;
    }

    public static UpbitCoinCode parseByCode(String coinCode) {
        for (UpbitCoinCode code : UpbitCoinCode.values()) {
            if (code.getCode().equalsIgnoreCase(coinCode)) {
                return code;
            }
        }
        return null;
    }
}
