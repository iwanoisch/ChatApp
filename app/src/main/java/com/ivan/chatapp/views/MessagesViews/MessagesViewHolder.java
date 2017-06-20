package com.ivan.chatapp.views.MessagesViews;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivan.chatapp.R2;
import com.ivan.chatapp.entities.Message;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesViewHolder extends RecyclerView.ViewHolder{

    @BindView(R2.id.list_messages_friendPicture)
    ImageView mFriendPicture;

    @BindView(R2.id.list_messages_userPicture)
    ImageView mUserPicture;

    @BindView(R2.id.list_messages_userText)
    TextView mUserText;

    @BindView(R2.id.list_messages_friendText)
    TextView mFriendText;



    public MessagesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void populate(Context context, Message message, String currentUserEmail){
        if (!currentUserEmail.equals(message.getMessageSenderEmail())){
            mUserPicture.setVisibility(View.GONE);
            mUserText.setVisibility(View.GONE);
            mFriendPicture.setVisibility(View.VISIBLE);
            mFriendText.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(message.getMessageSenderPicture())
                    .into(mFriendPicture);
            mFriendText.setText(message.getMessageText());
        }else {
            mUserPicture.setVisibility(View.VISIBLE);
            mUserText.setVisibility(View.VISIBLE);
            mFriendPicture.setVisibility(View.GONE);
            mFriendText.setVisibility(View.GONE);

            Picasso.with(context)
                    .load(message.getMessageSenderPicture())
                    .into(mUserPicture);
            mUserText.setText(message.getMessageText());
        }


    }

}
