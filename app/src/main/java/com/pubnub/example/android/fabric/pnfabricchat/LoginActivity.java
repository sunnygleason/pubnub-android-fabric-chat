package com.pubnub.example.android.fabric.pnfabricchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.google.common.collect.ImmutableMap;
import com.pubnub.example.android.fabric.pnfabricchat.util.DateTimeUtil;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private TwitterLoginButton loginButton;
    private DigitsAuthButton digitsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                SharedPreferences sp = getSharedPreferences(Constants.DATASTREAM_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.DATASTREAM_UUID, session.getUserName());
                edit.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();

                SharedPreferences sp = getSharedPreferences(Constants.DATASTREAM_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.DATASTREAM_UUID, session.getPhoneNumber());
                edit.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}
