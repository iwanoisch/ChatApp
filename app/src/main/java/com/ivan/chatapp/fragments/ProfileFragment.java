package com.ivan.chatapp.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ivan.chatapp.R;
import com.ivan.chatapp.R2;
import com.ivan.chatapp.activities.BaseFragmentActivity;
import com.ivan.chatapp.services.LiveAccountServices;
import com.ivan.chatapp.services.LiveFriendServices;
import com.ivan.chatapp.utils.Constants;
import com.ivan.chatapp.utils.MarshMellowPermissions;
import com.roughike.bottombar.BottomBar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.socket.client.IO;
import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends BaseFragment{

    @BindView(R2.id.bottomBar)
    BottomBar mBottombar;

    @BindView(R2.id.fragment_profile_camera_Picture)
    ImageView mCameraImage;

    @BindView(R2.id.fragment_profile_image_Picture)
    ImageView mImageView;

    @BindView(R2.id.fragment_profile_userPicture)
    ImageView mUserPicture;

    @BindView(R2.id.fragment_profile_userEmail)
    TextView mUserEmail;

    @BindView(R2.id.fragment_profile_userName)
    TextView muserName;

    @BindView(R2.id.fragment_profile_signOut)
    Button mSignOutButton;


    private Unbinder mUnbinder;


    private LiveFriendServices mLiveFriendsServices;

    private DatabaseReference mAllFriendRequestsReference;
    private ValueEventListener mAllFriendRequestListener;

    private DatabaseReference mUsersNewMessagesReference;
    private ValueEventListener mUsersNewMessagesListener;

    private String mUserEmailString;


    private final int REQUEST_CODE_CAMERA = 100;
    private final int REQUEST_CODE_PICTURE = 101;


    private Uri mTempUri;

    private MarshMellowPermissions mMarshMellowPermission;

    private BaseFragmentActivity mActivity;
    private Socket mSocket;





    public static ProfileFragment newInstance(){
        return  new ProfileFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mSocket = IO.socket(Constants.IP_LOCAL_HOST);
        } catch (URISyntaxException e) {
            Log.i(RegisterFragment.class.getSimpleName(),e.getMessage());
            Toast.makeText(getActivity(),"Can't connect to the server",Toast.LENGTH_SHORT).show();
        }
        mSocket.connect();
        mLiveFriendsServices = LiveFriendServices.getInstance();
        mUserEmailString = mSharedPreferences.getString(Constants.USER_EMAIL,"");
        mMarshMellowPermission = new MarshMellowPermissions((BaseFragmentActivity) getActivity());
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile,container,false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mBottombar.selectTabWithId(R.id.tab_profile);
        setUpBottomBar(mBottombar,3);



        Picasso.with(getActivity())
                .load(mSharedPreferences.getString(Constants.USER_PICTURE,""))
                .into(mUserPicture);
        mUserEmail.setText(mUserEmailString);
        muserName.setText(mSharedPreferences.getString(Constants.USER_NAME,""));


        mAllFriendRequestsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_FRIEND_REQUEST_RECIEVED).child(Constants.encodeEmail(mUserEmailString));
        mAllFriendRequestListener = mLiveFriendsServices.getFriendRequestBottom(mBottombar,R.id.tab_friends);
        mAllFriendRequestsReference.addValueEventListener(mAllFriendRequestListener);


        mUsersNewMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIRE_BASE_PATH_USER_NEW_MESSAGES).child(Constants.encodeEmail(mUserEmailString));
        mUsersNewMessagesListener = mLiveFriendsServices.getAllNewMessages(mBottombar, R.id.tab_messages);

        mUsersNewMessagesReference.addValueEventListener(mUsersNewMessagesListener);
        return rootView;
    }

    @OnClick(R2.id.fragment_profile_image_Picture)
    public void setmImageView(){
        if (!mMarshMellowPermission.checkPermissionForWriteExternalStorage()){
            mMarshMellowPermission.requestPermissionForWriteExternalStorage();
        } else if (!mMarshMellowPermission.checkPermissionForReadExternalStorage()){
            mMarshMellowPermission.requestPermissionForReadExternalStorage();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent,"Choose Image With"),
                    REQUEST_CODE_PICTURE);
        }

    }

    @OnClick(R2.id.fragment_profile_camera_Picture)
    public void setmCameraImage(){

        if (!mMarshMellowPermission.checkPermissionForCamera()){
            mMarshMellowPermission.requestPermissionForCamera();
        } else if (!mMarshMellowPermission.checkPermissionForWriteExternalStorage()){
            mMarshMellowPermission.requestPermissionForWriteExternalStorage();
        } else if (!mMarshMellowPermission.checkPermissionForReadExternalStorage()){
            mMarshMellowPermission.requestPermissionForReadExternalStorage();
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mTempUri = Uri.fromFile(getOutputFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT,mTempUri);
            startActivityForResult(intent,REQUEST_CODE_CAMERA);
        }

    }


    @OnClick(R2.id.fragment_profile_signOut)
    public void setmSignOutButton(){
        mSharedPreferences.edit().putString(Constants.USER_PICTURE,"").apply();
        mSharedPreferences.edit().putString(Constants.USER_NAME,"").apply();
        mSharedPreferences.edit().putString(Constants.USER_EMAIL,"").apply();

        FirebaseAuth.getInstance().signOut();;
        getActivity().finish();

    }

    private static File getOutputFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES),"ChatApp");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdir()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
        "IMG_" + timeStamp + ".jpg");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICTURE){
            Uri selectedImageUri = data.getData();
            //Log.i(ProfileFragment.class.getSimpleName(), selectedImageUri.toString());

            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("usersProfilePics").child(Constants.encodeEmail(mUserEmailString))
                    .child(selectedImageUri.getLastPathSegment());

            mCompositeSubscription.add(LiveAccountServices.getInstance()
                    .changeProfilePhoto(storageReference,selectedImageUri,mActivity,
                            mUserEmailString,mUserPicture,mSharedPreferences,mSocket));
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA){
            Uri selectedImageUri = mTempUri;
            //Log.i(ProfileFragment.class.getSimpleName(),selectedImageUri.toString());

            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("usersProfilePics").child(Constants.encodeEmail(mUserEmailString))
                    .child(selectedImageUri.getLastPathSegment());

            mCompositeSubscription.add(LiveAccountServices.getInstance()
                    .changeProfilePhoto(storageReference,selectedImageUri,mActivity,
                            mUserEmailString,mUserPicture,mSharedPreferences,mSocket));
        }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseFragmentActivity) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}
