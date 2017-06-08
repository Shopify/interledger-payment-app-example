package com.shopify.paymentapp;

import com.google.gson.annotations.Expose;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by krystosterone on 2017-06-02.
 */

public class Quote {
    private final String spspEndpoint;
    private final double destinationAmount;

    public Quote(String spspEndpoint, double destinationAmount) {
        this.spspEndpoint = spspEndpoint;
        this.destinationAmount = destinationAmount;
    }

    public Response execute() throws JSONException, IOException {
        // It this really the way we should resolve the Quoting and Payments API URLs?
        // Should we have done a webfinger request to get those?
        // See: https://interledger.org/rfcs/0009-simple-payment-setup-protocol/#appendix-a-optional-webfinger-discovery
        String url = Credentials.LEDGER_URL + "/api/payments/quote";

        JSONObject payload = new JSONObject();

        // This is not a real spspEndpoint
        // PaymentRequest is actually passing a destination user address
        // This is because of how the ilp-kit was implemented
        payload.put("destination", this.spspEndpoint);
        payload.put("destinationAmount", this.destinationAmount);

        HashMap<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Authorization", "Basic " + Credentials.LEDGER_AUTH_TOKEN);

        return new JsonRequest<Response>(url, "POST", payload, requestHeaders).execute(Response.class);
    }

    public class Response {
        @Expose
        public String id;

        @Expose
        public String sourceAmount;

        @Expose
        public String destinationAmount;

        @Expose
        public String destinationAccount;

        @Expose
        public String connectorAccount;

        @Expose
        public String sourceExpiryDuration;

        @Expose
        public SpspEndpoint.Response spsp;
    }
}