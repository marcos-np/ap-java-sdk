package com.mp.javaPaymentSDK.models.quix_models.quix_accommodation;

import com.mp.javaPaymentSDK.models.quix_models.QuixBilling;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;

import java.util.Arrays;
import java.util.List;

public class QuixAccommodationPaySolExtendedData {

    private String product = null;
    private QuixBilling billing = null;
    private QuixCartAccommodation cart = null;
    private boolean disableFormEdition = false;

    public QuixAccommodationPaySolExtendedData() {
    }

    public QuixAccommodationPaySolExtendedData(String product, QuixBilling billing, QuixCartAccommodation cart, boolean disableFormEdition) {
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

    public QuixCartAccommodation getCart() {
        return cart;
    }

    public void setCart(QuixCartAccommodation cart) {
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
