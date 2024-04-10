package com.mp.javaPaymentSDK.models.requests.js;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.OperationTypes;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import com.mp.javaPaymentSDK.enums.Currency;
import kotlin.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JSPaymentRecurrentInitial {

    private Currency currency = null;
    private String merchantId = null;
    private String merchantKey = null;
    private String productId = null;
    private CountryCode country = null;
    private String customerId = null;
    private String amount = null;
    private String merchantTransactionId = null;
    private String prepayToken = null;
    private String statusURL = null;
    private String successURL = null;
    private String errorURL = null;
    private String cancelURL = null;
    private String awaitingURL = null;
    private PaymentSolutions paymentSolution = PaymentSolutions.creditcards;
    private OperationTypes operationType = OperationTypes.DEBIT;
    private final String paymentRecurringType = "newCof";
    private final String challengeInd = "04";
    private int apiVersion = -1;
    private HashMap<String, String> merchantParams = null;

    public JSPaymentRecurrentInitial() {
        merchantTransactionId = Utils.getInstance().generateRandomNumber();
    }

    public JSPaymentRecurrentInitial(Currency currency, CountryCode country, String customerId, String amount, String merchantTransactionId, String prepayToken, String statusURL, String successURL, String errorURL, String cancelURL, String awaitingURL, PaymentSolutions paymentSolution, OperationTypes operationType, int apiVersion) {
        this.currency = currency;
        this.country = country;
        this.customerId = customerId;
        this.amount = amount;
        this.merchantTransactionId = merchantTransactionId;
        this.prepayToken = prepayToken;
        this.statusURL = statusURL;
        this.successURL = successURL;
        this.errorURL = errorURL;
        this.cancelURL = cancelURL;
        this.awaitingURL = awaitingURL;
        this.paymentSolution = paymentSolution;
        this.operationType = operationType;
        this.apiVersion = apiVersion;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getProductId() {
        return productId;
    }

    public CountryCode getCountry() {
        return country;
    }

    public void setCountry(CountryCode country) {
        this.country = country;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OperationTypes getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypes operationType) {
        this.operationType = operationType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    public String getPrepayToken() {
        return prepayToken;
    }

    public void setPrepayToken(String prepayToken) {
        this.prepayToken = prepayToken;
    }

    public PaymentSolutions getPaymentSolution() {
        return paymentSolution;
    }

    public String getPaymentRecurringType() {
        return paymentRecurringType;
    }

    public String getChallengeInd() {
        return challengeInd;
    }

    public String getMerchantKey() {
        return merchantKey;
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

    public void setPaymentSolution(PaymentSolutions paymentSolution) {
        this.paymentSolution = paymentSolution;
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
        this.merchantKey = credentials.getMerchantKey();
    }

    public Pair<Boolean, String> isMissingField() {
        if (apiVersion < 0) {
            return new Pair<>(true, "Invalid apiVersion");
        }

        List<String> mandatoryFields = Arrays.asList(
                "merchantId", "productId", "merchantKey",
                "merchantTransactionId", "amount", "currency", "country",
                "paymentSolution", "customerId", "operationType", "statusURL",
                "successURL", "errorURL", "cancelURL", "awaitingURL",
                "challengeInd", "paymentRecurringType", "prepayToken"
        );

        return Utils.getInstance().containsNull(
                JSPaymentRecurrentInitial.class, this, mandatoryFields
        );
    }

    public Pair<Boolean, String> checkCredentials(Credentials credentials) {
        List<String> mandatoryFields = Arrays.asList(
                "merchantId", "productId", "merchantKey", "environment"
        );

        return Utils.getInstance().containsNull(
                Credentials.class, credentials, mandatoryFields
        );
    }
}
