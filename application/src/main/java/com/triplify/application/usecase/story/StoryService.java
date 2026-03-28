package com.triplify.application.usecase.story;

import com.triplify.application.usecase.story.dto.AddStoryRequest;
import com.triplify.application.usecase.story.dto.DeleteStoryRequest;
import com.triplify.application.usecase.story.dto.GetStoriesRequest;
import com.triplify.application.usecase.story.dto.GetStoryByIdRequest;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.application.usecase.story.dto.UpdateStoryRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface StoryService {

    Result<StoryResponse> addStory(AddStoryRequest request);

    Result<StoryResponse> updateStory(UpdateStoryRequest request);

    Result<Void> deleteStory(DeleteStoryRequest request);

    Result<StoryResponse> getStoryById(GetStoryByIdRequest request);

    Result<Page<StoryResponse>> getStories(GetStoriesRequest request);
}
