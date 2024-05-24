package com.mp.javaPaymentSDK.examples.creditcards.hosted;

import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.exceptions.FieldException;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.utils.Creds;

public class Recurring {

    public static void main(String[] args) {
        sendRecurringPaymentRequest();
    }

    public static void sendRecurringPaymentRequest() {
        try {
            // Step 1 - Creating Credentials Object
            Credentials credentials = new Credentials();
            credentials.setMerchantId(Creds.merchantId);
            credentials.setMerchantPass(Creds.merchantPass);
            credentials.setEnvironment(Creds.environment);
            credentials.setProductId(Creds.productId);
            credentials.setApiVersion(5);

            // Step 2 - Configure Payment Parameters
            HostedPaymentRecurrentInitial hostedPaymentRecurrentInitial = new HostedPaymentRecurrentInitial();
            hostedPaymentRecurrentInitial.setCurrency(Currency.EUR);
            hostedPaymentRecurrentInitial.setPaymentSolution(PaymentSolutions.creditcards);
            hostedPaymentRecurrentInitial.setAmount("50");
            hostedPaymentRecurrentInitial.setCountry(CountryCode.ES);
            hostedPaymentRecurrentInitial.setCustomerId("903");
            hostedPaymentRecurrentInitial.setStatusURL(Creds.statusUrl);
            hostedPaymentRecurrentInitial.setSuccessURL(Creds.successUrl);
            hostedPaymentRecurrentInitial.setErrorURL(Creds.errorUrl);
            hostedPaymentRecurrentInitial.setAwaitingURL(Creds.awaitingUrl);
            hostedPaymentRecurrentInitial.setCancelURL(Creds.cancelUrl);

            // Step 3 - Send Payment Request
            HostedPaymentAdapter hostedPaymentAdapter = new HostedPaymentAdapter(credentials);
            hostedPaymentAdapter.sendHostedRecurrentInitial(hostedPaymentRecurrentInitial, new ResponseListenerAdapter() {
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
