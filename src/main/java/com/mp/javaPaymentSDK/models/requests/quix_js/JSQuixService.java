package com.mp.javaPaymentSDK.models.requests.quix_js;

import com.mp.javaPaymentSDK.models.quix_models.QuixJSRequest;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData;
import kotlin.Pair;

public class JSQuixService extends QuixJSRequest {

    private QuixServicePaySolExtendedData paySolExtendedData;

    public JSQuixService() {
    }

    public JSQuixService(String amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, String prepayToken, QuixServicePaySolExtendedData paySolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL, firstName, lastName, customerEmail, dob, prepayToken, apiVersion);
        this.paySolExtendedData = paySolExtendedData;
    }

    public QuixServicePaySolExtendedData getPaySolExtendedData() {
        return paySolExtendedData;
    }

    public void setPaySolExtendedData(QuixServicePaySolExtendedData paySolExtendedData) {
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
