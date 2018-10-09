package cn.ckz.camera.manager;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by CKZ on 2017/8/1.
 */

public class MyCameraManager {
    private static final String TAG = "MyCameraManager";

    private static MyCameraManager manager;

    private Context mContext;

    private Camera camera;

    private int cameraType = 0;

    private MediaRecorder mRecorder;

    private int width;
    private int height;

    /**
     * 相机闪光状态
     */
    private int cameraFlash;

    private int recorderDegree = 90;

    public static final int PORTRAIT = 0;
    public static final int LANDSAPCE = 1;

    private int orientation = PORTRAIT;



    public void setOrientation(int orientation){

            recorderDegree = orientation;
    }
    /**
     * 前后置状态
     */
    private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;


    public static MyCameraManager getInstance(Context mContext){
        if (manager == null){
            synchronized (MyCameraManager.class){
                if (manager == null){
                    manager = new MyCameraManager(mContext);
                }
            }
        }
        return manager;
    }
    private MyCameraManager(Context mContext){
        this.mContext = mContext;
    }

    /**
     * 打开相机
     */
    public void openCamera(SurfaceHolder surfaceHolder){
        if (camera == null){
            Log.d(TAG,"打开相机");
            camera = Camera.open(cameraFacing);
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.set("orientation","portrait");
//                parameters.set("rottion",90);
                parameters.setPreviewSize(640,480);
                parameters.setPictureSize(640,480);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,e.toString());
                camera.release();
                camera = null;
            }
        }else {
            Log.d(TAG,"相机已经存在");
        }
    }

    public void openCamera(SurfaceTexture surfaceTexture,int width,int height){
           if (camera!=null){
               camera.release();
               camera = null;
           }
            camera = Camera.open(cameraFacing);
            try {
//                camera.cancelAutoFocus();
                camera.setDisplayOrientation(90);
                camera.setPreviewTexture(surfaceTexture);
                initCameraParameters(cameraFacing,width,height);
                this.width = width;
                this.height = height;
                Log.d("CameraOpenA","开启");
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,e.toString());
                camera.release();
                camera = null;
            }
    }



    /**
     * 关闭相机
     */
    public void closeCamera(){
        this.cameraType = 0;
        if (camera!=null){
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
//                cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            }catch (Exception e){
                Log.i(TAG,e.toString());
                camera.release();
                camera = null;
//                cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
    }

    public void resetCamera(){
        closeCamera();
        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * 更换摄像头
     * @param surfaceHolder
     */

    public void changeCamera(SurfaceHolder surfaceHolder) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int i = 0;i<cameraCount;i++){
            Camera.getCameraInfo(i,cameraInfo);
            if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK){
                //现在后后置，更改为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera();
                    cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    openCamera(surfaceHolder);
                    Log.d(TAG,"切换到前置摄像头");
                    break;
                }

            }else {
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera();
                    cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
                    openCamera(surfaceHolder);
                    Log.d(TAG,"切换到后置摄像头");
                    break;
                }
            }
        }
    }

    public void changeCamera(SurfaceTexture surfaceTexture,int width,int height) {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int i = 0;i<cameraCount;i++){
            Camera.getCameraInfo(i,cameraInfo);
            if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK){
                //现在后后置，更改为前置
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera();
                    cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    openCamera(surfaceTexture,width,height);
                    Log.d(TAG,"切换到前置摄像头");
                    break;
                }

            }else {
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera();
                    cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
                    openCamera(surfaceTexture,width,height);
                    Log.d(TAG,"切换到后置摄像头");
                    break;
                }
            }
        }
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    /**
     * 拍照
     * @param callback
     */
    public void takePhoto(Camera.PictureCallback callback){
        if (camera!=null){
            try {

                camera.takePicture(null,null,callback);
            }catch (Exception e){
                Toast.makeText(mContext,"拍照失败",Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void initCameraParameters(int cameraId, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
//

            setCameraType(cameraId,0,parameters);
//
        parameters.setRotation(90);//设置旋转代码,
        switch (cameraFlash) {
            case 0:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case 1:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
            case 2:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
//        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
//        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//        if (!isEmpty(pictureSizes) && !isEmpty(previewSizes)) {
//
//            Camera.Size optimalPicSize=null;
//
//            optimalPicSize = getOptimalCameraPreSize(pictureSizes, width, height);
//
//            Camera.Size optimalPreSize = getOptimalCameraPreSize(previewSizes, width, height);
//
//            parameters.setPictureSize(optimalPicSize.width,optimalPicSize.height);
//            parameters.setPreviewSize(optimalPreSize.width, optimalPreSize.height);
//
//            for (Camera.Size size:pictureSizes){
////                Log.d("VXCameraSize","宽度："+size.width+"\n高度："+size.height);
//            }
//            Log.d("VXCameraSize","获取宽度："+optimalPreSize.width+"\n获取高度："+optimalPreSize.height);
//            Log.d("VXCameraSize","拍摄宽度："+optimalPicSize.width+"\n拍摄高度："+optimalPicSize.height);
//            Log.d("VXCameraSize","surface宽度："+width+"\nsurface高度："+height);
//
//
//        }
        setPicSize(parameters,width,height);
        camera.setParameters(parameters);
        camera.cancelAutoFocus();
    }

    private void setPicSize( Camera.Parameters parameters,int width,int height){
        Point screenResolution;
        Point cameraResolution;
        Point cameraPictureSize;
//        WindowManager manager = ((Activity)mContext).getWindowManager();
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        manager.getDefaultDisplay().getMetrics(outMetrics);
        screenResolution = new Point(width,height);
        Log.d(TAG, "Screen resolution: " + screenResolution);


        //图片拉伸
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;
        // preview size is always something like 480*320, other 320*480
        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }
        //获取相机正确预览尺寸
        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
        Log.d(TAG, "Camera resolution: " + screenResolution);

        cameraPictureSize = getCameraPictureSizeResolution(parameters,screenResolutionForCamera);
        Log.d(TAG, "cameraPictureSize: "+ cameraPictureSize);
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPictureSize(cameraPictureSize.x,cameraPictureSize.y);

        Log.d("VxCameraSize","拍摄宽度："+cameraPictureSize.x+"\n拍摄高度："+cameraPictureSize.y);

    }

    /**
     * 获取预览合适尺寸
     */
    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {

        String previewSizeValueString = parameters.get("preview-size-values");
        // saw this on Xperia
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null) {
            Log.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            // Ensure that the camera resolution is a multiple of 8, as the screen may not be.
            cameraResolution = new Point(
                    (screenResolution.x >> 3) << 3,
                    (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }

    /**
     * 获取picture合适尺寸
     */
    private static Point getCameraPictureSizeResolution(Camera.Parameters parameters,Point screenResolution){
        String pictureSizeValueaString = parameters.get("picture-size-values");
        if (pictureSizeValueaString == null) pictureSizeValueaString = parameters.get("picture-size-value");
        Point cameraResolution = null;
        if (pictureSizeValueaString!=null){
            Log.d(TAG, "picture-size-values parameter: "+pictureSizeValueaString);
            cameraResolution = findBestPictureSizeValue(pictureSizeValueaString,screenResolution);
            Log.d("PictureSize","宽度："+cameraResolution.x+"高度："+cameraResolution.y);
        }
        return cameraResolution;

    }
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    /**
     * 从支持的预览尺寸集匹配出合适的尺寸
     */
    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                Log.w(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }
    /**
     * 从支持的picture尺寸集匹配出合适的尺寸
     */
    private static Point findBestPictureSizeValue(CharSequence pictureSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        Log.d("BeastSize",screenResolution.toString());
        for (String pictureSize : COMMA_PATTERN.split(pictureSizeValueString)) {

            pictureSize = pictureSize.trim();
            int dimPosition = pictureSize.indexOf('x');
            if (dimPosition < 0) {
                Log.w(TAG, "Bad picture-size: " + pictureSize);
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(pictureSize.substring(0, dimPosition));
                newY = Integer.parseInt(pictureSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Bad picture-size: " + pictureSize);
                continue;
            }

            if ((float)newX/(float)newY == (float)screenResolution.x/(float)screenResolution.y){
                return new Point(newX,newY);
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    /**
     *
     * @param sizes 相机support参数
     * @param w
     * @param h
     * @return 最佳Camera size
     */
    private Camera.Size getOptimalCameraPicSize(List<Camera.Size> sizes, int w, int h){
        sortCameraSize(sizes);
        int position = binarySearch(sizes, h/w);
        return sizes.get(position);
    }

    private Camera.Size getOptimalCameraPreSize(List<Camera.Size> sizes, int w, int h){
        sortCameraSize(sizes);
        int position = binarySize(sizes, w*h);
        return sizes.get(position);
    }

    /**
     *
     * @param sizes
     * @param targetNum 要比较的数
     * @return
     */
    private int binarySearch(List<Camera.Size> sizes,int targetNum){
        int targetIndex;
        int left = 0,right;
        int length = sizes.size();
        for (right = length-1;left != right;){
            int midIndex = (right + left)/2;
            int mid = right - left;
            Camera.Size size = sizes.get(midIndex);
            int midValue = size.width / size.height;
            if (targetNum == midValue){
                return midIndex;
            }
            if (targetNum > midValue){
                left = midIndex;
            }else {
                right = midIndex;
            }

            if (mid <= 1){
                break;
            }
        }
        Camera.Size rightSize = sizes.get(right);
        Camera.Size leftSize = sizes.get(left);
        int rightNum = rightSize.width / rightSize.height;
        int leftNum = leftSize.width / leftSize.height;
        targetIndex = Math.abs((rightNum - leftNum)/2) > Math.abs(rightNum - targetNum) ? right : left;
        return targetIndex;
    }

    private int binarySize(List<Camera.Size> sizes,int targetNum){
        int targetIndex;
        int left = 0,right;
        int length = sizes.size();
        int scale = 0;
        for (right = length-1;left != right;){
            int midIndex = (right + left)/2;
            int mid = right - left;
            Camera.Size size = sizes.get(midIndex);
            int midValue = size.width * size.height;
            if (targetNum == midValue){
                scale =midIndex;
            }
            if (targetNum > midValue){
                left = midIndex;
            }else {
                right = midIndex;
            }

            if (mid <= 1){
                break;
            }
        }
        Camera.Size rightSize = sizes.get(right);
        Camera.Size leftSize = sizes.get(left);
        int rightNum = rightSize.width * rightSize.height;
        int leftNum = leftSize.width * leftSize.height;
        targetIndex = Math.abs((leftNum - targetNum)) > Math.abs(rightNum - targetNum) ? right : left;
        if (scale!=0){
            return scale;
        }else {
            return targetIndex;

        }
    }



    /**
     * 排序
     * @param previewSizes
     */
    private void sortCameraSize(List<Camera.Size> previewSizes){
        Collections.sort(previewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size1, Camera.Size size2) {
                int compareHeight = size1.height - size2.height;
                if (compareHeight == 0){
                    return (size1.width == size2.width ? 0 :(size1.width > size2.width ? 1:-1));
                }
                return compareHeight;
            }
        });
        for (Camera.Size size:previewSizes){
            Log.d("VXCameraSize","宽度："+size.width+"\n高度："+size.height);
        }
    }

    /**
     * 集合不为空
     *
     * @param list
     * @param <E>
     * @return
     */
    private <E> boolean isEmpty(List<E> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 缩放
     * @param isZoomIn
     */
    public void handleZoom(boolean isZoomIn) {
        if (camera == null) return;
        Camera.Parameters params = camera.getParameters();
        if (params == null) return;
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
//            LogUtils.i("zoom not supported");
        }
    }


    /**
     * 手动聚焦
     *
     * @param
     */
    public boolean onFocus(float x,float y) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = camera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        Camera.AutoFocusCallback cb = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success){
                    initCameraParameters(cameraFacing,width,height);
                    camera.cancelAutoFocus();
                }
            }
        };

        if(Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(cb);
            }

            Log.i(TAG, "onCameraFocus:" + x + "," + y);

            //定点对焦
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = (int) (x - 300);
            int top = (int) (y - 300);
            int right = (int) (x + 300);
            int bottom = (int) (y + 300);
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                camera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }




        return focus(cb);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            camera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 对焦
     * @param x
     * @param y
     */
    public void handleFocusMetering(float x, float y) {
        if(camera!=null){
            final Camera.Parameters params = camera.getParameters();
            Camera.Size previewSize = params.getPreviewSize();
            Rect focusRect = calculateTapArea(x, y, 1f, previewSize);
            Rect meteringRect = calculateTapArea(x, y, 1.5f, previewSize);


            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 100));
                params.setFocusAreas(focusAreas);
            } else {
//                LogUtils.i("focus areas not supported");
            }
            if (params.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(meteringRect, 100));
                params.setMeteringAreas(meteringAreas);
            } else {
//                LogUtils.i("metering areas not supported");
            }
            camera.setParameters(params);
            camera.cancelAutoFocus();
//            camera.autoFocus(new Camera.AutoFocusCallback() {
//                @Override
//                public void onAutoFocus(boolean success, Camera camera) {
////
//                    if (success) {
//
////                        if (!Build.MODEL.equals("KORIDY H30")) {
////                            Camera.Parameters params = camera.getParameters();
////                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
////                            camera.setParameters(params);
////                        }else{
////                            Camera.Parameters parameters = camera.getParameters();
////                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
////                            camera.setParameters(parameters);
////                        }
//                        initCameraParameters(cameraFacing,width,height);
//                        camera.cancelAutoFocus(); // 只有加上了这一句，才会自动对焦。
//                    }
//
//
//                }
//            });
        }

    }
    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
    /**
     * 设置对焦类型
     * @param cameraType
     */
    public void setCameraType(int cameraId,int cameraType,Camera.Parameters parameters) {
        this.cameraType = cameraType;

        if (camera != null) {//拍摄视频时
            if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    if (cameraType == 0) {
                        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }
                    } else {
                        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        }
                    }
                }
            }
        }
    }

    public void auto(){
        if (camera != null) {//拍摄视频时
            initCameraParameters(cameraFacing,width,height);
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
//
                    if (success) {

                        if (!Build.MODEL.equals("KORIDY H30")) {
                            Camera.Parameters params = camera.getParameters();
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                            camera.setParameters(params);
                        }else{
                            Camera.Parameters parameters = camera.getParameters();
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            camera.setParameters(parameters);
                        }

                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }

                }
            });
        }
    }

    /**
     * 初始化mediaRecorder
     */
    private void initRecorder(){
        mRecorder.reset();
        if (camera!=null){
            camera.unlock();
            mRecorder.setCamera(camera);
        }

//        // 这两项需要放在setOutputFormat之前
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//
//        // Set output file format
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//
//        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
//        //after setVideoSource(),after setOutFormat()
//        mRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
////
//        mRecorder.setVideoFrameRate(mProfile.videoFrameRate);
//        //after setOutputFormat()
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        //after setOutputFormat()
//        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // 从麦克采集音频信息
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // TODO: 2016/10/20  设置视频格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        //after setVideoSource(),after setOutFormat()
        mRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        Log.d("CameraManager",mProfile.videoBitRate+"");
        if (mProfile.videoBitRate > 2*1024*1024) {
            mRecorder.setVideoEncodingBitRate((int) (2*1024*1024));
            Log.d("CameraManager","固定码率");
        }else{
            mRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
            Log.d("CameraManager","支持码率");
        }

        //after setVideoSource(),after setOutFormat();
        mRecorder.setVideoFrameRate(mProfile.videoFrameRate);
        //after setOutputFormat()
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //after setOutputFormat()
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);


        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK){
            mRecorder.setOrientationHint(recorderDegree);

        }else {

//
            mRecorder.setOrientationHint(270);


        }

        //设置记录会话的最大持续时间（毫秒）
        mRecorder.setMaxDuration(10 * 1000);

    }

    public String filePath = null;
    private String outputFile(){
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "video");
        if (!dir.exists()){
            dir.mkdirs();
        }
        String fileName = "video_"+String.valueOf(System.currentTimeMillis()/1000)+".mp4";
        filePath = dir.getAbsolutePath()+File.separator+fileName;
        return filePath;
    }
    public void startRecorder(){
        try {
            if (mRecorder == null){
            mRecorder = new MediaRecorder();
        }
//            Camera.Parameters parameters = camera.getParameters();
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//            camera.setParameters(parameters);
            initRecorder();
            mRecorder.setOutputFile(outputFile());
            Log.d(TAG,outputFile());
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void stopRecorder(){
        if (mRecorder!=null){
            try {
                mRecorder.stop();
            }catch (Exception e){
                mRecorder = null;
                mRecorder = new MediaRecorder();
            }

            mRecorder.release();
            mRecorder = null;
        }
    }
    private MediaPlayer mPlayer;

    public String  getFilePath(){
        return filePath;
    }

    public void startPlay(TextureView textureView){

        try {
            if (mPlayer == null){
                mPlayer = new MediaPlayer();
            }
            if (mPlayer.isPlaying())
            {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;

                mPlayer = new MediaPlayer();
            }
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(filePath);
            Log.d(TAG,"播放地址:"+filePath);
            mPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
//            mPlayer.prepare();
            try {
                mPlayer.prepareAsync();
            }catch (IllegalStateException e)
            {
                e.printStackTrace();
            }

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (mediaPlayer!=null){
                        mediaPlayer.start();
                    }
                }
            });

            mPlayer.setLooping(true);

            mPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    int videoWidth = mPlayer.getVideoWidth();
                    int videoHeight = mPlayer.getVideoHeight();

                }
            });
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPlay(TextureView textureView,String filePath){

        try {
            mPlayer = new MediaPlayer();

//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(filePath);
            mPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (mediaPlayer!=null){
                        mediaPlayer.start();
                    }
                }
            });
            mPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlay(){
        if (mPlayer!=null){
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    public void deleteFile(){
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
        }
    }

}
