package com.mp.javaPaymentSDK.models.quix_models;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class QuixJSRequest {

    private final Currency currency = Currency.EUR;
    private double amount = -1;
    private final CountryCode country = CountryCode.ES;
    private String customerId = null;
    private String merchantTransactionId = null;
    private String merchantId = null;
    private final PaymentSolutions paymentSolution = PaymentSolutions.quix;
    private String statusURL = null;
    private String successURL = null;
    private String errorURL = null;
    private String cancelURL = null;

    private String awaitingURL = null;
    private String firstName = null;
    private String lastName = null;
    private String productId = null;
    private String customerEmail = null;
    private final CountryCode customerCountry = CountryCode.ES;
    private String dob = null;
    private String prepayToken = null;
    private int apiVersion = -1;
    private HashMap<String, String> merchantParams = null;

    public QuixJSRequest() {
        merchantTransactionId = Utils.getInstance().generateRandomNumber();
    }

    public QuixJSRequest(double amount, String customerId, String merchantTransactionId, String statusURL, String successURL, String errorURL, String cancelURL,String awaitingURL, String firstName, String lastName, String customerEmail, String dob, String prepayToken, int apiVersion) {
        this.amount = amount;
        this.customerId = customerId;
        this.merchantTransactionId = merchantTransactionId;
        this.statusURL = statusURL;
        this.successURL = successURL;
        this.errorURL = errorURL;
        this.cancelURL = cancelURL;
        this.awaitingURL = awaitingURL;
        this.firstName = firstName;
        this.lastName = lastName;
        this.customerEmail = customerEmail;
        this.dob = dob;
        this.prepayToken = prepayToken;
        this.apiVersion = apiVersion;
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public CountryCode getCountry() {
        return country;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    public PaymentSolutions getPaymentSolution() {
        return paymentSolution;
    }

    public String getStatusURL() {
        return statusURL;
    }

    public void setStatusURL(String statusURL) {
        this.statusURL = statusURL;
    }

    public String getSuccessURL() {
        return successURL;
    }

    public void setSuccessURL(String successURL) {
        this.successURL = successURL;
    }

    public String getErrorURL() {
        return errorURL;
    }

    public void setErrorURL(String errorURL) {
        this.errorURL = errorURL;
    }

    public String getCancelURL() {
        return cancelURL;
    }

    public void setCancelURL(String cancelURL) {
        this.cancelURL = cancelURL;
    }

    public String getAwaitingURL() {
        return awaitingURL;
    }

    public void setAwaitingURL(String awaitingURL) {
        this.awaitingURL = awaitingURL;
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

    public String getProductId() {
        return productId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public CountryCode getCustomerCountry() {
        return customerCountry;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPrepayToken() {
        return prepayToken;
    }

    public void setPrepayToken(String prepayToken) {
        this.prepayToken = prepayToken;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public HashMap<String, String> getMerchantParams() {
        return merchantParams;
    }

    public void setMerchantParams(HashMap<String, String> merchantParams) {
        this.merchantParams = merchantParams;
    }

    public void setMerchantParameter(String key, String value) {
        if (merchantParams == null) {
            this.merchantParams = new HashMap<>();
        }
        this.merchantParams.put(key, value);
    }

    public void setCredentials(Credentials credentials) {
        this.merchantId = credentials.getMerchantId();
        this.productId = credentials.getProductId();
    }

    public Pair<Boolean, String> isMissingFields() {
        if (apiVersion < 0) {
            return new Pair<>(true, "Invalid apiVersion");
        }

        if (amount <= 0) {
            return new Pair<>(true, "Missing amount");
        }

        List<String> mandatoryFields = Arrays.asList(
                "currency", "country", "customerId", "merchantId",
                "merchantTransactionId", "paymentSolution", "statusURL", "successURL",
                "errorURL", "cancelURL","awaitingURL", "firstName", "lastName", "productId", "customerEmail",
                "customerCountry", "dob", "prepayToken"
        );

        return Utils.getInstance().containsNull(
                QuixJSRequest.class, this, mandatoryFields
        );
    }

    public Pair<Boolean, String> checkCredentials(Credentials credentials) {
        List<String> mandatoryFields = Arrays.asList(
                "merchantId", "productId", "environment"
        );

        return Utils.getInstance().containsNull(
                Credentials.class, credentials, mandatoryFields
        );
    }
}
