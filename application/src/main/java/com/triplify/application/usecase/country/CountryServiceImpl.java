package com.triplify.application.usecase.country;

import com.google.inject.Inject;
import com.triplify.application.security.Authenticated;
import com.triplify.application.usecase.country.dto.*;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.util.SafeCall;
import com.triplify.domain.error.CountryError;
import com.triplify.domain.model.Country;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.repository.CountryRepository;
import com.triplify.domain.result.Result;
import com.triplify.application.error.ApplicationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        return SafeCall.execute(() -> {
            if (countryRepository.existsByName(request.name(), request.nameSk())) {
                log.warn("Attempted to add country with existing name='{}' or nameSk='{}' by userId='{}'", request.name(), request.nameSk(), user.userId());
                return Result.fail(new CountryError.AlreadyExists(request.name()));
            }

            Country country = new Country(user.userId(), request.name(), request.nameSk(), request.emojiUnicode());
            countryRepository.create(country);
            log.info("Added new country with id='{}', name='{}' by userId='{}'", country.getId(), country.getName(), user.userId());
            return Result.ok(toResponse(country));
        }, "add country", log);
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> updateCountry(UpdateCountryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        return SafeCall.execute(() -> {
            Optional<Country> oldRes = countryRepository.findById(request.id());
            if (oldRes.isEmpty()) {
                log.warn("Attempt to update non-existing country with name='{}' by userId='{}'", request.name(), user.userId());
                return Result.fail(new CountryError.NotFound(request.id()));
            }

            Country old = oldRes.get();
            if (old.getCreatedById() != user.userId()) {
                log.warn("Attempted to update country not created by userId='{}' by userId='{}', countryName='{}'", old.getCreatedById(), user.userId(), old.getName());
                return Result.fail(new CountryError.NotFound(request.id()));
            }

            Country updated = new Country(user.userId(), request.name(), request.nameSk(), request.emojiUnicode());
            countryRepository.update(updated);
            log.info("Updated country with id='{}', name='{}' by userId='{}'", updated.getId(), updated.getName(), user.userId());
            return Result.ok(toResponse(updated));
        }, "update country", log);
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<Void> deleteCountry(DeleteCountryRequest request) {
        SessionUser user = sessionContext.getCurrent().orElseThrow();

        return SafeCall.execute(() -> {
            Optional<Country> oldRes = countryRepository.findById(request.id());
            if (oldRes.isEmpty()) {
                log.warn("Attempt to delete non-existing country with id='{}' by userId='{}'", request.id(), user.userId());
                return Result.fail(new CountryError.NotFound(request.id()));
            }

            Country old = oldRes.get();
            if (old.getCreatedById() != user.userId()) {
                log.warn("Attempted to delete country not created by userId='{}' by userId='{}', countryName='{}'", old.getCreatedById(), user.userId(), old.getName());
                return Result.fail(new CountryError.NotFound(request.id()));
            }

            countryRepository.delete(old);
            log.info("Deleted country with id='{}', name='{}' by userId='{}'", old.getId(), old.getName(), user.userId());
            return Result.ok(null);
        }, "delete country", log);
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> banCountry(BanCountryRequest request) {
        // TODO: implement country ban.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.banCountry"));
    }

    @Override
    @Authenticated(roles = {RoleEnum.CONFIGURATION_MANAGER})
    public Result<CountryResponse> unbanCountry(UnbanCountryRequest request) {
        // TODO: implement country unban.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.unbanCountry"));
    }

    @Override
    public Result<Page<CountryResponse>> getCountries(GetCountriesRequest request) {
        // TODO: implement country search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.getCountries"));
    }

    private CountryResponse toResponse(Country country) {
        return new CountryResponse(
                country.getId().toString(),
                country.getCreatedById().toString(),
                country.getName(),
                country.getNameSk(),
                country.getEmojiUnicode(),
                country.isAvailable()
        );
    }
}
