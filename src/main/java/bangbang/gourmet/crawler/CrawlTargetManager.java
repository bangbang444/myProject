package bangbang.gourmet.crawler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrawlTargetManager {

    public List<String> getDefaultKeywords(){
        return List.of("강남역 맛집");
    }
}
