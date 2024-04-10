package com.mp.javaPaymentSDK.models.requests.quix_hosted;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.QuixHostedRequest;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData;
import com.mp.javaPaymentSDK.utils.Utils;
import com.mp.javaPaymentSDK.enums.Currency;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class HostedQuixService extends QuixHostedRequest {

    private QuixServicePaySolExtendedData paySolExtendedData;

    public HostedQuixService() {
        this.setMerchantTransactionId(Utils.getInstance().generateRandomNumber());
    }

    public HostedQuixService(double amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, QuixServicePaySolExtendedData paySolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL,  firstName, lastName, customerEmail, dob, apiVersion);
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
