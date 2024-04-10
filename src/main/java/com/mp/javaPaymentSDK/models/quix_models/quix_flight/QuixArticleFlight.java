package com.mp.javaPaymentSDK.models.quix_models.quix_flight;

import com.google.gson.annotations.SerializedName;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class QuixArticleFlight {

    private String name = null;
    private final String type = "flight";
    private final String category = "physical";
    private String reference = null;
    private double unit_price_with_tax = -1;
    @SerializedName("customer_member_since")
    private String customerMemberSince = null;
    @SerializedName("departure_date")
    private String departureDate = null;
    private List<QuixPassengerFlight> passengers = null;
    private List<QuixSegmentFlight> segments = null;

    public QuixArticleFlight() {
    }

    public QuixArticleFlight(String name, String reference, double unit_price_with_tax, String customerMemberSince, String departureDate, List<QuixPassengerFlight> passengers, List<QuixSegmentFlight> segments) {
        this.name = name;
        this.reference = reference;
        this.unit_price_with_tax = unit_price_with_tax;
        this.customerMemberSince = customerMemberSince;
        this.departureDate = departureDate;
        this.passengers = passengers;
        this.segments = segments;
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

    public String getCustomerMemberSince() {
        return customerMemberSince;
    }

    public void setCustomerMemberSince(String customerMemberSince) {
        this.customerMemberSince = customerMemberSince;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public List<QuixPassengerFlight> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<QuixPassengerFlight> passengers) {
        this.passengers = passengers;
    }

    public List<QuixSegmentFlight> getSegments() {
        return segments;
    }

    public void setSegments(List<QuixSegmentFlight> segments) {
        this.segments = segments;
    }

    public Pair<Boolean, String> isMissingFields() {
        if (unit_price_with_tax <= 0) {
            return new Pair<>(true, "Missing unit_price_with_tax");
        }

        List<String> mandatoryFields = Arrays.asList(
                "name", "type", "category",
                "reference", "customerMemberSince", "departureDate",
                "passengers", "segments"
        );

        Pair<Boolean, String> missingField = Utils.getInstance().containsNull(
                this.getClass(), this, mandatoryFields
        );
        if (missingField.getFirst()) {
            return missingField;
        }

        if (passengers.isEmpty()) {
            return new Pair<>(true, "Missing passengers");
        }
        if (segments.isEmpty()) {
            return new Pair<>(true, "Missing passengers");
        }

        for (QuixPassengerFlight item : passengers) {
            missingField = item.isMissingFields();
            if (missingField.getFirst()) {
                return missingField;
            }
        }

        for (QuixSegmentFlight item : segments) {
            missingField = item.isMissingFields();
            if (missingField.getFirst()) {
                return missingField;
            }
        }

        return new Pair<>(false, null);
    }
}
