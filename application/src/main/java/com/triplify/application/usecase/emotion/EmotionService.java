package com.triplify.application.usecase.emotion;

import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.GetAllEmotionsRequest;
import com.triplify.application.usecase.emotion.dto.GetEmotionByIdRequest;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface EmotionService {

    Result<EmotionResponse> createEmotion(CreateEmotionRequest request);

    Result<EmotionResponse> updateEmotion(UpdateEmotionRequest request);

    Result<Void> deleteEmotion(DeleteEmotionRequest request);

    Result<EmotionResponse> findById(GetEmotionByIdRequest request);

    Result<Page<EmotionResponse>> getAllEmotions(GetAllEmotionsRequest request);
}
