package com.triplify.ui.pages.places;

import com.google.inject.Inject;
import com.triplify.application.usecase.place.details.PlaceDetailsService;
import com.triplify.application.usecase.place.details.dto.GetPlaceDetailsRequest;
import com.triplify.application.usecase.place.details.dto.PlaceDetailsResponse;
import com.triplify.application.usecase.place.PlaceService;
import com.triplify.application.usecase.place.dto.DeletePlaceRequest;
import com.triplify.application.usecase.place.dto.PlaceResponse;
import com.triplify.application.usecase.route.dto.RouteResponse;
import com.triplify.application.usecase.story.dto.StoryResponse;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.map.InteractiveMap;
import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.shared.component.detail_actions.view.DetailActionButtonsView;
import com.triplify.ui.shared.component.empty_state.view.EmptyStateCardView;
import com.triplify.ui.shared.component.media_card.view.EditorMediaCardView;
import com.triplify.ui.shared.component.section_header.view.SectionHeaderView;
import com.triplify.ui.shared.toast.ToastService;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.io.File;
import java.util.Locale;

public class PlaceDetailsController extends SimpleLifecycleAwareController {

    private static final String DEFAULT_IMAGE = "/com/triplify/ui/pages/trips/images/one.png";

    @FXML private Button backButton;
    @FXML private ImageView heroImageView;
    @FXML private Label placeTitleLabel;
    @FXML private Label placeCountryLabel;
    @FXML private Label descriptionTitleLabel;
    @FXML private Label descriptionValueLabel;
    @FXML private Label mapTitleLabel;
    @FXML private InteractiveMap placeMap;
    @FXML private DetailActionButtonsView actionButtonsView;
    @FXML private SectionHeaderView associatedPlacesHeader;
    @FXML private SectionHeaderView associatedStoriesHeader;
    @FXML private SectionHeaderView associatedRoutesHeader;
    @FXML private FlowPane associatedPlacesFlow;
    @FXML private FlowPane associatedStoriesFlow;
    @FXML private FlowPane associatedRoutesFlow;

    @Inject private PlaceDetailsService placeDetailsService;
    @Inject private PlaceService placeService;
    @Inject private ToastService toast;
    @Inject private ErrorHandler errorHandler;

    private String placeId;

