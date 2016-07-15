package com.pubnub.example.android.fabric.pnfabricchat.chat;

import android.util.Log;

import com.google.common.base.Throwables;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.pubnub.example.android.fabric.pnfabricchat.util.DateTimeUtil;
import com.pubnub.example.android.fabric.pnfabricchat.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatPnCallback extends Callback {
    private static final String TAG = ChatPnCallback.class.getName();
    private final ChatListAdapter chatListAdapter;

    public ChatPnCallback(ChatListAdapter presenceListAdapter) {
        this.chatListAdapter = presenceListAdapter;
    }

    @Override
    public void successCallback(String channel, Object messageObject) {
        try {
            Log.v(TAG, "message(" + JsonUtil.asJson(messageObject) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        try {
            if (messageObject instanceof JSONObject) {
                chatListAdapter.add(messageToPojo((JSONObject) messageObject));
            } else if (messageObject instanceof JSONArray) {
                JSONArray values = (JSONArray) ((JSONArray) messageObject).get(0);

                for (int i = 0; i < values.length(); i++) {
                    chatListAdapter.add(messageToPojo((JSONObject) values.get(i)));
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private ChatPojo messageToPojo(JSONObject messageObject) throws Exception {
        Map<String, Object> chatMessage = JsonUtil.fromJSONObject(messageObject, LinkedHashMap.class);

        String sender = (String) chatMessage.get("sender");
        String message = (String) chatMessage.get("message");
        String timestamp = (String) chatMessage.get("timestamp");

        ChatPojo chat = new ChatPojo(sender, message, timestamp);

        return chat;
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        try {
            Log.v(TAG, "messageErr(" + JsonUtil.asJson(error) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "connM(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void reconnectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "reconnM(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void disconnectCallback(String channel, Object message) {
        try {
            Log.v(TAG, "disconnM(" + JsonUtil.asJson(message) + ")");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
