package bangbang.gourmet.common.util;

public class RatingUtils {

    private RatingUtils() {}

    public static double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
