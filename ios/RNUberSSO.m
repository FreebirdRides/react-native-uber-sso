
#import "RNUberSSO.h"

@import UberCore;

@implementation RNUberSSO

@synthesize bridge = _bridge;

static NSString *const NO_CLIENT_ID_FOUND           = @"No 'clientId' found or its empty";
static NSString *const NO_REDIRECT_URI_FOUND        = @"No 'redirectUri' found or its empty";
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
  NSString* clientId = nil;
  NSString* redirecUri = nil;
  NSString* environment = nil;
  BOOL isDebug = NO;

  if (![initSdkOptions isKindOfClass:[NSNull class]]) {
      id isDebugValue = nil;
      clientId = (NSString*)[initSdkOptions objectForKey: uberClientId];
      environment = [initSdkOptions objectForKey: uberEnvironment];
      redirecUri = (NSString*)[initSdkOptions objectForKey: uberRedirectUri];
      isDebugValue = [initSdkOptions objectForKey: uberIsDebug];
      if ([isDebugValue isKindOfClass:[NSNumber class]]) {
          // isDebug is a boolean that will come through as an NSNumber
          isDebug = [(NSNumber*)isDebugValue boolValue];
      }
  }

  NSError* error = nil;

  if (!clientId || [clientId isEqualToString:@""]) {
    error = [NSError errorWithDomain:NO_CLIENT_ID_FOUND code:0 userInfo:nil];
  } else if (!redirecUri || [redirecUri isEqualToString:@""]) {
    error = [NSError errorWithDomain:NO_REDIRECT_URI_FOUND code:1 userInfo:nil];
  }

  if (!environment || [environment isEqualToString:@""]) {
    environment = @"sandbox";
  }

  if (error != nil) {
    errorCallback(error);
  } else {
    UBSDKConfiguration* configuration = UBSDKConfiguration.shared;
    [configuration setClientID:clientId];
    [configuration setCallbackURI:[NSURL URLWithString:redirecUri]];
    [configuration setIsSandbox:[environment isEqualToString:@"sandbox"]];
    
    successCallback(@[SUCCESS]);
  }
}

RCT_EXPORT_METHOD(login)
{
  UBSDKLoginManager *loginManager = [[UBSDKLoginManager alloc] init];
  NSArray *scopes = @[
    UBSDKScope.history,
    UBSDKScope.places,
    UBSDKScope.profile,
    UBSDKScope.allTrips,
    UBSDKScope.requestReceipt,
    UBSDKScope.request ];
  UIViewController *rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
  [loginManager loginWithRequestedScopes:scopes
                presentingViewController: rootViewController
                              completion: ^(UBSDKAccessToken * _Nullable accessToken, NSError * _Nullable error) {
                                // Completion block. If accessToken is non-nil, you're good to go
                                // Otherwise, error.code corresponds to the RidesAuthenticationErrorType that occured
                              }];
}

@end
