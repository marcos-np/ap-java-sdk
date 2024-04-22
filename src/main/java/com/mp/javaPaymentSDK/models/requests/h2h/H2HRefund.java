package com.mp.javaPaymentSDK.models.requests.h2h;

import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class H2HRefund {
    private String amount = null;
    private String merchantId = null;
    private String merchantTransactionId = null;
    private PaymentSolutions paymentSolution = null;
    private String transactionId = null;
    private int apiVersion = -1;
    private List<Pair<String, String>> merchantParams = null;

    public H2HRefund() {
    }

    public H2HRefund(String amount, String merchantTransactionId, PaymentSolutions paymentSolution, String transactionId, int apiVersion) {
        this.amount = amount;
        this.merchantTransactionId = merchantTransactionId;
        this.paymentSolution = paymentSolution;
        this.transactionId = transactionId;
        this.apiVersion = apiVersion;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
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
    }

    public Pair<Boolean, String> isMissingField() {
        if (apiVersion < 0) {
            return new Pair<>(true, "Invalid apiVersion");
        }

        List<String> mandatoryFields = Arrays.asList(
                "amount", "merchantId","merchantTransactionId", "paymentSolution", "transactionId"
        );

        return Utils.getInstance().containsNull(
                H2HRefund.class, this, mandatoryFields
        );
    }

    public Pair<Boolean, String> checkCredentials(Credentials credentials) {
        List<String> mandatoryFields = Arrays.asList(
                "merchantId", "merchantPass", "environment"
        );

        return Utils.getInstance().containsNull(
                Credentials.class, credentials, mandatoryFields
        );
    }
}
