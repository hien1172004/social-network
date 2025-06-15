package backend.example.mxh.until;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    public static String generateSlug(String input){
        if (input == null || input.isEmpty()) return "";

        String slug = input.trim().toLowerCase(Locale.ROOT);

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(slug)
                .replaceAll("");

        slug = slug.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+|-+$", "");

        return slug;
    }

    public static String getPublicId(String imageUrl){
        int start = imageUrl.lastIndexOf("/");
        int end = imageUrl.lastIndexOf("_");
        return imageUrl.substring(start + 1, end);
    }
}
