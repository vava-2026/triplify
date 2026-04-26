package com.triplify.application.usecase.country;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.country.dto.*;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.CountryError;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.CountryRepository;
import com.triplify.domain.result.Result;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Authenticated
public class CountryServiceImpl implements CountryService {
    private static final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);
    private final CountryRepository countryRepository;
    private final UserSessionContext sessionContext;

    @Inject
    public CountryServiceImpl(CountryRepository countryRepository, UserSessionContext sessionContext) {
        this.countryRepository = countryRepository;
        this.sessionContext = sessionContext;
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> addCountry(AddCountryRequest request) {
        log.info("Adding new country with name='{}'", request.name());
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        if (countryRepository.existsByName(request.name(), request.nameSk())) {
            log.warn("Attempted to add country with existing name='{}' or nameSk='{}' by userId='{}'", request.name(), request.nameSk(), user.userId());
            return Result.fail(new CountryError.AlreadyExists(request.name()));
        }

        Country country = new Country(user.userId(), request.name(), request.nameSk(), request.emojiUnicode());
        countryRepository.create(country);
        log.info("Added new country with id='{}', name='{}' by userId='{}'", country.getId(), country.getName(), user.userId());
        return Result.ok(CountryResponse.from(country));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> updateCountry(UpdateCountryRequest request) {
        log.info("Updating country with id='{}', name='{}'", request.id(), request.name());
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Country> oldRes = countryRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to update non-existing country with name='{}' by userId='{}'", request.name(), user.userId());
            return Result.fail(new CountryError.NotFound(request.id().toString()));
        }

        Country old = oldRes.get();
        if (old.getCreatedById() == null || !old.getCreatedById().equals(user.userId())) {
            log.warn("Attempted to update country not created by userId='{}' by userId='{}', countryName='{}'", old.getCreatedById(), user.userId(), old.getName());
            return Result.fail(new CountryError.NotOwner(request.id().toString()));
        }

        Country updated = new Country(old.getId(), user.userId(), request.name(), request.nameSk(), request.emojiUnicode(), old.isAvailable());
        countryRepository.update(updated);
        log.info("Updated country with id='{}', name='{}' by userId='{}'", updated.getId(), updated.getName(), user.userId());
        return Result.ok(CountryResponse.from(updated));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteCountry(DeleteCountryRequest request) {
        log.info("Deleting country with id='{}'", request.id());
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Country> oldRes = countryRepository.findById(request.id());
        if (oldRes.isEmpty()) {
            log.warn("Attempt to delete non-existing country with id='{}' by userId='{}'", request.id(), user.userId());
            return Result.fail(new CountryError.NotFound(request.id().toString()));
        }

        Country old = oldRes.get();
        if (old.getCreatedById() == null || !old.getCreatedById().equals(user.userId())) {
            log.warn("Attempted to delete country not created by userId='{}' by userId='{}', countryName='{}'", old.getCreatedById(), user.userId(), old.getName());
            return Result.fail(new CountryError.NotOwner(request.id().toString()));
        }

        countryRepository.delete(old);
        log.info("Deleted country with id='{}', name='{}' by userId='{}'", old.getId(), old.getName(), user.userId());
        return Result.ok(null);
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> banCountry(BanCountryRequest request) {
        log.info("Banning country with id='{}'", request.id());
        return changeAvailable(request.id(), false);
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> unbanCountry(UnbanCountryRequest request) {
        log.info("Unbanning country with id='{}'", request.id());
        return changeAvailable(request.id(), true);
    }

    @Override
    public Result<CountryResponse> getCountryById(GetCountryByIdRequest request) {
        Optional<Country> countryRes = countryRepository.findById(request.id());
        if (countryRes.isEmpty()) {
            log.warn("Attempt to get non-existing country with id='{}'", request.id());
            return Result.fail(new CountryError.NotFound(request.id().toString()));
        }

        return Result.ok(CountryResponse.from(countryRes.get()));
    }

    @Override
    public Result<Page<CountryResponse>> getCountries(GetCountriesRequest request) {
        var countryPage = countryRepository.findList(request.pageRequest(), request.filter());
        return Result.ok(countryPage.map(CountryResponse::from));
    }

    private Result<CountryResponse> changeAvailable(@NonNull UUID countryId, boolean available) {
        String logAction = available ? "unban" : "ban";
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        Optional<Country> oldRes = countryRepository.findById(countryId);
        if (oldRes.isEmpty()) {
            log.warn("Attempt to {} non-existing country with id='{}' by userId='{}'", logAction, countryId, user.userId());
            return Result.fail(new CountryError.NotFound(countryId.toString()));
        }

        Country old = oldRes.get();
        old.setAvailable(available);
        countryRepository.update(old);
        log.info("{} country with id='{}', name='{}' by userId='{}'", logAction, old.getId(), old.getName(), user.userId());
        return Result.ok(CountryResponse.from(old));
    }
}
