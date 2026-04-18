package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt=createPromptForActivity(activity);
        String aiResponse=geminiService.getRecommendation(prompt);
        log.info("Response from AI {}",aiResponse);
        return processAiResponse(activity,aiResponse);
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper=new ObjectMapper();
            JsonNode rootNode= mapper.readTree(aiResponse);
            JsonNode textNode= rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContent=textNode.asString();
            log.info("Json Response {}",jsonContent);
            JsonNode analysisJson=mapper.readTree(jsonContent);
            JsonNode analysisNode=analysisJson.path("analysis");
            StringBuilder fullAnalysis=new StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall:");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace:");
            addAnalysisSection(fullAnalysis,analysisNode,"heartRate","HeartRate:");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","CaloriesBurned:");
            List<String> improvements=extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions=extractSuggestions(analysisJson.path("suggestions"));
            List<String> safetys=extractSafetys(analysisJson.path("safety"));
            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safetys)
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .recommendation("Unable to provide fullAnalysis")
                .improvements(Collections.singletonList("Follow your current routine"))
                .suggestions(Collections.singletonList("Consult to fitness consultant"))
                .safety(Arrays.asList(
                        "Stay Hydrated",
                        "Follow rule",
                        "Always warm up before exercise"
                ))
                .build();
    }

    private List<String> extractSafetys(JsonNode safetyNode) {
        List<String> safetys=new ArrayList<>();
        if (safetyNode.isArray()){
            safetyNode.forEach(safety-> safetys.add(safety.asString()));
        }
        return safetys.isEmpty()?Collections.singletonList("No Specifc Safety Recommendation Provided")
                :safetys;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions=new ArrayList<>();
        if (suggestionsNode.isArray()){
            suggestionsNode.forEach(suggestion ->{
                String workout=suggestion.path("workout").asString();
                String description=suggestion.path("description").asString();
                suggestions.add(String.format("%s: %s",workout,description));
            });
        }
        return suggestions.isEmpty()?Collections.singletonList("No Specific Suggestion Provided"):suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements=new ArrayList<>();
        if(improvementNode.isArray()){
            improvementNode.forEach(improvement ->{
                String area=improvement.path("area").asString();
                String recommendation=improvement.path("recommendation").asString();
                improvements.add(String.format("%s: %s",area,recommendation));
            });
        }
        return improvements.isEmpty()? Collections.singletonList("No specific requirements provided")
                : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()){
            fullAnalysis=fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asString())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
