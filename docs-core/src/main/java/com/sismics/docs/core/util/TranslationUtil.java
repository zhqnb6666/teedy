package com.sismics.docs.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling translations using OpenAI API.
 */
public class TranslationUtil {
    private static final Logger log = LoggerFactory.getLogger(TranslationUtil.class);
    private static final String OPENAI_API_KEY = "sk-8fKgTLvJw5ISuZW9P2DUxAk57vULVSRs0fPbcjFGjw3vsFh0"; // Replace with your actual API key
    private static final String OPENAI_API_URL = "https://xiaoa.plus/v1/chat/completions";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Translates text from source language to target language using OpenAI API.
     *
     * @param text Text to translate
     * @param sourceLang Source language code (e.g., "en", "zh")
     * @param targetLang Target language code (e.g., "en", "zh")
     * @return Translated text
     */
    public static String translate(String text, String sourceLang, String targetLang) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(OPENAI_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
            httpPost.setHeader("Content-Type", "application/json");

            // Prepare the request body
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", String.format("Translate the following text from %s to %s: %s", 
                sourceLang, targetLang, text));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", new Object[]{message});
            requestBody.put("temperature", 0.3);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            // Execute the request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity);
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                    
                    // Extract the translated text from the response
                    // Note: The actual response structure might need adjustment based on OpenAI's API response format
                    return extractTranslatedText(responseMap);
                }
            }
        } catch (IOException e) {
            log.error("Error during translation", e);
        }
        return text; // Return original text if translation fails
    }

    /**
     * Extracts the translated text from the OpenAI API response.
     *
     * @param responseMap The response from OpenAI API
     * @return The translated text
     */
    private static String extractTranslatedText(Map<String, Object> responseMap) {
        try {
            // Navigate through the response structure to get the translated text
            if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof java.util.List) {
                java.util.List<?> choices = (java.util.List<?>) responseMap.get("choices");
                if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    if (firstChoice.containsKey("message") && firstChoice.get("message") instanceof Map) {
                        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                        if (message.containsKey("content")) {
                            return message.get("content").toString();
                        }
                    }
                }
            }
            log.warn("Unexpected response format from OpenAI API: " + responseMap);
            return "";
        } catch (Exception e) {
            log.error("Error extracting translated text from response", e);
            return "";
        }
    }

    public static void main(String[] args) {
        String text = "Hello, world!";
        String sourceLang = "en";
        String targetLang = "zh";
        String translatedText = translate(text, sourceLang, targetLang);
        System.out.println("Translated text: " + translatedText);
    }
}
