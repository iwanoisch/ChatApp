package com.ivan.chatapp.activities;


import android.support.v4.app.Fragment;

import com.ivan.chatapp.fragments.RegisterFragment;

public class RegisterActivity  extends BaseFragmentActivity{

    @Override
    Fragment createFragment() {
        return RegisterFragment.newInstance();
    }
}
