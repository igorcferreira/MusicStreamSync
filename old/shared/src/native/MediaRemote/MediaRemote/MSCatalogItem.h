//
//  MSCatalogItem.h
//  MediaRemote
//
//  Created by Igor Ferreira on 19/03/2025.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MSCatalogItem : NSObject

@property (nonatomic, strong) NSString *catalogId;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *artist;
@property (nonatomic, assign) NSTimeInterval duration;
@property (nonatomic, assign) NSTimeInterval elapsedTime;
@property (nonatomic, nullable, strong) NSString *album;
@property (nonatomic, nullable, strong) NSData *artworkData;

- (nullable instancetype) initWith:(NSDictionary *)information;

@end

NS_ASSUME_NONNULL_END
