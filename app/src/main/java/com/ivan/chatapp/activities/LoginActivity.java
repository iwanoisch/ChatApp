package com.ivan.chatapp.activities;

import android.support.v4.app.Fragment;

import com.ivan.chatapp.fragments.LoginFragment;

public class LoginActivity extends BaseFragmentActivity {


    @Override
    Fragment createFragment() {
        return LoginFragment.newInstance();
    }
}
