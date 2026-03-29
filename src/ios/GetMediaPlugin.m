#import "GetMediaPlugin.h"
#import <Photos/Photos.h>

@implementation GetMediaPlugin

- (void)getLast:(CDVInvokedUrlCommand*)command {
    NSInteger limit = [command.arguments[0] integerValue];
    
    PHFetchOptions *options = [[PHFetchOptions alloc] init];
    // Sort by creationDate descending to get the newest photos first
    options.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO]];
    
    // Fetch only images
    PHFetchResult<PHAsset *> *results = [PHAsset fetchAssetsWithMediaType:PHAssetMediaTypeImage options:options];
    
    NSMutableArray *items = [NSMutableArray array];
    dispatch_group_t group = dispatch_group_create();

    NSInteger count = MIN(results.count, limit);
    
    for (NSInteger i = 0; i < count; i++) {
        PHAsset *asset = results[i];
        dispatch_group_enter(group);
        
        NSString *localId = asset.localIdentifier;
        
        // Convert creationDate to milliseconds timestamp
        long long timestamp = (long long)([asset.creationDate timeIntervalSince1970] * 1000);
        
        PHContentEditingInputRequestOptions *inputOpts = [[PHContentEditingInputRequestOptions alloc] init];
        [asset requestContentEditingInputWithOptions:inputOpts completionHandler:^(PHContentEditingInput *input, NSDictionary *info) {
            
            NSURL *url = input.fullSizeImageURL;
            NSString *path = url ? url.path : @"";
            NSString *filename = url ? url.lastPathComponent : @"";
            NSString *mime = input.uniformTypeIdentifier ?: @"";
            
            NSData *data = url ? [NSData dataWithContentsOfURL:url] : nil;
            NSString *b64 = data ? [data base64EncodedStringWithOptions:0] : @"";
            
            NSDictionary *dict = @{
                @"id": localId,
                @"mimeType": mime,
                @"path": path,
                @"filename": filename,
                @"timestamp": @(timestamp), // Numerical timestamp in ms
                @"base64": b64
            };
            
            // Using a thread-safe way to add to the array since this is an async block
            @synchronized (items) {
                [items addObject:dict];
            }
            
            dispatch_group_leave(group);
        }];
    }

    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:items];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
    });
}

@end
