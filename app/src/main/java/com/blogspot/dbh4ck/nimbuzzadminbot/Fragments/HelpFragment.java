package com.blogspot.dbh4ck.nimbuzzadminbot.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blogspot.dbh4ck.nimbuzzadminbot.R;

import java.util.List;

public class HelpFragment extends ListFragment {


    String[] helpitems = new String[] {
            "All Available Command List:",
            "Command Syntax:  .k id",
            "Command Syntax:  .m id",
            "Command Syntax:  .b id",
            "Command Syntax:  .bip id",
            "Command Syntax:  .v id",
            "Command Syntax:  .mod id",
            "Command Syntax:  .a id",
            "Command Syntax:  .o id",
            "Command Syntax:  .addc id",
            "Command Syntax:  .show mas",
            "Command Syntax:  .remc id",
            "",
            "Coder:"
    };

    String[] helpitemsdetails = new String[] {
            "",
            "Usage: To kick out user from MUC",
            "Usage: To make user Member in current MUC",
            "Usage: To ban user from MUC",
            "Usage: To ipban user from MUC",
            "Usage: To make user visitor in current MUC",
            "Usage: To grant user moderator in current MUC",
            "Usage: To grant user administrator in current MUC",
            "Usage: To grant user ownership in current MUC",
            "Usage: To add user to Bot Master List",
            "Usage: To show current Bot Masters",
            "Usage: To remove user from Bot Master List",
            "",
            "Coded By DB~@NC"
    };

    public HelpFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_help_list, container, false);

        /*  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, helpitems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

              row.setBackgroundColor(Color.GRAY);

                return row;
            }
        };

        */

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_2,android.R.id.text1, helpitems){
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setTextColor(Color.BLUE);
                text2.setTextColor(Color.DKGRAY);
                text1.setText(helpitems[position]);
                text2.setText(helpitemsdetails[position]);

                view.setBackgroundColor(Color.CYAN);


                return view;
            }
        };

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

}
