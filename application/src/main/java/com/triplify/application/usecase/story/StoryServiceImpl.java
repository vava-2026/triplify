package com.triplify.application.usecase.story;

import com.google.inject.Inject;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.application.usecase.statistic.StatisticService;
import com.triplify.application.usecase.statistic.dto.IncrementStatisticRequest;
import com.triplify.domain.error.StoryError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.error.TripPlaceError;
import com.triplify.domain.error.TripRouteError;
import com.triplify.domain.filter.StoryFilter;
import com.triplify.domain.model.Emotion;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Story;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.EmotionRepository;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.domain.repository.StoryRepository;
import com.triplify.domain.repository.TripPlaceRepository;
import com.triplify.domain.repository.TripRepository;
import com.triplify.domain.repository.TripRouteRepository;
import com.triplify.domain.result.Result;
import com.triplify.domain.model.enums.StatisticType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Authenticated(roles = {RoleEnum.PRO_USER})
public class StoryServiceImpl implements StoryService {

    private static final Logger log = LoggerFactory.getLogger(StoryServiceImpl.class);
    private final int DEFAULT_PAGE_SIZE = 10;

    private final StoryRepository storyRepository;
    private final ImageRepository imageRepository;
    private final EmotionRepository emotionRepository;
    private final UserSessionContext sessionContext;
    private final TripRepository tripRepository;
    private final TripRouteRepository tripRouteRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final StatisticService statisticService;

    @Inject
    public StoryServiceImpl(StoryRepository storyRepository,
                            ImageRepository imageRepository,
                            EmotionRepository emotionRepository,
                            TripRepository tripRepository,
                            TripPlaceRepository tripPlaceRepository,
                            TripRouteRepository  tripRouteRepository,
                            UserSessionContext sessionContext,
                            StatisticService statisticService) {
        this.storyRepository = storyRepository;
        this.imageRepository = imageRepository;
        this.tripRepository = tripRepository;
        this.tripPlaceRepository = tripPlaceRepository;
        this.tripRouteRepository = tripRouteRepository;
        this.sessionContext = sessionContext;
        this.emotionRepository = emotionRepository;
        this.statisticService = statisticService;
    }

    @Override
    public Result<StoryResponse> addStory(AddStoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        if (request.tripId() == null && request.tripRouteId() == null && request.tripPlaceId() == null) {
            return Result.fail(new ApplicationError.Unexpected(
                    "Story must be linked to at least one of: tripId, tripRouteId, tripPlaceId."));
        }

        if (request.tripId() != null) {
            if (tripRepository.findById(request.tripId()).isEmpty()) {
                return Result.fail(new TripError.NotFound(request.tripId().toString()));
            }
        }

        if (request.tripRouteId() != null) {
            if (tripRouteRepository.findById(request.tripRouteId()).isEmpty()) {
                return Result.fail(new TripRouteError.NotFound(request.tripRouteId().toString()));
            }
        }

        if (request.tripPlaceId() != null) {
            if (tripPlaceRepository.findById(request.tripPlaceId()).isEmpty()) {
                return Result.fail(new TripPlaceError.NotFound(request.tripPlaceId().toString()));
            }
        }

        if (request.emotionId() != null) {
            Optional<Emotion> opt = emotionRepository.findById(request.emotionId());
            if (opt.isEmpty()) {
                return Result.fail(new ApplicationError.Unexpected("Emotion does not exist by the provided emotionId."));
            }
        }

        Story story = new Story(
                user.userId(),
                request.tripId(),
                request.tripRouteId(),
                request.tripPlaceId(),
                request.emotionId(),
                request.title(),
                request.description(),
                request.storyTime()
        );

        if (request.tagIds() != null) {
            request.tagIds().forEach(story::addTag);
        }

        storyRepository.create(story);
    statisticService.incrementStatistic(new IncrementStatisticRequest(user.userId(), StatisticType.STORIES_CREATED)).orThrow();
        log.info("Created story id='{}', title='{}' for userId='{}'",
                story.getId(), story.getTitle(), user.userId());
        return Result.ok(toResponse(story));
    }

    @Override
    public Result<StoryResponse> updateStory(UpdateStoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Story> existing = storyRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Attempt to update non-existing story id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new StoryError.NotFound(request.id().toString()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            log.warn("Attempt to update story id='{}' owned by userId='{}' by userId='{}'",
                    request.id(), story.getUserId(), user.userId());
            return Result.fail(new StoryError.NotOwner(request.id().toString()));
        }

        story.updateTitle(request.title());
        story.updateDescription(request.description());
        story.updateStoryTime(request.storyTime());
        story.updateEmotion(request.emotionId());

        Set<UUID> existingTagIds = new LinkedHashSet<>(story.getTagIds());
        existingTagIds.forEach(story::removeTag);
        if (request.tagIds() != null) {
            request.tagIds().forEach(story::addTag);
        }

        storyRepository.update(story);
        log.info("Updated story id='{}', title='{}' by userId='{}'",
                story.getId(), story.getTitle(), user.userId());
        return Result.ok(toResponse(story));
    }

    @Override
    public Result<Void> deleteStory(DeleteStoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Story> existing = storyRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Attempt to delete non-existing story id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new StoryError.NotFound(request.id().toString()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            log.warn("Attempt to delete story id='{}' owned by userId='{}' by userId='{}'",
                    request.id(), story.getUserId(), user.userId());
            return Result.fail(new StoryError.NotOwner(request.id().toString()));
        }

        storyRepository.delete(story);
        log.info("Deleted story id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok(null);
    }

    @Override
    public Result<StoryResponse> getStoryById(GetStoryByIdRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Story> existing = storyRepository.findById(request.id());
        if (existing.isEmpty()) {
            log.warn("Attempt to get non-existing story id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new StoryError.NotFound(request.id().toString()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            return Result.fail(new StoryError.NotFound(request.id().toString()));
        }

        return Result.ok(toResponse(story));
    }

    @Override
    public Result<Page<StoryResponse>> getStories(GetStoriesRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        GetStoriesRequest.Filter f = request.filter();
        StoryFilter filter = new StoryFilter(
                user.userId(),
                f.tripId(),
                f.tripRouteId(),
                f.tripPlaceId(),
                f.title(),
                f.storyTimeFrom(),
                f.storyTimeTo(),
                request.orderBy().storyTimeAsc()
        );

        log.debug("Getting stories for userId='{}'", user.userId());
        Page<Story> page = storyRepository.findList(request.pageRequest(), filter);
        return Result.ok(page.map(this::toResponse));
    }

    private StoryResponse toResponse(Story story) {
        Page<Image> imagesPage = imageRepository.findAll(
                new PageRequest(0, DEFAULT_PAGE_SIZE),
                story.getId().toString(),
                "STORY",
                null, null, true
        );

        return StoryResponse.from(story, imagesPage);
    }
}
