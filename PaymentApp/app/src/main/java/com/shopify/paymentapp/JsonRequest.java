package com.shopify.paymentapp;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by krystosterone on 2017-06-07.
 */

public class JsonRequest<T> {
    private final String url;
    private final String requestMethod;
    private final JSONObject payload;
    private final Map<String, String> requestHeaders;

    public JsonRequest(String url, String requestMethod) {
        this(url, requestMethod, null, null);
    }

    public JsonRequest(String url, String requestMethod, JSONObject payload, Map<String, String> requestHeaders) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.payload = payload;
        this.requestHeaders = requestHeaders;
    }

    public T execute(Class<T> responseClass) throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(this.requestMethod);
        connection.setRequestProperty("Content-Type", "application/json");

        if (requestHeaders != null) {
            for (Map.Entry<String, String> entry : this.requestHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (this.payload != null) {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(this.payload.toString());
            writer.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        reader.close();

        return new Gson().fromJson(stringBuilder.toString(), responseClass);
    }
}
