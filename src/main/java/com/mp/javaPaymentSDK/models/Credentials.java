package com.mp.javaPaymentSDK.models;

import com.mp.javaPaymentSDK.enums.Environment;

public class Credentials {

    private String merchantId;
    private String merchantKey;
    private String merchantPass;
    private Environment environment;
    private String productId;

    public Credentials() {
    }

    public Credentials(String merchantId, String merchantKey, String merchantPass, Environment environment, String productId) {
        this.merchantId = merchantId;
        this.merchantKey = merchantKey;
        this.merchantPass = merchantPass;
        this.environment = environment;
        this.productId = productId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getMerchantPass() {
        return merchantPass;
    }

    public void setMerchantPass(String merchantPass) {
        this.merchantPass = merchantPass;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
