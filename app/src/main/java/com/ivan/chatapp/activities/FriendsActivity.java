package com.ivan.chatapp.activities;


import android.support.v4.app.Fragment;

import com.ivan.chatapp.fragments.FriendFragment;

public class FriendsActivity extends BaseFragmentActivity{
    @Override
    Fragment createFragment() {
        return FriendFragment.newInstance();
    }
}
