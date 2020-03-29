//
//  AppvideoViewModule.m
//  Pods
//
//  Created by 高一 on 2020/2/26.
//

#import "AppvideoViewModule.h"
#import "DeviceUtil.h"
#import <WeexPluginLoader/WeexPluginLoader.h>
#import <AVFoundation/AVFoundation.h>

@implementation AppvideoViewModule

@synthesize weexInstance;

WX_PlUGIN_EXPORT_MODULE(eeuiVideo, AppvideoViewModule)
WX_EXPORT_METHOD(@selector(getDuration:call:))

-(void)getDuration:(NSString *)url call:(WXModuleCallback)call{
    if (call) {
        if (url.length == 0) {
            call(@{@"url":url, @"status":@"error", @"msg":@"请输入有效的视频地址", @"duration": @(0)});
            return;
        }
        url = [DeviceUtil rewriteUrl:url mInstance:weexInstance];
        dispatch_async(dispatch_get_main_queue(), ^{
            CGFloat duration = 0;
            NSDictionary *opts = [NSDictionary dictionaryWithObject:@(NO) forKey:AVURLAssetPreferPreciseDurationAndTimingKey];
            AVURLAsset *urlAsset = [AVURLAsset URLAssetWithURL:[NSURL URLWithString:url] options:opts]; // 初始化视频媒体文件
            CMTime videoDuration = urlAsset.duration;
            float videoDurationSeconds = (float) CMTimeGetSeconds(videoDuration);
            if (videoDurationSeconds > 0) {
                duration = videoDurationSeconds * 1000;
            }
            if (duration > 0) {
                call(@{@"url":url, @"status":@"success", @"msg":@"", @"duration": @(duration)});
            } else {
                call(@{@"url":url, @"status":@"error", @"msg":@"获取失败", @"duration": @(0)});
            }
        });
    }
}

@end
