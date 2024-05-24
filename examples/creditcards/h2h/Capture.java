package com.mp.javaPaymentSDK.examples.creditcards.h2h;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.enums.TransactionResult;
import com.mp.javaPaymentSDK.exceptions.FieldException;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPreAuthorizationCapture;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class Capture {

    public static void main(String[] args) {
        sendCapturePaymentRequest();
    }

    public static void sendCapturePaymentRequest() {
        try {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();

            // Step 1 - Creating Credentials Object
            Credentials credentials = new Credentials();
            credentials.setMerchantId(Creds.merchantId);
            credentials.setMerchantPass(Creds.merchantPass);
            credentials.setEnvironment(Creds.environment);
            credentials.setApiVersion(5);

            // Step 2 - Configure Payment Parameters
            H2HPreAuthorizationCapture h2HPreAuthorizationCapture = new H2HPreAuthorizationCapture();
            h2HPreAuthorizationCapture.setPaymentSolution(PaymentSolutions.caixapucpuce);
            h2HPreAuthorizationCapture.setTransactionId("7961829");

            // Step 3 - Send Payment Request
            H2HPaymentAdapter h2HPaymentAdapter = new H2HPaymentAdapter(credentials);
            h2HPaymentAdapter.sendH2hPreAuthorizationCapture(h2HPreAuthorizationCapture, new ResponseListenerAdapter() {
                // Step 4 - Handle the Response
                @Override
                public void onError(Error error, String errorMessage) {
                    System.out.println("Error received - " + error.name() + " - " + errorMessage);
                }

                @Override
                public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                    System.out.println("Final Notification Received");
                    System.out.println(gson.toJson(notification));
                    System.out.println("Transaction Result = " + transactionResult.name());
                }
            });
        } catch (FieldException fieldException) {
            fieldException.printStackTrace();
        }
    }

}
