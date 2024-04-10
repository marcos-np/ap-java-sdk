package com.mp.javaPaymentSDK.models.requests.quix_js;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.models.quix_models.QuixJSRequest;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixAccommodationPaySolExtendedData;
import kotlin.Pair;

public class JSQuixAccommodation extends QuixJSRequest {

    private QuixAccommodationPaySolExtendedData paySolExtendedData;

    public JSQuixAccommodation() {
    }

    public JSQuixAccommodation(double amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, String prepayToken, QuixAccommodationPaySolExtendedData paySolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL, firstName, lastName, customerEmail, dob, prepayToken, apiVersion);
        this.paySolExtendedData = paySolExtendedData;
    }

    public QuixAccommodationPaySolExtendedData getPaySolExtendedData() {
        return paySolExtendedData;
    }

    public void setPaySolExtendedData(QuixAccommodationPaySolExtendedData paySolExtendedData) {
        this.paySolExtendedData = paySolExtendedData;
    }

    public Pair<Boolean, String> isMissingFields() {
        if (paySolExtendedData == null) {
            return new Pair<>(true, "Missing paySolExtendedData");
        }

        Pair<Boolean, String> missingField = paySolExtendedData.isMissingField();
        if (missingField.getFirst()) {
            return missingField;
        }

        return super.isMissingFields();
    }
}
