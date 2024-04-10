package com.mp.javaPaymentSDK.examples.creditcards.h2h;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentSuccessive;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class RecurringSubsequent {

    public static void main(String[] args) {
        sendRecurringSubsequentPaymentRequest();
    }


    public static void sendRecurringSubsequentPaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

        // Step 2 - Configure Payment Parameters
        H2HPaymentRecurrentSuccessive h2HPaymentRecurrentSuccessive = new H2HPaymentRecurrentSuccessive();

        h2HPaymentRecurrentSuccessive.setAmount("50");
        h2HPaymentRecurrentSuccessive.setCurrency(Currency.EUR);
        h2HPaymentRecurrentSuccessive.setCountry(CountryCode.ES);
        h2HPaymentRecurrentSuccessive.setMerchantTransactionId("80004931");
        h2HPaymentRecurrentSuccessive.setCustomerId("903");
        h2HPaymentRecurrentSuccessive.setChName("First name Last name");
        h2HPaymentRecurrentSuccessive.setPaymentSolution(PaymentSolutions.creditcards);
        h2HPaymentRecurrentSuccessive.setCardNumberToken("6537275043632227");
        h2HPaymentRecurrentSuccessive.setSubscriptionPlan("511845609608301");
        h2HPaymentRecurrentSuccessive.setStatusURL(Creds.statusUrl);
        h2HPaymentRecurrentSuccessive.setSuccessURL(Creds.successUrl);
        h2HPaymentRecurrentSuccessive.setErrorURL(Creds.errorUrl);
        h2HPaymentRecurrentSuccessive.setAwaitingURL(Creds.awaitingUrl);
        h2HPaymentRecurrentSuccessive.setCancelURL(Creds.cancelUrl);
        h2HPaymentRecurrentSuccessive.setApiVersion(5);

        // Step 3 - Send Payment Request
        H2HPaymentAdapter h2HPaymentAdapter = new H2HPaymentAdapter(credentials);
        h2HPaymentAdapter.sendH2hPaymentRecurrentSuccessive(h2HPaymentRecurrentSuccessive, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                System.out.println("Response Notification Received");
                System.out.println(gson.toJson(notification));
                String redirectionURL = notification.getRedirectUrl();
                System.out.println("Redirection url = " + redirectionURL);
                socketAdapter.connect(h2HPaymentRecurrentSuccessive.getMerchantTransactionId(), new NotificationListener() {
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
