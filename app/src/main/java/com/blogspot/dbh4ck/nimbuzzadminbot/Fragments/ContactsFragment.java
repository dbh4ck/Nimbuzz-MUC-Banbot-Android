package com.blogspot.dbh4ck.nimbuzzadminbot.Fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.blogspot.dbh4ck.nimbuzzadminbot.R;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.XMPPLogic;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class ContactsFragment extends ListFragment {

    Presence presence;

    public ContactsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    String[] FriendList;
    String[] FStatusList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(XMPPLogic.connection.isConnected() && XMPPLogic.connection.isAuthenticated()){

            notifyRosterChanged();

            Roster roster = Roster.getInstanceFor(XMPPLogic.connection);

            if (!roster.isLoaded()) try {
                roster.reloadAndWait();
            } catch (SmackException.NotLoggedInException | SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
            }

            final ArrayList<String> userlist = new ArrayList<String>();
            final ArrayList<String> statuslist = new ArrayList<String>();
            for (RosterEntry entry : roster.getEntries() ) {
                presence = roster.getPresence(entry.getUser());
                userlist.add(entry.getUser());
                statuslist.add(presence.getStatus());

               // userlist.add(presence.getType().name());
               // userlist.add(presence.getStatus());

            }

            String[] mylist = new String[userlist.size()];
            for (int i = 0; i < userlist.size(); i++) {
                mylist[i] = userlist.get(i);
            }

            String[] stslist = new String[statuslist.size()];
            for (int i = 0; i < statuslist.size(); i++) {
                stslist[i] = statuslist.get(i);
            }

            FriendList = mylist;
            FStatusList = stslist;
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_2,android.R.id.text1, FriendList){
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setTextColor(Color.BLUE);
                text2.setTextColor(Color.DKGRAY);
                text1.setText("Jid: " + FriendList[position]);
                text2.setText("Status: " + FStatusList[position]);

                return view;
            }
        };

        setListAdapter(adapter);

      return super.onCreateView(inflater, container, savedInstanceState);

    }

    private void notifyRosterChanged() {
        Roster roster = Roster.getInstanceFor(XMPPLogic.connection);
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                //  DB~@NC WAS ONLINE
            }
        });
    }


}
