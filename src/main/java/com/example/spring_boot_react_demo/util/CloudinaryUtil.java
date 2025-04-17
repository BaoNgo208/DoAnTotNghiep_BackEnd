package com.example.spring_boot_react_demo.util;

import static com.example.spring_boot_react_demo.util.Constants.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CloudinaryUtil {
    private static final String UPLOAD_MARKER = "/upload/";
    private static final String VERSION_MARKER = "/v";

    public static String addTimeRangeForUrl(String url, double startTime, double endTime) {
        String timeParams = String.format(Locale.US,"so_%.2f,eo_%.2f", startTime, endTime);
        return addParamForUrl(url, timeParams);
    }

    public static String addSizeForUrl(String url, String size) {
        String[] dimensions = size.split("x");
        if (dimensions.length != 2) return url;

        String resizeParams = String.format(Locale.US,"w_%s,h_%s,c_fill", dimensions[0], dimensions[1]);
        return addParamForUrl(url, resizeParams);
    }

    private static String addParamForUrl(String url, String param) {
        int uploadIndex = url.indexOf(UPLOAD_MARKER);
        int versionIndex = url.indexOf(VERSION_MARKER, uploadIndex);

        if (uploadIndex == NOT_FOUND || versionIndex == NOT_FOUND) return url;

        int transformStart = uploadIndex + UPLOAD_MARKER.length();
        String newTransform = param;

        if (versionIndex > transformStart) {
            String currentTransform = url.substring(transformStart, versionIndex);
            newTransform = mergeParams(currentTransform, param);
        }

        return url.substring(0, transformStart) + newTransform + url.substring(versionIndex);
    }

    private static String mergeParams(String existingParams, String newParams) {
        Map<String, String> paramMap = new LinkedHashMap<>();

        addToParamMap(paramMap, existingParams);
        addToParamMap(paramMap, newParams);

        StringBuilder merged = new StringBuilder();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (!merged.isEmpty()) merged.append(COMMA);
            merged.append(entry.getKey());
            if (!entry.getValue().isEmpty()) {
                merged.append(DASH).append(entry.getValue());
            }
        }

        return merged.toString();
    }

    private static void addToParamMap(Map<String, String> map, String params) {
        for (String item : params.split(COMMA)) {
            // Split by dash ('_') with a limit of 2 to separate key and value
            String[] parts = item.split(DASH, 2);
            map.put(parts[ZERO], parts.length > ONE ? parts[ONE] : EMPTY_VALUE);
        }
    }
}