
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

-(void) handleCallback:(NSDictionary *) message {
  NSError *error;
  
  if ([NSJSONSerialization isValidJSONObject:message]) {
    NSData *jsonMessage = [NSJSONSerialization dataWithJSONObject:message
                                                          options:0
                                                            error:&error];
    if (jsonMessage) {
      NSString *jsonMessageStr = [[NSString alloc] initWithBytes:[jsonMessage bytes] length:[jsonMessage length] encoding:NSUTF8StringEncoding];
      
      NSString* status = (NSString*)[message objectForKey: @"status"];
      
      if([status isEqualToString:uberSuccess]) {
        [self reportOnSuccess:jsonMessageStr];
      } else {
        [self reportOnFailure:jsonMessageStr];
      }
      
      NSLog(@"jsonMessageStr = %@",jsonMessageStr);
    } else {
      NSLog(@"%@",error);
    }
  }
  else{
    [self reportOnFailure:@"failed to parse Response"];
  }
}

-(void) reportOnFailure:(NSString *)errorMessage {
//  [self.bridge.eventDispatcher sendAppEventWithName:afOnInstallConversionData body:errorMessage];
}

-(void) reportOnSuccess:(NSString *)data {
//  [self.bridge.eventDispatcher sendAppEventWithName:afOnInstallConversionData body:data];
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
                                if (error) {
                                  NSDictionary* errorMessage = @{
                                                                 @"status": uberFailure,
                                                                 @"type": uberOnSSOAccessToken,
                                                                 @"data": error.localizedDescription
                                                                 };
                                  
                                  [self performSelectorOnMainThread:@selector(handleCallback:) withObject:errorMessage waitUntilDone:NO];
                                } else {
                                  NSDictionary* message = @{
                                                            @"status": uberSuccess,
                                                            @"type": uberOnSSOAccessToken,
                                                            @"data": @{
                                                                @"accessToken": accessToken.tokenString,
                                                                @"refreshToken": accessToken.refreshToken
                                                                }
                                                            };
                                  
                                  [self performSelectorOnMainThread:@selector(handleCallback:) withObject:message waitUntilDone:NO];
                                }
                              }];
}

@end
