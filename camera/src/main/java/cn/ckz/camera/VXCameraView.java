package cn.ckz.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.ckz.camera.manager.MyCameraManager;
import cn.ckz.camera.view.CameraTouchView;
import cn.ckz.camera.view.TakePhotoButton;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by CKZ on 2017/8/15.
 */

public class VXCameraView extends Fragment implements View.OnClickListener{


    private static final String TAG = "BelowLollipopCamera";

    private TextureView mSurface;

    private CameraTouchView mTouch;//用于控制触摸聚焦以及方法

    private ImageView mClose;//关闭相机

    private TakePhotoButton mTake;//拍照按钮

    private ImageView mPic;//照片预览

    private ImageView mBack;//返回拍照

    private ImageView mSucess;//拍照成功，保存图片按钮

    private ImageView mChange;//切换摄像头

    private MyCameraManager manager;//相机功能管理类

    private File mFile = null;

    private boolean isToken = false;

    private boolean isPlaying = false;

    private TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            manager.openCamera(surfaceTexture,i,i1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            manager.resetCamera();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_vx_camera,container,false);

        File dir = new File(Environment.getExternalStorageDirectory(),"images");
        if(dir.exists()){
            dir.mkdirs();
        }
        manager = MyCameraManager.getInstance(getContext());
        mFile = new File(dir,"pic.jpg");
        initView(view);
        showCamera();
        setClick();
        setTouch();
        takePhotoes();
        return view;
    }

    /**
     * 拍照
     */
    private void takePhotoes() {
        mTake.setOnProgressTouchListener(new TakePhotoButton.OnProgressTouchListener() {
            @Override
            public void onClick(TakePhotoButton takePhotoButton) {
                //拍照
                manager.takePhoto(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        clipBitmap(bytes);
                        mPic.setImageBitmap(clipBitmap(bytes));
                        showPicture();
                    }
                });
            }

            @Override
            public void onLongClick(TakePhotoButton takePhotoButton) {
                //开始录像
                takePhotoButton.start();
                manager.startRecorder();
            }

            @Override
            public void onFinish() {
                //结束录像
                Log.d(TAG,"finish");
                manager.stopRecorder();
                showVideo();
            }
        });
    }

    private void showVideo(){
        Log.d(TAG,"showVideo");
        mSurface.setVisibility(View.VISIBLE);
        mTake.setVisibility(View.GONE);
        mClose.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);
        mTouch.setVisibility(View.GONE);
        mPic.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mSucess.setVisibility(View.VISIBLE);
        mBack.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.to_left));
        mSucess.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.to_right));
        manager.closeCamera();
        manager.startPlay(mSurface);
        isPlaying = true;
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        mSurface = (TextureView)view.findViewById(R.id.surface);
        mTouch = (CameraTouchView) view.findViewById(R.id.camera_touch);
        mTake = (TakePhotoButton) view.findViewById(R.id.take_photo);
        mClose = (ImageView) view.findViewById(R.id.close_camera);
        mPic = (ImageView) view.findViewById(R.id.picture);
        mBack = (ImageView) view.findViewById(R.id.camera_back);
        mSucess = (ImageView) view.findViewById(R.id.camera_sucess);
        mChange = (ImageView) view.findViewById(R.id.camera_change);
        mSurface.setSurfaceTextureListener(listener);

    }

    /**
     * 设置单击聚焦以及双指放大
     */
    private void setTouch(){
        mTouch.setOnViewTouchListener(new CameraTouchView.OnViewTouchListener() {
            @Override
            public void handleFocus(float x, float y) {
                MyCameraManager.getInstance(getContext()).handleFocusMetering(x,y);
            }

            @Override
            public void handleZoom(boolean zoom) {
                MyCameraManager.getInstance(getContext()).handleZoom(zoom);
            }
        });
    }

    private void setClick() {
        mClose.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mSucess.setOnClickListener(this);
        mChange.setOnClickListener(this);
    }

    /**
     * 预览时显示的控件
     */
    public void showCamera(){
        mSurface.setVisibility(View.VISIBLE);
        mTake.setVisibility(View.VISIBLE);
        mClose.setVisibility(View.VISIBLE);
        mChange.setVisibility(View.VISIBLE);

        mPic.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        mSucess.setVisibility(View.GONE);
        isToken = false;
    }

    /**
     * 完成拍照时显示的控件
     */
    private void showPicture(){
        mSurface.setVisibility(View.GONE);
        mTake.setVisibility(View.GONE);
        mClose.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);

        mPic.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mSucess.setVisibility(View.VISIBLE);
        mBack.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.to_left));
        mSucess.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.to_right));
        isToken = true;
    }

    /**
     * 保存临时文件的方法
     */
    private String saveFile(Bitmap bitmap){
        try {
            mFile = File.createTempFile("img","");
            FileOutputStream fos = new FileOutputStream(mFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            bos.flush();
            bos.close();
            return mFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Bitmap clipBitmap = null;

    /**
     * 裁剪bitmap，填充当前屏幕，当前为前置摄像头时，将bitmap反转180°，以及镜像翻转
     * @param bytes
     * @return
     */
    private Bitmap clipBitmap(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        WindowManager wm = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        float width = wm.getDefaultDisplay().getWidth();
        float height = wm.getDefaultDisplay().getHeight();
        float w_h = width/height;
        int picW = (int) (h*w_h);
        int picH = h;

        if (MyCameraManager.getInstance(getContext()).getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_BACK){
            clipBitmap = Bitmap.createBitmap(bitmap,w/2- picW/2,0,picW,picH);
        }else {
            Matrix matrix = new Matrix();
            matrix.setRotate(180);
            matrix.postScale(-1,1);
            clipBitmap = Bitmap.createBitmap(bitmap,w/2- picW/2,0,picW,picH,matrix,false);
        }
        return  clipBitmap;
    }

    /**
     * activity关闭动画
     */

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.close_camera) {
            Intent cancleIntent = new Intent();
            getActivity().setResult(RESULT_CANCELED, cancleIntent);
            getActivity().finish();

        } else if (i == R.id.camera_back) {
            manager.closeCamera();
            if (isToken){
                clipBitmap.recycle();
            }else if (isPlaying){
                manager.stopPlay();
                manager.deleteFile();

            }
            manager.openCamera(mSurface.getSurfaceTexture(), mSurface.getWidth(), mSurface.getHeight());
            showCamera();

        } else if (i == R.id.camera_sucess) {
            saveFile(clipBitmap);
            //返回数据
            Intent intent = new Intent();
            intent.putExtra("imageUri", Uri.fromFile(mFile).toString());
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();

        } else if (i == R.id.camera_change) {//更改摄像头
            manager.changeCamera(mSurface.getSurfaceTexture(), mSurface.getWidth(), mSurface.getHeight());

        }
    }
}
