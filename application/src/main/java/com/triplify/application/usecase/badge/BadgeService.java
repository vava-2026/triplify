package com.triplify.application.usecase.badge;

import com.triplify.application.usecase.badge.dto.AddBadgeRequest;
import com.triplify.application.usecase.badge.dto.BadgeResponse;
import com.triplify.application.usecase.badge.dto.DeleteBadgeRequest;
import com.triplify.application.usecase.badge.dto.GetBadgesRequest;
import com.triplify.application.usecase.badge.dto.UpdateBadgeRequest;
import com.triplify.domain.result.Result;

import java.util.List;

public interface BadgeService {

    Result<BadgeResponse> addBadge(AddBadgeRequest request);

    Result<BadgeResponse> updateBadge(UpdateBadgeRequest request);

    Result<Void> deleteBadge(DeleteBadgeRequest request);

    Result<List<BadgeResponse>> getBadges(GetBadgesRequest request);
}
