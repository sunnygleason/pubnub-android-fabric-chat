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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;

import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsSession;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.example.android.fabric.pnfabricchat.chat.ChatListAdapter;
import com.pubnub.example.android.fabric.pnfabricchat.chat.ChatPnCallback;
import com.pubnub.example.android.fabric.pnfabricchat.presence.PresenceListAdapter;
import com.pubnub.example.android.fabric.pnfabricchat.presence.PresencePnCallback;
import com.pubnub.example.android.fabric.pnfabricchat.util.DateTimeUtil;
import com.pubnub.example.android.fabric.pnfabricchat.util.JsonUtil;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterSession;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    private static final String TWITTER_KEY = "YOUR_TWITTER_KEY";
    private static final String TWITTER_SECRET = "YOUR_TWITTER_SECRET";

    private static final String TAG = MainActivity.class.getName();

    private Pubnub mPubnub;
    private ChatPnCallback mChatCallback;
    private ChatListAdapter mChatListAdapter;
    private PresencePnCallback mPresenceCallback;
    private PresenceListAdapter mPresenceListAdapter;

    private SharedPreferences mSharedPrefs;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        mSharedPrefs = getSharedPreferences(Constants.DATASTREAM_PREFS, MODE_PRIVATE);
        if (!mSharedPrefs.contains(Constants.DATASTREAM_UUID)) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            return;
        }

        this.mUsername = mSharedPrefs.getString(Constants.DATASTREAM_UUID, "");
        this.mChatListAdapter = new ChatListAdapter(this);
        this.mPresenceListAdapter = new PresenceListAdapter(this);

        this.mChatCallback = new ChatPnCallback(this.mChatListAdapter);
        this.mPresenceCallback = new PresencePnCallback(this.mPresenceListAdapter);

        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Presence"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final MainActivityTabManager adapter = new MainActivityTabManager
                (getSupportFragmentManager(), tabLayout.getTabCount());

        adapter.setChatListAdapter(this.mChatListAdapter);
        adapter.setPresenceAdapter(this.mPresenceListAdapter);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        initPubNub();
        initChannels();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                logout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        disconnectAndCleanup();

        Intent toLogin = new Intent(this, LoginActivity.class);
        startActivity(toLogin);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectAndCleanup();
    }

    public void publish(View view) {
        final EditText mMessage = (EditText) MainActivity.this.findViewById(R.id.new_message);

        final Map<String, String> message = ImmutableMap.<String, String>of("sender", MainActivity.this.mUsername, "message", mMessage.getText().toString(), "timestamp", DateTimeUtil.getTimeStampUtc());

        try {
            this.mPubnub.publish(Constants.CHANNEL_NAME, JsonUtil.asJSONObject(message), new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessage.setText("");
                        }
                    });
                    try {
                        Log.v(TAG, "publish(" + JsonUtil.asJson(message) + ")");
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    try {
                        Log.v(TAG, "publishErr(" + JsonUtil.asJson(error) + ")");
                    } catch (Exception e) {
                        throw Throwables.propagate(e);
                    }
                }
            });
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private final void initPubNub() {
        this.mPubnub = new Pubnub(Constants.PUBLISH_KEY, Constants.SUBSCRIBE_KEY);
        this.mPubnub.setUUID(this.mUsername);
    }

    private final void initChannels() {
        try {
            this.mPubnub.subscribe(Constants.CHANNEL_NAME, this.mChatCallback);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        try {
            this.mPubnub.subscribe(Constants.PRESENCE_CHANNEL_NAME, this.mPresenceCallback);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        this.mPubnub.hereNow(Constants.CHANNEL_NAME, this.mPresenceCallback);

        this.mPubnub.history(Constants.CHANNEL_NAME, 200, this.mChatCallback);
    }

    private void disconnectAndCleanup() {
        getSharedPreferences(Constants.DATASTREAM_PREFS, MODE_PRIVATE).edit().clear().commit();

        if (this.mPubnub != null) {
            this.mPubnub.unsubscribe(Constants.CHANNEL_NAME);
            this.mPubnub.unsubscribe(Constants.CHANNEL_NAME);
            this.mPubnub.shutdown();
            this.mPubnub = null;
        }

        Digits.getSessionManager().clearActiveSession();
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }
}
