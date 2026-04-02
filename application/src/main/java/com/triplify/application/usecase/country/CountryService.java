package com.triplify.application.usecase.country;

import com.triplify.application.usecase.country.dto.*;
import com.triplify.domain.pagination.Page;
import com.triplify.domain.result.Result;

public interface CountryService {

    Result<CountryResponse> addCountry(AddCountryRequest request);

    Result<CountryResponse> updateCountry(UpdateCountryRequest request);

    Result<Void> deleteCountry(DeleteCountryRequest request);

    Result<CountryResponse> banCountry(BanCountryRequest request);

    Result<CountryResponse> unbanCountry(UnbanCountryRequest request);

    Result<Page<CountryResponse>> getCountries(GetCountriesRequest request);
}
