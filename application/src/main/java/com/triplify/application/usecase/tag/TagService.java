package com.triplify.application.usecase.tag;

import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.DeleteTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.tag.dto.UpdateTagRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface TagService {

    Result<TagResponse> createTag(CreateTagRequest request);

    Result<TagResponse> updateTag(UpdateTagRequest request);

    Result<Void> deleteTag(DeleteTagRequest request);

    Result<Page<TagResponse>> getTags(GetTagsRequest request);
}
