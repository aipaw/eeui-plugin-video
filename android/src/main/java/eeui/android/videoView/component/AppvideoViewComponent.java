package eeui.android.videoView.component;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import org.song.videoplayer.PlayListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import app.eeui.framework.activity.PageActivity;
import app.eeui.framework.extend.integration.glide.Glide;
import app.eeui.framework.extend.module.eeuiPage;
import eeui.android.videoView.component.view.VideoView;

import static org.song.videoplayer.IVideoPlayer.MODE_WINDOW_FULLSCREEN;

public class AppvideoViewComponent extends WXVContainer<VideoView> {

    private Timer timer;
    private String url = "";
    private String title = "";

    public AppvideoViewComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected VideoView initComponentHostView(@NonNull Context context) {
        VideoView mVideoView = new VideoView(context);
        mVideoView.instace = getInstance();
        mVideoView.setPlayListener(new PlayListener() {
            @Override
            public void onStatus(int status) {

            }

            @Override
            public void onMode(int mode) {
                if (mode == 101) {
                    AppvideoViewComponent.this.fireEvent("onFullScreen");
                } else {
                    AppvideoViewComponent.this.fireEvent("onNormalScreen");
                }
            }

            @Override
            public void onEvent(int status, Integer... extra) {
                if (status == 11) {
                    AppvideoViewComponent.this.fireEvent("onPrepared");
                } else if (status == 12) {
                    AppvideoViewComponent.this.fireEvent("onStart");
                    statTimer();
                } else if (status == 13) {
                    AppvideoViewComponent.this.fireEvent("onPause");
                } else if (status == 18) {
                    AppvideoViewComponent.this.fireEvent("onCompletion");
                    cancelTimer();
                    firePlaying(true);
                } else if (status == 20) {
                    AppvideoViewComponent.this.fireEvent("onSeekComplete");
                } else if (status == 16) {
                    AppvideoViewComponent.this.fireEvent("onError");
                }
            }
        });
        if (getContext() instanceof PageActivity) {
            ((PageActivity) getContext()).setOnBackPressed("AppevideoComponent", () -> {
                if (mVideoView.getCurrentMode() == MODE_WINDOW_FULLSCREEN) {
                    mVideoView.quitWindowFullscreen();
                    return true;
                }
                return false;
            });
        }

        return mVideoView;
    }

    @WXComponentProp(name = "img")
    public void setImg(String src) {
        src = eeuiPage.rewriteUrl(getInstance(), src);
        if (getHostView() != null) {
            Glide.with((Activity) getContext()).load(src).into(getHostView().getCoverImageView());
        }
    }

    @WXComponentProp(name = "liveMode")
    public void setLiveMode(boolean live) {
        getHostView().liveMode = live;
        if (live) {
            getHostView().showChangeViews();
        }
    }


    @WXComponentProp(name = "autoPlay")
    public void setAutoPlay(boolean auto) {
        if (auto) {
            if (getHostView().getUrl() != null) {
                this.play();
            }
        }
    }

    @WXComponentProp(name = "pos")
    public void setPosition(int position) {
        if (getHostView() != null) {
            getHostView().seekTo(position);
        }
    }


    @WXComponentProp(name = "src")
    public void setSrc(String src) {
        this.url = eeuiPage.rewriteUrl(getInstance(), src);
        getHostView().setUp(this.url, this.title + "");
    }

    @WXComponentProp(name = "title")
    public void setTitle(String title) {
        this.title = title;
        getHostView().setUp(this.url, this.title + "");
    }

    @JSMethod
    public void seek(int sec) {
        getHostView().seekTo(sec);
    }


    @JSMethod
    public void play() {
        getHostView().play();
    }

    @JSMethod
    public void pause() {
        getHostView().pause();
    }

    @JSMethod
    public void fullScreen() {
        getHostView().enterWindowFullscreen();
    }

    @JSMethod
    public void quitFullScreen() {
        getHostView().quitWindowFullscreen();

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        firePlaying(false);
                    }
                });

            }
            super.handleMessage(msg);
        }
    };

    private void firePlaying(boolean compelete) {
        Map<String, Object> m = new HashMap<>();
        m.put("current", getHostView().getPosition());
        m.put("total", getHostView().getDuration());

        if (compelete) {
            m.put("percent", 1);
            fireEvent("onPlaying", m);
            cancelTimer();
        } else {
            if (getHostView().getDuration() != 0)
                m.put("percent", getHostView().getPosition() / (float) getHostView().getDuration());
            else
                m.put("percent", 0);
            fireEvent("onPlaying", m);
        }
    }

    private void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

    private void statTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(timerTask, 0, 500);
    }
}
