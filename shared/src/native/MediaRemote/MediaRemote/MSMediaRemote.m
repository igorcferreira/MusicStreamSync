#import "MSMediaRemote.h"
#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>
#import <objc/runtime.h>
#import "Enums.h"
#import "MSCatalogItem.h"

typedef void (*MRMediaRemoteGetNowPlayingInfoFunction)(dispatch_queue_t queue, void (^handler)(NSDictionary* information));
typedef void (*MRMediaRemoteSetElapsedTimeFunction)(double time);
typedef Boolean (*MRMediaRemoteSendCommandFunction)(MRMediaRemoteCommand cmd, NSDictionary* userInfo);

NSDictionary<NSString*, NSNumber*> *cmdTranslate = @{
    @"play": @(MRMediaRemoteCommandPlay),
    @"pause": @(MRMediaRemoteCommandPause),
    @"togglePlayPause": @(MRMediaRemoteCommandTogglePlayPause),
    @"next": @(MRMediaRemoteCommandNextTrack),
    @"previous": @(MRMediaRemoteCommandPreviousTrack),
};

@implementation MSMediaRemote

- (void)getCurrentItem:(void (^__strong)(MSCatalogItem *__strong))completion {
    CFURLRef ref = (__bridge CFURLRef) [NSURL fileURLWithPath:@"/System/Library/PrivateFrameworks/MediaRemote.framework"];
    CFBundleRef bundle = CFBundleCreate(kCFAllocatorDefault, ref);
    MRMediaRemoteGetNowPlayingInfoFunction MRMediaRemoteGetNowPlayingInfo = (MRMediaRemoteGetNowPlayingInfoFunction) CFBundleGetFunctionPointerForName(bundle, CFSTR("MRMediaRemoteGetNowPlayingInfo"));
    MRMediaRemoteGetNowPlayingInfo(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(NSDictionary* information) {
        MSCatalogItem *item = [[MSCatalogItem alloc] initWith: information];
        completion(item);
    });
}

- (void)pause {
    [self exec:@"pause"];
}
- (void)resume {
    [self exec:@"play"];
}
- (void) exec:(NSString *)command {
    CFURLRef ref = (__bridge CFURLRef) [NSURL fileURLWithPath:@"/System/Library/PrivateFrameworks/MediaRemote.framework"];
    CFBundleRef bundle = CFBundleCreate(kCFAllocatorDefault, ref);
    MRMediaRemoteSendCommandFunction MRMediaRemoteSendCommand = (MRMediaRemoteSendCommandFunction) CFBundleGetFunctionPointerForName(bundle, CFSTR("MRMediaRemoteSendCommand"));
    MRMediaRemoteSendCommand((MRMediaRemoteCommand) [cmdTranslate[command] intValue], nil);
}

@end
