package com.mp.javaPaymentSDK.models.requests.h2h;

import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class H2HPaymentRecurrentInitial {
    private String amount = null;
    private Currency currency = null;
    private CountryCode country = null;
    private String customerId = null;
    private String merchantId = null;
    private String merchantTransactionId;
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
    private OperationTypes operationType = OperationTypes.DEBIT;
    private PaymentSolutions paymentSolution = PaymentSolutions.creditcards;
    private PaymentRecurringType paymentRecurringType = PaymentRecurringType.newCof;
    private String challengeInd = ChallengeInd._04.getValue();
    private int apiVersion = -1;
    private boolean forceTokenRequest = false;
    private List<Pair<String, String>> merchantParams = null;

    public H2HPaymentRecurrentInitial() {
        merchantTransactionId = Utils.getInstance().generateRandomNumber();
    }

    public H2HPaymentRecurrentInitial(String amount, Currency currency, CountryCode country, String customerId, String merchantTransactionId, String chName, String cardNumber, String expDate, String cvnNumber, String statusURL, String successURL, String errorURL, String cancelURL, String awaitingURL, OperationTypes operationType, PaymentSolutions paymentSolution, int apiVersion) {
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.customerId = customerId;
        this.merchantTransactionId = merchantTransactionId;
        this.chName = chName;
        this.cardNumber = cardNumber;
        this.expDate = expDate;
        this.cvnNumber = cvnNumber;
        this.statusURL = statusURL;
        this.successURL = successURL;
        this.errorURL = errorURL;
        this.cancelURL = cancelURL;
        this.awaitingURL = awaitingURL;
        this.operationType = operationType;
        this.paymentSolution = paymentSolution;
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

    public ChallengeInd getChallengeInd() {
        return ChallengeInd.getChallengeInd(challengeInd);
    }

    public void setChallengeInd(ChallengeInd challengeInd) {
        this.challengeInd = challengeInd.getValue();
    }

    public boolean isForceTokenRequest() {
        return forceTokenRequest;
    }

    public void setForceTokenRequest(boolean forceTokenRequest) {
        this.forceTokenRequest = forceTokenRequest;
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
                "amount", "currency", "country", "customerId",
                "merchantId", "merchantTransactionId", "chName",
                "cardNumber", "expDate", "cvnNumber", "successURL",
                "errorURL", "cancelURL", "awaitingURL", "productId",
                "operationType", "paymentSolution", "paymentRecurringType",
                "challengeInd"
        );

        return Utils.getInstance().containsNull(
                H2HPaymentRecurrentInitial.class, this, mandatoryFields
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
