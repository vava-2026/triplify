package com.triplify.application.usecase.badgegroup;

import com.google.inject.Inject;
import com.triplify.application.error.ApplicationError;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.domain.model.BadgeGroup;
import com.triplify.domain.repository.BadgeGroupRepository;
import com.triplify.domain.result.Result;

import java.util.List;

@Authenticated
public class BadgeGroupServiceImpl implements BadgeGroupService {

    private final BadgeGroupRepository badgeGroupRepository;

    @Inject
    public BadgeGroupServiceImpl(BadgeGroupRepository badgeGroupRepository) {
        this.badgeGroupRepository = badgeGroupRepository;
    }

    @Override
    public Result<List<BadgeGroupResponse>> getAllBadgeGroups() {
        try {
            List<BadgeGroupResponse> groups = badgeGroupRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
            return Result.ok(groups);
        } catch (Exception ex) {
            return Result.fail(new ApplicationError.StorageFailure("getAllBadgeGroups", ex));
        }
    }

    private BadgeGroupResponse toResponse(BadgeGroup group) {
        return new BadgeGroupResponse(
                group.getId().toString(),
                group.getName(),
                group.getNameSk(),
                group.getDescription(),
                group.getDescriptionSk(),
                group.getCreatedById().toString()
        );
    }
}
