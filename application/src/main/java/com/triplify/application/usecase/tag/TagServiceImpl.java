package com.triplify.application.usecase.tag;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.tag.dto.CreateTagRequest;
import com.triplify.application.usecase.tag.dto.DeleteTagRequest;
import com.triplify.application.usecase.tag.dto.GetTagsRequest;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.tag.dto.UpdateTagRequest;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class TagServiceImpl implements TagService {

    @Override
    public Result<TagResponse> createTag(CreateTagRequest request) {
        // TODO: implement tag creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.createTag"));
    }

    @Override
    public Result<TagResponse> updateTag(UpdateTagRequest request) {
        // TODO: implement tag update.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.updateTag"));
    }

    @Override
    public Result<Void> deleteTag(DeleteTagRequest request) {
        // TODO: implement tag delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.deleteTag"));
    }

    @Override
    public Result<Page<TagResponse>> getTags(GetTagsRequest request) {
        // TODO: implement tag search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: TagService.getTags"));
    }
}
