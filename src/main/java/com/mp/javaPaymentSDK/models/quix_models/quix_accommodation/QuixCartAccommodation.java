package com.mp.javaPaymentSDK.models.quix_models.quix_accommodation;

import com.mp.javaPaymentSDK.enums.Currency;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;

public class QuixCartAccommodation {

    private Currency currency = null;
    private double total_price_with_tax = 0;
    private List<QuixItemCartItemAccommodation> items = new ArrayList<>();

    public QuixCartAccommodation() {
    }

    public QuixCartAccommodation(Currency currency, double total_price_with_tax, List<QuixItemCartItemAccommodation> items) {
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

    public List<QuixItemCartItemAccommodation> getItems() {
        return items;
    }

    public void setItems(List<QuixItemCartItemAccommodation> items) {
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

        for (QuixItemCartItemAccommodation item : items) {
            Pair<Boolean, String> missingField = item.isMissingFields();
            if (missingField.getFirst()) {
                return missingField;
            }
        }

        return new Pair<>(false, null);
    }
}
