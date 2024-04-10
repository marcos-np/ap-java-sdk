package com.mp.javaPaymentSDK.models.quix_models;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixAddress {

    private String street_address;
    private String postal_code;
    private String city;
    private String country;

    public QuixAddress() {
    }

    public QuixAddress(String street_address, String postal_code, String city, String country) {
        this.street_address = street_address;
        this.postal_code = postal_code;
        this.city = city;
        this.country = country;
    }

    public String getStreet_address() {
        return street_address;
    }

    public void setStreet_address(String street_address) {
        this.street_address = street_address;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(CountryCode country) {
        this.country = country.getAlpha3();
    }

    public Pair<Boolean, String> isMissingField() {
        List<String> mandatoryFields = Arrays.asList(
                "street_address", "postal_code", "city", "country"
        );
        return Utils.getInstance().containsNull(QuixAddress.class, this, mandatoryFields);
    }
}
