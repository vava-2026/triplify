package com.triplify.application.usecase.emotion;

import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.domain.result.Result;

import java.util.List;

public interface EmotionService {

    Result<EmotionResponse> createEmotion(CreateEmotionRequest request);

    Result<EmotionResponse> updateEmotion(UpdateEmotionRequest request);

    Result<Void> deleteEmotion(DeleteEmotionRequest request);

    Result<List<EmotionResponse>> getAllEmotions();
}
