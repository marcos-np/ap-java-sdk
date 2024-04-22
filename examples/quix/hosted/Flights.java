package com.mp.javaPaymentSDK.examples.quix.hosted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.adapters.NotificationAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.SocketAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress;
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.models.quix_models.quix_flight.*;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixFlight;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Creds;

import java.util.ArrayList;
import java.util.List;

public class Flights {

    public static void main(String[] args) {
        sendQuixHostedFlightsRequest();
    }

    private static void sendQuixHostedFlightsRequest() {
        SocketAdapter socketAdapter = new SocketAdapter();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        // region Step 1 - Creating Credentials Object
        Credentials credentials = new Credentials();
        credentials.setMerchantId(Creds.merchantId);
        credentials.setMerchantPass(Creds.merchantPass);
        credentials.setEnvironment(Creds.environment);
        credentials.setProductId(Creds.productIdFlight);
        // endregion

        // Step 2 - Configure Payment Parameters
        HostedQuixFlight hostedQuixFlight = new HostedQuixFlight();
        hostedQuixFlight.setApiVersion(5);
        hostedQuixFlight.setAmount("99");
        hostedQuixFlight.setCustomerId("903");
        hostedQuixFlight.setStatusURL(Creds.statusUrl);
        hostedQuixFlight.setCancelURL(Creds.cancelUrl);
        hostedQuixFlight.setErrorURL(Creds.errorUrl);
        hostedQuixFlight.setSuccessURL(Creds.successUrl);
        hostedQuixFlight.setAwaitingURL(Creds.awaitingUrl);
        hostedQuixFlight.setCustomerEmail("test@mail.com");
        hostedQuixFlight.setDob("01-12-1999");
        hostedQuixFlight.setFirstName("Name");
        hostedQuixFlight.setLastName("Last Name");

        QuixPassengerFlight quixPassengerFlight = new QuixPassengerFlight();
        quixPassengerFlight.setFirstName("Pablo");
        quixPassengerFlight.setLastName("Navvaro");

        List<QuixPassengerFlight> passangers = new ArrayList<>();
        passangers.add(quixPassengerFlight);

        QuixSegmentFlight quixSegmentFlight = new QuixSegmentFlight();
        quixSegmentFlight.setIataDepartureCode("MAD");
        quixSegmentFlight.setIataDestinationCode("BCN");

        List<QuixSegmentFlight> segments = new ArrayList<>();
        segments.add(quixSegmentFlight);

        QuixArticleFlight quixArticleFlight = new QuixArticleFlight();
        quixArticleFlight.setName("Nombre del servicio 2");
        quixArticleFlight.setReference("4912345678903");
        quixArticleFlight.setCustomerMemberSince("2023-10-30T00:00:00+01:00");
        quixArticleFlight.setDepartureDate("2024-12-31T23:59:59+01:00");
        quixArticleFlight.setPassengers(passangers);
        quixArticleFlight.setSegments(segments);
        quixArticleFlight.setUnit_price_with_tax(99);

        QuixItemCartItemFlight quixItemCartItemFlight = new QuixItemCartItemFlight();
        quixItemCartItemFlight.setArticle(quixArticleFlight);
        quixItemCartItemFlight.setUnits(1);
        quixItemCartItemFlight.setAuto_shipping(true);
        quixItemCartItemFlight.setTotal_price_with_tax(99);

        List<QuixItemCartItemFlight> items = new ArrayList<>();
        items.add(quixItemCartItemFlight);

        QuixCartFlight quixCartFlight = new QuixCartFlight();
        quixCartFlight.setCurrency(Currency.EUR);
        quixCartFlight.setItems(items);
        quixCartFlight.setTotal_price_with_tax(99);

        QuixAddress quixAddress = new QuixAddress();
        quixAddress.setCity("Barcelona");
        quixAddress.setCountry(CountryCode.ES);
        quixAddress.setStreet_address("Nombre de la vía y nº");
        quixAddress.setPostal_code("28003");

        QuixBilling quixBilling = new QuixBilling();
        quixBilling.setAddress(quixAddress);
        quixBilling.setFirst_name("Nombre");
        quixBilling.setLast_name("Apellido");

        QuixFlightPaySolExtendedData quixFlightPaySolExtendedData = new QuixFlightPaySolExtendedData();
        quixFlightPaySolExtendedData.setCart(quixCartFlight);
        quixFlightPaySolExtendedData.setBilling(quixBilling);
        quixFlightPaySolExtendedData.setProduct("instalments");

        hostedQuixFlight.setPaysolExtendedData(quixFlightPaySolExtendedData);
        // endregion

        // Step 3 - Send Payment Request
        HostedQuixPaymentAdapter hostedQuixPaymentAdapter = new HostedQuixPaymentAdapter(credentials);
        hostedQuixPaymentAdapter.sendHostedQuixFlightRequest(hostedQuixFlight, new ResponseListenerAdapter() {
            // Step 4 - Handle the Response
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("Error received - " + error.name() + " - " + errorMessage);
            }

            @Override
            public void onRedirectionURLReceived(String redirectionURL) {
                System.out.println("Redirection Url Received");
                System.out.println("Url = " + redirectionURL);
                socketAdapter.connect(hostedQuixFlight.getMerchantTransactionId(), new NotificationListener() {
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
