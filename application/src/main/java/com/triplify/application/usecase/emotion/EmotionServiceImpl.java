package com.triplify.application.usecase.emotion;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.emotion.dto.CreateEmotionRequest;
import com.triplify.application.usecase.emotion.dto.DeleteEmotionRequest;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.emotion.dto.GetAllEmotionsRequest;
import com.triplify.application.usecase.emotion.dto.GetEmotionByIdRequest;
import com.triplify.application.usecase.emotion.dto.UpdateEmotionRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.EmotionError;
import com.triplify.domain.model.Emotion;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.EmotionRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Authenticated
public class EmotionServiceImpl implements EmotionService {

    private static final Logger log = LoggerFactory.getLogger(EmotionServiceImpl.class);

    private final EmotionRepository emotionRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public EmotionServiceImpl(EmotionRepository emotionRepository, UserSessionContext sessionContext) {
        this.emotionRepository = emotionRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<EmotionResponse> createEmotion(CreateEmotionRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();
        log.debug("Creating emotion name='{}' by userId='{}'", request.name(), user.userId());

        if (emotionRepository.findByName(request.name()).isPresent()) {
            log.warn("Emotion with name='{}' already exists", request.name());
            return Result.fail(new EmotionError.AlreadyExists(request.name()));
        }


        Emotion emotion = new Emotion(
            user.userId(),
            request.name(),
            request.nameSk(),
            request.emojiUnicode()
        );

        emotionRepository.create(emotion);
        log.info("Created emotion id='{}', name='{}' by userId='{}'",
                emotion.getId(), emotion.getName(), user.userId());
        return Result.ok(EmotionResponse.from(emotion));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<EmotionResponse> updateEmotion(UpdateEmotionRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();
        log.debug("Updating emotion id='{}' by userId='{}'", request.id(), user.userId());

        Optional<Emotion> existing = emotionRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Emotion with id='{}' not found", request.id());
            return Result.fail(new EmotionError.NotFound(request.id()));
        }

        Emotion emotion = existing.get();

        Optional<Emotion> byName = emotionRepository.findByName(request.name());
        if (byName.isPresent() && !byName.get().getId().equals(emotion.getId())) {
            log.warn("Cannot update emotion id='{}': name='{}' already used by id='{}'",
                    emotion.getId(), request.name(), byName.get().getId());
            return Result.fail(new EmotionError.AlreadyExists(request.name()));
        }

        emotion.updateName(request.name());
        emotion.updateNameSk(request.nameSk());
        emotion.updateEmojiUnicode(request.emojiUnicode());

        emotionRepository.update(emotion);
        log.info("Updated emotion id='{}', name='{}' by userId='{}'",
                emotion.getId(), emotion.getName(), user.userId());
        return Result.ok(EmotionResponse.from(emotion));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteEmotion(DeleteEmotionRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();
        log.debug("Deleting emotion id='{}' by userId='{}'", request.id(), user.userId());

        if (emotionRepository.findById(request.id()).isEmpty()) {
            log.warn("Emotion with id='{}' not found", request.id());
            return Result.fail(new EmotionError.NotFound(request.id()));
        }

        emotionRepository.delete(request.id());
        log.info("Deleted emotion id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok();
    }

    @Override
    public Result<EmotionResponse> findById(GetEmotionByIdRequest request) {
        log.debug("Finding emotion by id='{}'", request.id());
        Optional<Emotion> existing = emotionRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Emotion with id='{}' not found", request.id());
            return Result.fail(new EmotionError.NotFound(request.id()));
        }

        log.info("Found emotion id='{}'", existing.get().getId());
        return Result.ok(EmotionResponse.from(existing.get()));
    }

    @Override
    public Result<List<EmotionResponse>> getAllEmotions() {
        log.debug("Retrieving all emotions");

        List<EmotionResponse> responses = emotionRepository.findAll().stream()
                .map(EmotionResponse::from)
                .toList();

        log.info("Retrieved all emotions. count={}", responses.size());
        return Result.ok(responses);
    }
}
