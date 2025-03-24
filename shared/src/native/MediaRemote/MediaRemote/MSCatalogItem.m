#import "MSCatalogItem.h"
#import <Foundation/Foundation.h>

/**
 Example of Music Item Dictionary of nothing playing:
 {
     kMRMediaRemoteNowPlayingInfoAlbum = "";
     kMRMediaRemoteNowPlayingInfoArtist = "";
     kMRMediaRemoteNowPlayingInfoContentItemIdentifier = 24065732;
     kMRMediaRemoteNowPlayingInfoDuration = "714.2935";
     kMRMediaRemoteNowPlayingInfoElapsedTime = "714.2935";
     kMRMediaRemoteNowPlayingInfoPlaybackRate = 0;
     kMRMediaRemoteNowPlayingInfoTimestamp = "2025-03-22 22:35:21 +0000";
     kMRMediaRemoteNowPlayingInfoTitle = "";
     kMRMediaRemoteNowPlayingInfoUniqueIdentifier = 24065732;
 }

 Example of Music Item Dictionary of playing item:
 {
     kMRMediaRemoteNowPlayingInfoAlbum = "Siren - Single";
     kMRMediaRemoteNowPlayingInfoAlbumiTunesStoreAdamIdentifier = 1801524852;
     kMRMediaRemoteNowPlayingInfoArtist = "Lydia the Bard & Tony Halliwell";
     kMRMediaRemoteNowPlayingInfoArtistiTunesStoreAdamIdentifier = 1656621383;
     kMRMediaRemoteNowPlayingInfoArtworkData = {length = 69296, bytes = 0xffd8ffe0 00104a46 49460001 01000048 ... 973a9f0b fbcfffd9 };
     kMRMediaRemoteNowPlayingInfoArtworkDataHeight = 600;
     kMRMediaRemoteNowPlayingInfoArtworkDataWidth = 600;
     kMRMediaRemoteNowPlayingInfoArtworkIdentifier = 81e2a209512d1493f4242274d8682007fbe19718591fe7ad70233be70248a540;
     kMRMediaRemoteNowPlayingInfoArtworkMIMEType = "image/jpeg";
     kMRMediaRemoteNowPlayingInfoComposer = "Tony Halliwell & Lydia Buckley";
     kMRMediaRemoteNowPlayingInfoContentItemIdentifier = 1801524853;
     kMRMediaRemoteNowPlayingInfoDuration = "236.043";
     kMRMediaRemoteNowPlayingInfoElapsedTime = "0.216";
     kMRMediaRemoteNowPlayingInfoGenre = "Singer/Songwriter";
     kMRMediaRemoteNowPlayingInfoIsMusicApp = 1;
     kMRMediaRemoteNowPlayingInfoMediaType = MRMediaRemoteMediaTypeMusic;
     kMRMediaRemoteNowPlayingInfoPlaybackRate = 1;
     kMRMediaRemoteNowPlayingInfoQueueIndex = 0;
     kMRMediaRemoteNowPlayingInfoRepeatMode = 1;
     kMRMediaRemoteNowPlayingInfoShuffleMode = 1;
     kMRMediaRemoteNowPlayingInfoTimestamp = "2025-03-22 22:46:28 +0000";
     kMRMediaRemoteNowPlayingInfoTitle = Siren;
     kMRMediaRemoteNowPlayingInfoTotalQueueCount = 0;
     kMRMediaRemoteNowPlayingInfoTotalTrackCount = 0;
     kMRMediaRemoteNowPlayingInfoTrackNumber = 1;
     kMRMediaRemoteNowPlayingInfoUniqueIdentifier = "-2661353853852821734";
     kMRMediaRemoteNowPlayingInfoiTunesStoreIdentifier = 1801524853;
     kMRMediaRemoteNowPlayingInfoiTunesStoreSubscriptionAdamIdentifier = 1801524853;
 }
 */

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
