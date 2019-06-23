package eeui.android.videoView.entry;

import android.content.Context;

import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;

import app.eeui.framework.extend.annotation.ModuleEntry;
import eeui.android.videoView.component.AppvideoViewComponent;

@ModuleEntry
public class videoViewEntry {

    /**
     * APP启动会运行此函数方法
     * @param content Application
     */
    public void init(Context content) {

        try {
            WXSDKEngine.registerComponent("video-view", AppvideoViewComponent.class);
        } catch (WXException e) {
            e.printStackTrace();
        }
    }

}
