package cn.ckz.camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ckz.camera.manager.MyCameraManager;
import cn.ckz.camera.utils.DisplayUtils;
import cn.ckz.camera.view.CameraTouchView;
import cn.ckz.camera.view.ImageViewFix;
import cn.ckz.camera.view.TakePhotoButton;
import cn.ckz.camera.view.TextureVideoPlayer;

import static android.os.Environment.DIRECTORY_DCIM;


/**
 * Created by CKZ on 2017/7/29.
 */

public class VXCameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = VXCameraActivity.class.getSimpleName();

    private Context context = this;

    private TextureView mSurface;

    private CameraTouchView mTouch;

    private ImageView mClose;

    private TakePhotoButton mTake;

    private ImageViewFix mPic;

    private TextureVideoPlayer player;

    private ImageView mBack;

    private ImageView mSucess;

    private ImageView mChange;

    private MyCameraManager manager;

    private File mFile = null;

    public boolean isToken = false;

    private boolean isPlaying = false;

    private int type;

    private int width = 0;

    private int height = 0;

    private int current = 1;

    int mOrientation = 0;

    private AlbumOrientationEventListener mAlbumOrientationEventListener;

    private TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

            if (width == 0){
                width = i;
                height = i1;
            }
            manager.openCamera(surfaceTexture, width, height);

            isFirst = false;

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            manager.auto();
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.layout_vx_camera);
        mAlbumOrientationEventListener = new AlbumOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
        if (mAlbumOrientationEventListener.canDetectOrientation()){
            mAlbumOrientationEventListener.enable();
        }else {
            Log.d("chengcj1", "Can't Detect Orientation");
        }
        File dir = new File(Environment.getExternalStorageDirectory(), "images");
        if (dir.exists()) {
            dir.mkdirs();
        }
        manager = MyCameraManager.getInstance(this);
        mFile = new File(dir, "img_" + String.valueOf(System.currentTimeMillis() / 1000) + ".jpg");
        initView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        showCamera();
        setClick();
        setTouch();
        takePhotoes();
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

                        long current = System.currentTimeMillis();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d HH:mm");
                        Date date = new Date(current);
                        String text = format.format(date);
