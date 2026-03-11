package com.triplify.ui.shared.util;

import javafx.scene.Node;

public record FxmlLoadResult<N extends Node, C>(N node, C controller) {}
