package com.triplify.ui.pages;

import com.triplify.ui.routing.TriplifyRouterContext;
import jdk.jshell.spi.ExecutionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowedPageController extends SimplePageController {

    private static final Logger log = LoggerFactory.getLogger(WindowedPageController.class);

    @Override
    public void onLifecycleShow() {
        log.info("Applying windowed mode for page: {}", getClass().getSimpleName());
        applyWindowedMode();
        onWindowedShow();
    }

    protected void onWindowedShow() {
    }

    private void applyWindowedMode() {
        TriplifyRouterContext context = (TriplifyRouterContext)getRouter().getContext();
        context.setFullScreenContent(false);
    }
}
