package com.mp.javaPaymentSDK.models.quix_models.quix_accommodation;

import com.google.gson.annotations.SerializedName;
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixArticleAccommodation {
    private String name = null;
    private final String type = "accommodation";
    private final String category = "physical";
    private String reference = null;
    private double unit_price_with_tax = -1;
    @SerializedName("checkin_date")
    private String checkinDate = null;
    @SerializedName("checkout_date")
    private String checkoutDate = null;
    @SerializedName("establishment_name")
    private String establishmentName = null;
    private QuixAddress address = null;
    private int guests = -1;

    public QuixArticleAccommodation() {
    }

    public QuixArticleAccommodation(String name, String reference, double unit_price_with_tax, String checkinDate, String checkoutDate, String establishmentName, QuixAddress address, int guests) {
        this.name = name;
        this.reference = reference;
        this.unit_price_with_tax = unit_price_with_tax;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.establishmentName = establishmentName;
        this.address = address;
        this.guests = guests;
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

    public String getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(String checkinDate) {
        this.checkinDate = checkinDate;
    }

    public String getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(String checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public String getEstablishmentName() {
        return establishmentName;
    }

    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }

    public QuixAddress getAddress() {
        return address;
    }

    public void setAddress(QuixAddress address) {
        this.address = address;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public Pair<Boolean, String> isMissingField() {
        if (unit_price_with_tax <= 0) {
            return new Pair<>(true, "Missing unit_price_with_tax");
        }
        if (guests <= 0) {
            return new Pair<>(true, "Missing guests");
        }

        List<String> mandatoryFields = Arrays.asList(
                "name", "type", "category", "reference", "unit_price_with_tax",
                "checkinDate", "checkoutDate", "establishmentName", "address", "guests"
        );
        Pair<Boolean, String> missingField = Utils.getInstance().containsNull(QuixArticleAccommodation.class, this, mandatoryFields);
        if (missingField.getFirst()) {
            return missingField;
        }

        return address.isMissingField();
    }
}
