package com.triplify.application.usecase.tag;

import com.google.inject.Inject;
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
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        var existing = tagRepository.findByUserIdAndName(user.userId(), request.name());
        if (existing.isPresent()) {
            return Result.fail(new TagError.AlreadyExists(request.name()));
        }

        Tag tag = new Tag(user.userId(), request.name(), toDomainColor(request.color()));
        tagRepository.create(tag);
        return Result.ok(toResponse(tag));
    }

    @Override
    public Result<TagResponse> updateTag(UpdateTagRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        var existing = tagRepository.findById(request.id());
        if (existing.isEmpty() || !existing.get().getUserId().equals(user.userId())) {
            return Result.fail(new TagError.NotFound(request.id().toString()));
        }

        var tagWithSameName = tagRepository.findByUserIdAndName(user.userId(), request.name());
        if (tagWithSameName.isPresent() && !tagWithSameName.get().getId().equals(existing.get().getId())) {
            return Result.fail(new TagError.AlreadyExists(request.name()));
        }

        Tag updatedTag = new Tag(existing.get().getId(), user.userId(), request.name(), toDomainColor(request.color()));
        tagRepository.update(updatedTag);
        return Result.ok(toResponse(updatedTag));
    }

    @Override
    public Result<Void> deleteTag(DeleteTagRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        var existing = tagRepository.findById(request.id());
        if (existing.isEmpty() || !existing.get().getUserId().equals(user.userId())) {
            return Result.fail(new TagError.NotFound(request.id().toString()));
        }
        tagRepository.delete(request.id());
        return Result.ok();
    }

    @Override
    public Result<Page<TagResponse>> getTags(GetTagsRequest request) {
        String name = request.filter() == null ? null : request.filter().name();
        Page<Tag> page = tagRepository.findList(request.pageRequest(), name);
        return Result.ok(page.map(this::toResponse));
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getUserId(),
                tag.getName(),
                tag.getColor() == null ? null : ColorTheme.from(tag.getColor())
        );
    }

    private ColorEnum toDomainColor(ColorTheme colorTheme) {
        return colorTheme.toColorEnum();
    }
}
