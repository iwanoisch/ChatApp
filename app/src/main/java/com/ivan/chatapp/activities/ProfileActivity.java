package com.ivan.chatapp.activities;


import android.support.v4.app.Fragment;

import com.ivan.chatapp.fragments.ProfileFragment;

public class ProfileActivity extends  BaseFragmentActivity{
    @Override
    Fragment createFragment() {
        return ProfileFragment.newInstance();
    }
}
