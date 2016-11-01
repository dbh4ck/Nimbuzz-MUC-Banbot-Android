package com.blogspot.dbh4ck.nimbuzzadminbot.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jxmpp.util.XmppStringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.blogspot.dbh4ck.nimbuzzadminbot.R;
import com.blogspot.dbh4ck.nimbuzzadminbot.XmppConfig.XMPPLogic;


public class ChatroomFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "JoinedRoomFragment";
    private String cmdIdent = ".";

    public static List<String> UsersList;

    private EditText RoomName, BotNick, BotMasId, RoomMsg;

    private Button EnterRoomBtn, LeaveRoomBtn, SendRoomMsgBtn;

    public String nickname;
    public static MultiUserChat muc;
    MultiUserChatManager mucchatmanager;

    private PacketListener packetListener;
    private PacketListener presenceListener;

    private List participants = new ArrayList();
    private ParticipantStatusListener participantStatusListener;

    public ArrayList<String> masters;
    String targetNick = null;

    private static final List<String> COMMAND_LIST;

    static {
        //  Commands
        COMMAND_LIST = new ArrayList<String>();
        COMMAND_LIST.add("!commands");
        COMMAND_LIST.add("!coder");
        COMMAND_LIST.add("!details");
    }

    public ChatroomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        masters = new ArrayList<String>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);
        RoomName = (EditText) view.findViewById(R.id.roomname);
        BotNick = (EditText) view.findViewById(R.id.botnick);
        EnterRoomBtn = (Button) view.findViewById(R.id.roomenterbtn);
        EnterRoomBtn.setOnClickListener(this);

        BotMasId = (EditText) view.findViewById(R.id.botmastxt);
        LeaveRoomBtn = (Button) view.findViewById(R.id.leaveroombtn);
        LeaveRoomBtn.setOnClickListener(this);

        RoomMsg = (EditText) view.findViewById(R.id.editText);
        SendRoomMsgBtn = (Button) view.findViewById(R.id.sendroommsgbtn);
        SendRoomMsgBtn.setOnClickListener(this);

        return view;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.roomenterbtn:
                try {
                    roomenter();

                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.leaveroombtn:
                try {
                    leaveRoom();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sendroommsgbtn:
                sendMsg2Room();
                break;

        }

    }

    private void sendMsg2Room() {
        if(XMPPLogic.connection.isConnected() && muc.isJoined()){
            Message message = new Message();
            message.setType(Message.Type.groupchat);
            message.setTo(muc.getRoom());
            message.setBody(RoomMsg.getText().toString());
            try {
                muc.sendMessage(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private void roomenter() throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        mucchatmanager = MultiUserChatManager.getInstanceFor(XMPPLogic.connection);
        muc = mucchatmanager.getMultiUserChat(RoomName.getText().toString() + "@conference.nimbuzz.com");
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(5);       //  set stanzas history to retrieve after joining muc #
        masters.add(BotMasId.getText().toString());

        try {

            muc.join(BotNick.getText().toString(), "", history, XMPPLogic.connection.getPacketReplyTimeout());

            if(XMPPLogic.connection != null && muc.isJoined()){

                Message dbmsg = new Message();
                dbmsg.addBody(null, "Joined to: " + muc.getRoom() + ":d:p");        //  set initial msg to Currently joined MUC #
                muc.sendMessage(dbmsg);

                addPacketListerner();      //  Add Packet Listner for MUC  #
                //  addPresenceListener();
                addParticipantsStatusListener();    //  Add Participants Status Listner in MUC  #



                TimerTask dbTask = new TimerTask() {
                    @Override
                    public void run() {
                        Message antikick = new Message();
                        antikick.setTo(muc.getRoom());
                        antikick.setType(Message.Type.groupchat);
                        antikick.setBody("hmm");
                        try {
                            muc.sendMessage(antikick);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                };

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(dbTask, 500, 14*60*1000);     //  14 mins delay
                // dbTask.scheduledExecutionTime();
                dbTask.run();

            }
            else{
                return;
            }

        }

        catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }


    private void leaveRoom() throws SmackException.NotConnectedException {

        if(muc.isJoined()){
            //  If joined already, perform following tasks #
            muc.leave();            //  leave room
            XMPPLogic.connection.removePacketListener(packetListener);      //  remove Group Packet Listner
            muc.removeParticipantStatusListener(participantStatusListener);     //  remove Participants Status Listner
            //  XMPPLogic.connection.removePacketListener(presenceListener);
        }

    }


    public String getRoom(){
        return muc.getRoom();
    }

    private void addPresenceListener() {

        PacketTypeFilter filter = PacketTypeFilter.PRESENCE;
        presenceListener = new PacketListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Presence presence = (Presence) packet;

                String from = presence.getFrom();
                if(presence.getType() == Presence.Type.available && presence.getFrom().contains("@conference.nimbuzz.com")){
                    synchronized (participants){
                        if(!participants.contains(from)){

                            Message message = new Message();
                            message.setTo(muc.getRoom());
                            message.setBody("Welcome :-) " + String.valueOf(participants.add(XmppStringUtils.parseResource(from))));

                        }
                    }

                }
                else if(presence.getType() == Presence.Type.unavailable && presence.getFrom().contains("@conference.nimbuzz.com")){
                    synchronized (participants){
                        participants.remove(from);

                        Message message = new Message();
                        message.setTo(muc.getRoom());
                        message.setBody("GoodBye :-( " + String.valueOf(participants.add(XmppStringUtils.parseResource(from))));

                    }
                }
            }
        };

        // XMPPLogic.connection.addPacketListener(presenceListener, filter);

    }


    private void addParticipantsStatusListener() {

        participantStatusListener = new ParticipantStatusListener() {
            @Override
            public void joined(String participant) {
                //    Greets with " welcome message " the newly Joined Participants in MUC #

                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "Welcome :-)");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void left(String participant) {

                //    Greets with " GoodBye message " to the leaving MUC Participants in #

                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "GoodBye :-(");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void kicked(String participant, String actor, String reason) {
                if(XmppStringUtils.parseResource(participant).equalsIgnoreCase(muc.getNickname()))
                    try {
                        muc.leave();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }


                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "Goodluck next Time ;-)");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void voiceGranted(String participant) {

            }

            @Override
            public void voiceRevoked(String participant) {

            }

            @Override
            public void banned(String participant, String actor, String reason) {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "You're badluck :-(");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void membershipGranted(String participant) {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "You're now Membered.");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void membershipRevoked(String participant) {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "Sorry! You're no more Member.");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void moderatorGranted(String participant) {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "You're now Moderator.");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void moderatorRevoked(String participant) {

            }

            @Override
            public void ownershipGranted(String participant) {

            }

            @Override
            public void ownershipRevoked(String participant) {

            }

            @Override
            public void adminGranted(String participant) {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setBody(XmppStringUtils.parseResource(participant) + ": " + "You're now Admin.");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void adminRevoked(String participant) {

            }

            @Override
            public void nicknameChanged(String participant, String newNickname) {

            }
        };

        muc.addParticipantStatusListener(participantStatusListener);
    }


    public void addPacketListerner() {

        MessageTypeFilter filter = (MessageTypeFilter) MessageTypeFilter.GROUPCHAT;
        packetListener = new PacketListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {

                Message message = (Message) packet;
                if(message.getBody() != null) {

                    String cmdgiver = XmppStringUtils.parseResource(message.getFrom());

                    if (message.getBody().startsWith(cmdIdent) && message.getBody().length() > 1 && getCommander(cmdgiver)) {
                        String[] commandArgs = message.getBody().replaceFirst(cmdIdent, "").split("\\s");
                        String commandString = commandArgs[0];
                        String targetNick = commandArgs[1];

                        if (commandArgs[0].equalsIgnoreCase("test")) {
                            if (commandArgs[1].equalsIgnoreCase("command")) {
                                testcommand();
                            }
                        } else if (commandArgs[0].equalsIgnoreCase("m")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                memcommand(targetNick);
                            }
                        } else if (commandArgs[0].equalsIgnoreCase("k")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                try {
                                    kickcommand(targetNick);
                                } catch (XMPPException.XMPPErrorException e) {
                                    e.printStackTrace();
                                } catch (SmackException.NoResponseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (commandArgs[0].equalsIgnoreCase("b")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                bancommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("addc")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                AddCommander(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("remc")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                RemoveCommander(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("show")) {
                            if (commandArgs[1].equalsIgnoreCase("mas")) {
                                ShowCommanders();
                            }

                        }else if (commandArgs[0].equalsIgnoreCase("v")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                visitorcommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("mod")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                moderatorcommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("a")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                admincommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("o")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                ownercommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("n")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                nonecommand(targetNick);
                            }
                        }else if (commandArgs[0].equalsIgnoreCase("bip")) {

                            if (commandArgs[1].equalsIgnoreCase(targetNick)) {
                                ipbancommand(targetNick);

                            }
                        }
                        else if (commandArgs[0].equalsIgnoreCase("show")) {

                            if (commandArgs[1].equalsIgnoreCase("users")) {
                                MucUsersList();

                            }
                        }

                    }

                    String mChatRoomSender = XmppStringUtils.parseResource(message.getFrom());
                    String mChatRoomMessage = message.getBody();

                    if (message.getFrom().equals(muc.getNickname()))
                        return;

                    if(message.getBody() != null && getCommander(cmdgiver)){

                        boolean isCommand = false;

                        for (String command : COMMAND_LIST) {
                            if (mChatRoomMessage.toLowerCase().startsWith(command)) {
                                isCommand = true;
                                break;
                            }
                        }

                        if (isCommand) {

                            processCommand(packet, mChatRoomMessage, mChatRoomSender);
                        }

                    }

                    Log.e(TAG, mChatRoomSender + ": " + mChatRoomMessage);

                    Message botMessage = null;      //  SET AUTO-REPLY TEXTS FROM BOT IN MUC

                    try {
                        botMessage = new Message();
                        if (message.getBody().equals("nothing")) {
                            botMessage.setBody("Fine...");
                        } else if (message.getBody().equals("make me toast")) {
                            botMessage.setBody("Sure thing");
                        } else if (message.getBody().equals("hi")) {
                            botMessage.setBody("Hi too:D:P");
                        } else if (message.getBody().equals("Hi")) {
                            botMessage.setBody("Hi too:D:P");
                        } else if (message.getBody().equals("Hello")) {
                            botMessage.setBody("Hello there :)");
                        } else if (message.getBody().equals("hello")) {
                            botMessage.setBody("Hello :P");
                        } else if (message.getBody().equals(":-D")) {
                            botMessage.setBody("Why are u laughing :s:S");
                        } else if (message.getBody().equals(":-D:-D")) {
                            botMessage.setBody("Are u Mad?:-|");
                        } else if (message.getBody().equals("hahaha")) {
                            botMessage.setBody("weird laugh:d:D");
                        } else if (message.getBody().equals("lol")) {
                            botMessage.setBody("LOL :o");
                        } else if (message.getBody().equals("hm")) {
                            botMessage.setBody("hmm too:D:P");
                        } else if (message.getBody().equals(":-)")) {
                            botMessage.setBody("cute smile :)");
                        } else if (message.getBody().equals("fuck")) {
                            botMessage.setBody("hey! Don't be rude :@");
                        } else if (message.getBody().equals("sex")) {
                            botMessage.setBody("I done with my sex :p");
                        } else if (message.getBody().equals("Sex")) {
                            botMessage.setBody("I done with my sex :p");
                        } else if (message.getBody().equals("Fuck")) {
                            botMessage.setBody("hey! Don't be rude :@");
                        } else if (message.getBody().equals("bitch")) {
                            botMessage.setBody("I will kick u :@");
                        } else if (message.getBody().equals("Bitch")) {
                            botMessage.setBody("I will kick u :@");
                        } else if (message.getBody().equals("haha")) {
                            botMessage.setBody(":D:P");
                        } else if (message.getBody().equals("Hi bot")) {
                            botMessage.setBody("Hi too:D:P");
                        } else if (message.getBody().equals("Hello bot")) {
                            botMessage.setBody("Im not Bot:@");
                        } else if (message.getBody().equals("Bot")) {
                            botMessage.setBody("Yep! :)");
                        } else if (message.getBody().equals("bot")) {
                            botMessage.setBody("Yep! :)");
                        } else if (message.getBody().equals("BOT")) {
                            botMessage.setBody("Yep! :)");
                        } else if (message.getBody().equals("shit")) {
                            botMessage.setBody("Pls don't shit over here! :|:D:P");
                        }else if (message.getBody().contains("master")) {
                            botMessage.setBody("My Master is db~@NC :-)");
                        }else if (message.getBody().contains("boss")) {
                            botMessage.setBody("My Boss is db~@NC :-)");
                        }
                        else{
                            return;     //  Else DO-NOTHING #
                        }

                        //  SEND THE AUTO-REPLY TEXTS TO MUC #
                        muc.sendMessage(mChatRoomSender + ": " + botMessage.getBody());

                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                }

            }

        };

        XMPPLogic.connection.addPacketListener(packetListener, filter);

    }

    public void MucUsersList() {

        //  Iterator<String> users = (Iterator<String>) muc.getOccupants();
        //  String body = "Room Users:\n\n";
        //   while (users.hasNext()) {
        //       body = new StringBuilder(String.valueOf(body)).append(XmppStringUtils.parseResource(users.next())).append("\n").toString();
        //  }
        //  msg(body);

        try {
            ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(XMPPLogic.connection);
            DiscoverItems discoItems = null;
            try {
                discoItems = discoManager.discoverItems(muc.getRoom());
            } catch (SmackException.NoResponseException | SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

            Iterator<DiscoverItems.Item> occupants = (Iterator<DiscoverItems.Item>) discoItems.getItems();

            String body = "Room Users:\n\n";

            while (occupants.hasNext()) {
                body = String.valueOf(body) + XmppStringUtils.parseResource(String.valueOf(occupants.next())) + "\n";
                 }

            msg(body);


        }
        catch (XMPPException e) {
            e.printStackTrace();
        }


    }

    private void ipbancommand(String targetNick) {
        Message message = new Message();
        message.setTo(muc.getRoom());
        message.setType(Message.Type.groupchat);
        message.setBody("/ban f " + targetNick);
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message1 = new Message();
        message1.setTo(muc.getRoom());
        message1.setType(Message.Type.groupchat);
        message1.setBody(targetNick + " is IP-Banned.");
        try {
            muc.sendMessage(message1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    public String getJid(String targetNick)
    {
        if (targetNick.indexOf("@") > 0)
            return targetNick;

        Occupant occupant = muc.getOccupant(muc.getRoom() + "/" + targetNick);
        if (occupant == null)
            return null;

        return occupant.getJid();
    }

    private void nonecommand(String targetNick) {
        try {
            muc.revokeVoice(targetNick);
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Grant Ownership to MUC User (only if Bot ID is Owner itself)
    private void ownercommand(String targetNick) {
        try {
            muc.grantOwnership(targetNick + "@nimbuzz.com");
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody(targetNick + " is now Owner.");
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Grant Admin & Moderator to MUC User
    private void admincommand(String targetNick) {
        try {
            muc.grantModerator(targetNick);
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody(targetNick + " is now Admin.");
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Grant Moderator to MUC User
    private void moderatorcommand(String targetNick) {

        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody("/mod " + targetNick);
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message1 = new Message();
        message1.setType(Message.Type.groupchat);
        message1.setTo(muc.getRoom());
        message1.setBody(targetNick + " is now Moderator.");
        try {
            muc.sendMessage(message1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Make Visitor MUC User
    private void visitorcommand(String targetNick) {
        Message message = new Message();
        message.setTo(muc.getRoom());
        message.setType(Message.Type.groupchat);
        message.setBody("/mute " + targetNick);

        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message1 = new Message();
        message1.setType(Message.Type.groupchat);
        message1.setTo(muc.getRoom());
        message1.setBody(targetNick + " is now Visitor.");
        try {
            muc.sendMessage(message1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Provides a list of Current Bot's Commanders
    public void ShowCommanders() {

        String mrs = "Bot Masters:\n\n";

        for (int i2 = 0; i2 < masters.size(); i2++) {

            mrs = new StringBuilder(String.valueOf(mrs)).append(masters.get(i2)).append("\n").toString();
        }
        msg(mrs);

    }

    //  Remove Commander from Bot's Masterlist
    public void RemoveCommander(String targetNick) {
        int i = 0;
        do
        {
            if (i >= masters.size())
            {
                masters.remove(i);
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setType(Message.Type.groupchat);
                message.setBody(targetNick + " not found in Masterlist");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                return;
            }
            boolean flag1 = masters.get(i).trim().toLowerCase().contains(targetNick.toLowerCase().trim());
            boolean flag;
            flag = targetNick.trim().length() == masters.get(i).trim().length();
            if (flag & flag1)
            {
                masters.remove(i);
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setType(Message.Type.groupchat);
                message.setBody(targetNick + " has Removed from Masterlist");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                return;
            }
            i++;
        } while (true);
    }


    //  Add Commander to Bot's Masterlist
    public void AddCommander(String targetNick) {
        int i = 0;
        do
        {
            if (i >= masters.size())
            {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setType(Message.Type.groupchat);
                message.setBody(targetNick + " has Added to Masterlist");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                masters.add(targetNick.toLowerCase().trim());
                return;
            }
            boolean flag1 = masters.get(i).trim().toLowerCase().contains(targetNick.toLowerCase().trim());
            boolean flag;
            flag = targetNick.trim().length() == masters.get(i).trim().length();
            if (flag & flag1)
            {
                Message message = new Message();
                message.setTo(muc.getRoom());
                message.setType(Message.Type.groupchat);
                message.setBody(targetNick + " already Exist in Masterlist");
                try {
                    muc.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                return;
            }
            i++;
        } while (true);
    }


    //  To Check if commander exist in Masterlist then True (proceed ahead with Command), else return False (Do-Nothing)
    public Boolean getCommander(String targetNick)
    {
        int i = 0;
        do
        {
            if (i >= masters.size())
            {
                return Boolean.valueOf(false);
            }
            boolean flag1 = masters.get(i).trim().toLowerCase().contains(targetNick.toLowerCase().trim());
            boolean flag;
            flag = targetNick.trim().length() == masters.get(i).trim().length();
            if (flag & flag1)
            {
                return Boolean.valueOf(true);
            }
            i++;
        } while (true);
    }


    //  Grant Membership to MUC User
    private void memcommand(String targetNick) {
        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody("/mem " + targetNick);

        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Message message1 = new Message();
        message1.setTo(muc.getRoom());
        message1.setType(Message.Type.groupchat);
        message1.setBody(targetNick + " is now Member.");
        try {
            muc.sendMessage(message1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    //  Kick MUC User
    private void kickcommand(String targetNick) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        muc.kickParticipant(targetNick, "f*ck off");
        //  db~@NC WAS ONLINE
    }


    //  Ban MUC User
    private void bancommand(String targetNick) {
        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody("/ban " + targetNick);
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }


        Message message1 = new Message();
        message1.setTo(muc.getRoom());
        message1.setType(Message.Type.groupchat);
        message1.setBody(targetNick + " is Banned.");
        try {
            muc.sendMessage(message1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    //  Test Command by db~@NC
    private void testcommand() {
        Message message = new Message();
        message.setType(Message.Type.groupchat);
        message.setTo(muc.getRoom());
        message.setBody("test succeed:D");
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    //  Processing of Commands with No-Arguments .%^&***db~***&^%.
    private void processCommand(Stanza packet, String command, String mChatRoomSender) throws SmackException.NotConnectedException {

        if ("!coder".equalsIgnoreCase(command)) {
            processCoderCommand(packet, mChatRoomSender);
        }
        else if ("!details".equalsIgnoreCase(command)) {
            processDetailsCommand(packet, mChatRoomSender);
        }

        else {
            //  Else Process Commands with Arguments .%^&***db~***&^%.
            processCommandsWithArgs(packet, command, mChatRoomSender);
        }
    }

    private void processBotMasterCommand(Stanza packet, String command, String mChatRoomSender) throws SmackException.NotConnectedException {
        String mrs = "the Masters of me are\n";

        for (int i2 = 0; i2 < masters.size(); i2++) {

            mrs = new StringBuilder(String.valueOf(mrs)).append(masters.get(i2)).append("\n").toString();
        }

        msg(mrs);
        return;

    }


    //  Bot Coder Command
    private void processCoderCommand(Stanza packet, String mChatRoomSender) {
        Message message = new Message(mChatRoomSender, Message.Type.groupchat);
        message.setBody("My Coder is db~@NC :-)\n\nTo know more, visit http://dbh4ck.blogspot.in\nThanks! :-D:-P");
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Bot Details Command
    private void processDetailsCommand(Stanza packet, String mChatRoomSender) {
        Message message = new Message(mChatRoomSender, Message.Type.groupchat);
        message.setBody("I,m Nimbuzz MUC BanBot For Android Devices.\nCoded in pure Java (Android) :-)\n\nTo know more, visit http://dbh4ck.blogspot.in\n Thanks!");
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    //  Args Commands
    private void processCommandsWithArgs(Stanza packet, String command, String mChatRoomSender) {

        if (command.startsWith("!commands")) {

            processAllCommands(packet, command, mChatRoomSender);

        }

        else if (command.startsWith("!mas")) {

            try {
                processBotMasterCommand(packet, command, mChatRoomSender);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

        }

    }


    //  Universal Method Send MSG TO MUC

    public void msg(String body) {
        try {
            Message msg = new Message();
            msg.setBody(body);
            msg.setTo(muc.getRoom());
            msg.setType(Message.Type.groupchat);
            XMPPLogic.connection.sendPacket(msg);
        } catch (Exception e) {
        }
    }


    //  All Available Commands Detail
    private void processAllCommands(Stanza packet, String command, String mChatRoomSender) {
        Message message = new Message(mChatRoomSender, Message.Type.groupchat);
        StringBuffer sb = new StringBuffer();
        sb.append("\n All Commands:\n \n");
        sb.append("\t kick:  .k id\n");
        sb.append("\t member:  .m id\n");
        sb.append("\t ban:  .b id\n");
        sb.append("\t ip ban:  .bip id\n");
        sb.append("\t visitor:  .v id\n");
        sb.append("\t admin:  .a id\n");
        sb.append("\t owner:  .o id\n");
        sb.append("\t add comm.:  .addc id\n");
        sb.append("\t remove comm.:  .remc id\n");

        //  sb.append("\t auto member:  .am ON & .am OFF");
        //  sb.append("\t autoban:  .ab ON & .ab OFF");

        message.setBody(sb.toString());
        try {
            muc.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    public  String getUserJid(){
        return XMPPLogic.connection.getUser();
    }
}
