package com.triplify.application.usecase.badge;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.badge.dto.AddBadgeRequest;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.DeleteBadgeRequest;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.badge.dto.UpdateBadgeRequest;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.image.dto.UpdateImageRequest;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.BadgeError;
import com.triplify.domain.error.BadgeGroupError;
import com.triplify.domain.model.Badge;
import com.triplify.domain.model.BadgeGroup;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.repository.BadgeGroupRepository;
import com.triplify.domain.repository.BadgeRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Authenticated
public class BadgeServiceImpl implements BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeServiceImpl.class);
    private static final String DEFAULT_IMAGE_DESCRIPTION = "Badge image for ";

    private final BadgeRepository badgeRepository;
    private final BadgeGroupRepository badgeGroupRepository;
    private final ImageService imageService;
    private final UserSessionContext sessionContext;

    @Inject
    public BadgeServiceImpl(BadgeRepository badgeRepository,
                            BadgeGroupRepository badgeGroupRepository,
                            ImageService imageService,
                            UserSessionContext sessionContext) {
        this.badgeRepository = badgeRepository;
        this.badgeGroupRepository = badgeGroupRepository;
        this.imageService = imageService;
        this.sessionContext = sessionContext;
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<BadgeResponse> addBadge(AddBadgeRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<BadgeGroup> groupRes = badgeGroupRepository.findById(request.groupId());
        if (groupRes.isEmpty()) {
            log.warn("Attempt to add badge for non-existing groupId='{}' by userId='{}'", request.groupId(), user.userId());
            return Result.fail(new BadgeGroupError.NotFound(request.groupId()));
        }

        if (badgeRepository.existsByNameAndLevel(request.groupId(), request.name(), request.level())) {
            log.warn("Attempted to add duplicate badge name='{}', level='{}', groupId='{}' by userId='{}'",
                    request.name(), request.level(), request.groupId(), user.userId());
            return Result.fail(new BadgeError.AlreadyExists(request.name()));
        }

        Badge badge;
        try {
            badge = new Badge(user.userId(), UUID.fromString(request.groupId()), request.name(), request.nameSk(), request.level());
            badge.updateDescription(request.description());
            badge.updateDescriptionSk(request.descriptionSk());
            badge.updateRequiredValue(request.requiredValue());
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ApplicationError.Unexpected(ex.getMessage()));
        }

        ImageResponse image = null;
        if (request.image() != null) {
            Result<ImageResponse> imageResult = imageService.addImage(new AddImageRequest(request.image(), DEFAULT_IMAGE_DESCRIPTION + request.name()));
            if (imageResult.isFailure()) {
                return Result.fail(imageResult.getError());
            }
            image = imageResult.getValue();
            badge.updateImage(UUID.fromString(image.id()));
        }

        badgeRepository.create(badge);
        log.info("Added new badge with id='{}', name='{}' by userId='{}'", badge.getId(), badge.getName(), user.userId());
        return Result.ok(toResponse(badge, groupRes.get(), image));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<BadgeResponse> updateBadge(UpdateBadgeRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Badge> oldRes = badgeRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to update non-existing badge with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new BadgeError.NotFound(request.id()));
        }

        Badge old = oldRes.get();
        String groupId = old.getGroupId().toString();

        Optional<BadgeGroup> groupRes = badgeGroupRepository.findById(groupId);
        if (groupRes.isEmpty()) {
            log.warn("Attempt to update badge with non-existing groupId='{}' by userId='{}'", groupId, user.userId());
            return Result.fail(new BadgeGroupError.NotFound(groupId));
        }

        if (badgeRepository.existsByNameAndLevelExcludingId(groupId, request.name(), request.level(), request.id())) {
            log.warn("Attempted to update badge to duplicate name='{}', level='{}', groupId='{}' by userId='{}'",
                    request.name(), request.level(), groupId, user.userId());
            return Result.fail(new BadgeError.AlreadyExists(request.name()));
        }

        Badge badge;
        try {
            badge = new Badge(
                    old.getId(),
                    old.getCreatedById(),
                    old.getGroupId(),
                    old.getImageId(),
                    null,
                    old.getName(),
                    old.getNameSk(),
                    old.getDescription(),
                    old.getDescriptionSk(),
                    request.level(),
                    old.getRequiredValue()
            );

            badge.updateName(request.name());
            badge.updateNameSk(request.nameSk());
            badge.updateDescription(request.description());
            badge.updateDescriptionSk(request.descriptionSk());
            badge.updateRequiredValue(request.requiredValue());
        } catch (IllegalArgumentException ex) {
            return Result.fail(new ApplicationError.Unexpected(ex.getMessage()));
        }

        ImageResponse image = null;
        if (request.image() != null) {
            Result<ImageResponse> imageResult;
            if (old.getImageId() != null) {
                imageResult = imageService.updateImage(new UpdateImageRequest(
                        old.getImageId().toString(),
                        request.image(),
                        DEFAULT_IMAGE_DESCRIPTION + request.name()
                ));
            } else {
                imageResult = imageService.addImage(new AddImageRequest(request.image(), DEFAULT_IMAGE_DESCRIPTION + request.name()));
            }

            if (imageResult.isFailure()) {
                return Result.fail(imageResult.getError());
            }

            image = imageResult.getValue();
            badge.updateImage(UUID.fromString(image.id()));
        } else if (old.getImageId() != null) {
            image = resolveImage(old.getImageId());
        }

        badgeRepository.update(badge);
        log.info("Updated badge with id='{}', name='{}' by userId='{}'", badge.getId(), badge.getName(), user.userId());
        return Result.ok(toResponse(badge, groupRes.get(), image));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteBadge(DeleteBadgeRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Badge> oldRes = badgeRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to delete non-existing badge with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new BadgeError.NotFound(request.id()));
        }

        Badge badge = oldRes.get();
        badgeRepository.delete(badge);

        if (badge.getImageId() != null) {
            Result<Void> imageDelete = imageService.deleteImage(new DeleteImageRequest(badge.getImageId().toString()));
            if (imageDelete.isFailure()) {
                log.warn("Badge id='{}' deleted but linked image id='{}' could not be deleted", badge.getId(), badge.getImageId());
            }
        }

        log.info("Deleted badge with id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok(null);
    }

    @Override
    public Result<List<BadgeResponse>> getBadges(GetBadgesRequest request) {
        GetBadgesRequest.Filter filter = request != null ? request.filter() : null;
        String groupId = filter != null ? filter.groupId() : null;
        String createdById = filter != null ? filter.createdById() : null;

        try {
            List<BadgeResponse> badges = badgeRepository.findAll(groupId, createdById).stream()
                    .map(this::toResponse)
                    .toList();
            return Result.ok(badges);
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getBadges", ex));
        }
    }

    private BadgeResponse toResponse(Badge badge) {
        BadgeGroup group = badgeGroupRepository.findById(badge.getGroupId().toString())
                .orElseThrow(() -> new IllegalStateException("Badge group '%s' not found".formatted(badge.getGroupId())));

        return toResponse(badge, group, resolveImage(badge.getImageId()));
    }

    private BadgeResponse toResponse(Badge badge, BadgeGroup group, ImageResponse image) {
        return new BadgeResponse(
                badge.getId().toString(),
                badge.getCreatedById().toString(),
                toGroupResponse(group),
                image,
                badge.getName(),
                badge.getNameSk(),
                badge.getDescription(),
                badge.getDescriptionSk(),
                badge.getLevel(),
                badge.getRequiredValue()
        );
    }

    private BadgeGroupResponse toGroupResponse(BadgeGroup group) {
        return new BadgeGroupResponse(
                group.getId().toString(),
                group.getName(),
                group.getNameSk(),
                group.getDescription(),
                group.getDescriptionSk(),
                group.getCreatedById().toString()
        );
    }

    private ImageResponse resolveImage(UUID imageId) {
        if (imageId == null) {
            return null;
        }

        Result<ImageResponse> imageResult = imageService.getImageById(new GetImageByIdRequest(imageId.toString()));
        return imageResult.isSuccess() ? imageResult.getValue() : null;
    }
}
