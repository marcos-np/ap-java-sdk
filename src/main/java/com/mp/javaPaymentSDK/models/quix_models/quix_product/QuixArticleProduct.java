package com.mp.javaPaymentSDK.models.quix_models.quix_product;

import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixArticleProduct {
    private String name = null;
    private final String type = "product";
    private final String category = "physical";
    private String reference = null;
    private double unit_price_with_tax = -1;

    public QuixArticleProduct() {
    }

    public QuixArticleProduct(String name, String reference, double unit_price_with_tax) {
        this.name = name;
        this.reference = reference;
        this.unit_price_with_tax = unit_price_with_tax;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getUnit_price_with_tax() {
        return unit_price_with_tax;
    }

    public void setUnit_price_with_tax(double unit_price_with_tax) {
        this.unit_price_with_tax = unit_price_with_tax;
    }

    public Pair<Boolean, String> isMissingFields() {
        if (unit_price_with_tax <= 0) {
            return new Pair<>(true, "Missing unit_price_with_tax");
        }

        List<String> mandatoryFields = Arrays.asList(
                "name", "type", "category", "reference"
        );

        return Utils.getInstance().containsNull(
                this.getClass(), this, mandatoryFields
        );
    }
}
