package com.quiply.cordova.saveimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * The SaveImage class offers a method saving an image to the devices' media gallery.
 */
public class SaveImage extends CordovaPlugin {
    public static final int WRITE_PERM_REQUEST_CODE = 1;
    private final String ACTION = "saveImageToGallery";
    private final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private CallbackContext callbackContext;
    private String filePath;
    

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION)) {
            saveImageToGallery(args, callbackContext);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check saveImage arguments and app permissions
     *
     * @param args              JSON Array of args
     * @param callbackContext   callback id for optional progress reports
     *
     * args[0] filePath         file path string to image file to be saved to gallery
     */  
    private void saveImageToGallery(JSONArray args, CallbackContext callback) throws JSONException {
    	this.filePath = args.getString(0);
    	this.callbackContext = callback;
        Log.d("SaveImage", "SaveImage in filePath: " + filePath);
        
        if (filePath == null || filePath.equals("")) {
        	callback.error("Missing filePath");
            return;
        }
        
        if (PermissionHelper.hasPermission(this, WRITE_EXTERNAL_STORAGE)) {
        	Log.d("SaveImage", "Permissions already granted, or Android version is lower than 6");
        	performImageSave();
        } else {
        	Log.d("SaveImage", "Requesting permissions for WRITE_EXTERNAL_STORAGE");
        	PermissionHelper.requestPermission(this, WRITE_PERM_REQUEST_CODE, WRITE_EXTERNAL_STORAGE);
        }      
    }
    
    
    /**
     * Save image to device gallery
     */
    private void performImageSave() throws JSONException {
        // create file from passed path
        File srcFile = new File(filePath);

        // destination gallery folder - external storage
        File dstGalleryFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        Log.d("SaveImage", "SaveImage dstGalleryFolder: " + dstGalleryFolder);

        try {
            // Create export file in destination folder (gallery)
            File expFile = copyFile(srcFile, dstGalleryFolder);

            // Update image gallery
            scanPhoto(expFile);

            callbackContext.success(expFile.toString());
        } catch (RuntimeException e) {
            callbackContext.error("RuntimeException occurred: " + e.getMessage());
        }
    }

    /**
     * Copy a file to a destination folder
     *
     * @param srcFile       Source file to be stored in destination folder
     * @param dstFolder     Destination folder where to store file
     * @return File         The newly generated file in destination folder
     */
    private File copyFile(File srcFile, File dstFolder) {
        // if destination folder does not exist, create it
        if (!dstFolder.exists()) {
            if (!dstFolder.mkdir()) {
                throw new RuntimeException("Destination folder does not exist and cannot be created.");
            }
        }

        // Generate image file name using current date and time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        File newFile = new File(dstFolder.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        // Read and write image files
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(srcFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Source file not found: " + srcFile + ", error: " + e.getMessage());
        }
        try {
            outChannel = new FileOutputStream(newFile).getChannel();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Copy file not found: " + newFile + ", error: " + e.getMessage());
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new RuntimeException("Error transfering file, error: " + e.getMessage());
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    Log.d("SaveImage", "Error closing input file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    Log.d("SaveImage", "Error closing output file channel: " + e.getMessage());
                    // does not harm, do nothing
                }
            }
        }

        return newFile;
    }

    /**
     * Invoke the system's media scanner to add your photo to the Media Provider's database,
     * making it available in the Android Gallery application and to other apps.
     *
     * @param imageFile The image file to be scanned by the media scanner
     */
    private void scanPhoto(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        cordova.getActivity().sendBroadcast(mediaScanIntent);
    }
    

    /**
     * Callback from PermissionHelper.requestPermission method
     */
	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				Log.d("SaveImage", "Permission not granted by the user");
				callbackContext.error("Permissions denied");
				return;
			}
		}
		
		switch (requestCode) {
		case WRITE_PERM_REQUEST_CODE:
			Log.d("SaveImage", "User granted the permission for WRITE_EXTERNAL_STORAGE");
			performImageSave();
			break;
		}
	}
}
