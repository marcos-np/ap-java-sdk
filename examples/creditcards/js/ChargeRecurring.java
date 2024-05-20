package com.mp.javaPaymentSDK.examples.creditcards.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.js.JSPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class ChargeRecurring {

    public static void main(String[] args) {
        sendChargePaymentRequest();
    }

    public static void sendChargePaymentRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantKey(Creds.merchantKey);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productId);

        // Step 2 - Configure Payment Parameters
        JSPaymentRecurrentInitial jsPaymentRecurrentInitial = new JSPaymentRecurrentInitial();
        jsPaymentRecurrentInitial.setAmount("30");
        jsPaymentRecurrentInitial.setPrepayToken("972c352e-06cc-4ff3-8e1a-60aabd9ff2f2");
        jsPaymentRecurrentInitial.setCountry(CountryCode.ES);
        jsPaymentRecurrentInitial.setCustomerId("55");
        jsPaymentRecurrentInitial.setCurrency(Currency.EUR);
        jsPaymentRecurrentInitial.setOperationType(OperationTypes.DEBIT);
        jsPaymentRecurrentInitial.setPaymentSolution(PaymentSolutions.creditcards);
        jsPaymentRecurrentInitial.setStatusURL(Creds.statusUrl);
        jsPaymentRecurrentInitial.setSuccessURL(Creds.successUrl);
        jsPaymentRecurrentInitial.setErrorURL(Creds.errorUrl);
        jsPaymentRecurrentInitial.setAwaitingURL(Creds.awaitingUrl);
        jsPaymentRecurrentInitial.setCancelURL(Creds.cancelUrl);
        jsPaymentRecurrentInitial.setPaymentRecurringType(PaymentRecurringType.newSubscription);
        jsPaymentRecurrentInitial.setApiVersion(5);

        // Step 3 - Send Payment Request
        JSPaymentAdapter jsPaymentAdapter = new JSPaymentAdapter(credentials);
        jsPaymentAdapter.sendJSPaymentRecurrentInitial(jsPaymentRecurrentInitial, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                System.out.println("Intermediate Notification Received");
                System.out.println(gson.toJson(notification));
                socketAdapter.connect(jsPaymentRecurrentInitial.getMerchantTransactionId(), new NotificationListener() {
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
