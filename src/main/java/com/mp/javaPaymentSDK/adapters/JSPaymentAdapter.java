package com.mp.javaPaymentSDK.adapters;

import com.google.gson.Gson;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.enums.Endpoints;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.requests.js.JSPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.callbacks.JSPaymentListener;
import com.mp.javaPaymentSDK.callbacks.RequestListener;
import com.mp.javaPaymentSDK.enums.TransactionResult;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.js.JSAuthorizationRequest;
import com.mp.javaPaymentSDK.models.requests.js.JSCharge;
import com.mp.javaPaymentSDK.models.responses.JSAuthorizationResponse;
import kotlin.Pair;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class JSPaymentAdapter {

    private Credentials credentials;


    private final NetworkAdapter networkAdapter = new NetworkAdapter();

    public JSPaymentAdapter(Credentials credentials) {
        this.credentials = credentials;
    }

    public void sendJSAuthorizationRequest(JSAuthorizationRequest jsAuthorizationRequest, JSPaymentListener jsPaymentListener) {

        Pair<Boolean, String> isMissingCred = jsAuthorizationRequest.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            jsPaymentListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsAuthorizationRequest.setCredentials(credentials);

        String endpoint = Endpoints.AUTH_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> isMissingField = jsAuthorizationRequest.isMissingField();
        if (isMissingField.getFirst()) {
            jsPaymentListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsAuthorizationRequest.getCurrency().name());
        bodyJson.put("merchantId", jsAuthorizationRequest.getMerchantId());
        bodyJson.put("merchantKey", credentials.getMerchantKey());
        bodyJson.put("productId", jsAuthorizationRequest.getProductId());
        bodyJson.put("country", jsAuthorizationRequest.getCountry());
        bodyJson.put("customerId", jsAuthorizationRequest.getCustomerId());
        bodyJson.put("operationType", jsAuthorizationRequest.getOperationType());
        bodyJson.put("apiVersion", String.valueOf(jsAuthorizationRequest.getApiVersion()));

        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        networkAdapter.sendRequest(null, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in JS Payment - " + error.getMessage() + " - " + errorMessage);
                jsPaymentListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String rawResponse = responseBody.string();
                        Gson gson = new Gson();
                        JSAuthorizationResponse jsAuthorizationResponse
                                = gson.fromJson(rawResponse, JSAuthorizationResponse.class);
                        if (jsAuthorizationResponse.getAuthToken() != null) {
                            jsPaymentListener.onAuthorizationResponseReceived(rawResponse, jsAuthorizationResponse);
                        }
                        else {
                            jsPaymentListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        jsPaymentListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    jsPaymentListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    jsPaymentListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendJSChargeRequest(JSCharge jsCharge, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = jsCharge.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsCharge.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> isMissingField = jsCharge.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsCharge.getCurrency().name());
        bodyJson.put("merchantId", jsCharge.getMerchantId());
        bodyJson.put("productId", jsCharge.getProductId());
        bodyJson.put("country", jsCharge.getCountry());
        bodyJson.put("customerId", jsCharge.getCustomerId());
        bodyJson.put("operationType", jsCharge.getOperationType());
        bodyJson.put("amount", jsCharge.getAmount());
        bodyJson.put("merchantTransactionId", jsCharge.getMerchantTransactionId());
        bodyJson.put("statusURL", jsCharge.getStatusURL());
        bodyJson.put("successURL", jsCharge.getSuccessURL());
        bodyJson.put("errorURL", jsCharge.getErrorURL());
        bodyJson.put("cancelURL", jsCharge.getCancelURL());
        bodyJson.put("awaitingURL", jsCharge.getAwaitingURL());
        bodyJson.put("paymentSolution", jsCharge.getPaymentSolution());
        bodyJson.put("apiVersion", String.valueOf(jsCharge.getApiVersion()));

        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsCharge.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsCharge.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint,  new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in JS Payment - " + error.getMessage() + " - " + errorMessage);
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
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendJSPaymentRecurrentInitial(JSPaymentRecurrentInitial jsPaymentRecurrentInitial, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = jsPaymentRecurrentInitial.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        jsPaymentRecurrentInitial.setCredentials(credentials);

        String endpoint = Endpoints.CHARGE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> isMissingField = jsPaymentRecurrentInitial.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("currency", jsPaymentRecurrentInitial.getCurrency().name());
        bodyJson.put("merchantId", jsPaymentRecurrentInitial.getMerchantId());
        bodyJson.put("productId", jsPaymentRecurrentInitial.getProductId());
        bodyJson.put("country", jsPaymentRecurrentInitial.getCountry());
        bodyJson.put("customerId", jsPaymentRecurrentInitial.getCustomerId());
        bodyJson.put("operationType", jsPaymentRecurrentInitial.getOperationType());
        bodyJson.put("amount", jsPaymentRecurrentInitial.getAmount());
        bodyJson.put("merchantTransactionId", jsPaymentRecurrentInitial.getMerchantTransactionId());
        bodyJson.put("paymentSolution", jsPaymentRecurrentInitial.getPaymentSolution());
        bodyJson.put("challengeInd", jsPaymentRecurrentInitial.getChallengeInd());
        bodyJson.put("paymentRecurringType", jsPaymentRecurrentInitial.getPaymentRecurringType());
        bodyJson.put("apiVersion", String.valueOf(jsPaymentRecurrentInitial.getApiVersion()));

        RequestBody requestBody = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        HashMap<String, String> headers = new HashMap<>();
        headers.put("prepayToken", jsPaymentRecurrentInitial.getPrepayToken());
        headers.put("apiVersion", String.valueOf(jsPaymentRecurrentInitial.getApiVersion()));

        networkAdapter.sendRequest(headers, null, requestBody, endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in JS Payment - " + error.getMessage() + " - " + errorMessage);
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
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }
}
