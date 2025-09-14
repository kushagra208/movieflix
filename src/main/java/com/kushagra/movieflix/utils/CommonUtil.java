package com.kushagra.movieflix.utils;

public class CommonUtil {
    public static boolean isNullOrEmpty(String data) {
        if (data == null || data.isBlank()) {
            return true;
        }

        return false;
    }

    public static int parseRating(String ratingStr) {
        try {
            if (ratingStr == null || ratingStr.equalsIgnoreCase("N/A")) return 0;
            // rating string may be "7.6" or "7.6/10" → take leading number
            String num = ratingStr.split("/")[0].trim();
            return (int) Math.round(Double.parseDouble(num));
        } catch (Exception e) {
            return 0;
        }
    }
}
