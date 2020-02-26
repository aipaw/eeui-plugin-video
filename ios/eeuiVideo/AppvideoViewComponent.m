//
//  AppvideoViewComponent.m
//  AFNetworking
//
//

#import "AppvideoViewComponent.h"
#import <WeexPluginLoader/WeexPluginLoader.h>
#import <Masonry/Masonry.h>
#import "SPVideoPlayerView.h"
#import "SPVideoPlayerControlView.h"
#import "DeviceUtil.h"
#import "SDWebImageManager.h"

@implementation AppvideoViewComponent
@synthesize weexInstance;

WX_PlUGIN_EXPORT_COMPONENT(eeui-video, AppvideoViewComponent)
WX_EXPORT_METHOD(@selector(play))
WX_EXPORT_METHOD(@selector(pause))
WX_EXPORT_METHOD(@selector(seek:))
WX_EXPORT_METHOD(@selector(fullScreen))
WX_EXPORT_METHOD(@selector(quitFullScreen))
WX_EXPORT_METHOD(@selector(getDuration:))

- (instancetype)initWithRef:(NSString *)ref type:(NSString *)type styles:(NSDictionary *)styles attributes:(NSDictionary *)attributes events:(NSArray *)events weexInstance:(WXSDKInstance *)weexInstance
{
    if( self = [super initWithRef:ref type:type styles:styles attributes:attributes events:events weexInstance:weexInstance])
    {
        self.weexInstance=weexInstance;
        _src = attributes[@"src"];
        _title = attributes[@"title"];
        _img = attributes[@"img"];
        _autoPlay = [attributes[@"autoPlay"] boolValue];
        _liveMode = [attributes[@"liveMode"] boolValue];
        _totalTime = -1;
        if (attributes[@"pos"]){
            _position = (int) [attributes[@"pos"] longLongValue] / 1000;
        }else{
            _position=0;
        }
    }
    return self;
}

-(UIView*)loadView{
    UIView *mView = [UIView new];
    mView.backgroundColor=[UIColor blackColor];
    return mView;
}

- (SPVideoItem *)videoItem {
    SPVideoItem *_videoItem=[SPVideoItem new];
    
    _videoItem                  = [[SPVideoItem alloc] init];
    _videoItem.title            = self.title;
    _videoItem.videoURL         = [self getUrl:self.src];
    _videoItem.placeholderImage = [UIImage imageNamed:@"qyplayer_aura2_background_normal_iphone_375x211_"];
    _videoItem.seekTime         = _position;
    _videoItem.fatherView       = self.view;
    
    return _videoItem;
}

- (void)updateAttributes:(NSDictionary *)attributes{
    if(attributes[@"src"])
        _src = attributes[@"src"];
    if(attributes[@"title"])
        _title = attributes[@"title"];
    if(attributes[@"img"])
        _img = attributes[@"img"];
    if(attributes[@"autoPlay"])
        _autoPlay = [attributes[@"autoPlay"] boolValue];
    if(attributes[@"pos"]){
        _position = (int)[attributes[@"pos"] longLongValue]/1000;
    }else{
        _position=0;
    }
    if(_img){
        [SDWebImageDownloader.sharedDownloader downloadImageWithURL:[self getUrl:_img] options:SDWebImageDownloaderLowPriority progress:nil completed:^(UIImage * _Nullable image, NSData * _Nullable data, NSError * _Nullable error, BOOL finished) {
            if (image) {
                self->_placeholder.image = image;
            }
        }];
    }
}

-(void)dealloc{
    
}

