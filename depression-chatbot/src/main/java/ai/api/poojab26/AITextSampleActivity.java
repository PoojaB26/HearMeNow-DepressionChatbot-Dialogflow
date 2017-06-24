package ai.api.poojab26;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

/**
 * Created by alexey on 07/12/16.
 */
public class AITextSampleActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = AITextSampleActivity.class.getName();

    private Gson gson = GsonFactory.getGson();

    private TextView resultTextView;
    private EditText queryEditText;
    private AIDataService aiDataService;

    private List<ChatMessage> msgList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;

    ChatMessage chatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_aitext_sample);

        resultTextView = (TextView) findViewById(R.id.resultTextView);
        queryEditText = (EditText) findViewById(R.id.textQuery);

        findViewById(R.id.buttonSend).setOnClickListener(this);

        initService();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new ChatAdapter(msgList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //ChatMessage chatMessage = new ChatMessage();

        //prepareListData();

    }

    private void prepareListData() {
        ChatMessage chatMessage = new ChatMessage(false, "Mad Max");
        msgList.add(chatMessage);

        chatMessage = new ChatMessage(true, "Mad Max 2");
        msgList.add(chatMessage);

        chatMessage = new ChatMessage(true, "Hell Max 2");
        msgList.add(chatMessage);
    }

    private void initService() {

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);


        aiDataService = new AIDataService(this, config);
    }



    /*
    * AIRequest should have query OR event
    */
    private void sendRequest() {

        final String queryString = String.valueOf(queryEditText.getText());
        final String eventString = null;
        final String contextString = "suicide";

        if (TextUtils.isEmpty(queryString)) {
            onError(new AIError(getString(R.string.non_empty_query)));
            return;
        }

        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                String event = params[1];

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query);
                if (!TextUtils.isEmpty(event))
                    request.setEvent(new AIEvent(event));
                final String contextString = params[2];
                RequestExtras requestExtras = null;
                if (!TextUtils.isEmpty(contextString)) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

        task.execute(queryString, eventString, contextString);
    }




    private void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "onResult");

                resultTextView.setText(gson.toJson(response));

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
                chatMessage = new ChatMessage(true, result.getResolvedQuery());
                msgList.add(chatMessage);

                Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                chatMessage = new ChatMessage(false, result.getFulfillment().getSpeech());
                msgList.add(chatMessage);

                TTS.speak(speech);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }

        });
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(error.toString());
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSend:
                sendRequest();
                break;

        }
    }
}