package com.triplify.application.usecase.tag.dto;

import com.triplify.application.error.ValidationMessage;
import jakarta.validation.constraints.NotBlank;

public record DeleteTagRequest(

        @NotBlank(message = ValidationMessage.Constants.REQUIRED)
        String tagId
) {
}
