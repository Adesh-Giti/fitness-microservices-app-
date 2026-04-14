package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.RequestActivity;
import com.fitness.activityservice.dto.ResponseActivity;
import com.fitness.activityservice.models.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor        //Dependency injection, immutable objects
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidation userValidation;
    private final KafkaTemplate<String,Activity> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public ResponseActivity trackActivity(RequestActivity request) {

        boolean isValid=userValidation.validateUser(request.getUserId());

        if (!isValid){
            throw new RuntimeException("Invalid user "+request.getUserId());
        }

        Activity activity=Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .additionalMetrics(request.getAdditionalMetrics())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .build();
        Activity savedActivity=activityRepository.save(activity);
        try {
            kafkaTemplate.send(topicName,activity.getUserId(),activity);
        }catch (Exception e){
            log.info("Error in activity produer {}",e.toString());
        }
        return mapToResponse(savedActivity);
    }

    private ResponseActivity mapToResponse(Activity savedActivity) {
        ResponseActivity response=new ResponseActivity();
        response.setId(savedActivity.getId());
        response.setUserId(savedActivity.getUserId());
        response.setDuration(savedActivity.getDuration());
        response.setType(savedActivity.getType());
        response.setCaloriesBurned(savedActivity.getCaloriesBurned());
        response.setAdditionalMetrics(savedActivity.getAdditionalMetrics());
        response.setCreatedAt(savedActivity.getCreatedAt());
        response.setStartTime(savedActivity.getStartTime());
        response.setUpdatedAt(savedActivity.getUpdatedAt());
        return response;
    }
}
