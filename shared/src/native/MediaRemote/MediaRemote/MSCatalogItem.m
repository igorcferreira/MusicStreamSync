//
//  MSCatalogItem.m
//  MediaRemote
//
//  Created by Igor Ferreira on 14/04/2025.
//
#import "MSCatalogItem.h"
#import <Foundation/Foundation.h>

@implementation MSCatalogItem

- (nullable instancetype)initWith:(nonnull NSDictionary *)information {

    NSString * catalogId = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoiTunesStoreSubscriptionAdamIdentifier"];
    if (catalogId == nil) {
        catalogId = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoiTunesStoreIdentifier"];
    }
    if (catalogId == nil) {
        return nil;
    }

    NSString *title = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoTitle"];
    if (title == nil || [title isEqualToString:@""]) {
        return nil;
    }

    NSString *artist = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoArtist"];
    if (artist == nil || [artist isEqualToString:@""]) {
        return nil;
    }

    self = [super init];
    if (self) {
        self.catalogId = catalogId;
        self.title = title;
        self.artist = artist;
        self.duration = [[information objectForKey:@"kMRMediaRemoteNowPlayingInfoDuration"] doubleValue];
        self.elapsedTime = [[information objectForKey:@"kMRMediaRemoteNowPlayingInfoElapsedTime"] doubleValue];
        self.album = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoAlbum"];
        self.artworkData = [information objectForKey:@"kMRMediaRemoteNowPlayingInfoArtworkData"];

    }
    return self;
}

@end
