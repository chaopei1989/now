package com.nao.im.ui.activity.player;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.R;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.SameMusic;
import com.nao.im.net.GMDataCenter;
import com.nao.im.net.SameApi;
import com.nao.im.net.data.RequestManager;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.net.parser.IReplyListener;
import com.nao.im.net.parser.ReplyWrapper;
import com.nao.im.ui.activity.main.MainActivity;
import com.nao.im.util.UiUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaopei on 2015/9/11.
 * 搜索音乐页面
 */
public class SearchMusicFragment extends Fragment implements OnScrollListener, View.OnClickListener, SameApi.IRequestResponse, Response.ErrorListener, AdapterView.OnItemClickListener {

    private final static boolean DEBUG = GMEnv.DEBUG;

    private final static String TAG = DEBUG ? "SearchMusicFragment" : SearchMusicFragment.class.getSimpleName();

    private EditText mSearchEditText;

    private TextView mSearch;

    private ListView mResultList;

    private ResultAdapter mResultAdapter;

    private List<MusicEntity> mData = new ArrayList<MusicEntity>();

    private final static int MSG_TOAST = 0;

    private MainActivity mActivity;

    private View mProgress;

    private Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mActivity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_TOAST:
                    Toast.makeText(mActivity, msg.obj + "", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    View mFooter;
    TextView mFooterTextView;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (null == mActivity) {
            return;
        }
        if (!hidden) {
//            mActivity.setTitle("点歌", true);
            mSearchEditText.requestFocus();
            if (0 == mData.size()) {
                setSoftInputVisible(true);
            } else {
                setSoftInputVisible(false);
            }
        } else {
            setSoftInputVisible(false);
        }
    }

    private void setSoftInputVisible(boolean visible) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!visible) {
            imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        } else {
            imm.showSoftInput(mSearchEditText, 0);
        }
    }

    private void initViews(View root) {
        mSearchEditText = (EditText) root.findViewById(R.id.search_edit);
        mProgress = root.findViewById(R.id.progress_wait);
        mSearchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mSearch.performClick();
                    return true;
                }
                return false;
            }
        });
        mSearch = (TextView) root.findViewById(R.id.search);
        mResultList = (ListView) root.findViewById(R.id.result_list);
        mResultAdapter = new ResultAdapter();
        mResultList.setAdapter(mResultAdapter);
        mSearch.setOnClickListener(this);
        mResultList.setOnItemClickListener(this);
        mResultList.setOnScrollListener(this);
        mFooter = LayoutInflater.from(mActivity).inflate(R.layout.search_footer_view, null);
        mFooterTextView = (TextView) mFooter.findViewById(R.id.footer_text);
        mResultList.addFooterView(mFooter);
        mResultList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setSoftInputVisible(false);
                }
                return false;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_music, container, false);
        initViews(v);
        return v;
    }

    String mCurrentSearch;

    boolean searching = false;

    private void search() {
        if (!searching) {
            searching = true;
            mProgress.setVisibility(View.VISIBLE);
            SameApi.requestMusicSearch(mCurrentSearch, limit, offset, this, this, this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                mCurrentSearch = mSearchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(mCurrentSearch)) {
                    RequestManager.cancelAll(this);
                    offset = 0;
                    searching = false;
                    mData.clear();
                    mResultAdapter.notifyDataSetChanged();
                    setSoftInputVisible(false);
                    search();
                } else {
                    mH.obtainMessage(MSG_TOAST, 0, 0, "请输入搜索内容").sendToTarget();
                }
                break;
        }
    }

    @Override
    public void onResponse(final List<SameMusic> results) {
        for (SameMusic music : results) {
            if (DEBUG) {
                Log.d(TAG, "[onResponse] : music.source_url=" + music.source_url);
            }
            mData.add(new MusicEntity(music.source_url, music.name, music.album_name, music.album_art_url, music.artist_name));
        }
        mH.post(new Runnable() {
            @Override
            public void run() {
                if (0 == results.size() || 0 != results.size() % 30) {
                    offset = -1;
                } else {
                    offset = mData.size();
                }
                if (0 <= offset) {
                    mFooterTextView.setText("正在加载更多...");
                } else {
                    mFooterTextView.setText("没有更多了");
                }
                mResultAdapter.notifyDataSetChanged();
            }
        });

        mProgress.setVisibility(View.GONE);
        searching = false;
    }

    @Override
    public void onError(int code, String msg) {
        if (DEBUG) {
            Log.d(TAG, "[onError] :code=" + code);
            Log.d(TAG, "[onError] :msg=" + msg);
        }
        mProgress.setVisibility(View.GONE);
        searching = false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (DEBUG) {
            Log.d(TAG, "[onErrorResponse]", error);
        }
        mProgress.setVisibility(View.GONE);
        searching = false;
    }

    MusicConfirm mConfirm;

    void showConfirm(final MusicEntity music) {
        if (null == mConfirm) {
            mConfirm = new MusicConfirm(mActivity, R.style.Dialog);
        }
        mConfirm.cover(music.getCover())
                .artist(music.getArtist())
                .title(music.getTitle())
                .okListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            MediaPlayer mp = new MediaPlayer();
                            mp.setDataSource(music.getUrl());
                            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    if (DEBUG) {
                                        Log.d(TAG, "[onError] : what=" + what + ", extra=" + extra);
                                    }
                                    mH.obtainMessage(MSG_TOAST, 0, 0, "点歌失败，该音频不可用").sendToTarget();
                                    return true;
                                }
                            });
                            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    music.setDuration(mp.getDuration());
                                    mp.release();
                                    if (DEBUG) {
                                        Log.d(TAG, music.toString());
                                    }
                                    GMDataCenter.postChannelRequest(App.getContext(), new IReplyListener<Object>() {
                                        @Override
                                        public void onReplyParserResponse(List<Object> response) {
                                            if (DEBUG) {
                                                Log.d(TAG, "[onReplyParserResponse]");
                                            }
                                            mH.obtainMessage(MSG_TOAST, 0, 0, "点歌成功").sendToTarget();
                                        }

                                        @Override
                                        public void onReplyParserError(ReplyWrapper<Object> wrapper) {
                                            if (DEBUG) {
                                                Log.d(TAG, "[onReplyParserError] : status=" + wrapper.getStatus());
                                            }
                                            mH.obtainMessage(MSG_TOAST, 0, 0, "点歌失败，status=" + wrapper.getStatusName() + ", msg=" + wrapper.getMsg()).sendToTarget();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if (DEBUG) {
                                                Log.d(TAG, "[onErrorResponse] : error=" + error, error);
                                            }
                                            mH.obtainMessage(MSG_TOAST, 0, 0, "点歌失败，error=" + error).sendToTarget();
                                        }
                                    }, String.format("{\"userId\":%d,\"openId\":\"%s\",\"title\":\"%s\",\"cover\":\"%s\",\"url\":\"%s\",\"album\":\"%s\",\"artist\":\"%s\",\"duration\":%d}",
                                            SocialUserManager.getInstance().getSocialUser().getUserId(),
                                            SocialUserManager.getInstance().getSocialUser().getOpenId(),
                                            music.getTitle().replace("\"", "\\\""),
                                            music.getCover(),
                                            music.getUrl(),
                                            music.getAlbum().replace("\"", "\\\""),
                                            music.getArtist().replace("\"", "\\\""),
                                            music.getDuration()));
                                }
                            });
                            mp.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                            mH.obtainMessage(MSG_TOAST, 0, 0, "点歌失败，IOException = " + e.getMessage()).sendToTarget();
                        }
