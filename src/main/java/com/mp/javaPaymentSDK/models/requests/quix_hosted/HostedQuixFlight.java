package com.mp.javaPaymentSDK.models.requests.quix_hosted;

import com.mp.javaPaymentSDK.models.quix_models.quix_flight.QuixFlightPaySolExtendedData;
import com.mp.javaPaymentSDK.models.quix_models.QuixHostedRequest;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

public class HostedQuixFlight extends QuixHostedRequest {

    private QuixFlightPaySolExtendedData paySolExtendedData;

    public HostedQuixFlight() {
        this.setMerchantTransactionId(Utils.getInstance().generateRandomNumber());
    }

    public HostedQuixFlight(String amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, QuixFlightPaySolExtendedData paySolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL, firstName, lastName, customerEmail, dob, apiVersion);
        this.paySolExtendedData = paySolExtendedData;
    }

    public QuixFlightPaySolExtendedData getPaysolExtendedData() {
        return paySolExtendedData;
    }

    public void setPaysolExtendedData(QuixFlightPaySolExtendedData paysolExtendedData) {
        this.paySolExtendedData = paysolExtendedData;
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
