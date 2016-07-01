package com.nao.im.ui.activity.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.model.Channel;
import com.nao.im.model.Message;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.player.AVIMManager;
import com.nao.im.player.ChannelService;
import com.nao.im.player.IChannelService;
import com.nao.im.ui.activity.main.MainActivity;
import com.nao.im.ui.activity.view.OnLayoutListView;
import com.nao.im.util.SysUtils;
import com.squareup.picasso.Picasso;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chaopei on 2015/11/6.
 */
public class IMFragment extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener, OnLayoutListView.OnLayoutChangedListener {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "IMFragment" : IMFragment.class.getSimpleName();

    private OnLayoutListView mChatList;

    private ChatAdapter mChatAdapter;

    private View mSend;

    private EditText mMsgEdit;

    private MainActivity mActivity;

    private List<Message> mData = new ArrayList<Message>();

    private final static int MSG_UI_NOTIFY_DATA_CHANGED = 0, MSG_UI_SEND_SUCCESS = 1, MSG_TOAST = 2, MSG_UI_CURR_USER = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_im, container, false);
        initViews(v);
        initData();
        initReceiver();
        return v;
    }

    private void initViews(View parent) {
        mChatList = (OnLayoutListView) parent.findViewById(R.id.chat_list);
        mChatList.setOnLayoutChangedListener(this);
        mChatAdapter = new ChatAdapter();
        mChatList.setAdapter(mChatAdapter);
        mChatList.addFooterView(new View(mActivity));
        mChatList.addHeaderView(new View(mActivity));
        mChatList.setOnScrollListener(this);
        mSend = parent.findViewById(R.id.msg_send);
        mSend.setOnClickListener(this);
        mMsgEdit = (EditText) parent.findViewById(R.id.msg_edit);
    }

    private Handler mH = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_UI_NOTIFY_DATA_CHANGED:
                    if (bottom) {
                        mChatAdapter.notifyDataSetChanged();
                        mChatList.setSelection(mData.size() - 1);
                    } else {
                        mChatAdapter.notifyDataSetChanged();
                    }
                    break;
                case MSG_UI_SEND_SUCCESS:
                    mChatAdapter.notifyDataSetChanged();
                    mChatList.setSelection(mData.size() - 1);
                    break;
                case MSG_TOAST:
                    Toast.makeText(mActivity, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UI_CURR_USER:

                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        clearNotification();
    }

    BroadcastReceiver mChatReceiver;

    private void initReceiver() {
        mChatReceiver = new ChatReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AVIMManager.Constants.ACTION_MSG_RECEIVE);
        filter.addAction(AVIMManager.Constants.ACTION_MSG_SEND_FAILED);
        filter.addAction(AVIMManager.Constants.ACTION_MSG_SEND_SUCCESS);
        SysUtils.registerBroadcastReceiver(mActivity, mChatReceiver, filter);
    }

    int lastMsgId = -1;

    boolean bottom = true;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            bottom = true;
        } else {
            bottom = false;
        }
    }

    public void setSoftInputVisible(boolean visible) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!visible) {
            imm.hideSoftInputFromWindow(mMsgEdit.getWindowToken(), 0);
        } else {
            imm.showSoftInput(mMsgEdit, 0);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onChanged(View view, boolean changed, int l, int t, int r, int b) {
        if (DEBUG) {
            Log.d(TAG, "[onChanged] : bottom=" + bottom);
        }
        if (bottom) {
            mChatList.setSelection(mData.size() - 1);
        }
    }

    class ChatReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AVIMManager.Constants.ACTION_MSG_RECEIVE.equals(intent.getAction())) {
                clearNotification();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IChannelService service = ChannelService.getService();
                            if (!service.isEnable()) {
                                return;
                            }
                            Channel channel = service.getChannel();
                            if (null == channel) {
                                return;
                            }
                            List<Message> data = DataSupport.where("id>" + lastMsgId + " AND channelId=" + channel.getChannelId()).order("timestamp asc").find(Message.class);
                            if (data.size() > 0) {
                                lastMsgId = data.get(data.size() - 1).getId();
                                mData.addAll(data);
                                mH.sendEmptyMessage(MSG_UI_NOTIFY_DATA_CHANGED);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else if (AVIMManager.Constants.ACTION_MSG_SEND_SUCCESS.equals(intent.getAction())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IChannelService service = ChannelService.getService();
                            if (!service.isEnable()) {
                                return;
                            }
                            Channel channel = service.getChannel();
                            if (null == channel) {
                                return;
                            }
                            List<Message> data = DataSupport.where("id>" + lastMsgId + " AND channelId=" + channel.getChannelId()).order("timestamp asc").find(Message.class);
                            if (data.size() > 0) {
                                lastMsgId = data.get(data.size() - 1).getId();
                                mData.addAll(data);
                                mH.sendEmptyMessage(MSG_UI_SEND_SUCCESS);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else if (AVIMManager.Constants.ACTION_MSG_SEND_FAILED.equals(intent.getAction())) {

            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.unregisterReceiver(mChatReceiver);
    }

    private Comparator<Message> mMessageComparator = new Comparator<Message>() {
        @Override
        public int compare(Message lhs, Message rhs) {
            long d = lhs.getTimestamp().getTime() - rhs.getTimestamp().getTime();
            if (d > 0) {
                return 1;
            } else if(d < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IChannelService service = ChannelService.getService();
                    if (!service.isEnable()) {
                        return;
                    }
                    Channel channel = service.getChannel();
                    if (null == channel) {
                        return;
                    }
                    mData = DataSupport.where("channelId=" + channel.getChannelId()).order("timestamp desc").limit(30).find(Message.class);
                    if (mData.size() > 0) {
                        lastMsgId = mData.get(0).getId();
                        Collections.sort(mData, mMessageComparator);
                    }
                    mH.sendEmptyMessage(MSG_UI_NOTIFY_DATA_CHANGED);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.msg_send:
                String msg = mMsgEdit.getText().toString();
                if (TextUtils.isEmpty(msg.trim())) {
                    mH.obtainMessage(MSG_TOAST, 0 ,0, "消息不得为空").sendToTarget();
                    return;
                }
                try {
                    mMsgEdit.setText("");
                    AVIMManager.getInstance().sendMessage(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG) {
            Log.d(TAG, "[onConfigurationChanged] : bottom=" + bottom);
        }
        if (bottom) {
            mChatList.setSelection(mData.size() - 1);
        }
    }

    private void clearNotification() {
        try {
            AVIMManager.getInstance().clearNotification();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class ChatAdapter extends BaseAdapter {
        final int TYPE_RIGHT = 0, TYPE_LEFT = 1, TYPE_TIME = 2;

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            Message msg = mData.get(position);
            if (msg.getFromId() == SocialUserManager.getInstance().getSocialUser().getUserId()) {
                return TYPE_RIGHT;
            } else {
                return TYPE_LEFT;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int viewType = getItemViewType(position);
            Holder holder = null;
            if (null == convertView) {
                switch (viewType) {
                    case TYPE_LEFT:
                        convertView = LayoutInflater.from(mActivity).inflate(R.layout.view_chat_item_left, parent, false);
                        holder = new Holder();
                        holder.icon = (ImageView) convertView.findViewById(R.id.user_icon_left);
                        holder.userName = (TextView) convertView.findViewById(R.id.user_name);
                        holder.message = (TextView) convertView.findViewById(R.id.msg);
                        convertView.setTag(holder);
                        break;
                    case TYPE_RIGHT:
                        convertView = LayoutInflater.from(mActivity).inflate(R.layout.view_chat_item_right, parent, false);
                        holder = new Holder();
                        holder.icon = (ImageView) convertView.findViewById(R.id.user_icon_right);
                        holder.userName = (TextView) convertView.findViewById(R.id.user_name);
                        holder.message = (TextView) convertView.findViewById(R.id.msg);
                        convertView.setTag(holder);
                        break;
                }
            } else {
                holder = (Holder) convertView.getTag();
            }
            Message msg = mData.get(position);
            if (msg.getFromId() == SocialUserManager.getInstance().getSocialUser().getUserId()) {
                holder.userName.setText("我");
            } else {
                holder.userName.setText(msg.getFromUserName());
            }
            Picasso.with(mActivity).load(msg.getIconUrl()).into(holder.icon);
            holder.message.setText(msg.getTxt());
            return convertView;
        }

        class Holder {
            ImageView icon;
            TextView userName;
            TextView message;
        }
    }
}
