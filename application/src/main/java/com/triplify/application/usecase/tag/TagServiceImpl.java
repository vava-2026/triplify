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
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.TagRepository;
import com.triplify.domain.result.Result;

@Authenticated
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserSessionContext userSessionContext;

    @Inject
    TagServiceImpl(TagRepository tagRepository, UserSessionContext userSessionContext) {
        this.tagRepository = tagRepository;
        this.userSessionContext = userSessionContext;
    }

    @Override
    public Result<TagResponse> createTag(CreateTagRequest request) {
        try {
            SessionUser user = userSessionContext.getCurrent().orElseThrow();
            var existing = tagRepository.findByUserIdAndName(user.userId().toString(), request.name());
            if (existing.isPresent()) {
                return Result.fail(new TagError.AlreadyExists(request.name()));
            }

            Tag tag = new Tag(user.userId(), request.name(), toDomainColor(request.color()));
            tagRepository.create(tag);
            return Result.ok(toResponse(tag));
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("createTag", ex));
        }
    }

    @Override
    public Result<TagResponse> updateTag(UpdateTagRequest request) {
        // TODO: implement tag update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.updateTag"));
    }

    @Override
    public Result<Void> deleteTag(DeleteTagRequest request) {
        // TODO: implement tag delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.deleteTag"));
    }

    @Override
    public Result<Page<TagResponse>> getTags(GetTagsRequest request) {
        try {
            String name = request.filter() == null ? null : request.filter().name();
            Page<Tag> page = tagRepository.findList(request.pageRequest(), name);
            return Result.ok(page.map(this::toResponse));
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getTags", ex));
        }
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId().toString(),
                tag.getUserId().toString(),
                tag.getName(),
                tag.getColor() == null ? null : ColorTheme.from(tag.getColor())
        );
    }

    private ColorEnum toDomainColor(ColorTheme colorTheme) {
        return switch (colorTheme) {
            case GRAY -> ColorEnum.GRAY;
            case RED -> ColorEnum.RED;
            case ORANGE -> ColorEnum.ORANGE;
            case YELLOW -> ColorEnum.YELLOW;
            case GREEN -> ColorEnum.GREEN;
            case TEAL -> ColorEnum.TEAL;
            case BLUE -> ColorEnum.BLUE;
            case PURPLE -> ColorEnum.PURPLE;
            case PINK -> ColorEnum.PINK;
        };
    }
}
