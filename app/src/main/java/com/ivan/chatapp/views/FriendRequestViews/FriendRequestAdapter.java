package com.ivan.chatapp.views.FriendRequestViews;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivan.chatapp.R;
import com.ivan.chatapp.activities.BaseFragmentActivity;
import com.ivan.chatapp.entities.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter{

    private BaseFragmentActivity mActivity;
    private LayoutInflater mInflater;
    private List<User> mUsers;
    private OnOptionListener mListener;

    public FriendRequestAdapter(BaseFragmentActivity mActivity, OnOptionListener mListener) {
        this.mActivity = mActivity;
        this.mListener = mListener;
        mInflater = mActivity.getLayoutInflater();
        mUsers = new ArrayList<>();
    }

    public void setmUsers(List<User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_friend_request,parent,false);
        final FriendRequestViewHolder friendRequestViewHolder = new FriendRequestViewHolder(view);

        friendRequestViewHolder.approveImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                User user = (User) friendRequestViewHolder.itemView.getTag();
                mListener.OnOptionClicked(user, "0");

            }
        });

        friendRequestViewHolder.rejectImageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                User user = (User) friendRequestViewHolder.itemView.getTag();
                mListener.OnOptionClicked(user, "1");
            }
        });

        return friendRequestViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((FriendRequestViewHolder) holder).populate(mActivity,mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public interface OnOptionListener{
        void OnOptionClicked(User user, String result);
    }

}
