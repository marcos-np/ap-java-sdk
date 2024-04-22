package com.mp.javaPaymentSDK.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import kotlin.Pair;
import org.apache.commons.validator.routines.UrlValidator;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Generic Utils Class
 */

public class Utils {

    private static final Utils utils= new Utils();

    private final static int RANDOM_NUMBER_SIZE = 8;

    private final static String amountFormat = "0.0000";

    private Utils() {
    }
    public static Utils getInstance(){
        return utils;
    }

    /**
     *
     * @param clazz The class type that will be converted to query
     * @param object The object that will be converted
     * @return Query string of the object
     */
    public String buildQuery(Class clazz, Object object) {
        String queryString = "";

        try {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.get(object) != null) {
                    String value = f.get(object).toString();
                    if (f.getName().equalsIgnoreCase("merchantParams")) {
                        value = merchantParamsQuery((List<Pair<String, String>>) f.get(object));
                    }
                    queryString = queryString.concat(f.getName() + "=" + value + "&");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryString.substring(0, queryString.length() - 1);
    }

    public String encodeUrl(String httpQuery) {
        return URLEncoder.encode(httpQuery, StandardCharsets.UTF_8).replace("%3D", "=").replace("%26", "&").replace("%3B", ";").replace("%3A", ":");
    }

    public String merchantParamsQuery(List<Pair<String, String>> merchantParams) {
        StringBuilder merchantParamsQuery = new StringBuilder();
        for (Pair<String, String> parameter : merchantParams) {
            merchantParamsQuery.append(parameter.getFirst());
            merchantParamsQuery.append(":");
            merchantParamsQuery.append(parameter.getSecond());
            merchantParamsQuery.append(";");
        }
        return merchantParamsQuery.substring(0, merchantParamsQuery.length() - 1);
    }

    /**
     * Checks for null variables in a class
     * @param clazz The class type that will be converted to query
     * @param object The object that will be checked
     * @param mandatoryFieldsList The fields that will be checked
     * @return Pair of Boolean and String
     * Boolean: if there is missing field
     * String: the name of the missing field
     */
    public Pair<Boolean, String> containsNull(Class clazz, Object object, List<String> mandatoryFieldsList) {
        ArrayList<String> mandatoryFields = new ArrayList<>(mandatoryFieldsList);
        try {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (mandatoryFields.contains(f.getName())) {
                    mandatoryFields.remove(f.getName());
                    if (f.get(object) == null) {
                        return new Pair<>(true, "Missing " + f.getName());
                    }
                }
            }
            if(!mandatoryFields.isEmpty()) {
                return new Pair<>(true, "Missing " + mandatoryFields.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(true, "An Exception Occurred");
        }

        return new Pair<>(false, null);
    }

    /**
     * Copy an array to another array
     * @param source Source array that will be copied from
     * @param sourceStartIdx Start index in the source array
     * @param sourceEndIdx End index in the source array
     * @param destination Destination array that will contain the result
     * @param destinationStartIdx Start index in the destination array
     */
    public void arrayCopy(
            byte[] source,
            int sourceStartIdx,
            int sourceEndIdx,
            byte[] destination,
            int destinationStartIdx
    ) {
        System.arraycopy(source, sourceStartIdx, destination, destinationStartIdx, sourceEndIdx - sourceStartIdx);
    }

    /**
     * Print map in a beuty form
     * @param mp Map to be printed
     */
    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    public boolean isValidURL(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    public String generateRandomNumber() {
        Random random = new Random();
        int leftLimit = 48; // numeral '0'
        int rightLimit = 57; // letter '9'

        return random.ints(leftLimit, rightLimit + 1)
                .limit(RANDOM_NUMBER_SIZE)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public String parseAmount(String amount) {
        if (amount.contains(",")) {
            return null;
        }
        try {
            double doubleAmount = Double.parseDouble(amount);
            if (doubleAmount < 0) {
                return null;
            }
            NumberFormat numberFormat = new DecimalFormat(amountFormat);
            return numberFormat.format(doubleAmount);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
