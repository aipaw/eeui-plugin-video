//
//  AppvideoViewComponent.m
//

#import "AppvideoViewComponent.h"
#import <WeexPluginLoader/WeexPluginLoader.h>
#import "UIImageView+WebCache.h"
#import "DeviceUtil.h"

@implementation AppvideoViewComponent
@synthesize weexInstance;

WX_PlUGIN_EXPORT_COMPONENT(eeui-video, AppvideoViewComponent)
WX_EXPORT_METHOD(@selector(play))
WX_EXPORT_METHOD(@selector(pause))
WX_EXPORT_METHOD(@selector(seek:))
WX_EXPORT_METHOD(@selector(fullScreen))
WX_EXPORT_METHOD(@selector(quitFullScreen))
WX_EXPORT_METHOD(@selector(getDuration:))

- (instancetype)initWithRef:(NSString *)ref type:(NSString *)type styles:(NSDictionary *)styles attributes:(NSDictionary *)attributes events:(NSArray *)events weexInstance:(WXSDKInstance *)weexInstance {
    if (self = [super initWithRef:ref type:type styles:styles attributes:attributes events:events weexInstance:weexInstance]) {
        self.weexInstance = weexInstance;
        _pos = 0;
        _totalTime = -1;
        _status = @"暂停播放";
        for (NSString *key in styles.allKeys) {
            [self dataKey:key value:styles[key] isUpdate:NO];
        }
        for (NSString *key in attributes.allKeys) {
            [self dataKey:key value:attributes[key] isUpdate:NO];
        }
    }
    return self;
}

- (UIView *)loadView {
    UIView * containerView = [[UIView alloc] init];

    _player = [SJVideoPlayer player];
    //_player.pausedToKeepAppearState = YES;                  //是否在暂停时保持控制层显示
    _player.autoplayWhenSetNewAsset = _autoPlay;            //自动播放
    _player.rotationManager.disabledAutorotation = YES;     //禁止自动旋转
    _player.defaultEdgeControlLayer.hiddenBackButtonWhenOrientationIsPortrait = YES;    //取消竖屏返回按钮
    _player.resumePlaybackWhenAppDidEnterForeground = YES;  //进入前台恢复播放

    _player.URLAsset = [[SJVideoPlayerURLAsset alloc] initWithURL:[NSURL URLWithString:_src] startPosition:(double) _pos / 1000];
    _player.URLAsset.title = _title;

    if (_img.length > 0) {
        [_player.presentView.placeholderImageView sd_setImageWithURL:[NSURL URLWithString:_img]];
    }

    _player.playbackObserver.currentTimeDidChangeExeBlock = ^(__kindof SJBaseVideoPlayer * _Nonnull player) {
        [self fireEvent:@"onPlaying" params:@{@"current":@((int) (player.currentTime * 1000)), @"total":@((int) (player.duration * 1000)), @"percent":@(player.rate)}];
    };

    _player.playbackObserver.timeControlStatusDidChangeExeBlock = ^(__kindof SJBaseVideoPlayer * _Nonnull player) {
        if (player.isPlaying && [_status isEqualToString:@"暂停播放"]) {
            _status = @"开始播放";
            [self fireEvent:@"onStart" params:nil];
        } else if (player.isPaused && [_status isEqualToString:@"开始播放"]) {
            _status = @"暂停播放";
            if (player.isPlaybackFinished) {
                [self fireEvent:@"onCompletion" params:nil];
            }else {
                [self fireEvent:@"onPause" params:nil];
            }
        }
    };

    [containerView addSubview:_player.view];

    [_player.view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.offset(0);
    }];

    return containerView;
}

- (void)updateStyles:(NSDictionary *)styles {
    for (NSString *key in styles.allKeys) {
        [self dataKey:key value:styles[key] isUpdate:YES];
    }
}

- (void)updateAttributes:(NSDictionary *)attributes {
    for (NSString *key in attributes.allKeys) {
        [self dataKey:key value:attributes[key] isUpdate:YES];
    }
}

#pragma mark data

- (void)dataKey:(NSString *)key value:(id)value isUpdate:(BOOL)isUpdate {
    key = [DeviceUtil convertToCamelCaseFromSnakeCase:key];
    if ([key isEqualToString:@"eeui"] && [value isKindOfClass:[NSDictionary class]]) {
        NSArray *array = [value allKeys];
        for (NSString *k in array) {
            [self dataKey:k value:value[k] isUpdate:isUpdate];
        }
    } else if ([key isEqualToString:@"src"] || [key isEqualToString:@"url"]) {
        _src = [[self getUrl:[WXConvert NSString:value]] absoluteString];
        if (isUpdate) {
            [self setSrc:_src];
        }
    } else if ([key isEqualToString:@"img"]) {
        _img = [[self getUrl:[WXConvert NSString:value]] absoluteString];
        if (isUpdate) {
            [self setImg:_img];
        }
    } else if ([key isEqualToString:@"autoPlay"]) {
        _autoPlay = [WXConvert BOOL:value];
        if (isUpdate) {
            [self setAutoPlay:_autoPlay];
        }
    } else if ([key isEqualToString:@"pos"] || [key isEqualToString:@"seek"]) {
        _pos = [WXConvert NSInteger:value];
        if (isUpdate) {
            [self setPos:_pos];
        }
    } else if ([key isEqualToString:@"title"]) {
        _title = [WXConvert NSString:value];
        if (isUpdate) {
            [self setTitle:_title];
        }
    }
}

- (void) setSrc:(NSString *)src {
    _src = src;
    _player.URLAsset = [[SJVideoPlayerURLAsset alloc] initWithURL:[NSURL URLWithString:_src]];
    _player.URLAsset.title = _title;
}

- (void) setImg:(NSString *)img {
    _img = img;
    if (_img.length > 0) {
        [_player.presentView.placeholderImageView sd_setImageWithURL:[NSURL URLWithString:_img]];
    }
}

- (void) setAutoPlay:(BOOL)autoPlay {
    _autoPlay = autoPlay;
    _player.autoplayWhenSetNewAsset = _autoPlay;
}

- (void) setPos:(NSInteger)pos {
    _pos = pos;
    [_player seekToTime:(double)_pos / 1000 completionHandler:nil];
}

- (void) setTitle:(NSString *)title {
    _title = title;
    _player.URLAsset.title = _title;
}

- (void) play {
    [_player play];
}

- (void) seek:(NSInteger)pos {
    [self setPos:pos];
}

- (void) pause {
    [_player pause];
}

- (void) fullScreen {
    [_player rotate:SJOrientation_LandscapeLeft animated:YES];
}

- (void) quitFullScreen {
    [_player rotate:SJOrientation_Portrait animated:YES];
}

-(void)getDuration:(WXModuleKeepAliveCallback)callback {
    if (callback == nil) {
        return;
    }
    if (_totalTime == -1 && [_player duration] > 0) {
        _totalTime = [_player duration];
    }
    if (_totalTime == -1) {
        callback(@{@"status":@"error", @"msg": @"视频尚未开始播放无法获取时长", @"duration":@(0)}, NO);
    } else {
        callback(@{@"status":@"success", @"msg": @"", @"duration":@(_totalTime)}, NO);
    }
}

- (NSURL *)getUrl:(NSString *)src {
    return [NSURL URLWithString:[DeviceUtil rewriteUrl:src mInstance:weexInstance]];
}

@end
