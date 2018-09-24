
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@interface RNUberSSO : NSObject <RCTBridgeModule>

@end

// Appsflyer JS objects
#define uberClientId                    @"clientId"
#define uberEnvironment                 @"environment"
#define uberIsDebug                     @"isDebug"
#define uberRedirectUri                 @"redirectUri"

// Appsflyer native objects
#define uberSuccess                     @"success"
#define uberFailure                     @"failure"
#define uberOnUberSSOFailure            @"onUberSSOFailure"
#define uberOnUberSSOSuccess            @"onUberSSOSuccess"
#define uberOnSSOAccessToken            @"onSSOUberSSOAccessToken"
