package com.mp.javaPaymentSDK.models.requests.h2h;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import com.mp.javaPaymentSDK.enums.Currency;
import kotlin.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class H2HRedirection {

    private String amount = null;
    private CountryCode country = null;
    private Currency currency = null;
    private String customerId = null;
    private String merchantId = null;
    private String merchantTransactionId;
    private PaymentSolutions paymentSolution = null;
    private String chName = null;
    private String cardNumber = null;
    private String expDate = null;
    private String cvnNumber = null;
    private String statusURL = null;
    private String successURL = null;
    private String errorURL = null;
    private String cancelURL = null;
    private String awaitingURL = null;
    private String productId = null;
    private int apiVersion = -1;
    private HashMap<String, String> merchantParams = null;

    public H2HRedirection() {
        merchantTransactionId = Utils.getInstance().generateRandomNumber();
    }

    public H2HRedirection(String amount, CountryCode country, Currency currency, String customerId, String merchantTransactionId, PaymentSolutions paymentSolution, String chName, String cardNumber, String expDate, String cvnNumber, String statusURL, String successURL, String errorURL, String cancelURL, String awaitingURL, int apiVersion) {
        this.amount = amount;
        this.country = country;
        this.currency = currency;
        this.customerId = customerId;
        this.merchantTransactionId = merchantTransactionId;
        this.paymentSolution = paymentSolution;
        this.chName = chName;
        this.cardNumber = cardNumber;
        this.expDate = expDate;
        this.cvnNumber = cvnNumber;
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    public String getChName() {
        return chName;
    }

    public void setChName(String chName) {
        this.chName = chName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCvnNumber() {
        return cvnNumber;
    }

    public void setCvnNumber(String cvnNumber) {
        this.cvnNumber = cvnNumber;
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
                "merchantId", "merchantTransactionId", "paymentSolution",
                "chName", "cardNumber", "expDate",
                "cvnNumber", "statusURL", "successURL", "errorURL",
                "cancelURL", "awaitingURL", "productId"
        );

        return Utils.getInstance().containsNull(
                H2HRedirection.class, this, mandatoryFields
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
