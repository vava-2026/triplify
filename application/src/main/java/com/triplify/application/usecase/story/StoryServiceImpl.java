package com.triplify.application.usecase.story;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.emotion.dto.EmotionResponse;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.domain.error.StoryError;
import com.triplify.domain.filter.StoryFilter;
import com.triplify.domain.model.Emotion;
import com.triplify.domain.model.Image;
import com.triplify.domain.model.Story;
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.domain.repository.ImageRepository;
import com.triplify.domain.repository.StoryRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Authenticated(roles = {RoleEnum.PRO_USER})
public class StoryServiceImpl implements StoryService {

    private static final Logger log = LoggerFactory.getLogger(StoryServiceImpl.class);

    private final StoryRepository storyRepository;
    private final ImageRepository imageRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public StoryServiceImpl(StoryRepository storyRepository,
                            ImageRepository imageRepository,
                            UserSessionContext sessionContext) {
        this.storyRepository = storyRepository;
        this.imageRepository = imageRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    public Result<StoryResponse> addStory(AddStoryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        UUID tripId = parseUuid(request.tripId());
        UUID tripRouteId = parseUuid(request.tripRouteId());
        UUID tripPlaceId = parseUuid(request.tripPlaceId());

        if (tripId == null && tripRouteId == null && tripPlaceId == null) {
            return Result.fail(new ApplicationError.Unexpected(
                    "Story must be linked to at least one of: tripId, tripRouteId, tripPlaceId."));
        }

        Story story = new Story(
                user.userId(),
                tripId,
                tripRouteId,
                tripPlaceId,
                parseUuid(request.emotionId()),
                request.title(),
                request.description(),
                request.storyTime()
        );

        if (request.tagIds() != null) {
            request.tagIds().forEach(tagId -> story.addTag(UUID.fromString(tagId)));
        }

        storyRepository.create(story);
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
            return Result.fail(new StoryError.NotFound(request.id()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            log.warn("Attempt to update story id='{}' owned by userId='{}' by userId='{}'",
                    request.id(), story.getUserId(), user.userId());
            return Result.fail(new StoryError.NotOwner(request.id()));
        }

        story.updateTitle(request.title());
        story.updateDescription(request.description());
        story.updateStoryTime(request.storyTime());
        story.updateEmotion(parseUuid(request.emotionId()));

        Set<UUID> existingTagIds = new LinkedHashSet<>(story.getTagIds());
        existingTagIds.forEach(story::removeTag);
        if (request.tagIds() != null) {
            request.tagIds().forEach(tagId -> story.addTag(UUID.fromString(tagId)));
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
            return Result.fail(new StoryError.NotFound(request.id()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            log.warn("Attempt to delete story id='{}' owned by userId='{}' by userId='{}'",
                    request.id(), story.getUserId(), user.userId());
            return Result.fail(new StoryError.NotOwner(request.id()));
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
            return Result.fail(new StoryError.NotFound(request.id()));
        }

        Story story = existing.get();
        if (!story.getUserId().equals(user.userId())) {
            return Result.fail(new StoryError.NotFound(request.id()));
        }

        return Result.ok(toResponse(story));
    }

    @Override
    public Result<Page<StoryResponse>> getStories(GetStoriesRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        GetStoriesRequest.Filter f = request.filter();
        boolean asc = request.orderBy() != null && request.orderBy().storyTimeAsc();

        StoryFilter filter = new StoryFilter(
                user.userId(),
                f != null ? f.tripId() : null,
                f != null ? f.tripRouteId() : null,
                f != null ? f.tripPlaceId() : null,
                f != null ? f.title() : null,
                f != null ? f.storyTimeFrom() : null,
                f != null ? f.storyTimeTo() : null,
                asc
        );

        log.debug("Getting stories for userId='{}'", user.userId());
        Page<Story> page = storyRepository.findList(request.pageRequest(), filter);
        return Result.ok(page.map(this::toResponse));
    }

    private StoryResponse toResponse(Story story) {
        EmotionResponse emotionResponse = null;
        Emotion emotion = story.getEmotion();
        if (emotion != null) {
            emotionResponse = new EmotionResponse(
                    emotion.getId().toString(),
                    emotion.getCreatedById().toString(),
                    emotion.getName(),
                    emotion.getNameSk(),
                    emotion.getEmojiUnicode()
            );
        }

        Set<TagResponse> tagResponses = story.getTags().stream()
                .map(this::tagToResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Load images linked to this story. We use a large page to retrieve all of them.
        Page<Image> imagesPage = imageRepository.findAll(
                new PageRequest(0, 200),
                story.getId().toString(),
                "STORY",
                null, null, true
        );
        Set<ImageResponse> imageResponses = imagesPage.items().stream()
                .map(ImageResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new StoryResponse(
                story.getId().toString(),
                story.getUserId().toString(),
                uuidStr(story.getTripId()),
                uuidStr(story.getTripRouteId()),
                uuidStr(story.getTripPlaceId()),
                emotionResponse,
                story.getTitle(),
                story.getDescription(),
                story.getStoryTime(),
                story.getCreatedAt(),
                tagResponses,
                imageResponses
        );
    }

    private TagResponse tagToResponse(Tag tag) {
        return new TagResponse(
                tag.getId().toString(),
                tag.getUserId().toString(),
                tag.getName(),
                ColorTheme.from(tag.getColor())
        );
    }

    private static UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private static String uuidStr(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }
}
