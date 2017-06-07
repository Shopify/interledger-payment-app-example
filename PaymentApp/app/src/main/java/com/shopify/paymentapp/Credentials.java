package com.shopify.paymentapp;

/**
 * Created by krystosterone on 2017-06-07.
 */

public enum Credentials {
    LEDGER_URL("https://red.ilpdemo.org"),
    LEDGER_ILP("alice@red.ilpdemo.org"),
    LEDGER_AUTH_TOKEN("YWxpY2U6YWxpY2U="); // base64(alice:alice)

    private final String value;

    Credentials(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
