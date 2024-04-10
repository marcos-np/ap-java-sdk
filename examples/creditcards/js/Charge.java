package com.mp.javaPaymentSDK.examples.creditcards.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.js.JSCharge;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class Charge {

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
        JSCharge jsCharge = new JSCharge();
        jsCharge.setAmount("30");
        jsCharge.setPrepayToken("56c9942b-d303-4421-b637-286d29b26dc3");
        jsCharge.setCountry(CountryCode.ES);
        jsCharge.setCustomerId("55");
        jsCharge.setCurrency(Currency.EUR);
        jsCharge.setOperationType(OperationTypes.DEBIT);
        jsCharge.setPaymentSolution(PaymentSolutions.creditcards);
        jsCharge.setStatusURL(Creds.statusUrl);
        jsCharge.setSuccessURL(Creds.successUrl);
        jsCharge.setErrorURL(Creds.errorUrl);
        jsCharge.setAwaitingURL(Creds.awaitingUrl);
        jsCharge.setCancelURL(Creds.cancelUrl);
        jsCharge.setApiVersion(5);

        // Step 3 - Send Payment Request
        JSPaymentAdapter jsPaymentAdapter = new JSPaymentAdapter(credentials);
        jsPaymentAdapter.sendJSChargeRequest(jsCharge, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                System.out.println("Intermediate Notification Received");
                System.out.println(gson.toJson(notification));
                socketAdapter.connect(jsCharge.getMerchantTransactionId(), new NotificationListener() {
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
