package eeui.android.videoView.component;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

import app.eeui.framework.activity.PageActivity;
import app.eeui.framework.extend.integration.glide.Glide;
import app.eeui.framework.extend.module.eeuiConstants;
import app.eeui.framework.extend.module.eeuiPage;
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
                        mDurations.put(mUrl, (long) -1);
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
        if (getContext() instanceof PageActivity) {
            ((PageActivity) getContext()).setOnBackPressed("AppevideoComponent", () -> {
                if (mVideoView.isFullScreen()) {
                    mVideoView.stopFullScreen();
                    return true;
                }
                return false;
            });
        }
    }

    @WXComponentProp(name = "img")
    public void setImg(String url) {
        url = eeuiPage.rewriteUrl(getContext(), url);
        Glide.with(getContext()).load(url).into(mImageView);
        mImageView.setVisibility(View.VISIBLE);
    }

    @WXComponentProp(name = "autoPlay")
    public void setAutoPlay(boolean auto) {
        mAutoPlay = auto;
        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @WXComponentProp(name = "pos")
    public void setPosition(int position) {
        mVideoView.seekTo(position);
    }


    @WXComponentProp(name = "src")
    public void setSrc(String url) {
        mUrl = url;
        mVideoView.setUrl(url);
        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @WXComponentProp(name = "title")
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
