package com.pubnub.example.android.fabric.pnfabricchat.presence;

import android.util.Log;

import com.google.common.base.Throwables;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.example.android.fabric.pnfabricchat.util.DateTimeUtil;
import com.pubnub.example.android.fabric.pnfabricchat.util.JsonUtil;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PresencePnCallback extends Callback {
    private static final String TAG = PresencePnCallback.class.getName();
    private final PresenceListAdapter presenceListAdapter;

    public PresencePnCallback(PresenceListAdapter presenceListAdapter) {
        this.presenceListAdapter = presenceListAdapter;
    }

    @Override
    public void successCallback(String channel, Object message) {
        try {
            Log.v(TAG, "presenceP(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        try {
            Map<String, Object> presence = JsonUtil.fromJSONObject((JSONObject) message, LinkedHashMap.class);

            List<String> uuids;
            if (presence.containsKey("uuids")) {
                uuids = (List<String>) presence.get("uuids");
            } else {
                uuids = Arrays.asList((String) presence.get("uuid"));
            }

            for (String sender : uuids) {
                String presenceString = presence.containsKey("action") ? (String) presence.get("action") : "join";
                String timestamp = DateTimeUtil.getTimeStampUtc();

                PresencePojo pm = new PresencePojo(sender, presenceString, timestamp);
                presenceListAdapter.add(pm);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        try {
            Log.v(TAG, "presenceErr(" + JsonUtil.asJson(error) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "connP(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void reconnectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "reconnP(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void disconnectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "disconnP(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
