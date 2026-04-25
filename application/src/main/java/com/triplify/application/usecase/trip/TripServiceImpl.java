package com.triplify.application.usecase.trip;

import com.google.inject.Inject;
import com.triplify.application.shared.ColorTheme;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.category.dto.CategoryResponse;
import com.triplify.application.usecase.country.dto.CountryResponse;
import com.triplify.application.usecase.image.ImageService;
import com.triplify.application.usecase.image.dto.AddImageRequest;
import com.triplify.application.usecase.image.dto.DeleteImageRequest;
import com.triplify.application.usecase.image.dto.GetImageByIdRequest;
import com.triplify.application.usecase.image.dto.ImageResponse;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.tag.dto.TagResponse;
import com.triplify.application.usecase.trip.dto.AddTripRequest;
import com.triplify.application.usecase.trip.dto.DeleteTripRequest;
import com.triplify.application.usecase.trip.dto.GetTripByIdRequest;
import com.triplify.application.usecase.trip.dto.GetTripsRequest;
import com.triplify.application.usecase.trip.dto.TripResponse;
import com.triplify.application.usecase.trip.dto.UpdateTripRequest;
import com.triplify.application.usecase.trip.dto.UpdateTripStatusRequest;
import com.triplify.domain.error.CategoryError;
import com.triplify.domain.error.CountryError;
import com.triplify.domain.error.TagError;
import com.triplify.domain.error.TripError;
import com.triplify.domain.filter.TripFilter;
import com.triplify.domain.model.Category;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.Tag;
import com.triplify.domain.model.Trip;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.domain.repository.CountryRepository;
import com.triplify.domain.repository.TagRepository;
import com.triplify.domain.repository.TripRepository;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Authenticated
public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);
    private static final String DEFAULT_IMAGE_DESCRIPTION = "Trip image ";

    private final TripRepository tripRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final TagRepository tagRepository;
    private final UserSessionContext userSessionContext;
    private final ImageService imageService;

    @Inject
    TripServiceImpl(
            TripRepository tripRepository,
            CategoryRepository categoryRepository,
            CountryRepository countryRepository,
            TagRepository tagRepository,
            UserSessionContext userSessionContext,
            ImageService imageService
    ) {
        this.tripRepository = tripRepository;
        this.categoryRepository = categoryRepository;
        this.countryRepository = countryRepository;
        this.tagRepository = tagRepository;
        this.userSessionContext = userSessionContext;
        this.imageService = imageService;
    }

    @Override
    public Result<TripResponse> addTrip(AddTripRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();

        ResolvedTripRelations relations = resolveRelations(
                request.categoryId(),
                request.countryIds() == null ? Set.of() : request.countryIds(),
                request.tagIds() == null ? Set.of() : request.tagIds()
        ).orThrow();

        validateDates(request.startedAt(), request.endedAt()).orThrow();

        Instant now = Instant.now();
        Trip trip = new Trip(
                UUID.randomUUID(),
                user.userId(),
                relations.category().getId(),
                relations.category(),
                null,
                null,
                request.title(),
                normalizeDescription(request.description()),
                request.status(),
                request.startedAt(),
                request.endedAt(),
                now,
                now,
                toIdSet(relations.tags()),
                toIdSet(relations.countries()),
                new LinkedHashSet<>(relations.tags()),
                new LinkedHashSet<>(relations.countries())
        );

        tripRepository.create(trip);
        tripRepository.replaceTagIds(trip.getId(), toIdSet(relations.tags()));
        tripRepository.replaceCountryIds(trip.getId(), toIdSet(relations.countries()));

        updateTripCoverImage(trip.getId(), request.images(), null).orThrow();

        log.info("Added trip id='{}', title='{}' by userId='{}'", trip.getId(), trip.getTitle(), user.userId());
        return getTripById(new GetTripByIdRequest(trip.getId()));
    }

    @Override
    public Result<TripResponse> updateTrip(UpdateTripRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Trip existing = requireOwnedTrip(request.id(), user.userId()).orThrow();

        validateStatusTransition(existing.getStatus(), request.status()).orThrow();

        validateDates(request.startedAt(), request.endedAt()).orThrow();

        ResolvedTripRelations relations = resolveRelations(
                request.categoryId(),
                request.countryIds() == null ? Set.of() : request.countryIds(),
                request.tagIds() == null ? Set.of() : request.tagIds()
        ).orThrow();
        Trip updatedTrip = new Trip(
                existing.getId(),
                existing.getUserId(),
                relations.category().getId(),
                relations.category(),
                existing.getCoverImageId(),
                existing.getCoverImage(),
                request.title(),
                normalizeDescription(request.description()),
                request.status(),
                request.startedAt(),
                request.endedAt(),
                existing.getCreatedAt(),
                Instant.now(),
                toIdSet(relations.tags()),
                toIdSet(relations.countries()),
                new LinkedHashSet<>(relations.tags()),
                new LinkedHashSet<>(relations.countries())
        );

        tripRepository.update(updatedTrip);
        tripRepository.replaceTagIds(updatedTrip.getId(), toIdSet(relations.tags()));
        tripRepository.replaceCountryIds(updatedTrip.getId(), toIdSet(relations.countries()));

        updateTripCoverImage(
                updatedTrip.getId(),
                request.images(),
                existing.getCoverImageId()
        ).orThrow();

        log.info("Updated trip id='{}' by userId='{}'", updatedTrip.getId(), user.userId());
        return getTripById(new GetTripByIdRequest(updatedTrip.getId()));
    }

    @Override
    public Result<Void> deleteTrip(DeleteTripRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Trip existing = requireOwnedTrip(request.id(), user.userId()).orThrow();

        if (existing.getCoverImageId() != null) {
            var coverResult = imageService.getImageById(new GetImageByIdRequest(existing.getCoverImageId()));
            if (coverResult.isSuccess()) {
                imageService.deleteImage(new DeleteImageRequest(coverResult.getValue().id())).orThrow();
            }
        }

        tripRepository.delete(existing);
        log.info("Deleted trip id='{}' by userId='{}'", request.id(), user.userId());
        return Result.ok();
    }

    @Override
    public Result<TripResponse> updateStatus(UpdateTripStatusRequest request) {
        SessionUser user = userSessionContext.getCurrent().orElseThrow();
        Trip existing = requireOwnedTrip(request.id(), user.userId()).orThrow();
        validateStatusTransition(existing.getStatus(), request.status()).orThrow();

        Instant startedAt = request.startedAt() != null ? request.startedAt() : existing.getStartedAt();
        Instant endedAt = request.endedAt() != null ? request.endedAt() : existing.getEndedAt();
        validateDates(startedAt, endedAt).orThrow();

        Trip updatedTrip = new Trip(
                existing.getId(),
                existing.getUserId(),
                existing.getCategoryId(),
                existing.getCategory(),
                existing.getCoverImageId(),
                existing.getCoverImage(),
                existing.getTitle(),
                existing.getDescription(),
                request.status(),
                startedAt,
                endedAt,
                existing.getCreatedAt(),
                Instant.now(),
                new LinkedHashSet<>(existing.getTagIds()),
                new LinkedHashSet<>(existing.getCountryIds()),
                new LinkedHashSet<>(existing.getTags()),
                new LinkedHashSet<>(existing.getCountries())
        );

        tripRepository.update(updatedTrip);
        log.info("Updated trip status id='{}' to status='{}' by userId='{}'", request.id(), request.status(), user.userId());
        return getTripById(new GetTripByIdRequest(request.id()));
    }

    @Override
    public Result<TripResponse> getTripById(GetTripByIdRequest request) {
        return tripRepository.findById(request.id())
                .<Result<TripResponse>>map(this::toResponse)
                .orElseGet(() -> Result.fail(new TripError.NotFound(request.id().toString())));
    }

    @Override
    public Result<Page<TripResponse>> getTrips(GetTripsRequest request) {
        GetTripsRequest.Filter filter = request.filter();
        TripFilter tripFilter = new TripFilter(
                filter != null ? filter.name() : null,
                filter != null ? filter.countryId() : null,
                filter != null ? filter.status() : null,
                filter != null ? filter.categoryId() : null,
                filter != null ? filter.tagIds() : null,
                filter != null ? filter.startedFrom() : null,
                filter != null ? filter.startedTo() : null
        );

        boolean startTimeAsc = request.orderBy() != null && request.orderBy().startTimeDirectionAsc();
        Page<Trip> tripsPage = tripRepository.findList(request.pageRequest(), tripFilter, startTimeAsc);

        List<TripResponse> responses = new ArrayList<>(tripsPage.items().size());
        for (Trip trip : tripsPage.items()) {
            responses.add(toResponse(trip).orThrow());
        }

        return Result.ok(new Page<>(responses, tripsPage.page(), tripsPage.size(), tripsPage.hasNext()));
    }

    private Result<Trip> requireOwnedTrip(UUID tripId, UUID userId) {
        var tripRes = tripRepository.findById(tripId);
        if (tripRes.isEmpty()) {
            return Result.fail(new TripError.NotFound(tripId.toString()));
        }

        Trip trip = tripRes.get();
        if (!Objects.equals(trip.getUserId(), userId)) {
            return Result.fail(new TripError.NotOwner(tripId.toString()));
        }

        return Result.ok(trip);
    }

    private Result<ResolvedTripRelations> resolveRelations(UUID categoryId, Set<UUID> countryIds, Set<UUID> tagIds) {
        var category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            return Result.fail(new CategoryError.NotFound(categoryId.toString()));
        }

        LinkedHashSet<Country> countries = new LinkedHashSet<>();
        for (UUID countryId : countryIds) {
            var country = countryRepository.findById(countryId);
            if (country.isEmpty()) {
                return Result.fail(new CountryError.NotFound(countryId.toString()));
            }
            countries.add(country.get());
        }

        List<Tag> foundTags = tagRepository.findByIds(tagIds);
        if (foundTags.size() != tagIds.size()) {
            Set<UUID> foundTagIds = foundTags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            for (UUID tagId : tagIds) {
                if (!foundTagIds.contains(tagId)) {
                    return Result.fail(new TagError.NotFound(tagId.toString()));
                }
            }
        }

        return Result.ok(new ResolvedTripRelations(
                category.get(),
                countries,
                new LinkedHashSet<>(foundTags)
        ));
    }

    private Result<Void> validateDates(Instant startedAt, Instant endedAt) {
        if (startedAt != null && endedAt != null && endedAt.isBefore(startedAt)) {
            return Result.fail(new TripError.InvalidDates(
                    startedAt.atZone(ZoneOffset.UTC).toLocalDate(),
                    endedAt.atZone(ZoneOffset.UTC).toLocalDate()
            ));
        }
        return Result.ok();
    }

    private Result<Void> validateStatusTransition(StatusEnum from, StatusEnum to) {
        if (from == to) {
            return Result.ok();
        }

        boolean valid = switch (from) {
            case PLANNED -> to == StatusEnum.ONGOING || to == StatusEnum.VISITED || to == StatusEnum.CANCELED;
            case ONGOING -> to == StatusEnum.VISITED || to == StatusEnum.CANCELED;
            case VISITED, CANCELED -> false;
        };

        if (!valid) {
            return Result.fail(new TripError.InvalidStatusTransition(from.getValue(), to.getValue()));
        }
        return Result.ok();
    }

    private Result<Void> updateTripCoverImage(UUID tripId, Set<Path> requestedImages, UUID existingCoverImageId) {
        if (requestedImages == null) {
            return Result.ok();
        }

        Path newCoverPath = requestedImages.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (newCoverPath == null) {
            tripRepository.updateCoverImageId(tripId, null);
            if (existingCoverImageId != null) {
                imageService.deleteImage(new DeleteImageRequest(existingCoverImageId)).orThrow();
            }
            return Result.ok();
        }

        Result<ImageResponse> imageResult = imageService.addImage(new AddImageRequest(newCoverPath, DEFAULT_IMAGE_DESCRIPTION + tripId));
        if (imageResult.isFailure()) {
            return Result.fail(imageResult.getError());
        }

        UUID newCoverImageId = imageResult.getValue().id();
        tripRepository.updateCoverImageId(tripId, newCoverImageId);

        if (existingCoverImageId != null
                && !existingCoverImageId.equals(newCoverImageId)) {
            var deleteResult = imageService.deleteImage(new DeleteImageRequest(existingCoverImageId));
            if (deleteResult.isFailure()) {
                return Result.fail(deleteResult.getError());
            }
        }
        return Result.ok();
    }

    private Result<TripResponse> toResponse(Trip trip) {
        ImageResponse coverImage = trip.getCoverImage() == null ? null : ImageResponse.from(trip.getCoverImage());

        Category category = trip.getCategory();
        CategoryResponse categoryResponse = category == null ? null : new CategoryResponse(
                category.getId(),
                category.getCreatedById(),
                category.getName(),
                category.getNameSk(),
                category.getDescription(),
                category.getDescriptionSk(),
                category.getEmojiUnicode(),
                category.getColor() == null ? null : ColorTheme.from(category.getColor())
        );

        Set<TagResponse> tagResponses = trip.getTags().stream()
                .map(tag -> new TagResponse(
                        tag.getId(),
                        tag.getUserId(),
                        tag.getName(),
                        tag.getColor() == null ? null : ColorTheme.from(tag.getColor())
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<CountryResponse> countryResponses = trip.getCountries().stream()
                .map(CountryResponse::from)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Result.ok(new TripResponse(
                trip.getId(),
                trip.getUserId(),
                categoryResponse,
                trip.getTitle(),
                trip.getDescription(),
                trip.getStatus(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                tagResponses,
                coverImage,
                countryResponses
        ));
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description;
    }

    private <T> LinkedHashSet<UUID> toIdSet(Set<T> items) {
        return items.stream()
                .map(item -> switch (item) {
                    case Tag tag -> tag.getId();
                    case Country country -> country.getId();
                    default -> throw new IllegalArgumentException("Unsupported relation type: " + item.getClass().getName());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private record ResolvedTripRelations(
            Category category,
            LinkedHashSet<Country> countries,
            LinkedHashSet<Tag> tags
    ) {
    }
}