//                        mActivity.switchSearchFragment(false);
                        mConfirm.dismiss();
                    }
                })
                .cancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mConfirm.dismiss();
                    }
                });
        mConfirm.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (view == mFooter) {
            return;
        }
        final MusicEntity music = mData.get(position);
        showConfirm(music);
    }

    @Override
    public void onPause() {
        super.onPause();
        setSoftInputVisible(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RequestManager.cancelAll(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    int offset = 0, limit = 30;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 1 && totalItemCount == (firstVisibleItem + visibleItemCount)) {
            if (DEBUG) {
                Log.d(TAG, "[onScroll] : End of the list.");
            }
            if (1 == totalItemCount % 30 && 0 <= offset) {
                search();
            }
        }
    }

    class MusicConfirm extends Dialog {

        ImageView cover;
        TextView title;
        TextView artist;
        View ok, cancel;
        private final int IMG_WH = UiUtils.dp2px(R.dimen.av_dp_54);

        public MusicConfirm(Context context, int theme) {
            super(context, theme);
            setContentView(R.layout.dialog_music_confirm);
            cover = (ImageView) findViewById(R.id.music_cover);
            title = (TextView) findViewById(R.id.music_title);
            artist = (TextView) findViewById(R.id.music_artist);
            ok = findViewById(R.id.ok);
            cancel = findViewById(R.id.cancel);
        }

        public MusicConfirm cover(String cover) {
            Picasso.with(mActivity).load(cover).resize(IMG_WH, IMG_WH).into(this.cover);
            return this;
        }

        public MusicConfirm title(String title) {
            this.title.setText(title);
            return this;
        }

        public MusicConfirm artist(String artist) {
            this.artist.setText(artist);
            return this;
        }

        public MusicConfirm okListener(View.OnClickListener listener) {
            ok.setOnClickListener(listener);
            return this;
        }

        public MusicConfirm cancelListener(View.OnClickListener listener) {
            cancel.setOnClickListener(listener);
            return this;
        }

    }

    class ResultAdapter extends BaseAdapter {

        private final int IMG_WH = UiUtils.dp2px(R.dimen.av_dp_40);

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
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (null == convertView) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.search_result_view, null);
                holder = new Holder();
                holder.title = (TextView) convertView.findViewById(R.id.music_title);
                holder.artist = (TextView) convertView.findViewById(R.id.music_artist);
                holder.cover = (ImageView) convertView.findViewById(R.id.music_cover);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            MusicEntity music = mData.get(position);
            Picasso.with(mActivity).load(music.getCover()).resize(IMG_WH, IMG_WH).into(holder.cover);
            holder.title.setText(music.getTitle());
            holder.artist.setText(music.getArtist());
            return convertView;
        }

        class Holder {
            ImageView cover;
            TextView title;
            TextView artist;
        }
    }
}