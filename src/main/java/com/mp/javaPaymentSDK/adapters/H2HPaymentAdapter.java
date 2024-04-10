package com.mp.javaPaymentSDK.adapters;

import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.callbacks.RequestListener;
import com.mp.javaPaymentSDK.enums.Endpoints;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.h2h.*;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.HexUtils;
import com.mp.javaPaymentSDK.utils.SecurityUtils;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;
import okhttp3.FormBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class H2HPaymentAdapter {

    private Credentials credentials;

    private final NetworkAdapter networkAdapter = new NetworkAdapter();

    public H2HPaymentAdapter(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void sendH2hPaymentRequest(H2HRedirection h2HRedirection, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = h2HRedirection.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.H2H_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HRedirection.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HRedirection.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HRedirection.class, h2HRedirection);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);


        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HRedirection.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HRedirection.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
                    try {
                        String rawResponse = responseBody.string();
                        Notification notification = NotificationAdapter.parseNotification(rawResponse);
                        responseListener.onResponseReceived(rawResponse, notification, notification.getTransactionResult() );
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

    public void sendH2hPreAuthorizationRequest(H2HPreAuthorization h2HPreAuthorization, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = h2HPreAuthorization.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.H2H_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HPreAuthorization.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HPreAuthorization.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HPreAuthorization.class, h2HPreAuthorization);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HPreAuthorization.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HPreAuthorization.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
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

    public void sendH2hPreAuthorizationCapture(H2HPreAuthorizationCapture h2HPreAuthorizationCapture, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = h2HPreAuthorizationCapture.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.CAPTURE_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HPreAuthorizationCapture.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HPreAuthorizationCapture.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HPreAuthorizationCapture.class, h2HPreAuthorizationCapture);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HPreAuthorizationCapture.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HPreAuthorizationCapture.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage());
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
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

    public void sendH2hPaymentRecurrentInitial(H2HPaymentRecurrentInitial h2HPaymentRecurrentInitial, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = h2HPaymentRecurrentInitial.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.H2H_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HPaymentRecurrentInitial.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HPaymentRecurrentInitial.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HPaymentRecurrentInitial.class, h2HPaymentRecurrentInitial);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HPaymentRecurrentInitial.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HPaymentRecurrentInitial.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
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

    public void sendH2hPaymentRecurrentSuccessive(H2HPaymentRecurrentSuccessive h2HPaymentRecurrentSuccessive, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = h2HPaymentRecurrentSuccessive.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.H2H_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HPaymentRecurrentSuccessive.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HPaymentRecurrentSuccessive.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HPaymentRecurrentSuccessive.class, h2HPaymentRecurrentSuccessive);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HPaymentRecurrentSuccessive.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HPaymentRecurrentSuccessive.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
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

    public void sendH2hVoidRequest(H2HVoid h2HVoid, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = h2HVoid.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.VOID_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HVoid.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HVoid.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HVoid.class, h2HVoid);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HVoid.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HVoid.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
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

    public void sendH2hRefundRequest(H2HRefund h2HRefund, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = h2HRefund.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.REFUND_ENDPOINT.getEndpoint(credentials.getEnvironment());

        h2HRefund.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = h2HRefund.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String httpQuery = Utils.getInstance().buildQuery(H2HRefund.class, h2HRefund);
        String finalQueryParameter = URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&");
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(h2HRefund.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(h2HRefund.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in H2H Payment - " + error.getMessage() + " - " + errorMessage);
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
