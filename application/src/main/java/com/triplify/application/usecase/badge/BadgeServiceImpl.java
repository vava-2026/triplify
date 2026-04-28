package com.triplify.application.usecase.badge;

import com.google.inject.Inject;
import com.triplify.application.shared.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.badge.dto.AddBadgeRequest;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.DeleteBadgeRequest;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.badge.dto.UpdateBadgeRequest;
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
            return Result.fail(new BadgeGroupError.NotFound(request.groupId().toString()));
        }

        if (badgeRepository.existsByNameAndLevel(request.groupId(), request.name(), request.level())) {
            log.warn("Attempted to add duplicate badge name='{}', level='{}', groupId='{}' by userId='{}'",
                    request.name(), request.level(), request.groupId(), user.userId());
            return Result.fail(new BadgeError.AlreadyExists(request.name()));
        }

        Badge badge;
        badge = new Badge(user.userId(), request.groupId(), request.name(), request.nameSk(), request.level());
        badge.updateDescription(request.description());
        badge.updateDescriptionSk(request.descriptionSk());
        badge.updateRequiredValue(request.requiredValue());

        ImageResponse image = null;
        if (request.image() != null) {
            image = imageService.addImage(new AddImageRequest(request.image(), DEFAULT_IMAGE_DESCRIPTION + request.name())).orThrow();
            badge.updateImage(image.id());
        }

        badgeRepository.create(badge);
        log.info("Added new badge with id='{}', name='{}' by userId='{}'", badge.getId(), badge.getName(), user.userId());
        return Result.ok(BadgeResponse.from(badge, image));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<BadgeResponse> updateBadge(UpdateBadgeRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Badge> oldRes = badgeRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to update non-existing badge with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new BadgeError.NotFound(request.id().toString()));
        }

        Badge old = oldRes.get();
        UUID groupId = old.getGroupId();

        if (badgeRepository.existsByNameAndLevelExcludingId(groupId, request.name(), request.level(), request.id())) {
            log.warn("Attempted to update badge to duplicate name='{}', level='{}', groupId='{}' by userId='{}'",
                    request.name(), request.level(), groupId, user.userId());
            return Result.fail(new BadgeError.AlreadyExists(request.name()));
        }

        Badge badge;
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

        ImageResponse image = null;
        if (request.image() != null) {
            Result<ImageResponse> imageResult;
            if (old.getImageId() != null) {
                imageResult = imageService.updateImage(new UpdateImageRequest(
                        old.getImageId(),
                        request.image(),
                        DEFAULT_IMAGE_DESCRIPTION + request.name()
                ));
            } else {
                imageResult = imageService.addImage(new AddImageRequest(request.image(), DEFAULT_IMAGE_DESCRIPTION + request.name()));
            }

            image = imageResult.orThrow();
            badge.updateImage(image.id());
        } else if (old.getImageId() != null) {
            image = resolveImage(old.getImageId());
        }

        badgeRepository.update(badge);
        log.info("Updated badge with id='{}', name='{}' by userId='{}'", badge.getId(), badge.getName(), user.userId());
        return Result.ok(BadgeResponse.from(badge, image));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteBadge(DeleteBadgeRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Badge> oldRes = badgeRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to delete non-existing badge with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new BadgeError.NotFound(request.id().toString()));
        }

        Badge badge = oldRes.get();
        badgeRepository.delete(badge);

        if (badge.getImageId() != null) {
            Result<Void> imageDelete = imageService.deleteImage(new DeleteImageRequest(badge.getImageId()));
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
        UUID groupId = filter != null ? filter.groupId() : null;
        UUID createdById = filter != null ? filter.createdById() : null;

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
        return BadgeResponse.from(badge, resolveImage(badge.getImageId()));
    }

    private ImageResponse resolveImage(UUID imageId) {
        if (imageId == null) {
            return null;
        }

        Result<ImageResponse> imageResult = imageService.getImageById(new GetImageByIdRequest(imageId));
        return imageResult.isSuccess() ? imageResult.getValue() : null;
    }
}
