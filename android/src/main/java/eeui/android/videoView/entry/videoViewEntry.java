package eeui.android.videoView.entry;

import android.content.Context;

import com.dueeeke.videoplayer.ijk.IjkPlayerFactory;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;

import app.eeui.framework.extend.annotation.ModuleEntry;
import eeui.android.videoView.component.AppvideoViewComponent;
import eeui.android.videoView.module.AppvideoModule;

@ModuleEntry
public class videoViewEntry {

    /**
     * APP启动会运行此函数方法
     * @param content Application
     */
    public void init(Context content) {

        VideoViewManager.setConfig(VideoViewConfig.newBuilder().setPlayerFactory(IjkPlayerFactory.create()).build());

        try {
            WXSDKEngine.registerComponent("eeui-video", AppvideoViewComponent.class);
            WXSDKEngine.registerModule("eeuiVideo", AppvideoModule.class);
        } catch (WXException e) {
            e.printStackTrace();
        }
    }

}
