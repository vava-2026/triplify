package com.triplify.application.usecase.user.dto;

import java.nio.file.Path;

public record UpdateUserAvatarRequest(

        Path avatar
) {
}
