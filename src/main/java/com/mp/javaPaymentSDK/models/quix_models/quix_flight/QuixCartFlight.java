package com.mp.javaPaymentSDK.models.quix_models.quix_flight;

import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class QuixCartFlight {

    private Currency currency = null;
    private double total_price_with_tax = 0;
    private List<QuixItemCartItemFlight> items = new ArrayList<>();

    public QuixCartFlight() {
    }

    public QuixCartFlight(Currency currency, double total_price_with_tax, List<QuixItemCartItemFlight> items) {
        this.currency = currency;
        this.total_price_with_tax = total_price_with_tax;
        this.items = items;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public double getTotal_price_with_tax() {
        return total_price_with_tax;
    }

    public void setTotal_price_with_tax(double total_price_with_tax) {
        this.total_price_with_tax = total_price_with_tax;
    }

    public List<QuixItemCartItemFlight> getItems() {
        return items;
    }

    public void setItems(List<QuixItemCartItemFlight> items) {
        this.items = items;
    }

    public Pair<Boolean, String> isMissingField() {
        if (total_price_with_tax <= 0) {
            return new Pair<>(true, "Missing total_price_with_tax");
        }
        if (currency == null) {
            return new Pair<>(true, "Missing currency");
        }
        if (items == null || items.isEmpty()) {
            return new Pair<>(true, "Missing items");
        }

        for (QuixItemCartItemFlight item : items) {
            Pair<Boolean, String> missingField = item.isMissingFields();
            if (missingField.getFirst()) {
                return missingField;
            }
        }

        return new Pair<>(false, null);
    }
}
