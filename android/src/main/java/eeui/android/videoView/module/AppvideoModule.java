package eeui.android.videoView.module;

import android.os.Handler;
import android.text.TextUtils;

import com.dueeeke.videoplayer.player.VideoView;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.util.HashMap;
import java.util.Map;

import app.eeui.framework.extend.module.eeuiPage;

public class AppvideoModule extends WXModule {

    @JSMethod
    public void getDuration(String url, JSCallback call) {
        if (call == null) {
            return;
        }
        url = eeuiPage.rewriteUrl(mWXSDKInstance, url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("status", "error");
        data.put("duration", 0);
        data.put("msg", "");
        if (TextUtils.isEmpty(url)) {
            data.put("msg", "请输入有效的视频地址");
            call.invoke(data);
            return;
        }
        //
        VideoView mVideo = new VideoView(mWXSDKInstance.getContext());
        mVideo.setUrl(url);
        mVideo.setMute(true);
        mVideo.setEnableAudioFocus(false);
        mVideo.addOnStateChangeListener(new VideoView.OnStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {
                //
            }

            @Override
            public void onPlayStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_IDLE:
                        break;
                    case VideoView.STATE_PREPARING:
                        new Handler().postDelayed(() -> {
                            data.put("status", "error");
                            data.put("msg", "获取超时");
                            call.invoke(data);
                            mVideo.release();
                        }, 30000);
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_PLAYING:
                    case VideoView.STATE_PAUSED:
                    case VideoView.STATE_BUFFERING:
                    case VideoView.STATE_BUFFERED:
                        data.put("status", "success");
                        data.put("duration", mVideo.getDuration());
                        call.invoke(data);
                        mVideo.release();
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        data.put("status", "error");
                        data.put("msg", "获取失败");
                        call.invoke(data);
                        mVideo.release();
                        break;
                }
            }
        });
        mVideo.start();
    }
}
