package com.mp.javaPaymentSDK.models.requests.hosted;

import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HostedPaymentRecurrentInitial {

    private Currency currency = null;
    private String amount = null;
    private CountryCode country = null;
    private String customerId = null;
    private String merchantId = null;
    private String merchantTransactionId = null;
    private PaymentSolutions paymentSolution = null;
    private String statusURL = null;
    private String errorURL = null;
    private String successURL = null;
    private String cancelURL = null;
    private String awaitingURL = null;
    private String productId = null;
    private OperationTypes operationType = OperationTypes.DEBIT;
    private PaymentRecurringType paymentRecurringType = PaymentRecurringType.newCof;
    private int apiVersion = -1;
    private boolean forceTokenRequest = false;
    private boolean showRememberMe = false;
    private List<Pair<String, String>> merchantParams = null;

    public HostedPaymentRecurrentInitial() {
        merchantTransactionId = Utils.getInstance().generateRandomNumber();
    }

    public HostedPaymentRecurrentInitial(Currency currency, String amount, CountryCode country, String customerId, String merchantTransactionId, PaymentSolutions paymentSolution, String statusURL, String errorURL, String successURL, String cancelURL, String awaitingURL, OperationTypes operationType, int apiVersion) {
        this.currency = currency;
        this.amount = amount;
        this.country = country;
        this.customerId = customerId;
        this.merchantTransactionId = merchantTransactionId;
        this.paymentSolution = paymentSolution;
        this.statusURL = statusURL;
        this.errorURL = errorURL;
        this.successURL = successURL;
        this.cancelURL = cancelURL;
        this.awaitingURL = awaitingURL;
        this.operationType = operationType;
        this.apiVersion = apiVersion;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    public String getStatusURL() {
        return statusURL;
    }

    public void setStatusURL(String statusURL) {
        this.statusURL = statusURL;
    }

    public String getErrorURL() {
        return errorURL;
    }

    public void setErrorURL(String errorURL) {
        this.errorURL = errorURL;
    }

    public String getSuccessURL() {
        return successURL;
    }

    public void setSuccessURL(String successURL) {
        this.successURL = successURL;
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

    public OperationTypes getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypes operationType) {
        this.operationType = operationType;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public PaymentRecurringType getPaymentRecurringType() {
        return paymentRecurringType;
    }

    public void setPaymentRecurringType(PaymentRecurringType paymentRecurringType) {
        this.paymentRecurringType = paymentRecurringType;
    }

    public boolean isForceTokenRequest() {
        return forceTokenRequest;
    }

    public void setForceTokenRequest(boolean forceTokenRequest) {
        this.forceTokenRequest = forceTokenRequest;
    }

    public boolean isShowRememberMe() {
        return showRememberMe;
    }

    public void setShowRememberMe(boolean showRememberMe) {
        this.showRememberMe = showRememberMe;
    }

    public void setMerchantParameters(List<Pair<String, String>> merchantParams) {
        if (this.merchantParams == null) {
            this.merchantParams = merchantParams;
        }
        else {
            this.merchantParams.addAll(merchantParams);
        }
    }

    public List<Pair<String, String>> getMerchantParameters() {
        return merchantParams;
    }

    public void setMerchantParameter(String key, String value) {
        if (merchantParams == null) {
            this.merchantParams = new ArrayList<>();
        }
        this.merchantParams.add(new Pair<>(key, value));
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
                "currency", "merchantId", "productId", "paymentSolution",
                "operationType", "merchantTransactionId", "amount",
                "country", "customerId", "statusURL", "successURL", "errorURL",
                "cancelURL", "awaitingURL", "paymentRecurringType"
        );

        return Utils.getInstance().containsNull(
                HostedPaymentRecurrentInitial.class, this, mandatoryFields
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
