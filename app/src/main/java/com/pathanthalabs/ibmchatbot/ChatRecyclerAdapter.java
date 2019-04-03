package com.pathanthalabs.ibmchatbot;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int viewState = 100;
    private ArrayList<Message> messageArrayList;


    ChatRecyclerAdapter(ArrayList<Message> messageArrayList) {
        this.messageArrayList=messageArrayList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        if (viewType == viewState) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_chat_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.watson_chat_item, parent, false);
        }


        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getId()!=null && message.getId().equals("1")) {
            return viewState;
        }
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        message.setMessage(message.getMessage());
        ((ViewHolder) holder).message.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;

        ViewHolder(View view) {
            super(view);
            message =itemView.findViewById(R.id.message);
        }
    }


}
