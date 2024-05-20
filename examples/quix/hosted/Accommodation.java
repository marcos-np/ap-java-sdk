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
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixAccommodationPaySolExtendedData;
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress;
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixArticleAccommodation;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixCartAccommodation;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixAccommodation;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class Accommodation {

    public static void main(String[] args) {
        sendQuixHostedAccommodationRequest();
    }

    private static void sendQuixHostedAccommodationRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdAccommodation);
        // endregion

        // region Step 2 - Configure Payment Parameters
        HostedQuixAccommodation hostedQuixAccommodation = new HostedQuixAccommodation();
        hostedQuixAccommodation.setAmount("99");
        hostedQuixAccommodation.setCustomerId("903");
        hostedQuixAccommodation.setStatusURL(Creds.statusUrl);
        hostedQuixAccommodation.setCancelURL(Creds.cancelUrl);
        hostedQuixAccommodation.setErrorURL(Creds.errorUrl);
        hostedQuixAccommodation.setSuccessURL(Creds.successUrl);
        hostedQuixAccommodation.setAwaitingURL(Creds.awaitingUrl);
        hostedQuixAccommodation.setCustomerEmail("test@mail.com");
        hostedQuixAccommodation.setDob("01-12-1999");
        hostedQuixAccommodation.setFirstName("Name");
        hostedQuixAccommodation.setLastName("Last Name");
        hostedQuixAccommodation.setApiVersion(5);

        QuixAddress quixAddress = new QuixAddress();
        quixAddress.setCity("Barcelona");
        quixAddress.setCountry(CountryCode.ES);
        quixAddress.setStreet_address("Nombre de la vía y nº");
        quixAddress.setPostal_code("28003");

        QuixArticleAccommodation quixArticleAccommodation = new QuixArticleAccommodation();
        quixArticleAccommodation.setName("Nombre del servicio 2");
        quixArticleAccommodation.setReference("4912345678903");
        quixArticleAccommodation.setCheckinDate("2024-10-30T00:00:00+01:00");
        quixArticleAccommodation.setCheckoutDate("2024-12-31T23:59:59+01:00");
        quixArticleAccommodation.setGuests(1);
        quixArticleAccommodation.setEstablishmentName("Hotel");
        quixArticleAccommodation.setAddress(quixAddress);
        quixArticleAccommodation.setUnit_price_with_tax(99);

        QuixItemCartItemAccommodation quixItemCartItemAccommodation = new QuixItemCartItemAccommodation();
        quixItemCartItemAccommodation.setArticle(quixArticleAccommodation);
        quixItemCartItemAccommodation.setUnits(1);
        quixItemCartItemAccommodation.setAuto_shipping(true);
        quixItemCartItemAccommodation.setTotal_price_with_tax(99);

        List<QuixItemCartItemAccommodation> items = new ArrayList<>();
        items.add(quixItemCartItemAccommodation);

        QuixCartAccommodation quixCartAccommodation = new QuixCartAccommodation();
        quixCartAccommodation.setCurrency(Currency.EUR);
        quixCartAccommodation.setItems(items);
        quixCartAccommodation.setTotal_price_with_tax(99);

        QuixBilling quixBilling = new QuixBilling();
        quixBilling.setAddress(quixAddress);
        quixBilling.setFirst_name("Nombre");
        quixBilling.setLast_name("Apellido");

        QuixAccommodationPaySolExtendedData quixAccommodationPaySolExtendedData = new QuixAccommodationPaySolExtendedData();
        quixAccommodationPaySolExtendedData.setCart(quixCartAccommodation);
        quixAccommodationPaySolExtendedData.setBilling(quixBilling);
        quixAccommodationPaySolExtendedData.setProduct("instalments");

        hostedQuixAccommodation.setPaySolExtendedData(quixAccommodationPaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        HostedQuixPaymentAdapter hostedQuixPaymentAdapter = new HostedQuixPaymentAdapter(credentials);
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onRedirectionURLReceived(String redirectionURL) {
                System.out.println("Redirection Url Received");
                System.out.println("Url = " + redirectionURL);
                socketAdapter.connect(hostedQuixAccommodation.getMerchantTransactionId(), new NotificationListener() {
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
