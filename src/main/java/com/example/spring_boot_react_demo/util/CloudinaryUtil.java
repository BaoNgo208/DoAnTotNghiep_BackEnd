package com.example.spring_boot_react_demo.util;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;

import static com.example.spring_boot_react_demo.util.Constants.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudinaryUtil {
    private static final String UPLOAD_MARKER = "/upload/";
    private static final String VERSION_MARKER = "/v";

    public static String getPublicId(String url) {
        String[] parts = url.split(SLASH);
        // publicId is the last part of the URL after the final "/"
        String lastPart = parts.length > ZERO ? parts[parts.length - ONE] : EMPTY_VALUE;

        // Remove extension if exists (e.g., .jpg, .mp4, etc.)
        int dotIndex = lastPart.lastIndexOf(DOT);
        if (dotIndex != NOT_FOUND) {
            lastPart = lastPart.substring(ZERO, dotIndex);
        }
        return lastPart;
    }

    public static String addTimeRangeForUrl(String url, double startTime, double endTime) {
        String timeParams = String.format(Locale.US, "so_%.2f,eo_%.2f", startTime, endTime);
        return addParamForUrl(url, timeParams);
    }

    public static String addSizeForUrl(String url, String size) {
        //Parses a size string in the format: WxH
        String[] dimensions = size.split("x");
        if (dimensions.length != 2) return url;
        String resizeParams = String.format(Locale.US, "w_%s,h_%s,c_fill", dimensions[0], dimensions[1]);
        return addParamForUrl(url, resizeParams);
    }

    public static String addBackgroundForUrl(String url, String backgroundId) {
        String backgroundParams = String.format("l_%s,fl_relative,w_1.0,h_1.0,c_fill", backgroundId);
        return addParamForUrl(url, backgroundParams);
    }

    public static String removeBackgroundForUrl(String url) {
        int uploadIndex = url.indexOf(UPLOAD_MARKER);
        int versionIndex = url.indexOf(VERSION_MARKER, uploadIndex);

        int transformStart = uploadIndex + UPLOAD_MARKER.length();
        if (versionIndex <= transformStart) {
            throw new AppException(ErrorCode.BACKGROUND_DOSE_NOT_EXIST);
        }

        String currentTransform = url.substring(transformStart, versionIndex);
        String[] transformParts = currentTransform.split(SLASH);
        // currentTransform contains two parts: backgroundParam/sizeOrTimeParam
        if (transformParts.length != 2 || transformParts[ZERO].isEmpty()) {
            throw new AppException(ErrorCode.BACKGROUND_DOSE_NOT_EXIST);
        }

        String updatedTransform = SLASH + transformParts[ONE];
        return url.substring(ZERO, transformStart) + updatedTransform + url.substring(versionIndex);
    }

    public static double[] extractStartAndEndTime(String url) {
        Pattern pattern = Pattern.compile("so_([\\d.]+),eo_([\\d.]+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            double startTime = Double.parseDouble(matcher.group(1));
            double endTime = Double.parseDouble(matcher.group(2));
            return new double[]{startTime, endTime};
        }
        throw new IllegalArgumentException("No valid time range found in URL");
    }

    public static String removeTimeFromUrl(String url) {
        return url.replaceAll("so_[\\d.]+,eo_[\\d.]+(,|/)?", "");
    }

    public static String cleanCloudinaryUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        url = url.replace("http://", "https://");
        url = url.replace("upload//", "upload/");
        return url;
    }

    private static String addParamForUrl(String url, String param) {
        int uploadIndex = url.indexOf(UPLOAD_MARKER);
        int versionIndex = url.indexOf(VERSION_MARKER, uploadIndex);

        if (uploadIndex == NOT_FOUND || versionIndex == NOT_FOUND) return url;

        int transformStart = uploadIndex + UPLOAD_MARKER.length();
        String newTransform;
        if (versionIndex < transformStart) {
            newTransform = param;
        } else {
            String currentTransform = url.substring(transformStart, versionIndex);
            newTransform = updateBlockByParamType(currentTransform, param);
        }

        return url.substring(ZERO, transformStart) + newTransform + url.substring(versionIndex);
    }

    private static String updateBlockByParamType(String existingParams, String newParams) {
        String[] params = existingParams.split(SLASH);
        int paramCount = params.length;
        // If existingParams has 2 parts (overlay + timing), replace the appropriate one
        if (paramCount == 2) {
            if (newParams.startsWith(OVERLAY_PREFIX)) {
                return overrideParams(params[ZERO], newParams) + SLASH + params[ONE];
            }
            else {
                return params[ZERO] + SLASH + overrideParams(params[ONE], newParams);
            }
        }
        // If only timing block exists
        else {
            if (newParams.startsWith(OVERLAY_PREFIX)) {
                return newParams + SLASH + existingParams;
            } else {
                return overrideParams(existingParams, newParams);
            }
        }
    }

    public static String getVersionOfVideo(String videoAsset ){
        // Regex to find "/v<number>/" in the URL (e.g., /v123456/)
        String regex = "/(v\\d+)/";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(videoAsset);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static String concatVersionWithVideoAsset(String previousVideoAsset, String version) {
        if (previousVideoAsset == null || version == null) {
            return null;
        }

        int lastSlashIndex = previousVideoAsset.lastIndexOf(SLASH);
        if (lastSlashIndex == NOT_FOUND) {
            return previousVideoAsset;
        }

        String pathBeforeVideoId = previousVideoAsset.substring(ZERO, lastSlashIndex + ONE);
        String videoId = previousVideoAsset.substring(lastSlashIndex + ONE);
        pathBeforeVideoId = pathBeforeVideoId.replaceAll("v\\d+/+$", EMPTY_VALUE);

        return pathBeforeVideoId + version + SLASH + videoId;
    }

    private static String overrideParams(String oldParams, String newParams) {
        Map<String, String> paramMap = new LinkedHashMap<>();

        addToParamMap(paramMap, oldParams);
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