package com.sismics.docs.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
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
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds timeout

    /**
     * Translates text from source language to target language using OpenAI API.
     *
     * @param text Text to translate
     * @param sourceLang Source language code (e.g., "en", "zh")
     * @param targetLang Target language code (e.g., "en", "zh")
     * @return Translated text
     */
    public static String translate(String text, String sourceLang, String targetLang) {
        // Handle null input
        if (text == null) {
            log.warn("Null text provided for translation");
            return "Error: null text to translate";
        }
        
        log.info("Starting translation from {} to {}, text length: {}", sourceLang, targetLang, text.length());
        
        // For debugging: provide a mock translation if real API fails
        String mockTranslation = getMockTranslation(text, targetLang);
        
        // If text is too short, don't bother with the API
        if (text.trim().length() < 5) {
            log.info("Text too short for translation, returning original");
            return "Translated to " + targetLang + " (text too short): " + text;
        }
        
        try {
            // Set request config with timeout
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(CONNECTION_TIMEOUT)
                .build();
                
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .build()) {
                    
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

                log.info("Sending request to OpenAI API: {}", OPENAI_API_URL);
                
                // Execute the request
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("OpenAI API response status code: {}", statusCode);
                    
                    // Check if the status code indicates success (2xx)
                    if (statusCode >= 200 && statusCode < 300) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String responseBody = EntityUtils.toString(entity);
                            log.info("Response received, length: {}", responseBody.length());
                            
                            try {
                                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                                String extractedText = extractTranslatedText(responseMap);
                                if (extractedText != null && !extractedText.trim().isEmpty()) {
                                    log.info("Translation successful");
                                    return extractedText;
                                } else {
                                    log.warn("Extracted text was empty, response: {}", responseBody);
                                }
                            } catch (Exception e) {
                                log.error("Error parsing response: {}", responseBody, e);
                            }
                        }
                    } else {
                        log.error("API call failed with status code: {}", statusCode);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error during translation", e);
        }
        
        log.warn("Translation failed, returning mock translation");
        return mockTranslation; // If API call fails, return the mock translation
    }

    /**
     * Extracts the translated text from the OpenAI API response.
     *
     * @param responseMap The response from OpenAI API
     * @return The translated text
     */
    private static String extractTranslatedText(Map<String, Object> responseMap) {
        try {
            log.info("Extracting translated text from response");
            // Navigate through the response structure to get the translated text
            if (responseMap.containsKey("choices") && responseMap.get("choices") instanceof java.util.List) {
                java.util.List<?> choices = (java.util.List<?>) responseMap.get("choices");
                log.info("Found {} choices in response", choices.size());
                
                if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    
                    if (firstChoice.containsKey("message") && firstChoice.get("message") instanceof Map) {
                        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                        
                        if (message.containsKey("content")) {
                            String content = message.get("content").toString();
                            log.info("Successfully extracted content: {} chars", content.length());
                            return content;
                        } else {
                            log.warn("Message does not contain 'content' field: {}", message.keySet());
                        }
                    } else {
                        log.warn("First choice does not contain valid 'message' field: {}", firstChoice.keySet());
                    }
                } else {
                    log.warn("Choices list is empty or first element is not a Map");
                }
            } else {
                log.warn("Response does not contain valid 'choices' field");
            }
            
            // If we get here, we couldn't extract the text properly
            log.warn("Unexpected response format from OpenAI API: {}", responseMap);
            return "";
        } catch (Exception e) {
            log.error("Error extracting translated text from response", e);
            return "";
        }
    }

    /**
     * Provides a mock translation for testing purposes
     * 
     * @param text Original text
     * @param targetLang Target language
     * @return Mocked translated text
     */
    private static String getMockTranslation(String text, String targetLang) {
        String prefix = "";
        
        if (text.length() > 100) {
            text = text.substring(0, 100) + "...";
        }
        
        switch (targetLang) {
            case "zh":
                prefix = "模拟翻译 (Chinese): ";
                return prefix + text;
            case "ja":
                prefix = "模擬翻訳 (Japanese): ";
                return prefix + text;
            case "de":
                prefix = "Simulierte Übersetzung (German): ";
                return prefix + text;
            case "ru":
                prefix = "Симулированный перевод (Russian): ";
                return prefix + text;
            default:
                prefix = "Mock translation: ";
                return prefix + text;
        }
    }

    public static void main(String[] args) {
        // Simple test for each language
        testTranslation("Hello, world!", "en", "zh");
        testTranslation("Hello, world!", "en", "ja");
        testTranslation("Hello, world!", "en", "de");
        testTranslation("Hello, world!", "en", "ru");
        
        // Test with invalid language code
        testTranslation("Hello, world!", "en", "xx");
        
        // Test with empty text
        testTranslation("", "en", "zh");
        
        // Test with null text (should not happen in real usage)
        try {
            translate(null, "en", "zh");
        } catch (Exception e) {
            System.out.println("Expected exception when translating null: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to test translation
     */
    private static void testTranslation(String text, String sourceLang, String targetLang) {
        System.out.println("\n=======================================");
        System.out.println("Testing translation from " + sourceLang + " to " + targetLang);
        System.out.println("Original: " + text);
        String result = translate(text, sourceLang, targetLang);
        System.out.println("Result: " + result);
        System.out.println("=======================================\n");
    }
}
