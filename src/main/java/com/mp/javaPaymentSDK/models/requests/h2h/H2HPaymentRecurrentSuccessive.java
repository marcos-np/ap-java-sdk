package com.mp.javaPaymentSDK.models.requests.h2h;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class H2HPaymentRecurrentSuccessive {
    private String amount = null;
    private CountryCode country = null;
    private Currency currency = null;
    private String customerId = null;
    private String merchantId = null;
    private String merchantTransactionId = null;
    private PaymentSolutions paymentSolution = null;
    private String chName = null;
    private String cardNumberToken = null;
    private String subscriptionPlan = null;
    private String statusURL = null;
    private String successURL = null;
    private String errorURL = null;
    private String cancelURL = null;
    private String awaitingURL = null;
    private String productId = null;
    private final String paymentRecurringType = "cof";
    private int apiVersion = -1;
    private HashMap<String, String> merchantParams = null;

    public H2HPaymentRecurrentSuccessive() {
    }

    public H2HPaymentRecurrentSuccessive(String amount, CountryCode country, Currency currency, String customerId, String merchantTransactionId, PaymentSolutions paymentSolution, String chName, String cardNumberToken, String subscriptionPlan, String statusURL, String successURL, String errorURL, String cancelURL, String awaitingURL, int apiVersion) {
        this.amount = amount;
        this.country = country;
        this.currency = currency;
        this.customerId = customerId;
        this.merchantTransactionId = merchantTransactionId;
        this.paymentSolution = paymentSolution;
        this.chName = chName;
        this.cardNumberToken = cardNumberToken;
        this.subscriptionPlan = subscriptionPlan;
        this.statusURL = statusURL;
        this.successURL = successURL;
        this.errorURL = errorURL;
        this.cancelURL = cancelURL;
        this.awaitingURL = awaitingURL;
        this.apiVersion = apiVersion;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public CountryCode getCountry() {
        return country;
    }

    public void setCountry(CountryCode country) {
        this.country = country;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantId() {
        return merchantId;
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

    public void setPaymentSolution(PaymentSolutions paymentSolution) {
        this.paymentSolution = paymentSolution;
    }

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }

    public String getCardNumberToken() {
        return cardNumberToken;
    }

    public void setCardNumberToken(String cardNumberToken) {
        this.cardNumberToken = cardNumberToken;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public String getPaymentRecurringType() {
        return paymentRecurringType;
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

    public String getProductId() {
        return productId;
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

    public Pair<Boolean, String> isMissingField() {
        if (apiVersion < 0) {
            return new Pair<>(true, "Invalid apiVersion");
        }

        List<String> mandatoryFields = Arrays.asList(
                "amount", "country", "currency", "customerId",
                "merchantId","productId", "merchantTransactionId", "paymentSolution",
                "chName", "cardNumberToken", "subscriptionPlan", "statusURL",
                "successURL", "errorURL", "cancelURL", "awaitingURL", "paymentRecurringType"
        );

        return Utils.getInstance().containsNull(
                H2HPaymentRecurrentSuccessive.class, this, mandatoryFields
        );
    }

    public Pair<Boolean, String> checkCredentials(Credentials credentials) {
        List<String> mandatoryFields = Arrays.asList(
                "merchantId", "productId", "merchantPass", "environment"
        );

        return Utils.getInstance().containsNull(
                Credentials.class, credentials, mandatoryFields
        );
    }
}
