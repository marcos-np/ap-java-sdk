package com.mp.javaPaymentSDK.examples.quix.js.flights;

import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter;
import com.mp.javaPaymentSDK.callbacks.JSPaymentListener;
import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.OperationTypes;
import com.mp.javaPaymentSDK.exceptions.FieldException;
import com.mp.javaPaymentSDK.exceptions.InvalidFieldException;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.js.JSAuthorizationRequest;
import com.mp.javaPaymentSDK.models.responses.JSAuthorizationResponse;
import com.mp.javaPaymentSDK.utils.Creds;

public class AuthFlights {

    public static void main(String[] args) {
        sendAuthPaymentRequest();
    }

    public static void sendAuthPaymentRequest() {
        try {
            // Step 1 - Creating Credentials Object
            Credentials credentials = new Credentials();
            credentials.setMerchantId(Creds.merchantId);
            credentials.setMerchantKey(Creds.merchantKey);
            credentials.setEnvironment(Creds.environment);
            credentials.setProductId(Creds.productIdFlight);
            credentials.setApiVersion(5);

            // Step 2 - Configure Payment Parameters
            JSAuthorizationRequest jsAuthorizationRequest = new JSAuthorizationRequest();
            jsAuthorizationRequest.setCountry(CountryCode.ES);
            jsAuthorizationRequest.setCustomerId("55");
            jsAuthorizationRequest.setCurrency(Currency.EUR);
            jsAuthorizationRequest.setOperationType(OperationTypes.DEBIT);
            jsAuthorizationRequest.setAnonymousCustomer(true);

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
                    System.out.println(rawResponse);
                }
            });
        } catch (FieldException fieldException) {
            fieldException.printStackTrace();
        }
    }
}
