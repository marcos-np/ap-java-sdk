package com.mp.javaPaymentSDK.examples.quix.js.service;

import com.mp.javaPaymentSDK.adapters.JSQuixPaymentAdapter;
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter;
import com.mp.javaPaymentSDK.enums.*;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.exceptions.FieldException;
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

public class ChargeService {

    public static void main(String[] args) {
        sendQuixJsServiceRequest();
    }

    private static void sendQuixJsServiceRequest() {
        try {
            // region Step 1 - Creating Credentials Object
            Credentials credentials = new Credentials();
            credentials.setMerchantId(Creds.merchantId);
            credentials.setEnvironment(Creds.environment);
            credentials.setProductId(Creds.productIdService);
            credentials.setApiVersion(5);
            // endregion

            // region Step 2 - Configure Payment Parameters
            JSQuixService jsQuixService = new JSQuixService();
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
            jsQuixService.setIpAddress("0.0.0.0");

            QuixArticleService quixArticleService = new QuixArticleService();
            quixArticleService.setName("Nombre del servicio 2");
            quixArticleService.setReference("4912345678903");
            quixArticleService.setStartDate("2024-10-30T00:00:00+01:00");
            quixArticleService.setEndDate("2024-12-31T23:59:59+01:00");
            quixArticleService.setUnitPriceWithTax(99);
            quixArticleService.setCategory(Category.digital);

            QuixItemCartItemService quixItemCartItemService = new QuixItemCartItemService();
            quixItemCartItemService.setArticle(quixArticleService);
            quixItemCartItemService.setUnits(1);
            quixItemCartItemService.setAutoShipping(true);
            quixItemCartItemService.setTotalPriceWithTax(99);

            List<QuixItemCartItemService> items = new ArrayList<>();
            items.add(quixItemCartItemService);

            QuixCartService quixCartService = new QuixCartService();
            quixCartService.setCurrency(Currency.EUR);
            quixCartService.setItems(items);
            quixCartService.setTotalPriceWithTax(99);

            QuixAddress quixAddress = new QuixAddress();
            quixAddress.setCity("Barcelona");
            quixAddress.setCountry(CountryCode.ES);
            quixAddress.setStreetAddress("Nombre de la vía y nº");
            quixAddress.setPostalCode("28003");

            QuixBilling quixBilling = new QuixBilling();
            quixBilling.setAddress(quixAddress);
            quixBilling.setFirstName("Nombre");
            quixBilling.setLastName("Apellido");

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
                    System.out.println(rawResponse);
                }
            });
        } catch (FieldException fieldException) {
            fieldException.printStackTrace();
        }
    }
}
