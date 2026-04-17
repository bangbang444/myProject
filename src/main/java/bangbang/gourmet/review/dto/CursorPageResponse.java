package bangbang.gourmet.review.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursorId,
        boolean hasNext
) {
    public static <T> CursorPageResponse<T> of(List<T> items, Long nextCursorId, boolean hasNext) {
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }
}
