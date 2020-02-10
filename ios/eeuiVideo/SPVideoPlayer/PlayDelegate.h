//
//  PlayDelegate.h
//  Pods
//
//  Created by 郑江荣 on 2019/1/17.
//

#ifndef PlayDelegate_h
#define PlayDelegate_h


#endif /* PlayDelegate_h */
@protocol PlayDelegate <NSObject>

@optional
/**  */
- (void)onPrepare;
/**  */
- (void)onStart;
/** 播放中 */
 - (void)onPlaying;
/** 暂停 */
- (void)onPause;

/** 完成 */
- (void)onCompelete;
@end
