package com.mp.javaPaymentSDK.models.quix_models.quix_flight;

import com.google.gson.annotations.SerializedName;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixPassengerFlight {

    @SerializedName("first_name")
    private String firstName = null;
    @SerializedName("last_name")
    private String lastName = null;

    public QuixPassengerFlight() {

    }

    public QuixPassengerFlight(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Pair<Boolean, String> isMissingFields() {
        List<String> mandatoryFields = Arrays.asList(
                "firstName", "lastName"
        );
        return Utils.getInstance().containsNull(
                this.getClass(), this, mandatoryFields
        );
    }
}
