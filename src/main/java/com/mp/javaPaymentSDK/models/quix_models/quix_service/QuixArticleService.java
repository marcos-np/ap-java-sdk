package com.mp.javaPaymentSDK.models.quix_models.quix_service;

import com.google.gson.annotations.SerializedName;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixArticleService {
    private String name = null;
    private final String type = "service";
    private final String category = "digital";
    private String reference = null;
    @SerializedName("start_date")
    private String startDate = null;
    @SerializedName("end_date")
    private String endDate = null;
    private double unit_price_with_tax = -1;

    public QuixArticleService() {
    }

    public QuixArticleService(String name, String reference, String startDate, String endDate, double unit_price_with_tax) {
        this.name = name;
        this.reference = reference;
        this.startDate = startDate;
        this.endDate = endDate;
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
        this.unit_price_with_tax = Double.parseDouble(Utils.getInstance().roundAmount(unit_price_with_tax));
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Pair<Boolean, String> isMissingField() {
        if (unit_price_with_tax <= 0) {
            return new Pair<>(true, "Missing unit_price_with_tax");
        }
        List<String> mandatoryFields = Arrays.asList(
                "name", "type", "category", "reference",
                "startDate", "endDate"
        );
        return Utils.getInstance().containsNull(
                this.getClass(), this, mandatoryFields
        );
    }
}
