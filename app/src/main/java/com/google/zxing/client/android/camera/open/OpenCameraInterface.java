package com.google.zxing.client.android.camera.open;

import android.hardware.Camera;
import android.util.Log;

public final class OpenCameraInterface {

    private static final String TAG = OpenCameraInterface.class.getName();

    private OpenCameraInterface() {
    }

    public static final int NO_REQUESTED_CAMERA = -1;

    public static OpenCamera open(int cameraId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.w(TAG, "No cameras!");
            return null;
        }

        boolean explicitRequest = cameraId >= 0;

        Camera.CameraInfo selectedCameraInfo = null;
        int index;
        if (explicitRequest) {
            index = cameraId;
            selectedCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, selectedCameraInfo);
        } else {
            index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                CameraFacing reportedFacing = CameraFacing.values()[cameraInfo.facing];
                if (reportedFacing == CameraFacing.BACK) {
                    selectedCameraInfo = cameraInfo;
                    break;
                }
                index++;
            }
        }

        Camera camera;
        if (index < numCameras) {
            Log.i(TAG, "Opening camera #" + index);
            camera = Camera.open(index);
        } else {
            if (explicitRequest) {
                Log.w(TAG, "Requested camera does not exist: " + cameraId);
                camera = null;
            } else {
                Log.i(TAG, "No camera facing " + CameraFacing.BACK + "; returning camera #0");
                camera = Camera.open(0);
                selectedCameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, selectedCameraInfo);
            }
        }

        if (camera == null) {
            return null;
        }

        return new OpenCamera(index,
                camera,
                CameraFacing.values()[selectedCameraInfo.facing],
                selectedCameraInfo.orientation);
    }
}