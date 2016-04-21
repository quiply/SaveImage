#import "SaveImage.h"
#import <Cordova/CDV.h>

@implementation SaveImage
@synthesize callbackId;

- (void)saveImageToGallery:(CDVInvokedUrlCommand*)command {
	[self.commandDelegate runInBackground:^{
	    self.callbackId = command.callbackId;

		NSString *imgAbsolutePath = [command.arguments objectAtIndex:0];

        NSLog(@"Image absolute path: %@", imgAbsolutePath);

	    UIImage *image = [UIImage imageWithContentsOfFile:imgAbsolutePath];
	    UIImageWriteToSavedPhotosAlbum(image, self, @selector(image:didFinishSavingWithError:contextInfo:), nil);

		CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:imgAbsolutePath];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}];
}

- (void)dealloc {
	[callbackId release];
    [super dealloc];
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo {
    // Was there an error?
    if (error != NULL) {
        NSLog(@"SaveImage, error: %@",error);
		CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString:error.description];
		[self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    } else {
        // No errors

        // Show message image successfully saved
        NSLog(@"SaveImage, image saved");
		CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:@"Image saved"];
		[self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    }
}

@end
