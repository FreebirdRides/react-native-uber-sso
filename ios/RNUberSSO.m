
#import "RNUberSSO.h"

@implementation RNUberSSO

@synthesize bridge = _bridge;

static NSString *const NO_DEVKEY_FOUND              = @"No 'devKey' found or its empty";
static NSString *const NO_APPID_FOUND               = @"No 'appId' found or its empty";
static NSString *const NO_EVENT_NAME_FOUND          = @"No 'eventName' found or its empty";
static NSString *const NO_EMAILS_FOUND_OR_CORRUPTED = @"No 'emails' found, or list is corrupted";
static NSString *const SUCCESS                      = @"Success";

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initSdk: (NSDictionary*)initSdkOptions
                  successCallback :(RCTResponseSenderBlock)successCallback
                  errorCallback:(RCTResponseErrorBlock)errorCallback
                  )
{
    
    NSString* devKey = nil;
    NSString* appId = nil;
    BOOL isDebug = NO;
    BOOL isConversionData = NO;

    if (![initSdkOptions isKindOfClass:[NSNull class]]) {

        id isDebugValue = nil;
        id isConversionDataValue = nil;
        devKey = (NSString*)[initSdkOptions objectForKey: afDevKey];
        appId = (NSString*)[initSdkOptions objectForKey: afAppId];

        isDebugValue = [initSdkOptions objectForKey: afIsDebug];
        if ([isDebugValue isKindOfClass:[NSNumber class]]) {
            // isDebug is a boolean that will come through as an NSNumber
            isDebug = [(NSNumber*)isDebugValue boolValue];
        }
        isConversionDataValue = [initSdkOptions objectForKey: afConversionData];
        if ([isConversionDataValue isKindOfClass:[NSNumber class]]) {
            isConversionData = [(NSNumber*)isConversionDataValue boolValue];
        }
    }

    NSError* error = nil;

    if (!devKey || [devKey isEqualToString:@""]) {
        error = [NSError errorWithDomain:NO_DEVKEY_FOUND code:0 userInfo:nil];
        
    } else if (!appId || [appId isEqualToString:@""]) {
        error = [NSError errorWithDomain:NO_APPID_FOUND code:1 userInfo:nil];
    }

    if (error != nil) {
        errorCallback(error);
    } else {
        if(isConversionData == YES){
            [AppsFlyerTracker sharedTracker].delegate = self;
        }
        
        [AppsFlyerTracker sharedTracker].appleAppID = appId;
        [AppsFlyerTracker sharedTracker].appsFlyerDevKey = devKey;
        [AppsFlyerTracker sharedTracker].isDebug = isDebug;
        [[AppsFlyerTracker sharedTracker] trackAppLaunch];
        
        successCallback(@[SUCCESS]);
    }
}

@end
  