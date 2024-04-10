package com.mp.javaPaymentSDK.examples.creditcards.hosted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class Recurring {

    public static void main(String[] args) {
        sendRecurringPaymentRequest();
    }

    public static void sendRecurringPaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

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
        hostedPaymentRecurrentInitial.setApiVersion(5);

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
                socketAdapter.connect(hostedPaymentRecurrentInitial.getMerchantTransactionId(), new NotificationListener() {
                    @Override
                    public void onError(Error error, String errorMessage) {
                        System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                    }

                    @Override
                    public void onNotificationReceived(String notificationResponse) {
                        // Step 5 - Handle Payment Notification
                        try {
                            Notification notification = NotificationAdapter.parseNotification(notificationResponse);
                            if (notification != null) {
                                if (notification.isLastNotification()) {
                                    System.out.println("Final Notification Received For merchantTransactionId = " + notification.getMerchantTransactionId());
                                } else {
                                    System.out.println("Intermediate Notification For merchantTransactionId = " + notification.getMerchantTransactionId());
                                }
                            }
                            else {
                                System.out.println("Invalid Response Received");
                            }
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                            System.out.println("Invalid Response Received");
                        }
                    }
                });
            }
        });
    }
}
