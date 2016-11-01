package com.blogspot.dbh4ck.nimbuzzadminbot;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.dbh4ck.nimbuzzadminbot.Dialogs.dbDialog;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.AboutFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.ChatroomFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.ContactsFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.HelpFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.ProfileFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.Fragments.TabFragment;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.XMPPLogic;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jxmpp.util.XmppStringUtils;

public class Nav_Nimbuzz extends AppCompatActivity {

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Toolbar toolbar;
    TextView userjid;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ImageView ivCusToolbar;
    private dbDialog InfoDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav__nimbuzz);


        /**
         *Setup the DrawerLayout and NavigationView
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.dbhere);

       // actionBarDrawerToggle = new ActionBarDrawerToggle(Nav_Nimbuzz.this, mDrawerLayout, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();


                if (menuItem.getItemId() == R.id.id_home) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();

                }

                else if (menuItem.getItemId() == R.id.id_prof) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new ProfileFragment()).commit();
                }

                else if (menuItem.getItemId() == R.id.id_contact_list) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new ContactsFragment()).commit();
                }

                else if (menuItem.getItemId() == R.id.id_joinroom) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new ChatroomFragment()).commit();
                }

                else if (menuItem.getItemId() == R.id.id_help) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new HelpFragment()).commit();
                }

                else if (menuItem.getItemId() == R.id.id_about) {
                    FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    xfragmentTransaction.replace(R.id.containerView, new AboutFragment()).commit();
                }

                else if (menuItem.getItemId() == R.id.id_signout) {
                    //FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                    //xfragmentTransaction.replace(R.id.containerView, new AboutFragment()).commit();

                    XMPPLogic.connection.disconnect();
                    finish();
                    Intent LogInt = new Intent(getApplicationContext(), MainActivity.class);
                    LogInt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(LogInt);
                }

                else if (menuItem.getItemId() == R.id.id_exit) {

                    Intent LogInt2 = new Intent(Nav_Nimbuzz.this, MainActivity.class);
                    LogInt2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    LogInt2.putExtra("EXIT", true);

                    startActivity(LogInt2);
                    return true;
                }

                return false;
            }

        });

        View header = mNavigationView.getHeaderView(0);
        userjid = (TextView) header.findViewById(R.id.usrjid);
        userjid.setText(XmppStringUtils.parseBareJid(XMPPLogic.connection.getUser()));

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

    }


    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav__nimbuzz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            InfoDialog = new dbDialog(this);
            InfoDialog.show();
            InfoDialog.setCanceledOnTouchOutside(false);
            //return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
}
