package com.mp.javaPaymentSDK.models.quix_models.quix_flight;

import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixFlightPaySolExtendedData {

    private String product;
    private QuixBilling billing;
    private QuixCartFlight cart;
    private boolean disableFormEdition = false;

    public QuixFlightPaySolExtendedData() {
    }

    public QuixFlightPaySolExtendedData(String product, QuixBilling billing, QuixCartFlight cart, boolean disableFormEdition) {
        this.product = product;
        this.billing = billing;
        this.cart = cart;
        this.disableFormEdition = disableFormEdition;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public QuixBilling getBilling() {
        return billing;
    }

    public void setBilling(QuixBilling billing) {
        this.billing = billing;
    }

    public QuixCartFlight getCart() {
        return cart;
    }

    public void setCart(QuixCartFlight cart) {
        this.cart = cart;
    }

    public boolean isDisableFormEdition() {
        return disableFormEdition;
    }

    public void setDisableFormEdition(boolean disableFormEdition) {
        this.disableFormEdition = disableFormEdition;
    }

    public Pair<Boolean, String> isMissingField() {
        List<String> mandatoryFields = Arrays.asList(
                "product", "billing", "cart"
        );

        Pair<Boolean, String> missingField = Utils.getInstance().containsNull(
                this.getClass(), this, mandatoryFields
        );
        if (missingField.getFirst()) {
            return missingField;
        }
        missingField = billing.isMissingField();
        if (missingField.getFirst()) {
            return missingField;
        }
        return cart.isMissingField();
    }
}
