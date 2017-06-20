package com.ivan.chatapp.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivan.chatapp.R;
import com.ivan.chatapp.R2;
import com.ivan.chatapp.services.LiveFriendServices;
import com.ivan.chatapp.utils.Constants;
import com.ivan.chatapp.views.FriendsViewPagerAdapter;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FriendFragment extends BaseFragment{
    @BindView(R2.id.bottomBar)
    BottomBar mBottombar;

    @BindView(R2.id.fragment_friends_tabLayout)
    TabLayout mTablayout;

    @BindView(R2.id.fragment_friends_viewPager)
    ViewPager mViewPager;



    private LiveFriendServices mLiveFriendsServices;
    private DatabaseReference mAllFriendRequestsReference;
    private ValueEventListener mAllFriendRequestListener;

    private String mUserEmailString;

    private DatabaseReference mUsersNewMessagesReference;
    private ValueEventListener mUsersNewMessagesListener;



    public static FriendFragment newInstance(){
        return new FriendFragment();
    }


    private Unbinder mUnbinder;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLiveFriendsServices = LiveFriendServices.getInstance();
        mUserEmailString = mSharedPreferences.getString(Constants.USER_EMAIL,"");
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends,container,false);
        mUnbinder = ButterKnife.bind(this,rootView);
        mBottombar.selectTabWithId(R.id.tab_friends);
        setUpBottomBar(mBottombar,2);

        mUsersNewMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_NEW_MESSAGES).child(Constants.encodeEmail(mUserEmailString));
        mUsersNewMessagesListener = mLiveFriendsServices.getAllNewMessages(mBottombar, R.id.tab_messages);

        mUsersNewMessagesReference.addValueEventListener(mUsersNewMessagesListener);

        FriendsViewPagerAdapter friendsViewPagerAdapter = new FriendsViewPagerAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(friendsViewPagerAdapter);
        mTablayout.setupWithViewPager(mViewPager);


        mAllFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUEST_RECIEVED).child(Constants.encodeEmail(mUserEmailString));
        mAllFriendRequestListener = mLiveFriendsServices.getFriendRequestBottom(mBottombar,R.id.tab_friends);
        mAllFriendRequestsReference.addValueEventListener(mAllFriendRequestListener);


        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();


        if(mAllFriendRequestListener!=null){
            mAllFriendRequestsReference.removeEventListener(mAllFriendRequestListener);
        }

        if (mUsersNewMessagesListener!=null){
            mUsersNewMessagesReference.removeEventListener(mUsersNewMessagesListener);
        }
    }
}
