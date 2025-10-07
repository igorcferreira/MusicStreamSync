//
//  MediaRemote.h
//  MediaRemote
//
//  Created by Igor Ferreira on 19/03/2025.
//
#ifndef MSMediaRemote_h
#define MSMediaRemote_h
#import <Foundation/Foundation.h>
#import "MSCatalogItem.h"

@interface MSMediaRemote : NSObject
- (void) pause;
- (void) resume;
- (void) getCurrentItem:(void (^)(MSCatalogItem *content))completion;
@end

#endif
