package cn.ckz.vxcamera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.vuandroidadsdk.okhttp.CommonOkHttpClient;
import com.example.vuandroidadsdk.okhttp.listener.DisposeDataHandle;
import com.example.vuandroidadsdk.okhttp.listener.DisposeDataListener;
import com.example.vuandroidadsdk.okhttp.request.CommonRequest;
import com.example.vuandroidadsdk.okhttp.request.RequestParams;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.ckz.camera.ClipPictureActivity;
import cn.ckz.camera.VXCameraActivity;
import cn.ckz.camera.manager.CameraHelper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private android.widget.Button openalumb;
    private android.widget.Button opencamera;
    private Button openMyCamera,btnUpload;
    private android.widget.ImageView image;
    private int id;
    private Uri current;
    private List<String> fileList = new ArrayList<>();
    private String url = "http://118.31.34.249:9080/taihe/interfaces/serviceApp.do?m=manageOrder";
//private String url = "http://192.168.2.166:9080/taihe/interfaces/serviceApp.do?m=manageOrder";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.image = (ImageView) findViewById(R.id.image);
        this.opencamera = (Button) findViewById(R.id.open_camera);
        this.openalumb = (Button) findViewById(R.id.open_alumb);
        openMyCamera = (Button) findViewById(R.id.open_my_camera);
        openalumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_alumb;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);

            }
        });
        opencamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_camera;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);
            }
        });
        openMyCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_my_camera;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);
            }
        });
        findViewById(R.id.open_fragment_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_fragment_camera;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);
            }
        });
        findViewById(R.id.open_fragment_camera2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_fragment_camera2;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);
            }
        });
        findViewById(R.id.open_fragment_camera3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = R.id.open_fragment_camera3;
                MainActivityPermissionsDispatcher.nWithCheck(MainActivity.this);
            }
        });

        btnUpload = findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.fileParams.put("userId","818181835e651b1f015e66ce0c680093");
                params.fileParams.put("orderId","818181835ebd5afd015ec06f54780021");
                params.fileParams.put("handleRemark","提交");


                for (int i =0;i<fileList.size();i++){
                    File file = new File(fileList.get(i));
                    if (file.exists()){
                        Log.d("FilePath", file.getAbsolutePath());
                    }else {
                        Log.d("FilePath", "文件不存在");
                    }
                    try {
                        params.put("imagePath"+i,file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("UPLOADTAG", "onClick: "+params.toString());

                CommonOkHttpClient.post(CommonRequest.createMultiPostRequest(url,params),new DisposeDataHandle(new DisposeDataListener() {
                    @Override
                    public void onSuccess(Object responseObj) {

                    }

                    @Override
                    public void onFailure(Object reasonObj) {

                    }
                }));
            }
        });


        SpeechSynthesizer mTts=SpeechSynthesizer.createSynthesizer(MainActivity.this, null);
        //2.合成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //开始合成
//        mTts.startSpeaking("科大讯飞，让世界聆听我们的声音",  listener);
    }

    private SynthesizerListener listener =new SynthesizerListener() {

        @Override
        public void onSpeakResumed() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSpeakPaused() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSpeakBegin() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onCompleted(SpeechError arg0) {
            // TODO Auto-generated method stub
            //arg0.getPlainDescription(true);
        }

        @Override
        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
            // TODO Auto-generated method stub

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CameraHelper.ALBUM_REQUEST_CODE:

                if (resultCode == RESULT_OK){
                    File file = new File(data.getData().toString());
                    Log.d("TAG",data.getData().toString());
                    Intent intent = new Intent(MainActivity.this,ClipPictureActivity.class);
                    intent.putExtra("imageUri",data.getData().toString());
                    startActivityForResult(intent,300);
                }
                break;
            case CameraHelper.CAMERA_REQUEST_CODE:
                Log.d("TAG",resultCode+"");
                if (resultCode == RESULT_OK){
                    current = Uri.parse(data.getData().toString());
                    image.setImageURI(current);
                }
                break;
            case 100:
                Log.d("TAG",resultCode+"");
                if (resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    image.setImageBitmap(bitmap);

                }
                break;
            case 200:
                Log.d("TAG",resultCode+"");
                if (resultCode == RESULT_OK){

                    String uri = data.getStringExtra("filePath");
                    File file = new File(uri);
                    if (file.exists()){
                        Log.d("FilePath", file.getAbsolutePath());
                    }else {
                        Log.d("FilePath", "文件不存在"+uri);
                    }
                    fileList.add(uri);
                    Glide.with(MainActivity.this).load(uri).into(image);
                    Log.d("TAG",data.getStringExtra("filePath"));
                }
                break;
            case 300:
                Log.d("TAG",resultCode+"");
                if (resultCode == RESULT_OK){
                    Uri uri = Uri.parse(data.getStringExtra("imageUri"));
                    image.setImageURI(uri);
                }
                break;

        }
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void n() {
        switch (id) {
            case R.id.open_alumb:
                CameraHelper.getInstance(MainActivity.this).openAlbum();
                break;
            case R.id.open_camera:
                current= CameraHelper.getInstance(MainActivity.this).openCamera();
                break;
            case R.id.open_my_camera:
                Intent intent = new Intent(this,VXCameraActivity.class);
                startActivityForResult(intent,200);
                break;
            case R.id.open_fragment_camera:
                startActivityForResult(new Intent(this,Main2Activity.class),200);
                break;

            case R.id.open_fragment_camera2:
                startActivityForResult(new Intent(this,Main3Activity.class),200);
                break;
            case R.id.open_fragment_camera3:
                startActivityForResult(new Intent(this,Main4Activity.class),200);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


}
