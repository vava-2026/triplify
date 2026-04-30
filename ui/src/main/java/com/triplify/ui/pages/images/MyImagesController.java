package com.triplify.ui.pages.images;

import com.google.inject.Inject;
import com.triplify.application.shared.Pagination;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.GetImagesRequest;
import com.triplify.domain.model.enums.ImageOwnerType;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.domain.pagination.PageRequest;
import com.triplify.ui.error.ErrorHandler;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.pages.images.view.ImageCardView;
import com.triplify.ui.shared.component.card_grid.CardGridPane;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.component.select.view.SelectView;
import com.triplify.ui.shared.model.AppComponentSize;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.List;

public class MyImagesController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(MyImagesController.class);

    @FXML private VBox typeFilterContainer;
    @FXML private CardGridPane<ImageResponse> cardGrid;

    @Inject private ImageService imageService;
    @Inject private ErrorHandler errorHandler;
    @Inject private FxmlLoaderHelper fxmlLoader;

    private ImageOwnerType selectedType = ImageOwnerType.TRIP;
    private ImageViewModalView imageViewModal;

    @FXML
    private void initialize() {
        configureTypeFilter();
        configureGrid();
        cardGrid.refresh();
    }

    private void configureTypeFilter() {
        Select<ImageOwnerType> typeSelect = Select.<ImageOwnerType>builder()
                .placeholder(I18n.t("images.filter.placeholder"))
                .size(AppComponentSize.BIG)
                .items(List.of(
                        Entry.<ImageOwnerType>builder(ImageOwnerType.TRIP, Localization.textBinding("images.filter.trip")).build(),
                        Entry.<ImageOwnerType>builder(ImageOwnerType.TRIP_PLACE, Localization.textBinding("images.filter.place")).build(),
                        Entry.<ImageOwnerType>builder(ImageOwnerType.TRIP_ROUTE, Localization.textBinding("images.filter.route")).build()
                ))
                .onSelect(entry -> {
                    selectedType = entry.getValue();
                    cardGrid.refresh();
                })
                .build();
        typeSelect.setSelectedItem(typeSelect.getItems().getFirst());

        SelectView<ImageOwnerType> selectView = new SelectView<>();
        selectView.update(typeSelect);
        typeFilterContainer.getChildren().setAll(selectView);
    }

    private void configureGrid() {
        log.info("Configuring image grid");
        imageViewModal = new ImageViewModalView(imageService, errorHandler, fxmlLoader);

        cardGrid.setMinCardWidth(220);
        cardGrid.setMaxColumns(5);
        cardGrid.setPageSize(12);
        cardGrid.setEmptyTextKey("images.empty");

        cardGrid.setCardFactory(image -> {
            ImageCardView card = ImageCardView.create(image, () -> openImageViewModal(image));
            return card.getRoot();
        });

        log.info("Configuring page loader for image grid");
        cardGrid.setPageLoader((page, size) -> {
            var result = imageService.getImages(new GetImagesRequest(
                    new PageRequest(page - 1, size),
                    selectedType != null
                            ? new GetImagesRequest.Filter(null, selectedType, null, null)
                            : null,
                    new GetImagesRequest.OrderBy(false)
            ));
            if (result.isFailure()) {
                log.warn("Failed to load images: {}", result.getError().message());
                return new CardGridPane.PageResult<>(List.of(), null);
            }
            var domainPage = result.getValue();
            int totalPages = domainPage.hasNext() ? page + 1 : page;
            Pagination pagination = new Pagination(page, size, null, totalPages);
            return new CardGridPane.PageResult<>(domainPage.items(), pagination);
        });
    }

    private void openImageViewModal(ImageResponse image) {
        imageViewModal.show(
                cardGrid.getScene().getWindow(),
                image,
                deleted -> cardGrid.refresh()
        );
    }
}
