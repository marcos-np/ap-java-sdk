package com.mp.javaPaymentSDK.adapters;

import com.google.gson.Gson;
import com.mp.javaPaymentSDK.callbacks.RequestListener;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.enums.Endpoints;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation;
import com.mp.javaPaymentSDK.models.quix_models.quix_flight.QuixItemCartItemFlight;
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixItemCartItemService;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixAccommodation;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixFlight;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixItem;
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixService;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class JSQuixPaymentAdapter {

    private Credentials credentials;

    private final NetworkAdapter networkAdapter = new NetworkAdapter();

    private final Gson gson = new Gson();

    public JSQuixPaymentAdapter(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void sendJSQuixServiceRequest(JSQuixService jsQuixService, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = jsQuixService.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsQuixService.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = jsQuixService.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(jsQuixService.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        jsQuixService.setAmount(parsedAmount);

        List<QuixItemCartItemService> items = jsQuixService.getPaySolExtendedData().getCart().getItems();
        for (QuixItemCartItemService item : items) {
            String endDate = item.getArticle().getEndDate();
            String startDate = item.getArticle().getStartDate();
            if (endDate.contains(":")) {
                item.getArticle().setEndDate(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
            }
            if (startDate.contains(":")) {
                item.getArticle().setStartDate(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
            }
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsQuixService.getCurrency().name());
        bodyJson.put("merchantId", jsQuixService.getMerchantId());
        bodyJson.put("productId", jsQuixService.getProductId());
        bodyJson.put("country", jsQuixService.getCountry());
        bodyJson.put("paymentSolution", jsQuixService.getPaymentSolution());
        bodyJson.put("customerId", jsQuixService.getCustomerId());
        bodyJson.put("statusURL", jsQuixService.getStatusURL());
        bodyJson.put("successURL", jsQuixService.getSuccessURL());
        bodyJson.put("errorURL", jsQuixService.getErrorURL());
        bodyJson.put("cancelURL", jsQuixService.getCancelURL());
        bodyJson.put("awaiting", jsQuixService.getAwaitingURL());
        bodyJson.put("amount", jsQuixService.getAmount());
        bodyJson.put("firstName", jsQuixService.getFirstName());
        bodyJson.put("lastName", jsQuixService.getLastName());
        bodyJson.put("customerEmail", jsQuixService.getCustomerEmail());
        bodyJson.put("customerCountry", jsQuixService.getCustomerCountry());
        bodyJson.put("dob", jsQuixService.getDob());
        bodyJson.put("merchantTransactionId", jsQuixService.getMerchantTransactionId());
        if (jsQuixService.getMerchantParameters() != null && !jsQuixService.getMerchantParameters().isEmpty()) {
            bodyJson.put("merchantParams", Utils.getInstance().merchantParamsQuery(jsQuixService.getMerchantParameters()));
        }

        String paysolExtendedData = gson.toJson(jsQuixService.getPaySolExtendedData());
        bodyJson.put("paysolExtendedData", paysolExtendedData);
        bodyJson.put("apiVersion", String.valueOf(jsQuixService.getApiVersion()));


        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsQuixService.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsQuixService.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String rawResponse = responseBody.string();
                        Notification notification = NotificationAdapter.parseNotification(rawResponse);
                        responseListener.onResponseReceived(rawResponse, notification, notification.getTransactionResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendJSQuixFlightRequest(JSQuixFlight jsQuixFlight, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = jsQuixFlight.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsQuixFlight.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = jsQuixFlight.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(jsQuixFlight.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        jsQuixFlight.setAmount(parsedAmount);

        List<QuixItemCartItemFlight> items = jsQuixFlight.getPaySolExtendedData().getCart().getItems();
        for (QuixItemCartItemFlight item : items) {
            String customerMemberSince = item.getArticle().getCustomerMemberSince();
            String departureDate = item.getArticle().getDepartureDate();
            if (customerMemberSince.contains(":")) {
                item.getArticle().setCustomerMemberSince(URLEncoder.encode(customerMemberSince, StandardCharsets.UTF_8));
            }
            if (departureDate.contains(":")) {
                item.getArticle().setDepartureDate(URLEncoder.encode(departureDate, StandardCharsets.UTF_8));
            }
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsQuixFlight.getCurrency().name());
        bodyJson.put("merchantId", jsQuixFlight.getMerchantId());
        bodyJson.put("productId", jsQuixFlight.getProductId());
        bodyJson.put("country", jsQuixFlight.getCountry());
        bodyJson.put("paymentSolution", jsQuixFlight.getPaymentSolution());
        bodyJson.put("customerId", jsQuixFlight.getCustomerId());
        bodyJson.put("statusURL", jsQuixFlight.getStatusURL());
        bodyJson.put("successURL", jsQuixFlight.getSuccessURL());
        bodyJson.put("errorURL", jsQuixFlight.getErrorURL());
        bodyJson.put("cancelURL", jsQuixFlight.getCancelURL());
        bodyJson.put("awaitingURL", jsQuixFlight.getAwaitingURL());
        bodyJson.put("amount", jsQuixFlight.getAmount());
        bodyJson.put("firstName", jsQuixFlight.getFirstName());
        bodyJson.put("lastName", jsQuixFlight.getLastName());
        bodyJson.put("customerEmail", jsQuixFlight.getCustomerEmail());
        bodyJson.put("customerCountry", jsQuixFlight.getCustomerCountry());
        bodyJson.put("dob", jsQuixFlight.getDob());
        bodyJson.put("merchantTransactionId", jsQuixFlight.getMerchantTransactionId());
        if (jsQuixFlight.getMerchantParameters() != null && !jsQuixFlight.getMerchantParameters().isEmpty()) {
            bodyJson.put("merchantParams", Utils.getInstance().merchantParamsQuery(jsQuixFlight.getMerchantParameters()));
        }

        String paysolExtendedData = gson.toJson(jsQuixFlight.getPaySolExtendedData());
        bodyJson.put("paysolExtendedData", paysolExtendedData);
        bodyJson.put("apiVersion", String.valueOf(jsQuixFlight.getApiVersion()));


        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsQuixFlight.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsQuixFlight.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String rawResponse = responseBody.string();
                        Notification notification = NotificationAdapter.parseNotification(rawResponse);
                        responseListener.onResponseReceived(rawResponse, notification, notification.getTransactionResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendJSQuixAccommodationRequest(JSQuixAccommodation jsQuixAccommodation, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = jsQuixAccommodation.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsQuixAccommodation.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = jsQuixAccommodation.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(jsQuixAccommodation.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        jsQuixAccommodation.setAmount(parsedAmount);

        List<QuixItemCartItemAccommodation> items = jsQuixAccommodation.getPaySolExtendedData().getCart().getItems();
        for (QuixItemCartItemAccommodation item : items) {
            String checkinDate = item.getArticle().getCheckinDate();
            String checkoutDate = item.getArticle().getCheckoutDate();
            if (checkinDate.contains(":")) {
                item.getArticle().setCheckinDate(URLEncoder.encode(checkinDate, StandardCharsets.UTF_8));
            }
            if (checkoutDate.contains(":")) {
                item.getArticle().setCheckoutDate(URLEncoder.encode(checkoutDate, StandardCharsets.UTF_8));
            }
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsQuixAccommodation.getCurrency().name());
        bodyJson.put("merchantId", jsQuixAccommodation.getMerchantId());
        bodyJson.put("productId", jsQuixAccommodation.getProductId());
        bodyJson.put("country", jsQuixAccommodation.getCountry());
        bodyJson.put("paymentSolution", jsQuixAccommodation.getPaymentSolution());
        bodyJson.put("customerId", jsQuixAccommodation.getCustomerId());
        bodyJson.put("statusURL", jsQuixAccommodation.getStatusURL());
        bodyJson.put("successURL", jsQuixAccommodation.getSuccessURL());
        bodyJson.put("errorURL", jsQuixAccommodation.getErrorURL());
        bodyJson.put("cancelURL", jsQuixAccommodation.getCancelURL());
        bodyJson.put("awaitingURL", jsQuixAccommodation.getAwaitingURL());
        bodyJson.put("amount", jsQuixAccommodation.getAmount());
        bodyJson.put("firstName", jsQuixAccommodation.getFirstName());
        bodyJson.put("lastName", jsQuixAccommodation.getLastName());
        bodyJson.put("customerEmail", jsQuixAccommodation.getCustomerEmail());
        bodyJson.put("customerCountry", jsQuixAccommodation.getCustomerCountry());
        bodyJson.put("dob", jsQuixAccommodation.getDob());
        bodyJson.put("merchantTransactionId", jsQuixAccommodation.getMerchantTransactionId());
        if (jsQuixAccommodation.getMerchantParameters() != null && !jsQuixAccommodation.getMerchantParameters().isEmpty()) {
            bodyJson.put("merchantParams", Utils.getInstance().merchantParamsQuery(jsQuixAccommodation.getMerchantParameters()));
        }

        String paysolExtendedData = gson.toJson(jsQuixAccommodation.getPaySolExtendedData());
        bodyJson.put("paysolExtendedData", paysolExtendedData);
        bodyJson.put("apiVersion", String.valueOf(jsQuixAccommodation.getApiVersion()));


        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsQuixAccommodation.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsQuixAccommodation.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String rawResponse = responseBody.string();
                        Notification notification = NotificationAdapter.parseNotification(rawResponse);
                        responseListener.onResponseReceived(rawResponse, notification, notification.getTransactionResult());

                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendJSQuixItemRequest(JSQuixItem jsQuixItem, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = jsQuixItem.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsQuixItem.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = jsQuixItem.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(jsQuixItem.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        jsQuixItem.setAmount(parsedAmount);

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsQuixItem.getCurrency().name());
        bodyJson.put("merchantId", jsQuixItem.getMerchantId());
        bodyJson.put("productId", jsQuixItem.getProductId());
        bodyJson.put("country", jsQuixItem.getCountry());
        bodyJson.put("paymentSolution", jsQuixItem.getPaymentSolution());
        bodyJson.put("customerId", jsQuixItem.getCustomerId());
        bodyJson.put("statusURL", jsQuixItem.getStatusURL());
        bodyJson.put("successURL", jsQuixItem.getSuccessURL());
        bodyJson.put("errorURL", jsQuixItem.getErrorURL());
        bodyJson.put("cancelURL", jsQuixItem.getCancelURL());
        bodyJson.put("awaiting", jsQuixItem.getAwaitingURL());
        bodyJson.put("amount", jsQuixItem.getAmount());
        bodyJson.put("firstName", jsQuixItem.getFirstName());
        bodyJson.put("lastName", jsQuixItem.getLastName());
        bodyJson.put("customerEmail", jsQuixItem.getCustomerEmail());
        bodyJson.put("customerCountry", jsQuixItem.getCustomerCountry());
        bodyJson.put("dob", jsQuixItem.getDob());
        bodyJson.put("merchantTransactionId", jsQuixItem.getMerchantTransactionId());
        if (jsQuixItem.getMerchantParameters() != null && !jsQuixItem.getMerchantParameters().isEmpty()) {
            bodyJson.put("merchantParams", Utils.getInstance().merchantParamsQuery(jsQuixItem.getMerchantParameters()));
        }

        String paysolExtendedData = gson.toJson(jsQuixItem.getPaySolExtendedData());
        bodyJson.put("paysolExtendedData", paysolExtendedData);
        bodyJson.put("apiVersion", String.valueOf(jsQuixItem.getApiVersion()));


        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsQuixItem.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsQuixItem.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String rawResponse = responseBody.string();
                        Notification notification = NotificationAdapter.parseNotification(rawResponse);
                        responseListener.onResponseReceived(rawResponse, notification, notification.getTransactionResult());

                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }
}
