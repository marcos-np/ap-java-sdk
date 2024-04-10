package com.mp.javaPaymentSDK.models.requests.js;

import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.OperationTypes;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JSAuthorizationRequest {

    private Currency currency = null;
    private String merchantId = null;
    private String merchantKey = null;
    private String productId = null;
    private CountryCode country = null;
    private String customerId = null;
    private OperationTypes operationType = null;
    private int apiVersion = -1;
    private HashMap<String, String> merchantParams = null;

    public JSAuthorizationRequest() {
    }

    public JSAuthorizationRequest(Currency currency, CountryCode country, String customerId, OperationTypes operationType, int apiVersion) {
        this.currency = currency;
        this.country = country;
        this.customerId = customerId;
        this.operationType = operationType;
        this.apiVersion = apiVersion;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    public String getMerchantId() {
        return merchantId;
    }

    public String getMerchantKey() {
        return merchantKey;
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
                 "currency", "country", "customerId",
                "operationType"
        );

        return Utils.getInstance().containsNull(
                JSAuthorizationRequest.class, this, mandatoryFields
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
