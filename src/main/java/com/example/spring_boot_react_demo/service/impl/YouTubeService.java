package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static com.example.spring_boot_react_demo.util.Constants.OUTPUT_VIDEO_FILE;

@Service
public class YouTubeService {


    @Value("${youtube.clientId}")
    private String clientId;

    @Value("${youtube.clientSecret}")
    private String clientSecret;

    @Value("${youtube.redirectUri}")
    private String redirectUri;

    @Value("${youtube.tokenUri}")
    private String tokenUri;

    @Value("${youtube.uploadUrl}")
    private String uploadUrl;

    public String fetchAccessToken(String code) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    public String uploadToYouTube(String accessToken, String videoUrl) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            byte[] videoBytes = inputStream.readAllBytes();

            String metadata = "{\"snippet\": {\"title\": \"My Edited Video\", \"description\": \"Uploaded via API\"}, \"status\": {\"privacyStatus\": \"public\"}}";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("metadata", new HttpEntity<>(metadata, headers));

            body.add("file", new ByteArrayResource(videoBytes) {
                @Override
                public String getFilename() {
                    return OUTPUT_VIDEO_FILE;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, request, Map.class);

            return (String) response.getBody().get("id");
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

}