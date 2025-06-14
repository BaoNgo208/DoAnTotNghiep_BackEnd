package com.example.spring_boot_react_demo.service.impl;

import com.example.spring_boot_react_demo.exception.AppException;
import com.example.spring_boot_react_demo.exception.ErrorCode;
import com.example.spring_boot_react_demo.model.entity.Lyric;
import com.example.spring_boot_react_demo.model.entity.Project;
import com.example.spring_boot_react_demo.repository.LyricRepo;
import com.example.spring_boot_react_demo.repository.ProjectRepo;
import com.example.spring_boot_react_demo.service.CloudinaryService;
import com.example.spring_boot_react_demo.service.FFmpegService;
import com.example.spring_boot_react_demo.service.LyricService;
import com.example.spring_boot_react_demo.service.WhisperService;
import com.example.spring_boot_react_demo.util.Constants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static com.example.spring_boot_react_demo.util.AssUtil.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
//import java.util.concurrent.TimeUnit;
import static com.example.spring_boot_react_demo.util.FileUtil.*;
import java.time.Duration;
import java.util.stream.Collectors;

import static com.example.spring_boot_react_demo.util.Constants.MEDIA_TYPE_AUDIO;
import static com.example.spring_boot_react_demo.util.ConvertUtils.convertMultipartFileToFile;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhisperServiceImpl implements WhisperService {

    CloudinaryService cloudinaryService;
    FFmpegService ffmpegService;
    LyricService lyricService;
    LyricRepo lyricRepository;
    ProjectRepo projectRepository;

    OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public MultipartFile transcribeAudio(MultipartFile audioFile,String targetLang) {
        try {
            Request request = createTranscriptionRequest(audioFile);
            Response response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                log.error("Failed to transcribe audio: {}", response);
                throw new IOException("Unexpected response from OpenAI: " + response);
            }

            return generateAssFile(response.body().string(),targetLang);
        } catch (IOException e) {
            log.error("Error transcribing audio: {}", audioFile.getOriginalFilename(), e);
            return null;
        }
    }

    private Request createTranscriptionRequest(MultipartFile audioFile) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("file", audioFile.getOriginalFilename(), RequestBody.create(audioFile.getBytes(), MEDIA_TYPE_AUDIO))
                .addFormDataPart("response_format", "verbose_json")
//                .addFormDataPart("language", "es")
                .build();
        return new Request.Builder()
                .url(Constants.API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + Constants.OPENAI_API_KEY)
                .build();
    }

    private MultipartFile generateAssFile(String responseBody,String targetLang) throws IOException {
        JSONArray segments = new JSONObject(responseBody).getJSONArray("segments");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write(readHeaderFromFile());

            for (int i = 0; i < segments.length(); i++) {
                JSONObject segment = segments.getJSONObject(i);
                String originalText = segment.getString("text");

                // 🟡 Translate here
                String translatedText = translate(originalText, "en", targetLang); // ví dụ dịch từ English sang Spanish

                writer.write(String.format("Dialogue: 0,%s,%s,Default,,0,0,0,,%s\n",
                        convertSecondsToAssFormat(segment.getDouble("start")),
                        convertSecondsToAssFormat(segment.getDouble("end")),
                        translatedText));
            }
        }

        return new MockMultipartFile("output.ass", "output.ass", "text/plain", outputStream.toByteArray());
    }
    private String translate(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    sourceLang, targetLang, encodedText
            );

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = in.lines().collect(Collectors.joining());
                return response.split("\"")[1]; // basic parsing
            }
        } catch (Exception e) {
            e.printStackTrace();
            return text; // fallback: return original if translation fails
        }
    }



    private String convertSecondsToAssFormat(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int centisecs = (int) ((seconds - (int) seconds) * 100);
        return String.format("%01d:%02d:%02d.%02d", hours, minutes, secs, centisecs);
    }

    @Override
    public String processVideo(MultipartFile videoFile, Long projectId,String targetLang) {
        try {
            MultipartFile assFile = transcribeAudio(videoFile,targetLang);
            if (assFile == null) {
                throw new IOException("Failed to generate subtitles for: " + videoFile.getOriginalFilename());
            }
            saveAssContent(assFile,projectId);
            MultipartFile processedVideo = ffmpegService.addAssToVideo(videoFile, convertMultipartFileToFile(assFile, "process.srt"));
            if (processedVideo == null) {
                throw new IOException("Failed to generate video with subtitles for: " + videoFile.getOriginalFilename());
            }

            return cloudinaryService.uploadFile(processedVideo, "video");
        } catch (Exception e) {
            log.error("Error processing video: {}", videoFile.getOriginalFilename(), e);
            return null;
        }
    }

    @Async
    protected void saveAssContent(MultipartFile assFile, Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        String assContent = new String(assFile.getBytes(), StandardCharsets.UTF_8);
        assContent = validateAndFixAssFormat(assContent);
        Lyric lyric = new Lyric();
        lyric.setProject(project);
        lyric.setText(assContent);
        if(project.isEffect()) {
            lyric.setEffectText(assContent);
            String originText = lyricService.convertEffectTextToOriginal(project.getVideo(), assContent, project.getDuration());
            lyric.setOriginalText(originText);
        }
        else lyric.setOriginalText(assContent);
        lyricRepository.save(lyric);
    }
}