    @FXML
    public void initialize() {
        configureButtonIcon(backButton, "fth-chevron-left", "place-details-back-icon");
        Localization.bindText(backButton.textProperty(), "place.details.back");
        Localization.bindText(descriptionTitleLabel.textProperty(), "place.details.description");
        Localization.bindText(mapTitleLabel.textProperty(), "place.details.map");
        Localization.bindText(associatedPlacesHeader.titleProperty(), "place.details.section.places");
        Localization.bindText(associatedStoriesHeader.titleProperty(), "place.details.section.stories");
        Localization.bindText(associatedRoutesHeader.titleProperty(), "place.details.section.routes");

        installRoundedImageClip(heroImageView, 28);
        installRoundedPaneClip(placeMap, 20);
        placeMap.setSelectionEnabled(false);
        placeMap.setControlsVisible(false);
        Localization.bindText(actionButtonsView.getPrimaryButton().textProperty(), "place.details.action.edit");
        Localization.bindText(actionButtonsView.getSecondaryButton().textProperty(), "place.details.action.delete");
        actionButtonsView.getPrimaryButton().setOnAction(event -> onEditPlace());
        actionButtonsView.getSecondaryButton().setOnAction(event -> onDeletePlace());
        configureButtonIcon(actionButtonsView.getPrimaryButton(), "fth-edit-3", "app-btn-icon");
        configureButtonIcon(actionButtonsView.getSecondaryButton(), "fth-trash-2", "app-btn-icon");
    }

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        placeId = data == null ? null : data.getValue("placeId");
    }

    @Override
    public void onLifecycleShow() {
        loadPlaceDetails();
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    @FXML
    private void onEditPlace() {
        if (placeId == null || placeId.isBlank()) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", 0);
        args.addArgument("tripName", "");
        args.addArgument("placeId", placeId);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onDeletePlace() {
        if (placeId == null || placeId.isBlank()) {
            return;
        }

        var result = placeService.deletePlace(new DeletePlaceRequest(placeId));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            return;
        }

        toast.success(I18n.t("place.details.toast.deleted.title"), I18n.t("place.details.toast.deleted.body"));
        getRouter().popBackStack();
    }

    private void loadPlaceDetails() {
        if (placeId == null || placeId.isBlank()) {
            toast.warning(I18n.t("place.details.toast.notFound"));
            getRouter().popBackStack();
            return;
        }

        var result = placeDetailsService.getPlaceDetails(new GetPlaceDetailsRequest(placeId));
        if (result.isFailure()) {
            errorHandler.handle(result.getError());
            getRouter().popBackStack();
            return;
        }

        bind(result.getValue());
    }

    private void bind(PlaceDetailsResponse details) {
        PlaceResponse place = details.place();

        heroImageView.setImage(loadImage(imagePath(place)));
        placeTitleLabel.setText(safeText(place.title(), I18n.t("trip.add.fallback.place")));
        placeCountryLabel.setText(place.country() == null ? "" : safeText(place.country().name(), ""));
        descriptionValueLabel.setText(safeText(place.description(), I18n.t("place.details.empty.description")));

        placeMap.setMapCenter(place.latitude(), place.longitude());
        placeMap.setPinPosition(place.latitude(), place.longitude());

        renderAssociatedPlaces(details.associatedPlaces());
        renderAssociatedRoutes(details.associatedRoutes());
        renderAssociatedStories(details.associatedStories());
    }

    private void renderAssociatedPlaces(java.util.List<PlaceResponse> places) {
        associatedPlacesFlow.getChildren().clear();
        if (places.isEmpty()) {
            associatedPlacesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.places"), associatedPlacesFlow));
            return;
        }

        for (PlaceResponse place : places) {
            EditorMediaCardView card = new EditorMediaCardView();
            card.setPreviewImage(loadImage(imagePath(place)));
            card.setTitle(safeText(place.title(), I18n.t("trip.add.fallback.place")));
            card.setSubtitle(place.country() == null ? "" : safeText(place.country().name(), ""));
            card.setRemoveVisible(false);
            card.setCursor(Cursor.HAND);
            card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openPlace(place.id()));
            associatedPlacesFlow.getChildren().add(card);
        }
    }

    private void renderAssociatedRoutes(java.util.List<RouteResponse> routes) {
        associatedRoutesFlow.getChildren().clear();
        if (routes.isEmpty()) {
            associatedRoutesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.routes"), associatedRoutesFlow));
            return;
        }

        for (RouteResponse route : routes) {
            EditorMediaCardView card = new EditorMediaCardView();
            card.setPreviewImage(loadImage(route.coverImage() == null || route.coverImage().url() == null
                    ? DEFAULT_IMAGE
                    : route.coverImage().url().toString()));
            card.setTitle(safeText(route.title(), I18n.t("trip.add.fallback.route")));
            card.setSubtitle(String.format(Locale.US, I18n.t("place.details.route.meta"), route.length()));
            card.setRemoveVisible(false);
            associatedRoutesFlow.getChildren().add(card);
        }
    }

    private void renderAssociatedStories(java.util.List<StoryResponse> stories) {
        associatedStoriesFlow.getChildren().clear();
        if (stories.isEmpty()) {
            associatedStoriesFlow.getChildren().add(createEmptyState(I18n.t("place.details.empty.stories"), associatedStoriesFlow));
            return;
        }

        for (StoryResponse story : stories) {
            VBox card = new VBox(6);
            card.getStyleClass().add("place-details-story-card");

            Label title = new Label(safeText(story.title(), I18n.t("place.details.story.fallback")));
            title.getStyleClass().add("place-details-story-title");
            title.setWrapText(true);

            Label description = new Label(safeText(story.description(), I18n.t("place.details.empty.description")));
            description.getStyleClass().add("place-details-story-description");
            description.setWrapText(true);

            card.getChildren().addAll(title, description);
            associatedStoriesFlow.getChildren().add(card);
        }
    }

    private EmptyStateCardView createEmptyState(String text, FlowPane parent) {
        EmptyStateCardView card = new EmptyStateCardView();
        card.setText(text);
        card.prefWidthProperty().bind(parent.widthProperty());
        card.maxWidthProperty().bind(parent.widthProperty());
        return card;
    }

    private void openPlace(String targetPlaceId) {
        if (targetPlaceId == null || targetPlaceId.isBlank() || targetPlaceId.equals(placeId)) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("placeId", targetPlaceId);
        getRouter().moveto(RouteIds.PLACE_DETAILS, args);
    }

    private String imagePath(PlaceResponse place) {
        return place.coverImage() == null || place.coverImage().url() == null
                ? DEFAULT_IMAGE
                : place.coverImage().url().toString();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void configureButtonIcon(Button button, String iconLiteral, String styleClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(15);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
    }

    private Image loadImage(String imagePath) {
        String resolvedPath = imagePath == null || imagePath.isBlank() ? DEFAULT_IMAGE : imagePath;
        if (resolvedPath.startsWith("/")) {
            var resource = getClass().getResource(resolvedPath);
            if (resource != null) {
                return new Image(resource.toExternalForm(), true);
            }
        }

        File file = new File(resolvedPath);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }

        var fallback = getClass().getResource(DEFAULT_IMAGE);
        return new Image(fallback.toExternalForm(), true);
    }

    private void installRoundedImageClip(ImageView target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.fitWidthProperty());
        clip.heightProperty().bind(target.fitHeightProperty());
        target.setClip(clip);
    }

    private void installRoundedPaneClip(StackPane target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        target.setClip(clip);
    }
}
