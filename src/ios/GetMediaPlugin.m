#import "GetMediaPlugin.h"
#import <Photos/Photos.h>

@interface GetMediaPlugin : CDVPlugin
- (void)getLast:(CDVInvokedUrlCommand*)command;
@end

@implementation GetMediaPlugin

- (void)getLast:(CDVInvokedUrlCommand*)command {
    NSNumber *limit = [command.arguments objectAtIndex:0];
    PHFetchOptions *options = [[PHFetchOptions alloc] init];
    options.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO]];
    options.fetchLimit = [limit integerValue];
    PHFetchResult *results = [PHAsset fetchAssetsWithOptions:options];
    NSMutableArray *items = [NSMutableArray array];
    PHImageManager *manager = [PHImageManager defaultManager];

    dispatch_group_t group = dispatch_group_create();

    for (PHAsset *asset in results) {
        dispatch_group_enter(group);
        PHContentEditingInputRequestOptions *inputOptions = [[PHContentEditingInputRequestOptions alloc] init];
        [asset requestContentEditingInputWithOptions:inputOptions completionHandler:^(PHContentEditingInput *contentInput, NSDictionary *info) {
            NSURL *url = contentInput.fullSizeImageURL ?: (NSURL *)contentInput.audiovisualAsset;
            NSString *uti = contentInput.uniformTypeIdentifier;
            if (url) {
                [items addObject:@{@"uri": url.absoluteString, @"uti": uti ?: @""}];
            }
            dispatch_group_leave(group);
        }];
    }

    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:items];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    });
}

@end
