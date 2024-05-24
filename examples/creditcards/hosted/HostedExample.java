package com.mp.javaPaymentSDK.examples.creditcards.hosted;

import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.exceptions.FieldException;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRedirection;
import com.mp.javaPaymentSDK.utils.Creds;

public class HostedExample {

    public static void main(String[] args) {
        sendHostedPaymentRequest();
    }

    public static void sendHostedPaymentRequest() {
        try {
            // Step 1 - Creating Credentials Object
            Credentials credentials = new Credentials();
            credentials.setMerchantId(Creds.merchantId);
            credentials.setMerchantPass(Creds.merchantPass);
            credentials.setEnvironment(Creds.environment);
            credentials.setProductId(Creds.productId);
            credentials.setApiVersion(5);

            // Step 2 - Configure Payment Parameters
            HostedPaymentRedirection hostedPaymentRedirection = new HostedPaymentRedirection();
            hostedPaymentRedirection.setCurrency(Currency.EUR);
            hostedPaymentRedirection.setPaymentSolution(PaymentSolutions.creditcards);
            hostedPaymentRedirection.setAmount("50.1234");
            hostedPaymentRedirection.setCountry(CountryCode.ES);
            hostedPaymentRedirection.setCustomerId("903");
            hostedPaymentRedirection.setStatusURL(Creds.statusUrl);
            hostedPaymentRedirection.setSuccessURL(Creds.successUrl);
            hostedPaymentRedirection.setErrorURL(Creds.errorUrl);
            hostedPaymentRedirection.setAwaitingURL(Creds.awaitingUrl);
            hostedPaymentRedirection.setCancelURL(Creds.cancelUrl);
            hostedPaymentRedirection.setShowRememberMe(true);
            hostedPaymentRedirection.setForceTokenRequest(true);
            hostedPaymentRedirection.setMerchantParameter("name", "pablo");
            hostedPaymentRedirection.setMerchantParameter("surname", "ferrer");

            // Step 3 - Send Payment Request
            HostedPaymentAdapter hostedPaymentAdapter = new HostedPaymentAdapter(credentials);
            hostedPaymentAdapter.sendHostedPaymentRequest(hostedPaymentRedirection, new ResponseListenerAdapter() {
                // Step 4 - Handle the Response
                @Override
                public void onError(Error error, String errorMessage) {
                    System.out.println("Error received - " + error.name() + " - " + errorMessage);
                }

                @Override
                public void onRedirectionURLReceived(String redirectionURL) {
                    System.out.println("Redirection Url Received");
                    System.out.println("Url = " + redirectionURL);
                }
            });
        } catch (FieldException fieldException) {
            fieldException.printStackTrace();
        }
    }
}
