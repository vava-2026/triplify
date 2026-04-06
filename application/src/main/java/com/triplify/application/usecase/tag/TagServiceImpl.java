package com.triplify.application.usecase.tag;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.model.ColorTheme;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.DeleteTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.tag.dto.UpdateTagRequest;
import com.triplify.domain.error.TagError;
import com.triplify.domain.filter.TagFilter;
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.TagRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

@Authenticated
public class TagServiceImpl implements TagService {

    private static final Logger log = LoggerFactory.getLogger(TagServiceImpl.class);

    // Colors available in the SQL schema — TEAL is excluded as it is not stored in the DB.
    private static final ColorEnum[] AVAILABLE_COLORS = {
            ColorEnum.RED, ColorEnum.ORANGE, ColorEnum.YELLOW, ColorEnum.GREEN,
            ColorEnum.BLUE, ColorEnum.PURPLE, ColorEnum.PINK
    };
    private static final Random RANDOM = new Random();

    private final TagRepository tagRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public TagServiceImpl(TagRepository tagRepository, UserSessionContext sessionContext) {
        this.tagRepository = tagRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    public Result<TagResponse> createTag(CreateTagRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        if (tagRepository.existsByUserIdAndName(user.userId().toString(), request.name())) {
            log.warn("Attempted to create duplicate tag name='{}' for userId='{}'",
                    request.name(), user.userId());
            return Result.fail(new TagError.AlreadyExists(request.name()));
        }

        // Color is assigned randomly — users do not choose their tag color.
        ColorEnum randomColor = AVAILABLE_COLORS[RANDOM.nextInt(AVAILABLE_COLORS.length)];
        Tag tag = new Tag(user.userId(), request.name(), randomColor);

        tagRepository.create(tag);
        log.info("Created tag with id='{}', name='{}' for userId='{}'",
                tag.getId(), tag.getName(), user.userId());
        return Result.ok(toResponse(tag));
    }

    @Override
    public Result<TagResponse> updateTag(UpdateTagRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Tag> existing = tagRepository.findById(request.id());
        if (existing.isEmpty() || !existing.get().getUserId().equals(user.userId())) {
            log.warn("Attempt to update non-existing or not-owned tag with id='{}' by userId='{}'",
                    request.id(), user.userId());
            return Result.fail(new TagError.NotFound(request.id()));
        }

        Tag tag = existing.get();

        // Check name uniqueness only if the name is actually changing.
        if (!tag.getName().equalsIgnoreCase(request.name())
                && tagRepository.existsByUserIdAndName(user.userId().toString(), request.name())) {
            log.warn("Attempted to rename tag id='{}' to already-existing name='{}' for userId='{}'",
                    request.id(), request.name(), user.userId());
            return Result.fail(new TagError.AlreadyExists(request.name()));
        }

        try {
            tag.updateName(request.name());
            // Color is intentionally not updated — tag color is fixed after creation.
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ApplicationError.Unexpected(ex.getMessage()));
        }

        tagRepository.update(tag);
        log.info("Updated tag with id='{}', name='{}' for userId='{}'",
                tag.getId(), tag.getName(), user.userId());
        return Result.ok(toResponse(tag));
    }

    @Override
    public Result<Void> deleteTag(DeleteTagRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Tag> existing = tagRepository.findById(request.id());
        if (existing.isEmpty() || !existing.get().getUserId().equals(user.userId())) {
            log.warn("Attempt to delete non-existing or not-owned tag with id='{}' by userId='{}'",
                    request.id(), user.userId());
            return Result.fail(new TagError.NotFound(request.id()));
        }

        tagRepository.delete(existing.get());
        log.info("Deleted tag with id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok(null);
    }

    @Override
    public Result<Page<TagResponse>> getTags(GetTagsRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        TagFilter filter = new TagFilter(
                user.userId(),
                request.filter() != null ? request.filter().name() : null
        );

        log.debug("Getting tags for userId='{}', nameFilter='{}'", user.userId(), filter.name());

        Page<Tag> page = tagRepository.findList(request.pageRequest(), filter);
        return Result.ok(page.map(this::toResponse));
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId().toString(),
                tag.getUserId().toString(),
                tag.getName(),
                ColorTheme.from(tag.getColor())
        );
    }
}
