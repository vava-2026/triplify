package com.triplify.domain.model.tag;

import com.triplify.domain.model.story.Story;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryTag {
    @NonNull
    private Story story;
    @NonNull
    private Tag tag;
}