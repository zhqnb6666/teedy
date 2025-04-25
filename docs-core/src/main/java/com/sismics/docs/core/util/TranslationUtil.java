package com.sismics.docs.core.util;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Utility class for handling translations using OpenAI API via LangChain4j.
 */
public class TranslationUtil {
    private static final Logger log = LoggerFactory.getLogger(TranslationUtil.class);
    private static final String OPENAI_API_KEY = "sk-8fKgTLvJw5ISuZW9P2DUxAk57vULVSRs0fPbcjFGjw3vsFh0"; // Replace with your actual API key
    private static final String OPENAI_BASE_URL = "https://xiaoai.plus/v1"; // 自定义 API 基础 URL
    private static final int TIMEOUT_SECONDS = 30;
    
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
        
        // For debugging or fallback use
        String mockTranslation = getMockTranslation(text, targetLang);
        
        // If text is too short, don't bother with the API
        if (text.trim().length() < 5) {
            log.info("Text too short for translation, returning original");
            return "Translated to " + targetLang + " (text too short): " + text;
        }
        
        // If text is too long, truncate it to avoid token limits
        final int MAX_CHAR_LENGTH = 4000; // Reasonable character limit for translation
        String textToTranslate = text;
        boolean truncated = false;
        
        if (text.length() > MAX_CHAR_LENGTH) {
            textToTranslate = text.substring(0, MAX_CHAR_LENGTH);
            truncated = true;
            log.info("Text truncated from {} to {} characters for translation", text.length(), MAX_CHAR_LENGTH);
        }
        
        try {
            // Create LangChain4j OpenAI chat model with custom base URL
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(OPENAI_API_KEY)
                    .baseUrl(OPENAI_BASE_URL)
                    .modelName(OpenAiChatModelName.GPT_3_5_TURBO)
                    .temperature(0.3)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .build();
            
            log.info("Sending request to OpenAI API at {}", OPENAI_BASE_URL);
            
            // Create system message with translation instructions
            SystemMessage systemMessage = SystemMessage.from(
                "You are a translation assistant. Translate the text from " + sourceLang + " to " + targetLang + 
                ". Provide only the translated text without any additional explanation or formatting."
            );
            
            // Create user message with text to translate
            UserMessage userMessage = UserMessage.from(textToTranslate);
            
            // Execute the API call
            String response = model.generate(systemMessage, userMessage).content().text();
            
            if (response != null && !response.trim().isEmpty()) {
                log.info("Translation successful, received {} characters", response.length());
                
                if (truncated) {
                    return response.trim() + "\n\n[Note: Original text was truncated due to length limits]";
                } else {
                    return response.trim();
                }
            } else {
                log.warn("Received empty response from OpenAI API");
                // Fallback to mock translation if API response is empty
                return "API response empty. " + mockTranslation;
            }
            
        } catch (Exception e) {
            log.error("Error during translation: {}", e.getMessage(), e);
            // Return mock translation with error message for debugging
            return "Translation error: " + e.getMessage() + ". " + mockTranslation;
        }
    }
    
    /**
     * Provides a mock translation for debugging and fallback purposes.
     * 
     * @param text Original text
     * @param targetLang Target language code
     * @return A mock translated text
     */
    private static String getMockTranslation(String text, String targetLang) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Just a simple mock that prefixes the text to indicate it would be translated
        return "MOCK[" + targetLang + "]: " + text.substring(0, Math.min(50, text.length())) + 
               (text.length() > 50 ? "..." : "");
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
