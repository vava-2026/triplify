package com.triplify.application.usecase.badge;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.badge.dto.AddBadgeRequest;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.DeleteBadgeRequest;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.badge.dto.UpdateBadgeRequest;
import com.triplify.domain.result.Result;

import java.util.List;

public class BadgeServiceImpl implements BadgeService {

    @Override
    public Result<BadgeResponse> addBadge(AddBadgeRequest request) {
        // TODO: implement badge creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: BadgeService.addBadge"));
    }

    @Override
    public Result<BadgeResponse> updateBadge(UpdateBadgeRequest request) {
        // TODO: implement badge update.
        return Result.fail(new ApplicationError.Unexpected("TODO: BadgeService.updateBadge"));
    }

    @Override
    public Result<Void> deleteBadge(DeleteBadgeRequest request) {
        // TODO: implement badge delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: BadgeService.deleteBadge"));
    }

    @Override
    public Result<List<BadgeResponse>> getBadges(GetBadgesRequest request) {
        // TODO: implement badge retrieval with filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: BadgeService.getBadges"));
    }
}
