package com.mp.javaPaymentSDK.models.quix_models;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.models.Credentials;

public class QuixHostedQuery extends QuixHostedRequest {

    private String paysolExtendedData;

    public QuixHostedQuery() {
    }

    public QuixHostedQuery(QuixHostedRequest quixHostedRequest, Credentials credentials) {
        super(quixHostedRequest.getAmount(), quixHostedRequest.getCustomerId(), quixHostedRequest.getMerchantTransactionId(), quixHostedRequest.getStatusURL(), quixHostedRequest.getSuccessURL(), quixHostedRequest.getErrorURL(), quixHostedRequest.getCancelURL(), quixHostedRequest.getAwaitingURL(), quixHostedRequest.getFirstName(), quixHostedRequest.getLastName(), quixHostedRequest.getCustomerEmail(), quixHostedRequest.getDob(), quixHostedRequest.getApiVersion());
        setCredentials(credentials);
    }

    public QuixHostedQuery(QuixHostedRequest quixHostedRequest, String paysolExtendedData, Credentials credentials) {
        this(quixHostedRequest, credentials);
        this.paysolExtendedData = paysolExtendedData;
    }

    public QuixHostedQuery(String amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, String paysolExtendedData, int apiVersion) {
        super(amount, customerId, merchantTransactionId, statusURL, successURL, errorURL, cancelURL,awaitingURL, firstName, lastName, customerEmail, dob, apiVersion);
        this.paysolExtendedData = paysolExtendedData;
    }

    public String getPaysolExtendedData() {
        return paysolExtendedData;
    }

    public void setPaysolExtendedData(String paysolExtendedData) {
        this.paysolExtendedData = paysolExtendedData;
    }
}
