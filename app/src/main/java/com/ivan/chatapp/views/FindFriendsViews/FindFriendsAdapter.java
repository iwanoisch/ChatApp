package com.ivan.chatapp.views.FindFriendsViews;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivan.chatapp.R;
import com.ivan.chatapp.activities.BaseFragmentActivity;
import com.ivan.chatapp.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter {

    private BaseFragmentActivity mActivity;
    private List<User> mUsers;
    private LayoutInflater mInflater;
    private UserListener mListener;

    private HashMap<String,User> mFriendRequestSentMap;
    private HashMap<String,User> mFriendRequestRecivedMap;
    private HashMap<String,User> mCurrentUserFriendsMap;

    public FindFriendsAdapter(BaseFragmentActivity mActivity, UserListener mListener) {
        this.mActivity = mActivity;
        this.mListener = mListener;
        mInflater = mActivity.getLayoutInflater();
        mUsers = new ArrayList<>();
        mFriendRequestSentMap = new HashMap<>();
        mFriendRequestRecivedMap = new HashMap<>();
        mCurrentUserFriendsMap = new HashMap<>();
    }


    public void setmUsers(List<User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public void setmFriendRequestSentMap(HashMap<String, User> friendRequestSentMap) {
        mFriendRequestSentMap.clear();
        mFriendRequestSentMap.putAll(friendRequestSentMap);
        notifyDataSetChanged();

    }

    public void setmCurrentUserFriendsMap(HashMap<String, User> currentUserFriendsMap) {
        mCurrentUserFriendsMap.clear();
        mCurrentUserFriendsMap.putAll(currentUserFriendsMap);
        notifyDataSetChanged();
    }

    public void setmFriendRequestRecivedMap(HashMap<String, User> friendRequestRecivedMap) {
        mFriendRequestRecivedMap.clear();
        mFriendRequestRecivedMap.putAll(friendRequestRecivedMap);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View userView = mInflater.inflate(R.layout.list_user,parent,false);
        final FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(userView);

        findFriendsViewHolder.mAddFriend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
             User user =(User) findFriendsViewHolder.itemView.getTag();
                mListener.onUserClicked(user);
            }
        });

        return findFriendsViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((FindFriendsViewHolder) holder).populate(mActivity,mUsers.get(position)
                ,mFriendRequestSentMap,mFriendRequestRecivedMap,mCurrentUserFriendsMap);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public interface UserListener{
        void onUserClicked(User user);
    }
}
