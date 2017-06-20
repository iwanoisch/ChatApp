package com.ivan.chatapp.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivan.chatapp.R;
import com.ivan.chatapp.R2;
import com.ivan.chatapp.activities.BaseFragmentActivity;
import com.ivan.chatapp.entities.User;
import com.ivan.chatapp.services.LiveFriendServices;
import com.ivan.chatapp.utils.Constants;
import com.ivan.chatapp.views.FriendRequestViews.FriendRequestAdapter;

import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.socket.client.IO;
import io.socket.client.Socket;

public class FriendRequestsFragment extends BaseFragment implements FriendRequestAdapter.OnOptionListener{


    @BindView(R2.id.fragment_friend_request_recycleView)
    RecyclerView mRecycleView;

    @BindView(R2.id.fragment_friend_request_message)
    TextView mTexView;



    private LiveFriendServices mLiveFriendsServices;

    private DatabaseReference mGetAllUsersFriendRequestsReference;
    private ValueEventListener mGetAllUsersFriendRequestsListener;

    private Unbinder mUnbinder;

    private String mUserEmailString;

    private Socket mSocket;


    public static FriendRequestsFragment newInstance(){
        return new FriendRequestsFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            mSocket = IO.socket(Constants.IP_LOCAL_HOST);
        } catch (URISyntaxException e){
            Log.i(LoginFragment.class.getSimpleName(), e.getMessage());
            Toast.makeText(getActivity(),"Can't connect to the server", Toast.LENGTH_SHORT).show();

        }
        mSocket.connect();
        mLiveFriendsServices = LiveFriendServices.getInstance();
        mUserEmailString = mSharedPreferences.getString(Constants.USER_EMAIL,"");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_request,container,false);
        mUnbinder = ButterKnife.bind(this,rootView);



        FriendRequestAdapter adapter = new FriendRequestAdapter((BaseFragmentActivity) getActivity(),this);

        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGetAllUsersFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUEST_RECIEVED).child(Constants.encodeEmail(mUserEmailString));


        mGetAllUsersFriendRequestsListener = mLiveFriendsServices.getAllFriendRequest(adapter,mRecycleView,mTexView);

        mGetAllUsersFriendRequestsReference.addValueEventListener(mGetAllUsersFriendRequestsListener);
        mRecycleView.setAdapter(adapter);


        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        if(mGetAllUsersFriendRequestsListener != null){
            mGetAllUsersFriendRequestsReference.removeEventListener(mGetAllUsersFriendRequestsListener);
        }
    }

    @Override
    public void OnOptionClicked(User user, String result) {

        if(result.equals("0")){
            DatabaseReference userFriendReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIRE_BASE_PATH_USER_FRIENDS).child(Constants.encodeEmail(mUserEmailString))
                    .child(Constants.encodeEmail(user.getEmail()));
            userFriendReference.setValue(user);
            mGetAllUsersFriendRequestsReference.child(Constants.encodeEmail(user.getEmail()))
                    .removeValue();
            mCompositeSubscription.add(mLiveFriendsServices.approveDeclineFriendRequest(mSocket, mUserEmailString, user.getEmail(),"0"));
        } else{

            mGetAllUsersFriendRequestsReference.child(Constants.encodeEmail(user.getEmail()))
                    .removeValue();
            mCompositeSubscription.add(mLiveFriendsServices.approveDeclineFriendRequest(mSocket, mUserEmailString, user.getEmail(),"1"));


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}
