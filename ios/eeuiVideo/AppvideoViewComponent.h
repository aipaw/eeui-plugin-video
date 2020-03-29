#import "WXComponent.h"
#import <WeexSDK/WXEventModuleProtocol.h>
#import <WeexSDK/WXModuleProtocol.h>
#import <SJVideoPlayer/SJVideoPlayer.h>
#import <Masonry/Masonry.h>

NS_ASSUME_NONNULL_BEGIN

@interface AppvideoViewComponent : WXComponent <WXModuleProtocol>

@property(strong, nonatomic) SJVideoPlayer *player;

@property(strong, nonatomic) NSString *src;
@property(strong, nonatomic) NSString *title;
@property(strong, nonatomic) NSString *img;
@property(strong, nonatomic) NSString *status;
@property(nonatomic, assign) NSInteger pos;
@property(nonatomic, assign) BOOL autoPlay;
@property(nonatomic, assign) CGFloat totalTime;

@end

NS_ASSUME_NONNULL_END
