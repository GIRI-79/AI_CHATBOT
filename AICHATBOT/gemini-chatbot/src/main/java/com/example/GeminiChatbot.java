package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeminiChatbot {
    // Replace this with your actual Gemini API key
    private static final String API_KEY = "YOUR_GEMINI_API_KEY";
    // private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;


    public static String getGeminiResponse(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setup connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // Escape quotes in prompt
            String escapedPrompt = prompt.replace("\"", "\\\"");

            // Create JSON request manually (no text blocks)
            String jsonRequest = "{"
                    + "\"contents\": [{"
                    + "\"parts\": [{"
                    + "\"text\": \"" + escapedPrompt + "\""
                    + "}]"
                    + "}]"
                    + "}";

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            if (responseCode != 200) {
                return "Gemini: Error: API returned code " + responseCode + ". Response: " + response;
            }

            // Extract response text manually (naive parsing)
            String resStr = response.toString();
            if (resStr.contains("\"text\":")) {
                int start = resStr.indexOf("\"text\":") + 8;
                int end = resStr.indexOf("\"", start + 1);
                if (start >= 8 && end > start) {
                    return resStr.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
                }
            }

            return "Gemini: Couldn't parse response properly: " + resStr;

        } catch (Exception e) {
            return "Gemini: Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to Gemini Chatbot!");
        System.out.println("Type 'exit' to end the conversation");
        System.out.println("----------------------------------------");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (!userInput.isEmpty()) {
                String response = getGeminiResponse(userInput);
                System.out.println("\n" + response);
            }
        }

        scanner.close();
    }
}
















