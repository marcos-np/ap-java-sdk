package com.mp.javaPaymentSDK.examples.creditcards.h2h;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.PaymentSolutions;
import com.mp.javaPaymentSDK.enums.TransactionResult;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.h2h.H2HVoid;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

public class Void {

    public static void main(String[] args) {
        sendVoidPaymentRequest();
    }


    public static void sendVoidPaymentRequest() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);

        // Step 2 - Configure Payment Parameters
        H2HVoid h2HVoid = new H2HVoid();
        h2HVoid.setPaymentSolution(PaymentSolutions.caixapucpuce);
        h2HVoid.setTransactionId("7817740");
        h2HVoid.setMerchantTransactionId("76969499");
        h2HVoid.setApiVersion(5);

        // Step 3 - Send Payment Request
        H2HPaymentAdapter h2HPaymentAdapter = new H2HPaymentAdapter(credentials);
        h2HPaymentAdapter.sendH2hVoidRequest(h2HVoid, new ResponseListenerAdapter() {
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
    }
}
