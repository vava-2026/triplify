package com.triplify.application.usecase.badgegroup;

import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.domain.result.Result;

import java.util.List;

public interface BadgeGroupService {

    Result<List<BadgeGroupResponse>> getAllBadgeGroups();
}
