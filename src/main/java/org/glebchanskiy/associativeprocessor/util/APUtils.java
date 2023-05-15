package org.glebchanskiy.associativeprocessor.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class APUtils {
    private APUtils() {

    }

    public static String toDig(List<Boolean> bits) {
        return bits.stream().map(b -> Boolean.TRUE.equals(b) ? "1" : "0").collect(Collectors.joining());
    }

    public static String toDig(Boolean[] bits) {
        return Stream.of(bits).map(b -> Boolean.TRUE.equals(b) ? "1":"0").collect(Collectors.joining());
    }

    public static String toDig(Boolean bit) {
        return Boolean.TRUE.equals(bit) ? "1" : "0";
    }

    public static String toHex(int num) {
        return String.format("[%#x] ", num);
    }

    public static Integer toDecimal(List<Boolean> binary) {
        int decimal = 0;
        int power = binary.size();
        for (Boolean bit : binary) {
            power--;
            if (Boolean.TRUE.equals(bit))
                decimal += Math.pow(2, power);
        }
        return decimal;
    }

    public static int compare(List<Boolean> first, List<Boolean> second) {
        for (int i = 0; i < first.size(); i++) {
            if (first.get(i) && !second.get(i))
                return 1;
            else if (!first.get(i) && second.get(i))
                return -1;
        }
        return 0;
    }
}
