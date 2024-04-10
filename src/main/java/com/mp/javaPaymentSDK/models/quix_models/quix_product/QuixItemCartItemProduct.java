package com.mp.javaPaymentSDK.models.quix_models.quix_product;

import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct;
import kotlin.Pair;

public class QuixItemCartItemProduct {

    private QuixArticleProduct article = null;
    private int units;
    private double total_price_with_tax;
    private boolean auto_shipping = true;

    public QuixItemCartItemProduct() {
    }

    public QuixItemCartItemProduct(QuixArticleProduct article, int units, double total_price_with_tax, boolean auto_shipping) {
        this.article = article;
        this.units = units;
        this.total_price_with_tax = total_price_with_tax;
        this.auto_shipping = auto_shipping;
    }

    public QuixArticleProduct getArticle() {
        return article;
    }

    public void setArticle(QuixArticleProduct article) {
        this.article = article;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getTotal_price_with_tax() {
        return total_price_with_tax;
    }

    public void setTotal_price_with_tax(double total_price_with_tax) {
        this.total_price_with_tax = total_price_with_tax;
    }

    public boolean isAuto_shipping() {
        return auto_shipping;
    }

    public void setAuto_shipping(boolean auto_shipping) {
        this.auto_shipping = auto_shipping;
    }

    public Pair<Boolean, String> isMissingFields() {
        if (units <= 0) {
            return new Pair<>(true, "Missing units");
        }
        if (total_price_with_tax <= 0) {
            return new Pair<>(true, "Missing total_price_with_tax");
        }
        if (article == null) {
            return new Pair<>(true, "Missing article");
        }

        return article.isMissingFields();
    }
}
