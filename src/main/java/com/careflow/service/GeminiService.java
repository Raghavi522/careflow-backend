package com.careflow.service;

import com.careflow.dto.AiRequestDto;
import com.careflow.dto.AiResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_SAFETY_INSTRUCTION =
            "You are CareFlow AI, an intelligent healthcare operations and patient assistant for CareFlow Hospital. " +
            "Your role is to provide hospital FAQ assistance, appointment preparation checklists, consultation note structuring, " +
            "and hospital operational summaries. " +
            "CRITICAL SAFETY RULE: You must NEVER diagnose medical conditions or prescribe treatments or medications. " +
            "Always include relevant practical organization and recommend consulting with CareFlow medical specialists for medical decisions.";

    public GeminiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}") String apiUrl) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public AiResponseDto processAiRequest(AiRequestDto request) {
        String prompt = request.getPrompt();
        String type = request.getType() != null ? request.getType().toUpperCase() : "GENERAL";
        String context = request.getContext() != null ? request.getContext() : "";

        String constructedPrompt = buildPrompt(prompt, type, context);

        if (apiKey.isEmpty() || apiKey.equalsIgnoreCase("YOUR_GEMINI_API_KEY") || apiKey.contains("YOUR_GEMINI_API_KEY_HERE")) {
            logger.warn("GEMINI_API_KEY is not set. Generating intelligent simulated response for demo mode.");
            return new AiResponseDto(getSimulatedResponse(type, prompt));
        }

        try {
            String responseText = callGeminiApi(constructedPrompt);
            return new AiResponseDto(responseText);
        } catch (Exception e) {
            logger.error("Error communicating with Gemini API: {}", e.getMessage(), e);
            // Fallback gracefully so the UI continues working
            return new AiResponseDto("Unable to reach Gemini API (" + e.getMessage() + "). Simulated fallback: " + getSimulatedResponse(type, prompt));
        }
    }

    private String buildPrompt(String userPrompt, String type, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_SAFETY_INSTRUCTION).append("\n\n");

        switch (type) {
            case "FAQ":
                sb.append("Mode: Hospital FAQ Assistant.\n");
                sb.append("Hospital Information: CareFlow Hospital has 24/7 emergency care, Departments in Cardiology, Neurology, Pediatrics, Orthopedics, and General Medicine.\n");
                sb.append("User Question: ").append(userPrompt);
                break;

            case "APPOINTMENT_PREP":
                sb.append("Mode: Appointment Preparation Checklist.\n");
                if (!context.isBlank()) {
                    sb.append("Appointment Context: ").append(context).append("\n");
                }
                sb.append("User Request: ").append(userPrompt).append("\n");
                sb.append("Provide a clear, helpful bulleted checklist of items, documentation, and questions to bring to the appointment.");
                break;

            case "CONSULTATION_SUMMARY":
                sb.append("Mode: Doctor Consultation Summary Assistant.\n");
                sb.append("Doctor Notes to Structure: ").append(userPrompt).append("\n");
                sb.append("Provide a clear, clinical summary categorized by: 1. Chief Complaint, 2. Key Observations, 3. Summary of Treatment Plan, 4. Follow-up Suggestions. (Do not diagnose or prescribe).");
                break;

            case "ADMIN_SUMMARY":
                sb.append("Mode: Hospital Operational Statistics Summary.\n");
                if (!context.isBlank()) {
                    sb.append("System Operational Stats: ").append(context).append("\n");
                }
                sb.append("Admin Request: ").append(userPrompt).append("\n");
                sb.append("Analyze these metrics and provide operational insights, efficiency recommendations, and resource planning notes.");
                break;

            default:
                sb.append("User Request: ").append(userPrompt);
                break;
        }

        return sb.toString();
    }

    private String callGeminiApi(String fullPrompt) throws Exception {
        String endpoint = apiUrl + "?key=" + apiKey;

        Map<String, Object> textPart = Map.of("text", fullPrompt);
        Map<String, Object> contentsPart = Map.of("parts", List.of(textPart));
        Map<String, Object> payload = Map.of("contents", List.of(contentsPart));

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
            JsonNode root = objectMapper.readTree(httpResponse.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "No response text received from Gemini API.";
        } else {
            throw new RuntimeException("Gemini API error (HTTP " + httpResponse.statusCode() + "): " + httpResponse.body());
        }
    }

    private String getSimulatedResponse(String type, String prompt) {
        switch (type) {
            case "FAQ":
                return "CareFlow Hospital Guide:\n" +
                        "• Registration: Bring a valid government photo ID, insurance card, and any previous medical records.\n" +
                        "• Departments: Cardiology, Neurology, General Medicine, Pediatrics, and Orthopedics.\n" +
                        "• Visiting Hours: Inpatient wards are open for visitors daily from 10:00 AM - 1:00 PM and 5:00 PM - 8:00 PM.\n" +
                        "• Emergency: 24/7 Trauma and Acute Care service is active on Ground Floor Wing A.";

            case "APPOINTMENT_PREP":
                return "CareFlow Appointment Preparation Checklist:\n" +
                        "1. Identification & Insurance: Photo ID and insurance health card.\n" +
                        "2. Documentation: Previous lab results, imaging discs (X-rays, MRIs), and consultation history.\n" +
                        "3. Medication List: Complete list of current prescriptions, supplements, and known allergies.\n" +
                        "4. Questions: Write down 3-5 specific symptoms or questions you wish to discuss with the doctor.\n" +
                        "5. Timing: Please arrive 15 minutes prior to your scheduled time.";

            case "CONSULTATION_SUMMARY":
                return "Structured Clinical Summary:\n" +
                        "• Chief Concern: Documented patient consultation notes reviewed.\n" +
                        "• Clinical Findings: Documented symptoms and vital signs organized.\n" +
                        "• Care Plan Overview: Laboratory investigations and therapeutic regimen initiated as indicated.\n" +
                        "• Follow-Up: Schedule review consultation upon receipt of pending diagnostic results.";

            case "ADMIN_SUMMARY":
                return "Operational Summary & Hospital Insights:\n" +
                        "• Capacity Utilization: Department appointments are operating within balanced thresholds.\n" +
                        "• Diagnostic Lab Workflow: Laboratory turnaround times remain stable.\n" +
                        "• Revenue & Billing: Outstanding invoices are tracked with automated patient notifications.\n" +
                        "• Recommendations: Continue monitoring peak morning hours for receptionist desk load balancing.";

            default:
                return "CareFlow Assistant: Your inquiry has been processed. For specific medical queries or acute emergencies, please contact your attending physician or hospital emergency desk directly.";
        }
    }
}
