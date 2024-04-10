package com.mp.javaPaymentSDK.models.requests.quix_hosted;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.models.quix_models.QuixHostedRequest;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData;
import com.mp.javaPaymentSDK.utils.Utils;
import com.mp.javaPaymentSDK.enums.Currency;
import kotlin.Pair;

public class HostedQuixItem extends QuixHostedRequest {

    private QuixItemPaySolExtendedData paySolExtendedData;

    public HostedQuixItem() {
        this.setMerchantTransactionId(Utils.getInstance().generateRandomNumber());
    }

    public HostedQuixItem(double amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL, String awaitingURL, String firstName, String lastName, String customerEmail, String dob, QuixItemPaySolExtendedData paySolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL, firstName, lastName, customerEmail, dob, apiVersion);
        this.paySolExtendedData = paySolExtendedData;
    }

    public QuixItemPaySolExtendedData getPaySolExtendedData() {
        return paySolExtendedData;
    }

    public void setPaySolExtendedData(QuixItemPaySolExtendedData paySolExtendedData) {
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
