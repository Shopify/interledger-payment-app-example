package com.shopify.paymentapp;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by krystosterone on 2017-06-02.
 */

public class Payment {
    private final UUID uuid;
    private final String spspEndpoint;
    private final double totalAmount;
    private final Quote.Response quoteResponse;

    public Payment(UUID uuid, String spspEndpoint, double totalAmount, Quote.Response quoteResponse) {
        this.uuid = uuid;
        this.spspEndpoint = spspEndpoint;
        this.totalAmount = totalAmount;
        this.quoteResponse = quoteResponse;
    }

    // See: https://github.com/interledgerjs/ilp-kit/blob/3079862314f4433e974d14d56b65eafd374fbf5f/api/src/lib/pay.js#L44
    public Response execute() throws IOException, JSONException {
        // It this really the way we should resolve the Quoting and Payments API URLs?
        // Should we have done a webfinger request to get those?
        // See: https://interledger.org/rfcs/0009-simple-payment-setup-protocol/#appendix-a-optional-webfinger-discovery
        String url = Credentials.LEDGER_URL + "/api/payments/" + this.uuid;

        JSONObject destination = new JSONObject();
        // This is not a real spspEndpoint
        // PaymentRequest is actually passing a destination user address
        // This is because of how the ilp-kit was implemented
        destination.put("identifier", this.spspEndpoint);

        JSONObject payload = new JSONObject();
        payload.put("destination", destination);
        payload.put("quote", new JSONObject(new Gson().toJson(this.quoteResponse)));

        HashMap<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Authorization", "Basic " + Credentials.LEDGER_AUTH_TOKEN);

        return new JsonRequest<Response>(url, "PUT", payload, requestHeaders).execute(Response.class);
    }

    public class Response {
        @Expose
        public Destination destination;

        @Expose
        public Quote.Response quote;

        public class Destination {
            @Expose
            public String identifier;
        }
    }
}
