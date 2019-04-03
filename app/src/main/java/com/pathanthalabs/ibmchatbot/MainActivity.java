package com.pathanthalabs.ibmchatbot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatRecyclerAdapter adapter;
    private ArrayList<Message> messageList;
    private EditText userInput;
    private boolean firstRequest;
    private Map<String, Object> hashMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.editText);
        ImageButton btnSend = findViewById(R.id.sendBtn);

        recyclerView = findViewById(R.id.recyclerView);

        messageList = new ArrayList();
        adapter = new ChatRecyclerAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        userInput.setText("");
        firstRequest = true;
        sendMessage();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    sendMessage();
                }
            }
        });
    }

    private void sendMessage() {

        final com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;
        final String inputmessage = this.userInput.getText().toString().trim();
        if (!firstRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            firstRequest = false;
        }

        userInput.setText("");
        adapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    MessageOptions newMessageOptions;
                    MessageResponse response = new MessageResponse();

                    boolean isFirst = true;

                    Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                    service.setUsernameAndPassword(Utils.USERNAME, Utils.PASSWORD);

                    if (isFirst) {
                        InputData input = new InputData.Builder("Hi").build();
                        MessageOptions options = new MessageOptions.Builder(Utils.WORKSPACE_ID)
                                .input(input).context(context).build();
                        response = service.message(options).execute();

                        // Log.v("First Res", "Res" + response);
                        isFirst = false;
                    }

                    if (response.getContext() != null) {
                        hashMap.clear();
                        hashMap = response.getContext();

                        if (inputmessage.length() > 0) {
                            newMessageOptions = new MessageOptions.Builder()
                                    .workspaceId(Utils.WORKSPACE_ID)
                                    .input(new InputData.Builder(inputmessage).build())
                                    .context(response.getContext()).build();

                            response = service.message(newMessageOptions).execute();
                            //Log.v("First Res1", "Res" + response);
                        }
                    }

                    Message watsonMessage = new Message();
                    if (response != null) {
                        ArrayList responseList = new ArrayList();
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONObject output = jsonResponse.getJSONObject("output");
                        JSONArray textArray = output.getJSONArray("text");
                        for(int i=0;i<textArray.length();i++){
                            responseList.add(textArray.get(i));
                        }
                        if(null!=responseList && responseList.size()>0){
                            for(int i = 0;i<responseList.size();i++){
                                watsonMessage.setMessage((String) responseList.get(i));
                                Log.v("First Res", "JsonText" + responseList.get(i));
                                watsonMessage.setId("2");
                            }
                        }
                        messageList.add(watsonMessage);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                adapter.notifyDataSetChanged();

                                if (adapter.getItemCount() > 1) {

                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " Internet Connection is not available.", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}