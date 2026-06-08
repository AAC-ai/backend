package com.aac.backend.infra.clovavoice;

import com.aac.backend.global.exception.BusinessException;
import com.aac.backend.global.exception.ErrorCode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(ClovaVoiceProperties.class)
public class ClovaVoiceClient {

    private static final String HEADER_API_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private static final String HEADER_API_KEY = "X-NCP-APIGW-API-KEY";

    private static final String SPEAKER = "nhajun";
    private static final int VOLUME = 0;
    private static final int SPEED = 5;
    private static final int PITCH = 0;
    private static final String FORMAT = "mp3";

    private final ClovaVoiceProperties properties;
    private final RestClient restClient;

    public ClovaVoiceClient(ClovaVoiceProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public byte[] synthesize(String text) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("speaker", SPEAKER);
        params.add("text", text);
        params.add("volume", String.valueOf(VOLUME));
        params.add("speed", String.valueOf(SPEED));
        params.add("pitch", String.valueOf(PITCH));
        params.add("format", FORMAT);

        try {
            byte[] result = restClient.post()
                    .uri(properties.url())
                    .header(HEADER_API_KEY_ID, properties.apiKeyId())
                    .header(HEADER_API_KEY, properties.apiKey())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(byte[].class);
            if (result == null) {
                throw new BusinessException(ErrorCode.TTS_FAILED);
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TTS_FAILED);
        }
    }
}
