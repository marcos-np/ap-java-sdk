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
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class Recurring {

    public static void main(String[] args) {
        sendRecurringPaymentRequest();
    }


    public static void sendRecurringPaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

        // Step 2 - Configure Payment Parameters
        H2HPaymentRecurrentInitial h2HPaymentRecurrentInitial = new H2HPaymentRecurrentInitial();

        h2HPaymentRecurrentInitial.setAmount("50");
        h2HPaymentRecurrentInitial.setCurrency(Currency.EUR);
        h2HPaymentRecurrentInitial.setCountry(CountryCode.ES);
        h2HPaymentRecurrentInitial.setCardNumber("4907270002222227");
        h2HPaymentRecurrentInitial.setCustomerId("55");
        h2HPaymentRecurrentInitial.setChName("First name Last name");
        h2HPaymentRecurrentInitial.setCvnNumber("123");
        h2HPaymentRecurrentInitial.setExpDate("0625");
        h2HPaymentRecurrentInitial.setPaymentSolution(PaymentSolutions.creditcards);
        h2HPaymentRecurrentInitial.setStatusURL(Creds.statusUrl);
        h2HPaymentRecurrentInitial.setSuccessURL(Creds.successUrl);
        h2HPaymentRecurrentInitial.setErrorURL(Creds.errorUrl);
        h2HPaymentRecurrentInitial.setAwaitingURL(Creds.awaitingUrl);
        h2HPaymentRecurrentInitial.setCancelURL(Creds.cancelUrl);
        h2HPaymentRecurrentInitial.setApiVersion(5);
        h2HPaymentRecurrentInitial.setPaymentRecurringType(PaymentRecurringType.newCof);
        h2HPaymentRecurrentInitial.setForceTokenRequest(true);

        // Step 3 - Send Payment Request
        H2HPaymentAdapter h2HPaymentAdapter = new H2HPaymentAdapter(credentials);
        h2HPaymentAdapter.sendH2hPaymentRecurrentInitial(h2HPaymentRecurrentInitial, new ResponseListenerAdapter() {
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
                socketAdapter.connect(h2HPaymentRecurrentInitial.getMerchantTransactionId(), new NotificationListener() {
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
