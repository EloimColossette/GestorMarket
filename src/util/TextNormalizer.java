package util;

public class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeProductName(String input) {
        if (input == null) {
            return null;
        }

        String collapsed = input.trim().replaceAll("\\s+", " ");

        if (collapsed.isEmpty()) {
            return collapsed;
        }

        StringBuilder result = new StringBuilder(collapsed.length());
        boolean capitalizeNext = true;

        for (int i = 0; i < collapsed.length(); i++) {
            char c = collapsed.charAt(i);

            if (Character.isLetter(c)) {
                result.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                // depois de espaço ou hífen, a próxima letra também fica maiúscula
                capitalizeNext = (c == ' ' || c == '-');
            }
        }

        return result.toString();
    }
}