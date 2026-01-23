package bangbang.gourmet.crawler;

import java.util.regex.Pattern;

public class NaverMapConstants {
    private NaverMapConstants(){} // 인스턴스화 방지

    public static final class Search {
        public static final String SEARCH_URL = "https://map.naver.com/p/search/강남역 맛집";
        public static final String SEARCH_IFRAME = "#searchIframe";
        public static final String RESTAURANT_ITEM_LINK = "a.place_bluelink";
        public static final String LOAD_INDICATOR = "?c=";
    }

    public static final class Detail {
        public static final String ENTRY_IFRAME_SELECTOR = "#entryIframe";
        public static final String ENTRY_IFRAME = "entryIframe";
        public static final String TITLE_SELECTOR = "#_title span.GHAhO";
        public static final String CATEGORY_SELECTOR = "#_title span.lnJFt";
        public static final String ADDRESS_SELECTOR = "span.pz7wy";
        public static final String PHONE_NUMBER_SELECTOR = "span.xlx7Q";
        public static final String OPENING_HOUR_EXPAND_BTN_SELECTOR = "a[role='button'].gKP9i";
        public static final String DAY_SELECTOR = "span.i8cJw";
        public static final String TIMES_SELECTOR = "div.H3ua4";
    }

    public static final class Pagination {
        public static final String PAGE_NUMBER_SELECTOR = "a.mBN2s";
        public static final String NEXT_PAGE_ARROW_SELECTOR = "a.eUTV2";
        public static final String NEXT_PAGE_TEXT = "다음페이지";
    }

    public static final class Common {
        public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36";
        public static final String NO_PHONE_NUMBER = "번호없음";
        public static final String BREAK_TIME_LABEL = "브레이크타임";
        public static final String LAST_ORDER_LABEL = "라스트오더";
        public static final String DEFAULT_COORD = "0.0";

        public static final Pattern X_COORD = Pattern.compile("\"x\":\"(.*?)\"");
        public static final Pattern Y_COORD = Pattern.compile("\"y\":\"(.*?)\"");
        public static final Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}");
    }

    public static final class State {
        public static final String ARIA_EXPANDED = "aria-expanded";
        public static final String ARIA_DISABLED = "aria-disabled";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
        public static final String UNDEFINED = "undefined";
    }
}
