package com.yz.pushvideo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yz.pushvideo.utils.DisplayUtils;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ----------BigGod be here!----------/
 * ***┏┓******┏┓*********
 * *┏━┛┻━━━━━━┛┻━━┓*******
 * *┃             ┃*******
 * *┃     ━━━     ┃*******
 * *┃             ┃*******
 * *┃  ━┳┛   ┗┳━  ┃*******
 * *┃             ┃*******
 * *┃     ━┻━     ┃*******
 * *┃             ┃*******
 * *┗━━━┓     ┏━━━┛*******
 * *****┃     ┃神兽保佑*****
 * *****┃     ┃代码无BUG！***
 * *****┃     ┗━━━━━━━━┓*****
 * *****┃              ┣┓****
 * *****┃              ┏┛****
 * *****┗━┓┓┏━━━━┳┓┏━━━┛*****
 * *******┃┫┫****┃┫┫********
 * *******┗┻┛****┗┻┛*********
 * ━━━━━━神兽出没━━━━━━
 *
 * 项目名称：PushVideo
 * 类描述：
 * 创建人：zhangwei
 * 创建时间：2016-08-12 15:34
 * 修改人：zhangwei
 * 修改时间：2016-08-12 15:34
 * 修改备注：
 *
 * 第一步  初始化相机
 * 1.1> mCamera.setPreviewCallback(this);
 * 1.2> mCamera.setPreviewDisplay(holder);
 * 第二步  初始化SurfaceView相关参数
 * 2.1>   holder.addCallback(this);
 * 第三步  初始化 FFmepgRecord相关参数
 */

public class CameraSurfaceView extends SurfaceView implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private SurfaceHolder holder;
    private Camera mCamera;
    private boolean isPreView;
    private Frame yuvImages;
    private static final int AUDIO_CHANNEL_CODE = 1;
    private FFmpegFrameRecorder recorder;
    public static final String RTMP_PUSH_VIDEO_URL = "rtmp://ossrs.net:19350/live/livestream";
    private double frameRate = 30;
    private long startTime;
    private boolean recordering;
    private int sampleAudioRateInHz = 441000;
    private int imageWidth;
    private int imageHeight;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initCamera();
        initHoler();
    }

    private void initHoler() {
        holder = getHolder();
        holder.addCallback(this);
    }

    private void initCamera() {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(this);
    }

    /**
     * recorder = new FFmpegFrameRecorder(ffmpeg_link, imageWidth, imageHeight, 1);
     * recorder.setVideoCodec(28);
     * recorder.setFormat("flv");
     * recorder.setSampleRate(sampleAudioRateInHz);
     */
    private void initFFmpegRecord() {
        imageWidth = DisplayUtils.getScreenHeight(getContext());
        imageHeight = DisplayUtils.getScreenWidth(getContext());
        //图片的宽度跟高度
        recorder = new FFmpegFrameRecorder(RTMP_PUSH_VIDEO_URL, imageWidth, imageHeight, AUDIO_CHANNEL_CODE);

        //设置
        // YUV  Y表示颜色亮度  U表示色度 V表示浓度
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV444P);
        //设置视频编解码器
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //设置压缩格式
        recorder.setFormat("flv");
        recorder.setSampleRate(sampleAudioRateInHz);
        //设置帧率
        recorder.setFrameRate(frameRate);

        if (yuvImages == null) {
            yuvImages = new Frame(imageWidth, imageHeight, Frame.DEPTH_UBYTE, 2);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            stopPreview();
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        Camera.Parameters camParams = mCamera.getParameters();
        List<Camera.Size> sizes = camParams.getSupportedPreviewSizes();
        // Sort the list in ascending order
        Collections.sort(sizes, new Comparator<Camera.Size>() {

            public int compare(final Camera.Size a, final Camera.Size b) {
                return a.width * a.height - b.width * b.height;
            }
        });

        for (int i = 0; i < sizes.size(); i++) {
            if ((sizes.get(i).width >= imageWidth && sizes.get(i).height >= imageHeight) || i == sizes.size() - 1) {
                imageWidth = sizes.get(i).width;
                imageHeight = sizes.get(i).height;
                break;
            }
        }
        camParams.setPreviewSize(imageWidth, imageHeight);
//        camParams.setPreviewFrameRate(frameRate);
        mCamera.setParameters(camParams);
        // Set the holder (which might have changed) again
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(CameraSurfaceView.this);
            startPreview();
        } catch (Exception e) {

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.addCallback(null);
        mCamera.setPreviewCallback(null);
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        直播直接放入FFMEPGRecord
        if (yuvImages != null && recordering) {
//            ((ByteBuffer) yuvImages.image[0].position(0)).put(data);
            Buffer buffer = yuvImages.image[0];
            ByteBuffer position = (ByteBuffer) buffer.position(0);
            position.put(data);
            try {
                recorder.record(yuvImages);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void startPreview() {
        if (!isPreView && mCamera != null) {
            isPreView = true;
            mCamera.startPreview();
        }
    }

    private void stopPreview() {
        if (isPreView && mCamera != null) {
            isPreView = false;
            mCamera.stopPreview();
        }
    }

    public boolean isRecordering() {
        return recordering;
    }

    public void setRecordering(boolean recordering) {
        this.recordering = recordering;
    }
}
