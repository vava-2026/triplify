package com.triplify.application.usecase.emotion.dto;

import com.triplify.domain.pagination.PageRequest;

public record GetAllEmotionsRequest(
        PageRequest pageRequest
) {
    public GetAllEmotionsRequest {
        pageRequest = pageRequest == null ? PageRequest.defaultRequest() : pageRequest;
    }
}
