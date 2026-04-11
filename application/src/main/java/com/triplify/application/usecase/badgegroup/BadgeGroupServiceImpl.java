package com.triplify.application.usecase.badgegroup;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.domain.repository.BadgeGroupRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Authenticated
public class BadgeGroupServiceImpl implements BadgeGroupService {

    private static final Logger log = LoggerFactory.getLogger(BadgeGroupServiceImpl.class);
    private final BadgeGroupRepository badgeGroupRepository;

    @Inject
    public BadgeGroupServiceImpl(BadgeGroupRepository badgeGroupRepository) {
        this.badgeGroupRepository = badgeGroupRepository;
    }

    @Override
    public Result<List<BadgeGroupResponse>> getAllBadgeGroups() {
        log.debug("Getting all badge groups");
        List<BadgeGroupResponse> groups = badgeGroupRepository.findAll().stream()
                .map(BadgeGroupResponse::from)
                .toList();
        return Result.ok(groups);
    }
}
