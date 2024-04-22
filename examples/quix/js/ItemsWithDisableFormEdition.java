package com.mp.javaPaymentSDK.examples.quix.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.JSQuixPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.TransactionResult;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress;
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixItem;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class ItemsWithDisableFormEdition {

    public static void main(String[] args) {
        sendQuixJsItemRequest();
    }

    private static void sendQuixJsItemRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdItem);
        // endregion

        // region Step 2 - Configure Payment Parameters
        JSQuixItem jsQuixItem = new JSQuixItem();
        jsQuixItem.setPrepayToken("4c8eb302-890f-43ff-aa8c-7ee77acec777");
        jsQuixItem.setApiVersion(5);
        jsQuixItem.setAmount("99");
        jsQuixItem.setCustomerId("55");
        jsQuixItem.setStatusURL(Creds.statusUrl);
        jsQuixItem.setCancelURL(Creds.cancelUrl);
        jsQuixItem.setErrorURL(Creds.errorUrl);
        jsQuixItem.setSuccessURL(Creds.successUrl);
        jsQuixItem.setAwaitingURL(Creds.awaitingUrl);
        jsQuixItem.setCustomerEmail("test@mail.com");
        jsQuixItem.setDob("01-12-1999");
        jsQuixItem.setFirstName("Name");
        jsQuixItem.setLastName("Last Name");

        QuixArticleProduct quixArticleProduct = new QuixArticleProduct();
        quixArticleProduct.setName("Nombre del servicio 2");
        quixArticleProduct.setReference("4912345678903");
        quixArticleProduct.setUnit_price_with_tax(99);

        QuixItemCartItemProduct quixItemCartItemProduct = new QuixItemCartItemProduct();
        quixItemCartItemProduct.setArticle(quixArticleProduct);
        quixItemCartItemProduct.setUnits(1);
        quixItemCartItemProduct.setAuto_shipping(true);
        quixItemCartItemProduct.setTotal_price_with_tax(99);

        List<QuixItemCartItemProduct> items = new ArrayList<>();
        items.add(quixItemCartItemProduct);

        QuixCartProduct quixCartProduct = new QuixCartProduct();
        quixCartProduct.setCurrency(Currency.EUR);
        quixCartProduct.setItems(items);
        quixCartProduct.setTotal_price_with_tax(99);

        QuixAddress quixAddress = new QuixAddress();
        quixAddress.setCity("Barcelona");
        quixAddress.setCountry(CountryCode.ES);
        quixAddress.setStreet_address("Nombre de la vía y nº");
        quixAddress.setPostal_code("08003");

        QuixBilling quixBilling = new QuixBilling();
        quixBilling.setAddress(quixAddress);
        quixBilling.setFirst_name("Nombre");
        quixBilling.setLast_name("Apellido");

        QuixItemPaySolExtendedData quixItemPaySolExtendedData = new QuixItemPaySolExtendedData();
        quixItemPaySolExtendedData.setCart(quixCartProduct);
        quixItemPaySolExtendedData.setBilling(quixBilling);
        quixItemPaySolExtendedData.setProduct("instalments");
        quixItemPaySolExtendedData.setDisableFormEdition(true);

        jsQuixItem.setPaySolExtendedData(quixItemPaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        JSQuixPaymentAdapter jsQuixPaymentAdapter = new JSQuixPaymentAdapter(credentials);
        jsQuixPaymentAdapter.sendJSQuixItemRequest(jsQuixItem, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onResponseReceived(String rawResponse, Notification notification, TransactionResult transactionResult) {
                System.out.println("Response Received");
                System.out.println(gson.toJson(notification));
                System.out.println("Disable Form  Edition = " + notification.getDisableFormEdition());
                socketAdapter.connect(jsQuixItem.getMerchantTransactionId(), new NotificationListener() {
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
