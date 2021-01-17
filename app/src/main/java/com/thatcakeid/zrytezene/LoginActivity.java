package com.thatcakeid.zrytezene;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.thatcakeid.zrytezene.ui.login.LoginFragment;
import com.thatcakeid.zrytezene.ui.login.RegisterFragment;

public class LoginActivity extends AppCompatActivity {
    private Fragment login_fragment;
    private Fragment register_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            login_fragment = LoginFragment.newInstance();
            register_fragment = RegisterFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, login_fragment)
                    .commitNow();
        }
    }
}