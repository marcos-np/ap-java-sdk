package com.mp.javaPaymentSDK.models.quix_models;

import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixBilling {

    private String first_name;
    private String last_name;
    private QuixAddress address;

    public QuixBilling() {
    }

    public QuixBilling(String first_name, String last_name, QuixAddress address) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.address = address;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public QuixAddress getAddress() {
        return address;
    }

    public void setAddress(QuixAddress address) {
        this.address = address;
    }

    public Pair<Boolean, String> isMissingField() {
        List<String> mandatoryFields = Arrays.asList(
                "first_name", "last_name", "address"
        );
        Pair<Boolean, String> missingField = Utils.getInstance().containsNull(QuixBilling.class, this, mandatoryFields);
        if (missingField.getFirst()) {
            return missingField;
        }

        return address.isMissingField();
    }
}
