package bangbang.gourmet.global.ncp;

import java.util.List;

public record NaverMapDto(List<Result> results) {

    public record Result(
            String name, // addr
            Region region
    ) {}

    public record Region(
            Area area1, // 서울특별시
            Area area2, // 서초구
            Area area3 // 서초동
    ) {}

    public record Area(String name) {}
}