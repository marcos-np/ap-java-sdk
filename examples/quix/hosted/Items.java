package com.mp.javaPaymentSDK.examples.quix.hosted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.CountryCode;
import com.mp.javaPaymentSDK.enums.Currency;
import com.mp.javaPaymentSDK.enums.Environment;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.*;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct;
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static void main(String[] args) {
        sendQuixHostedItemRequest();
    }

    private static void sendQuixHostedItemRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdItem);
        // endregion

        // region Step 2 - Configure Payment Parameters
        HostedQuixItem hostedQuixItem = new HostedQuixItem();
        hostedQuixItem.setApiVersion(5);
        hostedQuixItem.setAmount("99");
        hostedQuixItem.setCustomerId("903");
        hostedQuixItem.setStatusURL(Creds.statusUrl);
        hostedQuixItem.setCancelURL(Creds.cancelUrl);
        hostedQuixItem.setErrorURL(Creds.errorUrl);
        hostedQuixItem.setSuccessURL(Creds.successUrl);
        hostedQuixItem.setAwaitingURL(Creds.awaitingUrl);
        hostedQuixItem.setCustomerEmail("test@mail.com");
        hostedQuixItem.setDob("01-12-1999");
        hostedQuixItem.setFirstName("Name");
        hostedQuixItem.setLastName("Last Name");

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
        quixAddress.setPostal_code("28003");

        QuixBilling quixBilling = new QuixBilling();
        quixBilling.setAddress(quixAddress);
        quixBilling.setFirst_name("Nombre");
        quixBilling.setLast_name("Apellido");

        QuixItemPaySolExtendedData quixItemPaySolExtendedData = new QuixItemPaySolExtendedData();
        quixItemPaySolExtendedData.setCart(quixCartProduct);
        quixItemPaySolExtendedData.setBilling(quixBilling);
        quixItemPaySolExtendedData.setProduct("instalments");

        hostedQuixItem.setPaySolExtendedData(quixItemPaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        HostedQuixPaymentAdapter hostedQuixPaymentAdapter = new HostedQuixPaymentAdapter(credentials);
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onRedirectionURLReceived(String redirectionURL) {
                System.out.println("Redirection Url Received");
                System.out.println("Url = " + redirectionURL);
                socketAdapter.connect(hostedQuixItem.getMerchantTransactionId(), new NotificationListener() {
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
