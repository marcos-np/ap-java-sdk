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
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPreAuthorization;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class PreAuthorization {

    public static void main(String[] args) {
        sendPreAuthorizationPaymentRequest();
    }


    public static void sendPreAuthorizationPaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

        // Step 2 - Configure Payment Parameters
        H2HPreAuthorization h2HPreAuthorization = new H2HPreAuthorization();
        h2HPreAuthorization.setAmount("50");
        h2HPreAuthorization.setCurrency(Currency.EUR);
        h2HPreAuthorization.setCountry(CountryCode.ES);
        h2HPreAuthorization.setCardNumber("4907270002222227");
        h2HPreAuthorization.setCustomerId("903");
        h2HPreAuthorization.setChName("First name Last name");
        h2HPreAuthorization.setCvnNumber("123");
        h2HPreAuthorization.setExpDate("0625");
        h2HPreAuthorization.setPaymentSolution(PaymentSolutions.creditcards);
        h2HPreAuthorization.setStatusURL(Creds.statusUrl);
        h2HPreAuthorization.setSuccessURL(Creds.successUrl);
        h2HPreAuthorization.setErrorURL(Creds.errorUrl);
        h2HPreAuthorization.setAwaitingURL(Creds.awaitingUrl);
        h2HPreAuthorization.setCancelURL(Creds.cancelUrl);
        h2HPreAuthorization.setApiVersion(5);

        // Step 3 - Send Payment Request
        H2HPaymentAdapter h2HPaymentAdapter = new H2HPaymentAdapter(credentials);
        h2HPaymentAdapter.sendH2hPreAuthorizationRequest(h2HPreAuthorization, new ResponseListenerAdapter() {
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
                socketAdapter.connect(h2HPreAuthorization.getMerchantTransactionId(), new NotificationListener() {
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
