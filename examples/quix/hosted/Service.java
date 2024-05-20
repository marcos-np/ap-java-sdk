package com.mp.javaPaymentSDK.examples.quix.hosted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter;
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
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixService;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class Service {

    public static void main(String[] args) {
        sendQuixHostedServiceRequest();
    }

    private static void sendQuixHostedServiceRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdService);
        // endregion

        // region Step 2 - Configure Payment Parameters
        HostedQuixService hostedQuixService = new HostedQuixService();
        hostedQuixService.setApiVersion(5);
        hostedQuixService.setAmount("99");
        hostedQuixService.setCustomerId("903");
        hostedQuixService.setStatusURL(Creds.statusUrl);
        hostedQuixService.setCancelURL(Creds.cancelUrl);
        hostedQuixService.setErrorURL(Creds.errorUrl);
        hostedQuixService.setSuccessURL(Creds.successUrl);
        hostedQuixService.setAwaitingURL(Creds.awaitingUrl);
        hostedQuixService.setCustomerEmail("test@mail.com");
        hostedQuixService.setDob("01-12-1999");
        hostedQuixService.setFirstName("Name");
        hostedQuixService.setLastName("Last Name");

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

        hostedQuixService.setPaySolExtendedData(quixServicePaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        HostedQuixPaymentAdapter hostedQuixPaymentAdapter = new HostedQuixPaymentAdapter(credentials);
        hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onRedirectionURLReceived(String redirectionURL) {
                System.out.println("Redirection Url Received");
                System.out.println("Url = " + redirectionURL);
                socketAdapter.connect(hostedQuixService.getMerchantTransactionId(), new NotificationListener() {
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
