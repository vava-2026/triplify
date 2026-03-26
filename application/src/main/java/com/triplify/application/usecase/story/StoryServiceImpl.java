package com.triplify.application.usecase.story;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class StoryServiceImpl implements StoryService {

    @Override
    public Result<StoryResponse> addStory(AddStoryRequest request) {
        // TODO: implement story creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: StoryService.addStory"));
    }

    @Override
    public Result<StoryResponse> updateStory(UpdateStoryRequest request) {
        // TODO: implement story update.
        return Result.fail(new ApplicationError.Unexpected("TODO: StoryService.updateStory"));
    }

    @Override
    public Result<Void> deleteStory(DeleteStoryRequest request) {
        // TODO: implement story deletion.
        return Result.fail(new ApplicationError.Unexpected("TODO: StoryService.deleteStory"));
    }

    @Override
    public Result<StoryResponse> getStoryById(GetStoryByIdRequest request) {
        // TODO: implement story retrieval by id.
        return Result.fail(new ApplicationError.Unexpected("TODO: StoryService.getStoryById"));
    }

    @Override
    public Result<Page<StoryResponse>> getStories(GetStoriesRequest request) {
        // TODO: implement story search with pagination, filters and ordering.
        return Result.fail(new ApplicationError.Unexpected("TODO: StoryService.getStories"));
    }
}

