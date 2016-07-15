package com.pubnub.example.android.fabric.pnfabricchat.chat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pubnub.example.android.fabric.pnfabricchat.R;

public class ChatTabFragment extends Fragment {
    private ChatListAdapter chatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ListView listView = (ListView) view.findViewById(R.id.chat_list);
        listView.setAdapter(chatAdapter);

        return view;
    }

    public void setAdapter(ChatListAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }
}
