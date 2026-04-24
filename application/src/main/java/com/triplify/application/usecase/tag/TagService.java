package com.triplify.application.usecase.tag;

import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.DeleteTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.ResolveOrCreateTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.tag.dto.UpdateTagRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public interface TagService {

    Result<TagResponse> createTag(CreateTagRequest request);

    Result<TagResponse> updateTag(UpdateTagRequest request);

    Result<Void> deleteTag(DeleteTagRequest request);

    Result<List<TagResponse>> getTags(GetTagsRequest request);

    Result<LinkedHashSet<UUID>> resolveOrCreateTags(ResolveOrCreateTagsRequest request);
}
