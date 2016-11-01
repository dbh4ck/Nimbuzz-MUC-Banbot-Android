package com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig;

import android.accounts.AccountManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.blogspot.dbh4ck.nimbuzzadminbot.Nav_Nimbuzz;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;

import static com.blogspot.dbh4ck.nimbuzzadminbot.MainActivity.pb;

public class dbXMPPConn extends Service {

    private static dbXMPPConn instance;

    public static dbXMPPConn getInstance() {
        return instance;
    }

    private PingManager pingManager;

    private static final String TAG = "dbXMPP";
    private static final String DOMAIN = "nimbuzz.com";
    private static final String HOST = "o.nimbuzz.com";
    private static final int PORT = 5222;

    private String userName ="";
    private String passWord = "";

    XMPPConnectionListener connectionListener = new XMPPConnectionListener();

    public static boolean connected = false;
    public static boolean isToasted = true;
    public static boolean chat_created = true;

    public dbXMPPConn() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null){
            userName = intent.getStringExtra("user");
            passWord = intent.getStringExtra("pwd");
            dbcon.init(userName, passWord);
            dbcon.connectConnection();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }



    @Override
    public void onDestroy() {
        dbcon.disconnectConnection();
        super.onDestroy();
        stopForeground(true);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }


    private dbXMPP dbcon = new dbXMPP();

    public  class dbXMPP{

        public void init(final String userName, final String passWord) {

            Log.i("dbXMPP", "Initializing!");

            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
            configBuilder.setUsernameAndPassword(userName, passWord);
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            configBuilder.setResource("dbanbot-4droid-coded-by-db~");
            configBuilder.setServiceName(DOMAIN);
            configBuilder.setHost(HOST);
            configBuilder.setPort(PORT);

            XMPPLogic.connection = new XMPPTCPConnection(configBuilder.build());
            XMPPLogic.connection.addConnectionListener(connectionListener);
            ReconnectionManager.getInstanceFor(XMPPLogic.connection).isAutomaticReconnectEnabled();

        }

        public void connectConnection() {
            AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... arg0) {

                    // Create a connection
                    try {
                        XMPPLogic.connection.connect();

                        login();

                       // createAccount();

                        connected = true;

                    } catch (IOException e) {
                    } catch (SmackException e) {

                    } catch (XMPPException e) {
                    }

                    return null;
                }
            };
            connectionThread.execute();

        }

        public void disconnectConnection() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    XMPPLogic.connection.disconnect();
                }
            }).start();
        }
    }

    private void createAccount() throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        org.jivesoftware.smackx.iqregister.AccountManager accountManager = org.jivesoftware.smackx.iqregister.AccountManager.getInstance(XMPPLogic.connection);
        accountManager.createAccount(userName, passWord);
    }


    private void sendping() throws InterruptedException {
        pingManager = PingManager.getInstanceFor(XMPPLogic.connection);
        pingManager.setPingInterval(150);

        pingManager.registerPingFailedListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                pingManager.setPingInterval(150);
            }
        });


    }


    private boolean loggedin = true;
    public void login() {

        try {

            XMPPLogic.connection.login(userName, passWord);

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            loggedin = false;
            chat_created = false;
        }

        if (!loggedin)
        {
            Log.e(TAG, "Unable to login");

            disconnect();
            loggedin = true;
        }

        else {

            Log.e(TAG, "Logged in");
        }
    }


    private void disconnect() {
        if(XMPPLogic.connection != null && XMPPLogic.connection.isConnected()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    XMPPLogic.connection.disconnect();
                    Log.e(TAG, "Connection disconnected");
                    return null;
                }
            }.execute();
        }
    }



    public void setPresence() throws SmackException.NotConnectedException {
        Presence dbpres = new Presence(Presence.Type.available);
        dbpres.setMode(Presence.Mode.dnd);
        dbpres.setStatus("Nimbuzz Login SMACK XMPP Android Coded By DB~@NC");
        XMPPLogic.connection.sendStanza(dbpres);
        try{
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    /** Connection Listener Class to check connection state  */

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("dbXMPP", "Connected!");
            connected = true;

            if (!connection.isAuthenticated()) {
                login();
            }

        }


        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "Connection Closed!" , Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("dbXMPP", "Connection Closed!");
            connected = false;
            chat_created = false;
            loggedin = false;
            pb.setVisibility(View.GONE);
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), "Connection Closed On Error", Toast.LENGTH_SHORT).show();
                    }
                });
            Log.d("dbXMPP", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;
            pb.setVisibility(View.GONE);
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d("dbXMPP", "Reconnecting " + arg0);

            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Re-Connection Failed", Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("dbXMPP", "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;

        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        Toast.makeText(getApplicationContext(), "Re-Connected!", Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("dbXMPP", "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;
            pb.setVisibility(View.GONE);
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("dbXMPP", "Authenticated!");
            loggedin = true;

            chat_created = true;

            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "Logged In Successfully", Toast.LENGTH_SHORT).show();

                        try {
                            setPresence();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        //     conference();

                        Intent dbint = new Intent(dbXMPPConn.this, Nav_Nimbuzz.class);
                        dbint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dbint);
                        try {
                            sendping();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        pb.setVisibility(View.GONE);
                    }



                });

        }
    }

}
