#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(MenuModule, RCTUIMenuModule, NSObject)

RCT_EXTERN_METHOD(showMenu:(NSArray*)actions
                  withHeaderConfig:(NSDictionary*)headerConfig
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

@end
