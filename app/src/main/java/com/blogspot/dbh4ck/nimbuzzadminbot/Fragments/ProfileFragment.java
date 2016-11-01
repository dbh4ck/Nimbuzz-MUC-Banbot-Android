package com.blogspot.dbh4ck.nimbuzzadminbot.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blogspot.dbh4ck.nimbuzzadminbot.R;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.XMPPLogic;

import org.jxmpp.util.XmppStringUtils;

public class ProfileFragment extends Fragment {
    private TextView botjid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        this.botjid = (TextView) view.findViewById(R.id.textView6);
        this.botjid.setText(XmppStringUtils.parseBareJid(XMPPLogic.connection.getUser()));
        return view;
    }
}
