package eeui.android.videoView.component.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.taobao.weex.WXSDKInstance;

import org.song.videoplayer.DemoQSVideoView;
import org.song.videoplayer.Util;

import eeui.android.videoView.R;

public class VideoView extends DemoQSVideoView {

    public WXSDKInstance instace;

    public VideoView(Context context) {
        super(context);
    }

    public boolean liveMode = false;

    @Override
    protected boolean showWifiDialog() {
        if (!isShowWifiDialog)
            return false;
        AlertDialog.Builder builder = new AlertDialog.Builder(instace.getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                prepareMediaPlayer();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
        return true;
    }

    @Override
    public void showChangeViews(View... views) {
        if (liveMode) {
            for (View v : changeViews)
                if (v != null) {
                    v.setVisibility(INVISIBLE);
                }
        } else {
            super.showChangeViews(views);
        }

    }

    @Override
    public void enterWindowFullscreen() {
        if (currentMode == MODE_WINDOW_NORMAL) {
            super.enterWindowFullscreen();
            Util.SET_LANDSCAPE(instace.getContext());
            ViewGroup vp = (ViewGroup) videoView.getParent();
            if (vp != null) {
                vp.removeView(videoView);
            }
            ViewGroup decorView = (ViewGroup) (Util.scanForActivity(instace.getContext())).getWindow().getDecorView();
            decorView.addView(videoView, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    @Override
    public void quitWindowFullscreen() {
        super.quitWindowFullscreen();
        Util.SET_PORTRAIT(instace.getContext());
    }
}
