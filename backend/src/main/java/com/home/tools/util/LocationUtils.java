package com.home.tools.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LocationUtils {

    private static final String UNSPECIFIED_LOCATION = "未指定位置";
    private static final String STANDARD_SEPARATOR = "-";
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[-_/]");

    private static final Map<String, String> ROOM_ALIASES = new HashMap<>();
    private static final Map<String, String> CABINET_ALIASES = new HashMap<>();

    static {
        ROOM_ALIASES.put("工具间", "车间");
        ROOM_ALIASES.put("工作室", "车间");
        ROOM_ALIASES.put("工作间", "车间");
        ROOM_ALIASES.put("储物间", "仓库");
        ROOM_ALIASES.put("储藏室", "仓库");
        ROOM_ALIASES.put("储藏间", "仓库");
        ROOM_ALIASES.put("杂物间", "仓库");
        ROOM_ALIASES.put("杂物室", "仓库");
        ROOM_ALIASES.put("库房", "仓库");
        ROOM_ALIASES.put("地下", "地下室");
        ROOM_ALIASES.put("地下车库", "车库");
        ROOM_ALIASES.put("车房", "车库");

        CABINET_ALIASES.put("工具柜", "");
        CABINET_ALIASES.put("储物柜", "");
        CABINET_ALIASES.put("零件柜", "");
        CABINET_ALIASES.put("文件柜", "");
        CABINET_ALIASES.put("柜子", "");
        CABINET_ALIASES.put("一号柜", "1");
        CABINET_ALIASES.put("二号柜", "2");
        CABINET_ALIASES.put("三号柜", "3");
        CABINET_ALIASES.put("四号柜", "4");
        CABINET_ALIASES.put("五号柜", "5");
        CABINET_ALIASES.put("1号柜", "1");
        CABINET_ALIASES.put("2号柜", "2");
        CABINET_ALIASES.put("3号柜", "3");
        CABINET_ALIASES.put("4号柜", "4");
        CABINET_ALIASES.put("5号柜", "5");
        CABINET_ALIASES.put("A柜", "A");
        CABINET_ALIASES.put("B柜", "B");
        CABINET_ALIASES.put("C柜", "C");
        CABINET_ALIASES.put("D柜", "D");
    }

    private LocationUtils() {}

    public static String normalizeLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return "";
        }

        String normalized = location.trim();
        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");

        String[] parts = SEPARATOR_PATTERN.split(normalized);
        if (parts.length <= 1) {
            return normalized.trim();
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            if (i > 0) {
                result.append(STANDARD_SEPARATOR);
            }
            result.append(normalizePart(part, i));
        }

        return result.toString();
    }

    private static String normalizePart(String part, int index) {
        if (part == null || part.isEmpty()) {
            return part;
        }

        String normalized = part;

        if (index == 0) {
            for (Map.Entry<String, String> entry : ROOM_ALIASES.entrySet()) {
                if (part.equalsIgnoreCase(entry.getKey())) {
                    normalized = entry.getValue();
                    break;
                }
            }
        } else if (index == 1) {
            for (Map.Entry<String, String> entry : CABINET_ALIASES.entrySet()) {
                if (part.equalsIgnoreCase(entry.getKey())) {
                    normalized = entry.getValue();
                    break;
                }
            }
            if (!normalized.isEmpty() && normalized.length() == 1) {
                normalized = normalized.toUpperCase();
            }
        }

        return normalized;
    }

    public static String normalizeLocationForDisplay(String location) {
        String normalized = normalizeLocation(location);
        if (normalized == null || normalized.isEmpty()) {
            return UNSPECIFIED_LOCATION;
        }
        return normalized;
    }

    public static boolean isUnspecified(String location) {
        return location == null || location.trim().isEmpty();
    }

    public static String getUnspecifiedLocationLabel() {
        return UNSPECIFIED_LOCATION;
    }
}
