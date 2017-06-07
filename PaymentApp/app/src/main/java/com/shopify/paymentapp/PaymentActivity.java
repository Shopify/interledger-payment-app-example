package com.shopify.paymentapp;

import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    public static String PAYMENT_METHOD_IDENTIFIER = "interledger";

    private final Handler mHandler = new Handler();

    private boolean mError;
    private double totalAmount;
    private String spspEndpoint;
    private Quote.Response quoteResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // I'm sorry
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_payment);
        findViewById(R.id.continue_button).setOnClickListener(this);

        Intent callingIntent = getIntent();
        if (null == callingIntent) {
            showError("Calling intent is null.");
            return;
        }

        Bundle extras = callingIntent.getExtras();
        if (extras == null) {
            showError("Calling intent contains no extras.");
            return;
        }

        String details = extras.getString("details");
        if (TextUtils.isEmpty(details)) {
            showError("No payment method details in the extras.");
            return;
        }

        JSONObject detailsJson = null;
        try {
            detailsJson = new JSONObject(details);
        } catch (JSONException e) {
            showError("Cannot parse the payment method details into JSON.");
            return;
        }

        JSONArray displayItems = detailsJson.optJSONArray("displayItems");
        JSONObject total = detailsJson.optJSONObject("total");
        if (total == null) {
            showError("Total is not specified.");
            return;
        }

        try {
            totalAmount = total.getJSONObject("amount").optDouble("value");
        } catch (JSONException e) {
            showError("Total amount is not specified.");
            return;
        }

        LinearLayout container = (LinearLayout) findViewById(R.id.line_items);
        if (displayItems != null) {
            for (int i = 0; i < displayItems.length(); i++) {
                if (!addItem(container, displayItems.optJSONObject(i))) {
                    showError("Invalid shopping cart item.");
                }
            }
        }

        if (!addItem(container, total)) showError("Invalid total.");

        String data = extras.getString("data");
        if (data == null) {
            showError("No payment method data available.");
            return;
        }

        JSONObject dataJson;
        try {
            dataJson = new JSONObject(data);
        } catch (JSONException e) {
            showError("Cannot parse the payment method data into JSON.");
            return;
        }

        // This is not a real SPSP Endpoint, rather a the destination's user address
        spspEndpoint = dataJson.optString("spspEndpoint");
        if (spspEndpoint == null) {
            showError("No spspEndpoint specified in payment method data.");
            return;
        }

        try {
            quoteResponse = new Quote(spspEndpoint, totalAmount).execute();

            addItem(container, "---");
            addItem(container, String.format("Destination Account %s", quoteResponse.destinationAccount));
            addItem(container, String.format("Source Amount %s", quoteResponse.sourceAmount));
            addItem(container, String.format("Destination Amount %s", quoteResponse.destinationAmount));
        } catch (JSONException e) {
            showError("Error in retrieving quote.");
            return;
        } catch (IOException e) {
            showError("Error in retrieving quote.");
            return;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO: Return error payload rather than empty intent
        if (mError) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Payment.Response paymentResponse;
        try {
            paymentResponse = new Payment(UUID.randomUUID(), spspEndpoint, totalAmount, quoteResponse).execute();
        } catch (IOException e) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        } catch (JSONException e) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Intent result = new Intent();
        Bundle details = new Bundle();
        details.putString("payeeAddress", paymentResponse.quote.destinationAccount);
        details.putString("address", paymentResponse.quote.destinationAmount);
        details.putString("fulfillment", "TODO"); // Not sure what this is

        Bundle extras = new Bundle();
        extras.putString("methodName", PAYMENT_METHOD_IDENTIFIER);
        extras.putBundle("instrumentDetails", details);

        result.putExtras(extras);
        setResult(RESULT_OK, result);
        finish();
    }

    private boolean addItem(LinearLayout container, JSONObject item) {
        if (item == null) return false;

        JSONObject amount = item.optJSONObject("amount");
        if (amount == null) return false;

        addItem(container, String.format("%s %s %s", item.optString("label"),
                amount.optString("currency"), amount.optString("value")));
        return true;
    }

    private void addItem(LinearLayout container, String text) {
        TextView line = new TextView(this);
        line.setText(text);
        container.addView(line);
    }

    private void showError(String error) {
        mError = true;
        ((TextView) findViewById(R.id.error_message)).setText(error);
    }
}
