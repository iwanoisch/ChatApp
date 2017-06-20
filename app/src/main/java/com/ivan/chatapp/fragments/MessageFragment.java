package com.ivan.chatapp.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivan.chatapp.R;
import com.ivan.chatapp.R2;
import com.ivan.chatapp.activities.BaseFragmentActivity;
import com.ivan.chatapp.entities.ChatRoom;
import com.ivan.chatapp.entities.Message;
import com.ivan.chatapp.services.LiveFriendServices;
import com.ivan.chatapp.utils.Constants;
import com.ivan.chatapp.views.MessagesViews.MessagesAdapter;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.socket.client.IO;
import io.socket.client.Socket;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MessageFragment extends  BaseFragment{

    public static final String FRIEND_DETAILS_EXTRA = "FRIEND_DETAIL_EXTRA";

    @BindView(R2.id.fragment_messages_friendPicture)
    ImageView mFriendPicture;

    @BindView(R2.id.fragment_messages_friendName)
    TextView mFriendName;

    @BindView(R2.id.fragment_messages_messageBox)
    EditText mMessageBox;

    @BindView(R2.id.fragment_messages_recycleview)
    RecyclerView mRecyclerView;

    @BindView(R2.id.fragment_messages_sendArrow)
    ImageView mSendMessage;




    private String mFriendEmailString;
    private String mFriendPictureString;
    private String mFriendNameString;
    private String mUserEmailString;


    private DatabaseReference mGetAllMessagesReference;
    private ValueEventListener mGetAllMessagesListener;


    private Unbinder mUnbinder;

    private Socket mSocket;
    private LiveFriendServices mLiveFriendServices;


    private DatabaseReference mUserChatRoomReference;
    private ValueEventListener mUserChatRoomListener;



    private PublishSubject<String> mMessageSubject;


    private MessagesAdapter mAdapter;



    public static MessageFragment newInstance(ArrayList<String> friendDetails){
        Bundle arguments = new Bundle();
        arguments.putStringArrayList(FRIEND_DETAILS_EXTRA, friendDetails);
        MessageFragment messageFragment = new MessageFragment();
        messageFragment.setArguments(arguments);
        return messageFragment;

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

        mLiveFriendServices = LiveFriendServices.getInstance();

        ArrayList<String> friendDetails = getArguments().getStringArrayList(FRIEND_DETAILS_EXTRA);
        mFriendEmailString = friendDetails.get(0);
        mFriendPictureString = friendDetails.get(1);
        mFriendNameString = friendDetails.get(2);
        mUserEmailString = mSharedPreferences.getString(Constants.USER_EMAIL,"");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages,container,false);
        mUnbinder = ButterKnife.bind(this,rootView);

        Picasso.with(getActivity())
                .load(mFriendPictureString)
                .into(mFriendPicture);
        mFriendName.setText(mFriendNameString);

        mAdapter = new MessagesAdapter((BaseFragmentActivity) getActivity(), mUserEmailString);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mUserChatRoomReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_CHAT_ROOMS).child(Constants.encodeEmail(mUserEmailString))
                .child(Constants.encodeEmail(mFriendEmailString));
        mUserChatRoomListener = getCurrentChatRoomListener();

        mUserChatRoomReference.addValueEventListener(mUserChatRoomListener);



        mGetAllMessagesReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIRE_BASE_PATH_USER_MESSAGES)
                .child(Constants.encodeEmail(mUserEmailString)).child(Constants.encodeEmail(mFriendEmailString));

        mGetAllMessagesListener = mLiveFriendServices.getAllMessages(mRecyclerView,mFriendName,mFriendPicture,mAdapter,mUserEmailString);

        mGetAllMessagesReference.addValueEventListener(mGetAllMessagesListener);

        mRecyclerView.setAdapter(mAdapter);



        mCompositeSubscription.add(createChatRoomSubscription());

        mRecyclerView.scrollToPosition(mAdapter.getmMessages().size()-1);
        messageBoxListener();
        return rootView;
    }


    @OnClick(R2.id.fragment_messages_sendArrow)
    public void setmSendMessage(){
        if (mMessageBox.getText().toString().equals("")){
            Toast.makeText(getActivity(),"Message Can't Be Blank", Toast.LENGTH_SHORT).show();
        } else {

            ChatRoom chatRoom =  new ChatRoom(mFriendPictureString, mFriendNameString,
                    mFriendEmailString, mMessageBox.getText().toString(), mUserEmailString,true,true);

            mUserChatRoomReference.setValue(chatRoom);


            DatabaseReference newMessageReference = mGetAllMessagesReference.push();
            Message message = new Message(newMessageReference.getKey(),mMessageBox.getText().toString()
                ,mUserEmailString,mSharedPreferences.getString(Constants.USER_PICTURE,""));

            newMessageReference.setValue(message);

            mCompositeSubscription.add(mLiveFriendServices.sendMessage(mSocket,mUserEmailString,
                    mSharedPreferences.getString(Constants.USER_PICTURE,""),mMessageBox.getText().toString(),
                    mFriendEmailString, mSharedPreferences.getString(Constants.USER_NAME,"")));


            View view = getActivity().getCurrentFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);

            mRecyclerView.scrollToPosition(mAdapter.getmMessages().size()-1);

            mMessageBox.setText("");


        }

    }


    private Subscription createChatRoomSubscription(){
        mMessageSubject = PublishSubject.create();
        return mMessageSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String message) {
                        if(!message.isEmpty()){
                            ChatRoom chatRoom =  new ChatRoom(mFriendPictureString, mFriendNameString,
                                    mFriendEmailString, message, mUserEmailString,true,false);

                            mUserChatRoomReference.setValue(chatRoom);
                        }
                    }
                });
    }

    private void messageBoxListener(){
        mMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mMessageSubject.onNext(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public ValueEventListener getCurrentChatRoomListener(){
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if (chatRoom!=null){
                    mUserChatRoomReference
                            .child("lastMessageRead")
                            .setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        if (mGetAllMessagesListener != null){
            mGetAllMessagesReference.removeEventListener(mGetAllMessagesListener);
        }

        if(mUserChatRoomListener!=null){
            mUserChatRoomReference.removeEventListener(mUserChatRoomListener);
        }
    }
}
