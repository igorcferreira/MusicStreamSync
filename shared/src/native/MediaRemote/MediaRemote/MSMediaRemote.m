//
//  MediaRemote.m
//  MediaRemote
//
//  Created by Igor Ferreira on 14/04/2025.
//
#import "MSMediaRemote.h"
#import "MSCatalogItem.h"
#import "Music.h"

const NSTimeInterval kPollingInterval = 10.0;

@interface MSMediaRemote()

@property (nonatomic, retain) MusicApplication *music;

@end

@implementation MSMediaRemote

@synthesize music;

- (id) init {
    self = [super init];
    if (self) {
        self.music = [SBApplication applicationWithBundleIdentifier:@"com.apple.music"];
    }
    return self;
}

- (void)dealloc {
    [[NSDistributedNotificationCenter defaultCenter] removeObserver:self name:nil object:nil];
    self.music = nil;
}

- (void)pause {
    [self.music playpause];
}

- (void)resume {
    [self.music pause];
}

- (void)getCurrentItem:(void (^__strong)(MSCatalogItem *))completion {
    MSCatalogItem *content = [self fetchCatalogItem];
    completion(content);
}

-(MSCatalogItem *)fetchCatalogItem {
    MusicTrack *currentTrack = nil;
    
    if ([self.music isRunning] && [self.music playerState] == MusicEPlSPlaying) {
        currentTrack = [self.music currentTrack];
    }
    
    if (currentTrack == nil) {
        return nil;
    }
    
    MSCatalogItem *item = [[MSCatalogItem alloc] init];
    item.title = [currentTrack name];
    item.artist = [currentTrack artist];
    item.album = [currentTrack album];
    item.artworkData = [[[currentTrack artworks] firstObject] rawData];
    item.duration = [currentTrack duration];
    item.elapsedTime = [self.music playerPosition];
    item.catalogId = [NSString stringWithFormat:@"%ld", [currentTrack id]];
    return item;
}

@end
