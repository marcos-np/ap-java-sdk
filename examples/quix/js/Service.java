package com.mp.javaPaymentSDK.examples.quix.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.JSQuixPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress;
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixArticleService;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixCartService;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixItemCartItemService;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixService;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class Service {

    public static void main(String[] args) {
        sendQuixJsServiceRequest();
    }

    private static void sendQuixJsServiceRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdService);
        // endregion

        // region Step 2 - Configure Payment Parameters
        JSQuixService jsQuixService = new JSQuixService();
        jsQuixService.setApiVersion(5);
        jsQuixService.setAmount("99");
        jsQuixService.setPrepayToken("0bcf287f-7687-40c2-ab7a-6d8e86f3d75e");
        jsQuixService.setCustomerId("55");
        jsQuixService.setStatusURL(Creds.statusUrl);
        jsQuixService.setCancelURL(Creds.cancelUrl);
        jsQuixService.setErrorURL(Creds.errorUrl);
        jsQuixService.setSuccessURL(Creds.successUrl);
        jsQuixService.setAwaitingURL(Creds.awaitingUrl);
        jsQuixService.setCustomerEmail("test@mail.com");
        jsQuixService.setDob("01-12-1999");
        jsQuixService.setFirstName("Name");
        jsQuixService.setLastName("Last Name");
        jsQuixService.setMerchantParameter("Test1", "value1");
        jsQuixService.setMerchantParameter("Test2", "value2");

        QuixArticleService quixArticleService = new QuixArticleService();
        quixArticleService.setName("Nombre del servicio 2");
        quixArticleService.setReference("4912345678903");
        quixArticleService.setStartDate("2024-10-30T00:00:00+01:00");
        quixArticleService.setEndDate("2024-12-31T23:59:59+01:00");
        quixArticleService.setUnit_price_with_tax(99);

        QuixItemCartItemService quixItemCartItemService = new QuixItemCartItemService();
        quixItemCartItemService.setArticle(quixArticleService);
        quixItemCartItemService.setUnits(1);
        quixItemCartItemService.setAuto_shipping(true);
        quixItemCartItemService.setTotal_price_with_tax(99);

        List<QuixItemCartItemService> items = new ArrayList<>();
        items.add(quixItemCartItemService);

        QuixCartService quixCartService = new QuixCartService();
        quixCartService.setCurrency(Currency.EUR);
        quixCartService.setItems(items);
        quixCartService.setTotal_price_with_tax(99);

        QuixAddress quixAddress = new QuixAddress();
        quixAddress.setCity("Barcelona");
        quixAddress.setCountry(CountryCode.ES);
        quixAddress.setStreet_address("Nombre de la vía y nº");
        quixAddress.setPostal_code("28003");

        QuixBilling quixBilling = new QuixBilling();
        quixBilling.setAddress(quixAddress);
        quixBilling.setFirst_name("Nombre");
        quixBilling.setLast_name("Apellido");

        QuixServicePaySolExtendedData quixServicePaySolExtendedData = new QuixServicePaySolExtendedData();
        quixServicePaySolExtendedData.setCart(quixCartService);
        quixServicePaySolExtendedData.setBilling(quixBilling);
        quixServicePaySolExtendedData.setProduct("instalments");

        jsQuixService.setPaySolExtendedData(quixServicePaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        JSQuixPaymentAdapter jsQuixPaymentAdapter = new JSQuixPaymentAdapter(credentials);
        jsQuixPaymentAdapter.sendJSQuixServiceRequest(jsQuixService, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                System.out.println("Intermediate Notification Received");
                System.out.println(gson.toJson(notification));
                socketAdapter.connect(jsQuixService.getMerchantTransactionId(), new NotificationListener() {
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