//                        mTvTime.setText(text);
                        mPic.setTime("");
                        type = 0;

                        clipBitmap = createWatermark(clipBitmap(bytes),text,"","");
                        mPic.setImageBitmap(clipBitmap);
                        showPicture();
                    }
                });
            }

            @Override
            public void onLongClick(TakePhotoButton takePhotoButton) {
                //开始录像
                type = 1;
                takePhotoButton.start();
                manager.startRecorder();
            }

            @Override
            public void onFinish() {
                //结束录像
                Log.d(TAG, "finish");
                manager.stopRecorder();
                showVideo();
            }
        });
    }

    private void showVideo() {
        Log.d(TAG, "showVideo");

        mTake.setVisibility(View.GONE);
        mClose.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);
        mTouch.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mSucess.setVisibility(View.VISIBLE);
        mPic.setVisibility(View.GONE);
        mBack.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_left));
        mSucess.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_right));
        manager.closeCamera();

        if (mOrientation == 0|| mOrientation == 180){
            mSurface.setVisibility(View.VISIBLE);
            player.setVisibility(View.GONE);
            manager.startPlay(mSurface);
        }else {
            mSurface.setVisibility(View.GONE);
            player.setVisibility(View.VISIBLE);
            player.setUrl(manager.getFilePath());
            player.play();
        }

        isPlaying = true;
        current = 2;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mSurface = (TextureView) findViewById(R.id.surface);
        mTouch = (CameraTouchView) findViewById(R.id.camera_touch);
        mTake = (TakePhotoButton) findViewById(R.id.take_photo);
        mClose = (ImageView) findViewById(R.id.close_camera);
        mPic = (ImageViewFix) findViewById(R.id.picture);
        mBack = (ImageView) findViewById(R.id.camera_back);
        mSucess = (ImageView) findViewById(R.id.camera_sucess);
        mChange = (ImageView) findViewById(R.id.camera_change);
        player = findViewById(R.id.video_player);
        player.setVideoMode(TextureVideoPlayer.CENTER_MODE);
        mSurface.setSurfaceTextureListener(listener);
        player.setOnVideoPlayingListener(new TextureVideoPlayer.OnVideoPlayingListener() {
            @Override
            public void onVideoSizeChanged(int vWidth, int vHeight) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) player.getLayoutParams();
                params.width = getResources().getDisplayMetrics().widthPixels;
                params.height = getResources().getDisplayMetrics().heightPixels;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onPlaying(int duration, int percent) {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onRestart() {

            }

            @Override
            public void onPlayingFinish() {

            }

            @Override
            public void onTextureDestory() {
                if (player != null){
                    player.pause();
                }
            }
        });

    }

    /**
     * 设置单击聚焦以及双指放大
     */
    private void setTouch() {
        mTouch.setOnViewTouchListener(new CameraTouchView.OnViewTouchListener() {
            @Override
            public void handleFocus(float x, float y) {
                MyCameraManager.getInstance(context).onFocus(x, y);
            }

            @Override
            public void handleZoom(boolean zoom) {
                MyCameraManager.getInstance(context).handleZoom(zoom);
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
    private void showCamera() {
        mSurface.setVisibility(View.VISIBLE);
        mTake.setVisibility(View.VISIBLE);
        mClose.setVisibility(View.VISIBLE);
        mChange.setVisibility(View.VISIBLE);
        mTouch.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.GONE);
        mSucess.setVisibility(View.GONE);
        mPic.setVisibility(View.GONE);
        player.setVisibility(View.GONE);
        isToken = false;
        current = 0;
    }



    /**
     * 完成拍照时显示的控件
     */
    private void showPicture() {
        mSurface.setVisibility(View.GONE);
        mTake.setVisibility(View.GONE);
        mClose.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);
        mTouch.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mSucess.setVisibility(View.VISIBLE);
        mPic.setVisibility(View.VISIBLE);
        player.setVisibility(View.GONE);
        mBack.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_left));
        mSucess.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_right));
        isToken = true;
        current = 1;
    }

    String photoPath;//拍照存储的地址

    private void initPhotoPath() {
        String dir = context.getExternalFilesDir(DIRECTORY_DCIM).getPath();
        photoPath = dir + File.separator + System.currentTimeMillis() + ".jpg";
    }

    private void savePhoto(Bitmap bitmap) {
        initPhotoPath();

        FileOutputStream fos = null;
        try {
            new File(photoPath).createNewFile();
            fos = new FileOutputStream(photoPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            String picName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            ContentValues values = new ContentValues();
            ContentResolver resolver = getContentResolver();
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.ImageColumns.DATA, photoPath);
            values.put(MediaStore.Images.ImageColumns.TITLE, picName);
            values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "照片");
//            values.put(MediaStore.Images.ImageColumns.ORIENTATION, 90);
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + picName}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存临时文件的方法
     */
    private String saveFile(Bitmap bitmap) {
        try {
            mFile = File.createTempFile("img", "");
            FileOutputStream fos = new FileOutputStream(mFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
     *
     * @param bytes
     * @return
     */
    private Bitmap clipBitmap(byte[] bytes) {
        Bitmap bitmap1 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap bitmap = null;
//        if (bitmap1.getWidth() > bitmap1.getHeight()) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);
            Log.d("photoOrientation",info.orientation+"");
            if (manager.getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_BACK){
                switch (mOrientation){
                    case 0:
                        bitmap = rotate(bitmap1, 0);
                        break;
                    case 90:
                        bitmap = rotate(bitmap1, 90);
                        break;
                    case 180:
                        bitmap = rotate(bitmap1, 180);
                        break;
                    case 270:
                        bitmap = rotate(bitmap1, 270);
                        break;
                }

            }else {
                switch (mOrientation){
                    case 0:
                        bitmap = rotate(bitmap1, 0);
                        break;
                    case 90:
                        bitmap = rotate(bitmap1, 270);
                        break;
                    case 180:
                        bitmap = rotate(bitmap1, 180);
                        break;
                    case 270:
                        bitmap = rotate(bitmap1, 90);
                        break;
                }
            }
//        } else {
//            bitmap = bitmap1;
//        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap afterBitmap = null;
        if (MyCameraManager.getInstance(context).getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {

            afterBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h);

        } else {
            Matrix matrix = new Matrix();
            matrix.setRotate(180);
            matrix.postScale(-1, 1);
            afterBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);
        }
        return afterBitmap;
    }

    public static Bitmap rotate(Bitmap bitmap, int angle) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /**
     * 重写返回键，当已经完成拍照，则返回拍照界面
     */
    @Override
    public void onBackPressed() {
        if (isToken) {
            manager.closeCamera();
            if (isToken) {
                if (clipBitmap != null) {
                    clipBitmap.recycle();
                }
            } else if (isPlaying) {
                manager.stopPlay();
                player.stop();
                manager.deleteFile();

            }
            manager.openCamera(mSurface.getSurfaceTexture(), mSurface.getWidth(), mSurface.getHeight());
            if (width == 0){
                width = mSurface.getWidth();
                height = mSurface.getHeight();
            }
            showCamera();
        } else {
            finish();

        }
    }

    /**
     * activity关闭动画
     */
    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.camera_activity_out);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.close_camera) {
            Intent cancleIntent = new Intent();
            setResult(RESULT_CANCELED, cancleIntent);
            finish();

        } else if (i == R.id.camera_back) {
            manager.closeCamera();
            if (isToken) {
                if (clipBitmap != null) {
                    clipBitmap.recycle();
                }
            } else if (isPlaying) {
                manager.stopPlay();
                player.stop();
                manager.deleteFile();

            }
            manager.openCamera(mSurface.getSurfaceTexture(), mSurface.getWidth(), mSurface.getHeight());
            if (width == 0){
                width = mSurface.getWidth();
                height = mSurface.getHeight();
            }
            showCamera();

        } else if (i == R.id.camera_sucess) {
            Intent intent = new Intent();
            if (type == 0) {
//                Bitmap shuiying = getViewBitmap(mPic);
                savePhoto(clipBitmap);
                //返回数据
                intent.putExtra("filePath", photoPath);
                intent.putExtra("type", type);
            } else {
                intent.putExtra("filePath", manager.filePath);
                intent.putExtra("type", type);
            }
            setResult(RESULT_OK, intent);
            finish();

        } else if (i == R.id.camera_change) {//更改摄像头
            manager.changeCamera(mSurface.getSurfaceTexture(), mSurface.getWidth(), mSurface.getHeight());

        }
    }

    // 为图片target添加水印文字
    // Bitmap target：被添加水印的图片
    // String mark：水印文章
    private Bitmap createWatermark(Bitmap target, String time,String area,String discription) {
        int w = target.getWidth();
        int h = target.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();

        // 水印的颜色
        p.setColor(Color.WHITE);

        // 水印的字体大小
        p.setTextSize(target.getWidth()*0.04f);

        p.setAntiAlias(true);// 去锯齿

        canvas.drawBitmap(target, 0, 0, p);

        if (!TextUtils.isEmpty(time)&& TextUtils.isEmpty(area)&& TextUtils.isEmpty(discription)){
            Rect rect = new Rect();
            p.getTextBounds(time, 0, time.length(), rect);
            int textWidth = rect.width();//文本的宽度
            int textHeight = rect.height();//文本的高度
            canvas.drawText(time,w-DisplayUtils.dp2px(context,
                    15f)- textWidth,h-DisplayUtils.dp2px(context,15f),p);
        }

        if (!TextUtils.isEmpty(area)&& TextUtils.isEmpty(discription)){
            Rect rect = new Rect();
            p.getTextBounds(time, 0, time.length(), rect);
            int timeWidth = rect.width();//文本的宽度
            int timeHeight = rect.height();//文本的高度
            canvas.drawText(time,w-DisplayUtils.dp2px(context,
                    15f)- timeWidth,h-DisplayUtils.dp2px(context,15f),p);

            p.getTextBounds(area,0,area.length(),rect);
            int areaWidth = rect.width();
            int areaHeight = rect.height();
            canvas.drawText(area,DisplayUtils.dp2px(context,
                    15f),h-DisplayUtils.dp2px(context,15f),p);
        }

        if (!TextUtils.isEmpty(area)&& !TextUtils.isEmpty(discription)){
            Rect rect = new Rect();
            p.getTextBounds(time, 0, time.length(), rect);
            int timeWidth = rect.width();//文本的宽度
            int timeHeight = rect.height();//文本的高度
            canvas.drawText(time,w-DisplayUtils.dp2px(context,
                    15f)- timeWidth,h-DisplayUtils.dp2px(context,15f)-timeHeight-20,p);

            p.getTextBounds(area,0,area.length(),rect);
            int areaWidth = rect.width();
            int areaHeight = rect.height();
            canvas.drawText(area,DisplayUtils.dp2px(context,
                    15f),h-DisplayUtils.dp2px(context,15f)-areaHeight-20,p);

            p.getTextBounds(discription,0,discription.length(),rect);
            int disWidth = rect.width();
            int disHeight = rect.height();
            canvas.drawText(discription,DisplayUtils.dp2px(context,
                    15f),h-DisplayUtils.dp2px(context,15f),p);
        }





//        // 在左边的中间位置开始添加水印
//        canvas.drawText(mark, 0, h / 2, p);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return bmp;
    }


    private Bitmap getViewBitmap(View addViewContent) {

        addViewContent.setDrawingCacheEnabled(true);

        addViewContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0,
                addViewContent.getMeasuredWidth(),
                addViewContent.getMeasuredHeight());

        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        return bitmap;
    }


    @Override
    protected void onDestroy() {
        mAlbumOrientationEventListener.disable();
        super.onDestroy();
        manager.stopPlay();
        player.stop();
    }




    private boolean isFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
       if (!isFirst){
           if (current == 0){
               showCamera();
           }else if (current == 1){
               showPicture();
           }else {
               showVideo();
           }
           manager.openCamera(mSurface.getSurfaceTexture(),width,height);
       }
    }


    private class AlbumOrientationEventListener extends OrientationEventListener{
        public AlbumOrientationEventListener(Context context) {
            super(context);
        }

        public AlbumOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            } 		//保证只返回四个方向
             	int newOrientation = ((orientation + 45) / 90 * 90) % 360;
            if (newOrientation != mOrientation) {
                mOrientation = newOrientation;
                Log.d("phoneOrientation",newOrientation+"");
                //返回的mOrientation就是手机方向，为0°、90°、180°和270°中的一个

                switch (mOrientation){
                    case 0:
                        manager.setOrientation(90);
                        break;
                    case 90:
                        manager.setOrientation(180);
                        break;
                    case 180:
                        manager.setOrientation(270);
                        break;
                    case 270:
                        manager.setOrientation(0);
                        break;

                }
//                if(newOrientation == 0 || newOrientation == 180){
//                    manager.setOrientation(MyCameraManager.PORTRAIT);
//                }else {
//                    manager.setOrientation(MyCameraManager.LANDSAPCE);
//                }
            }



        }


    }
}

