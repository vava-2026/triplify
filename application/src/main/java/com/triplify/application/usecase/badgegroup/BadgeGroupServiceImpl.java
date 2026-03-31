package com.triplify.application.usecase.badgegroup;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.badgegroup.dto.BadgeGroupResponse;
import com.triplify.domain.result.Result;

import java.util.List;

public class BadgeGroupServiceImpl implements BadgeGroupService {

    @Override
    public Result<List<BadgeGroupResponse>> getAllBadgeGroups() {
        // TODO: implement badge group retrieval.
        return Result.fail(new ApplicationError.Unexpected("TODO: BadgeGroupService.getAllBadgeGroups"));
    }
}
