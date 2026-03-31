package com.triplify.application.usecase.country;

import com.triplify.application.error.ApplicationError;
import com.triplify.application.usecase.country.dto.*;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public class CountryServiceImpl implements CountryService {

    @Override
    public Result<CountryResponse> addCountry(AddCountryRequest request) {
        // TODO: implement country creation.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.createCountry"));
    }

    @Override
    public Result<CountryResponse> updateCountry(UpdateCountryRequest request) {
        // TODO: implement country update.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.updateCountry"));
    }

    @Override
    public Result<Void> deleteCountry(DeleteCountryRequest request) {
        // TODO: implement country delete.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.deleteCountry"));
    }

    @Override
    public Result<CountryResponse> banCountry(BanCountryRequest request) {
        // TODO: implement country ban.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.banCountry"));
    }

    @Override
    public Result<CountryResponse> unbanCountry(UnbanCountryRequest request) {
        // TODO: implement country unban.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.unbanCountry"));
    }

    @Override
    public Result<Page<CountryResponse>> getCountries(GetCountriesRequest request) {
        // TODO: implement country search with pagination and filters.
        return Result.fail(new ApplicationError.Unexpected("TODO: CountryService.getCountries"));
    }
}
