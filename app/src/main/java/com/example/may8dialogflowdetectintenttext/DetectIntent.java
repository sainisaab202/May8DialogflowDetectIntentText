package com.example.may8dialogflowdetectintenttext;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.cx.v3beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.cx.v3beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.cx.v3beta1.QueryInput;
import com.google.cloud.dialogflow.cx.v3beta1.QueryResult;
import com.google.cloud.dialogflow.cx.v3beta1.SessionName;
import com.google.cloud.dialogflow.cx.v3beta1.SessionsClient;
import com.google.cloud.dialogflow.cx.v3beta1.SessionsSettings;
import com.google.cloud.dialogflow.cx.v3beta1.TextInput;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import io.grpc.stub.StreamObserver;

public class DetectIntent  implements StreamObserver<DetectIntentResponse> {

    private static String TAG = "DetectIntent";

    Context context;
    String projectId, locationId, agentId, sessionId;

    public DetectIntent(){
        projectId = "xxxxx";
        locationId= "xxxxxxx";
        agentId = "xxxxxxxxxxxxxx";
        sessionId = UUID.randomUUID().toString();
    }

    public Map<String, QueryResult> detectIntent(
            String text,
            String languageCode,
            Context context)
            throws IOException, ApiException {

        this.context = context;

        SessionsSettings.Builder sessionsSettingsBuilder = SessionsSettings.newBuilder();

        if (locationId.equals("global")) {
            sessionsSettingsBuilder.setEndpoint("dialogflow.googleapis.com:443");
        } else {
            sessionsSettingsBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
        }

        InputStream stream = context.getResources().openRawResource(R.raw.credentials);
        GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        SessionsSettings sessionsSettings = sessionsSettingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)).build();

        Map<String, QueryResult> queryResults = Maps.newHashMap();

        // Instantiates a client.
        // Note: close() needs to be called on the SessionsClient object to clean up resources
        // such as threads. In the example below, try-with-resources is used,
        // which automatically calls close().
        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {

            SessionName session = SessionName.of(projectId,locationId,agentId,sessionId);

            Log.e(TAG, session.toString());

            TextInput.Builder textInput = TextInput.newBuilder().setText(text);

            QueryInput queryInput =
                    QueryInput.newBuilder().setText(textInput).setLanguageCode(languageCode).build();

            // Build the DetectIntentRequest with the SessionName and QueryInput.
            DetectIntentRequest request =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .build();

            DetectIntentResponse response = sessionsClient.detectIntent(request);

            // Send the response to the observer
            this.onNext(response);
            this.onCompleted();

            // Display the query result.
            QueryResult queryResult = response.getQueryResult();

            Log.e(TAG, "Query Text: "+queryResult.getText());

            queryResults.put(text, queryResult);
        }
        return queryResults;
    }

    @Override
    public void onNext(DetectIntentResponse value) {
        Log.e("Response", "onNext - response Received "+value.getQueryResult().getResponseMessages(0));

        MainActivity.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, value.getQueryResult().getResponseMessages(0).getText().toString(), Toast.LENGTH_LONG).show();
                MainActivity.tvResponse.setText(value.getQueryResult().getResponseMessages(0).getText().toString());
            }
        });

    }

    @Override
    public void onError(Throwable t) {
        Log.e("Response", "onNext - response onError: "+t.getMessage());
    }

    @Override
    public void onCompleted() {
        Log.e("Response", "onNext - response completed");
    }
}