-(void)viewDidLoad{
    [super viewDidLoad];
    
    SPVideoPlayerView *video = [[SPVideoPlayerView alloc]init];
    _video = video;
    _video.requirePreviewView = NO;
    video.backgroundColor = [UIColor blackColor];
    [video configureControlView:nil videoItem:self.videoItem];
    if(_autoPlay){
        [video startPlay];
    }
    
    SPVideoPlayerControlView *control = (SPVideoPlayerControlView*)video.controlView;
    control.topBackButtonType = 1;
    control.liveMode = self.liveMode;
    if(control.liveMode) {
        [control hideControlView];
    }
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(onPlayTimer:) name:@"onPlayTimer" object:nil];
    [self fireEvent:@"didload" params:nil];
    
    SPVideoPlayerControlView *c = (SPVideoPlayerControlView*)_video.controlView;
    [c setImg:_img mInstance:weexInstance];
    
    UIImageView *placeholder = [UIImageView new];
    _placeholder = placeholder;
    placeholder.frame = CGRectMake(0, 0, video.frame.size.width, video.frame.size.height);
    [video addSubview:placeholder];
    
    [placeholder mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(0);
    }];
    for (UIView *v in video.subviews){
        if(v != _placeholder){
            [video bringSubviewToFront:v];
        }
    }
    
    [SDWebImageDownloader.sharedDownloader downloadImageWithURL:[self getUrl:_img] options:SDWebImageDownloaderLowPriority progress:nil completed:^(UIImage * _Nullable image, NSData * _Nullable data, NSError * _Nullable error, BOOL finished) {
        if (image) {
            placeholder.image = image;
        }
    }];
    
    _placeholder.hidden=false;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(videoPlayerStateChanged:) name:SPVideoPlayerStateChangedNSNotification object:nil];
    
}

-(void)onPlayTimer:(NSNotification*)notify{
    [self fireEvent:@"onPlaying" params:notify.userInfo];
}

/** 播放状态发生了改变 */
- (void)videoPlayerStateChanged:(NSNotification *)notification {
    SPVideoPlayerPlayState state = [notification.userInfo[@"playState"] integerValue];
    if (_totalTime == -1 && [_video getDuration] > 0) {
        _totalTime = [_video getDuration];
    }
    switch (state) {
        case SPVideoPlayerPlayStateReadyToPlay:    // 准备播放
            [self onPrepare];
            break;
            
        case SPVideoPlayerPlayStatePlaying:        // 正在播放
            [self onStart];
            _placeholder.hidden=true;
            break;
            
        case SPVideoPlayerPlayStatePause:          // 暂停播放
            [self onPause];
            break;
        case SPVideoPlayerPlayStateBuffering:      // 缓冲中
            break;
            
        case SPVideoPlayerPlayStateBufferSuccessed: // 缓冲成功
            break;
            
        case SPVideoPlayerPlayStateEndedPlay:      // 播放结束
            _placeholder.hidden=false;
            [self onCompelete];
            break;
            
        default:
            break;
    }
}

-(void)play{
    if (_video.playState == SPVideoPlayerPlayStatePlaying){
        return;
    }
    if(_video.playState == SPVideoPlayerPlayStateEndedPlay){
        [_video sp_controlViewRefreshButtonClicked:nil];
        SPVideoPlayerControlView *control = (SPVideoPlayerControlView*)_video.controlView;
        [control repeatButtonnAction:nil];
    }else if(_video.playState == SPVideoPlayerPlayStatePause){
        [_video play];
    }else{
        [_video startPlay];
    }
}

-(void)pause{
    [_video pause];
}

-(void)seek:(double)time{
    [_video seekToTime:time/1000 completionHandler:^(BOOL finished) {
        
    }];
}

-(void)fullScreen{
    [_video toFullScreen];
}

-(void)quitFullScreen{
    [_video quitFullScreen];
}

-(void)getDuration:(WXModuleKeepAliveCallback)callback {
    if (callback == nil) {
        return;
    }
    if (_totalTime == -1 && [_video getDuration] > 0) {
        _totalTime = [_video getDuration];
    }
    if (_totalTime == -1) {
        callback(@{@"status":@"error", @"msg": @"视频尚未开始播放无法获取时长", @"duration":@(0)}, NO);
    } else {
        callback(@{@"status":@"success", @"msg": @"", @"duration":@(_totalTime)}, NO);
    }
}

/**  */
- (void)onPrepare{
    [self fireEvent:@"onPrepared" params:nil];
}
/**  */
- (void)onStart{
    [self fireEvent:@"onStart" params:nil];
}
/** 播放中 */
- (void)onPlaying{
    [self fireEvent:@"onPlaying" params:nil];
}
/** 暂停 */
- (void)onPause{
    [self fireEvent:@"onPause" params:nil];
}
/** 完成 */
- (void)onCompelete{
    [self fireEvent:@"onCompletion" params:nil];
}

-(NSURL*)getUrl:(NSString*)src {
    return [NSURL URLWithString:[DeviceUtil rewriteUrl:src mInstance:weexInstance]];
}

@end
