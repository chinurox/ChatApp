package com.example.gargc.chatapp1;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Messages> mMessageList;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();;

    public MessageAdapter(List<Messages>mMessageList){
        this.mMessageList=mMessageList;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        String currentUserId=mAuth.getCurrentUser().getUid();
        Messages c=mMessageList.get(position);

        String from_user=c.getFrom();

        if(from_user.equals(currentUserId)){
            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);

        }else
        {
            holder.messageText.setBackgroundResource(R.drawable.drawable);
            holder.messageText.setTextColor(Color.WHITE);

        }
        holder.messageText.setText(c.getMessage());


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView messageText;
        CircleImageView profileImage;
        public MessageViewHolder(View view) {
            super(view);

            messageText=(TextView) view.findViewById(R.id.message_text_layout);
            profileImage=(CircleImageView) view.findViewById(R.id.message_profile_layout);

        }
    }
}
