package com.mp.javaPaymentSDK.examples.creditcards.hosted;

import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRedirection;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class HostedExample {

    public static void main(String[] args) {
        sendHostedPaymentRequest();
    }

    public static void sendHostedPaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

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
        hostedPaymentRedirection.setApiVersion(5);
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
                socketAdapter.connect(hostedPaymentRedirection.getMerchantTransactionId(), new NotificationListener() {
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
