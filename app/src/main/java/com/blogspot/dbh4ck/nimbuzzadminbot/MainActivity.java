package com.blogspot.dbh4ck.nimbuzzadminbot;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blogspot.dbh4ck.nimbuzzadminbot.Dialogs.dbDialog;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.XMPPLogic;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.dbXMPPConn;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private dbDialog InfoDialog;
    private Boolean exit;
    private Handler handler;
    private boolean mBounded;
    public dbXMPPConn mService;
    private View view;
    private IBinder mBinder = new ServiceBinder(this.mService);
    public static ProgressBar pb;



    public class ServiceBinder<T> extends Binder {
        private final WeakReference<T> mService;

        public ServiceBinder(T service) {
            this.mService = new WeakReference(service);
        }

        public T getService() {
            return this.mService.get();
        }
    }

    public MainActivity() {
        this.handler = new Handler();
        this.exit = Boolean.valueOf(false);
        this.mBinder = new ServiceBinder(this.mService);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context mContext = getApplicationContext();
        super.onCreate(savedInstanceState);

        if(getIntent().getBooleanExtra("EXIT", false)){
            finish();
            return;
        }

        setContentView(R.layout.activity_main);



        pb = (ProgressBar) findViewById(R.id.progressBar);

        Button SingInBtn = (Button) findViewById(R.id.btnSignin);
        SingInBtn.setOnClickListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Dialog for Displaying Info
        InfoDialog = new dbDialog(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        InfoDialog.show();
                        InfoDialog.setCanceledOnTouchOutside(false);
                    }
                });
            }
        });


    }


    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            InfoDialog = new dbDialog(this);
            InfoDialog.show();
            InfoDialog.setCanceledOnTouchOutside(false);
            //return true;
        }

        else if (id == R.id.exit){
            System.exit(1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        try {
            EditText userPwd = (EditText) findViewById(R.id.txtPwd);
            String userName = ((EditText) findViewById(R.id.txtUser)).getText().toString();
            String passWord = userPwd.getText().toString();
            Intent intent = new Intent(getBaseContext(), dbXMPPConn.class);
            intent.putExtra("user", userName);
            intent.putExtra("pwd", passWord);
            startService(intent);
            Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
            pb.setVisibility(View.VISIBLE);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed()
    {
        //   super.onBackPressed();
        if(exit){
            finish();
            pb.setVisibility(View.GONE);
        }
        else{
            pb.setVisibility(View.GONE);
            Toast.makeText(this, "Press Back Again To Exit" , Toast.LENGTH_SHORT).show();
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent dbdito = new Intent(Intent.ACTION_MAIN);
                    dbdito.addCategory(Intent.CATEGORY_HOME);
                    dbdito.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dbdito);
                    pb.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }

}
