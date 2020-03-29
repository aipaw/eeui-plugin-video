package eeui.android.videoView.component;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.PrepareView;
import com.dueeeke.videocontroller.component.VodControlView;
import com.dueeeke.videoplayer.player.VideoView;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import app.eeui.framework.activity.PageActivity;
import app.eeui.framework.extend.integration.glide.Glide;
import app.eeui.framework.extend.module.eeuiConstants;
import app.eeui.framework.extend.module.eeuiJson;
import app.eeui.framework.extend.module.eeuiPage;
import app.eeui.framework.extend.module.eeuiParse;
import eeui.android.videoView.R;
import eeui.android.videoView.component.extend.TitleView;

public class AppvideoViewComponent extends WXVContainer<ViewGroup> {

    private View mView;
    private VideoView mVideoView;
    private ImageView mImageView;
    private TitleView mTitleView;

    private StandardVideoController mController;
    private PrepareView mPrepareView;

    private String mUrl;
    private static Map<String, Long> mDurations = new HashMap<>();

    private boolean mAutoPlay;

    private boolean isPause;    //页面是否暂停
    private boolean isPlaying;  //是否播放中（用于页面恢复后是否播放）

    private long currentPosition = 0;
    private Timer videoTimer;
    private TimerTask videoTask;

    public AppvideoViewComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected ViewGroup initComponentHostView(@NonNull Context context) {
        mView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.my_video_view, null);
        initPagerView();
        //
        if (getEvents().contains(eeuiConstants.Event.READY)) {
            fireEvent(eeuiConstants.Event.READY, null);
            if (mAutoPlay) {
                mVideoView.start();
            }
        }
        //
        return (ViewGroup) mView;
    }

    @Override
    public void addSubView(View view, int index) {

    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
        isPause = true;
        isPlaying = mVideoView.getCurrentPlayState() == VideoView.STATE_PLAYING;
        if (isPlaying) {
            mVideoView.pause();
        }
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        isPause = false;
        if (isPlaying) {
            mVideoView.resume();
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        mVideoView.release();
    }

    @Override
    protected boolean setProperty(String key, Object param) {
        return initProperty(key, param) || super.setProperty(key, param);
    }

    private boolean initProperty(String key, Object val) {
        switch (key) {
            case "eeui":
                JSONObject json = eeuiJson.parseObject(eeuiParse.parseStr(val, ""));
                if (json.size() > 0) {
                    for (Map.Entry<String, Object> entry : json.entrySet()) {
                        initProperty(entry.getKey(), entry.getValue());
                    }
                }
                return true;

            case "src":
            case "url":
                setSrc(eeuiParse.parseStr(val, ""));
                return true;

            case "img":
                setImg(eeuiParse.parseStr(val, ""));
                return true;

            case "autoPlay":
                setAutoPlay(eeuiParse.parseBool(val, true));
                return true;

            case "pos":
            case "seek":
                setPos(eeuiParse.parseInt(val));
                return true;

            case "title":
                setTitle(eeuiParse.parseStr(val, ""));
                return true;

            default:
                return false;
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void initPagerView() {
        mVideoView = mView.findViewById(R.id.v_videoview);
        mController = new StandardVideoController(getContext());            //播放控制器
        mPrepareView = new PrepareView(getContext());                       //准备播放界面
        mImageView = mPrepareView.findViewById(R.id.thumb);                 //封面图
        mController.addControlComponent(mPrepareView);
        mController.addControlComponent(new CompleteView(getContext()));    //自动完成播放界面
        mController.addControlComponent(new ErrorView(getContext()));       //错误界面
        mController.addControlComponent(new VodControlView(getContext()));  //点播控制条
        mController.addControlComponent(new GestureView(getContext()));     //滑动控制视图
        mController.setCanChangePosition(true);                             //根据是否为直播决定是否需要滑动调节进度
        //
        ImageView startPlay = mPrepareView.findViewById(R.id.start_play);
        startPlay.setOnClickListener(v -> mVideoView.start());
        //
        mTitleView = new TitleView(getContext());                   //标题栏
        mController.addControlComponent(mTitleView);
        //
        mVideoView.setEnableAudioFocus(false);
        mVideoView.setVideoController(mController);
        mVideoView.addOnStateChangeListener(new VideoView.OnStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {
                switch (playerState) {
                    case VideoView.PLAYER_NORMAL://小屏
                        fireEvent("onNormalScreen");
                        mTitleView.changeTop();
                        break;
                    case VideoView.PLAYER_FULL_SCREEN://全屏
                        fireEvent("onFullScreen");
                        mTitleView.changeTop();
                        break;
                }
            }

            @Override
            public void onPlayStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_PLAYING:
                    case VideoView.STATE_PAUSED:
                    case VideoView.STATE_BUFFERING:
                    case VideoView.STATE_BUFFERED:
                        mDurations.put(mUrl, mVideoView.getDuration());
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        Long duration = mDurations.get(mUrl);
                        mDurations.put(mUrl, duration == null ? (long) -1 : duration);
                        break;
                }
                switch (playState) {
                    case VideoView.STATE_IDLE:
                        break;
                    case VideoView.STATE_PREPARING:
                        fireEvent("onPreparing");
                        break;
                    case VideoView.STATE_PREPARED:
                        fireEvent("onPrepared");
                        break;
                    case VideoView.STATE_PLAYING:
                        if (isPause) {
                            isPlaying = true;
                            mVideoView.pause();
                        }
                        fireEvent("onStart");
                        if (getEvents().contains("onPlaying")) {
                            fireEvent("onPlaying", eeuiJson.parseObject("{current:0,total:" + mVideoView.getDuration() + ",percent:0}"));
                        }
                        break;
                    case VideoView.STATE_PAUSED:
                        fireEvent("onPause");
                        break;
                    case VideoView.STATE_BUFFERING:
                        fireEvent("onSeekIng");
                        break;
                    case VideoView.STATE_BUFFERED:
                        fireEvent("onSeekComplete");
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        fireEvent("onCompletion");
                        break;
                    case VideoView.STATE_ERROR:
                        fireEvent("onError");
                        break;
                }
            }
        });
        //
        if (getEvents().contains("onPlaying")) {
            if (videoTimer != null) {
                videoTimer.cancel();
                videoTimer = null;
                videoTask = null;
            }
            videoTimer = new Timer();
            videoTask = new TimerTask() {
                @Override
                public void run() {
                    Long duration = mDurations.get(mUrl);
                    if (duration == null) {
                        return;
                    }
                    if (currentPosition != mVideoView.getCurrentPosition()) {
                        currentPosition = mVideoView.getCurrentPosition();
                        mView.post(()-> {
                            if (currentPosition == 0 && mVideoView.getCurrentPlayState() == VideoView.STATE_PLAYBACK_COMPLETED) {
                                fireEvent("onPlaying", eeuiJson.parseObject("{current:" + duration + ",total:" + duration + ",percent:1}"));
                            } else {
                                fireEvent("onPlaying", eeuiJson.parseObject("{current:" + currentPosition + ",total:" + duration + ",percent:" + (currentPosition /(double) duration) + "}"));
                            }
                        });
                    }
                }
            };
            videoTimer.schedule(videoTask, 1000, 1000);
        }
        //
        if (getContext() instanceof PageActivity) {
            ((PageActivity) getContext()).setOnBackPressed("__AppevideoComponent", () -> {
                if (mVideoView.isFullScreen()) {
                    ((PageActivity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mVideoView.stopFullScreen();
                    return true;
                }
                return false;
            });
        }
    }


    @JSMethod
    public void setSrc(String url) {
        mUrl = eeuiPage.rewriteUrl(getContext(), url);
        mVideoView.setUrl(mUrl);
        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @JSMethod
    public void setImg(String url) {
        url = eeuiPage.rewriteUrl(getContext(), url);
        Glide.with(getContext()).load(url).into(mImageView);
        mImageView.setVisibility(View.VISIBLE);
    }

    @JSMethod
    public void setAutoPlay(boolean auto) {
        mAutoPlay = auto;
        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @JSMethod
    public void setPos(int pos) {
        mVideoView.seekTo(pos);
    }

    @JSMethod
    public void setTitle(String title) {
        mTitleView.setTitle(title);
    }

    @JSMethod
    public void seek(int sec) {
        mVideoView.seekTo(sec);
    }

    @JSMethod
    public void play() {
        mVideoView.start();
    }

    @JSMethod
    public void pause() {
        mVideoView.pause();
    }

    @JSMethod
    public void fullScreen() {
        mVideoView.startFullScreen();
    }

    @JSMethod
    public void quitFullScreen() {
        mVideoView.stopFullScreen();
    }

    @JSMethod
    public void getDuration(JSCallback callback) {
        if (callback == null) {
            return;
        }
        Long duration = mDurations.get(mUrl);
        Map<String, Object> data = new HashMap<>();
        if (duration == null) {
            data.put("status", "error");
            data.put("duration", 0);
            data.put("msg", "视频尚未开始播放无法获取时长");
            callback.invoke(data);
        } else if (duration == -1) {
            data.put("status", "error");
            data.put("duration", 0);
            data.put("msg", "视频播放失败无法获取时长");
            callback.invoke(data);
        } else {
            data.put("status", "success");
            data.put("duration", duration);
            data.put("msg", "");
            callback.invoke(data);
        }
    }
}
