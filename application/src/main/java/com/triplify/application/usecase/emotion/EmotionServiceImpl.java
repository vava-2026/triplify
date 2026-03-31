package com.triplify.application.usecase.emotion;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.domain.result.Result;

import java.util.List;

public class EmotionServiceImpl implements EmotionService {

    @Override
    public Result<EmotionResponse> createEmotion(CreateEmotionRequest request) {
        // TODO: implement emotion creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: EmotionService.createEmotion"));
    }

    @Override
    public Result<EmotionResponse> updateEmotion(UpdateEmotionRequest request) {
        // TODO: implement emotion update.
        return Result.fail(new ApplicationError.Unexpected("TODO: EmotionService.updateEmotion"));
    }

    @Override
    public Result<Void> deleteEmotion(DeleteEmotionRequest request) {
        // TODO: implement emotion delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: EmotionService.deleteEmotion"));
    }

    @Override
    public Result<List<EmotionResponse>> getAllEmotions() {
        // TODO: implement emotion retrieval.
        return Result.fail(new ApplicationError.Unexpected("TODO: EmotionService.getAllEmotions"));
    }
}
