package com.shopify.paymentapp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

/**
 * Created by krystosterone on 2017-06-07.
 */

public class SpspEndpoint {
    private final String endpoint;

    public SpspEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Response execute() throws IOException {
        return new JsonRequest<Response>(this.endpoint, "GET").execute(Response.class);
    }

    public class Response {
        @Expose
        @SerializedName("destination_account")
        public String destinationAccount;

        @Expose
        @SerializedName("shared_secret")
        public String sharedSecret;

        @Expose
        @SerializedName("maximum_destination_amount")
        public String maximumDestinationAmount;

        @Expose
        @SerializedName("minimum_destination_amount")
        public String minimumDestinationAmount;

        @Expose
        @SerializedName("ledger_info")
        public LedgerInfo ledgerInfo;

        @Expose
        @SerializedName("receiver_info")
        public ReceiverInfo receiverInfo;

        public class LedgerInfo {
            @Expose
            @SerializedName("currency_code")
            public String currencyCode;

            @Expose
            @SerializedName("currency_scale")
            public String currencyScale;
        }

        public class ReceiverInfo {
            @Expose
            public String name;

            @Expose
            @SerializedName("image_url")
            public String imageUrl;
        }
    }
}
