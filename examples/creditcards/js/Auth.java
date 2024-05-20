package com.mp.javaPaymentSDK.examples.creditcards.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.callbacks.JSPaymentListener;
import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.js.JSAuthorizationRequest;
import com.mp.javaPaymentSDK.models.responses.JSAuthorizationResponse;
import com.mp.javaPaymentSDK.utils.Creds;

public class Auth {

    public static void main(String[] args) {
        sendAuthPaymentRequest();
    }

    public static void sendAuthPaymentRequest() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantKey(Creds.merchantKey);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

        // Step 2 - Configure Payment Parameters
        JSAuthorizationRequest jsAuthorizationRequest = new JSAuthorizationRequest();
        jsAuthorizationRequest.setCountry(CountryCode.ES);
        jsAuthorizationRequest.setCustomerId("55");
        jsAuthorizationRequest.setCurrency(Currency.EUR);
        jsAuthorizationRequest.setOperationType(OperationTypes.DEBIT);
        jsAuthorizationRequest.setApiVersion(5);
        jsAuthorizationRequest.setAnonymousCustomer(false);

        // Step 3 - Send Payment Request
        JSPaymentAdapter jsPaymentAdapter = new JSPaymentAdapter(credentials);
        jsPaymentAdapter.sendJSAuthorizationRequest(jsAuthorizationRequest, new JSPaymentListener() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onAuthorizationResponseReceived(String rawResponse, JSAuthorizationResponse response) {
                System.out.println("AuthToken Received: " + response.getAuthToken());
                System.out.println(gson.toJson(response));
            }
        });
    }
}
