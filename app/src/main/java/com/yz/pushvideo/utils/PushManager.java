package com.yz.pushvideo.utils;

import android.content.Context;
import android.media.AudioFormat;

import com.yz.pushvideo.camera.AudioTask;
import com.yz.pushvideo.camera.CameraSurfaceView;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

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
 * 项目名称：RtmpRecoder-master
 * 类描述：
 * 创建人：zhangwei
 * 创建时间：2016-08-16 11:03
 * 修改人：zhangwei
 * 修改时间：2016-08-16 11:03
 * 修改备注：
 */
public class PushManager {
    public static final String RTMP_PUSH_VIDEO_URL = "rtmp://ossrs.net:19350/live/livestream";
    //ffmpeg 推流处理类
    private FFmpegFrameRecorder recorder;
    //摄像头采集的SurfaceView
    private CameraSurfaceView csv;
    //上传视频的宽度
    public int imageWidth;
    //上传视频的高度
    private int imageHeight;

    //采样率
    private int sampleAudioRateInHz;
    //视频编码格式 默认h264
    private int videoCodec;
    //视频采样帧率
    private int videoFrameRate;
    private String videoFormat;
    //是否停止录制   true 表示正在录制  false 表示停止录制
    private boolean recording = false;
    private static final int VIDEO_CHANNEL_CODE = 1;
    //音频采集线程
    private Thread audioThread;
    private AudioTask audioTask;
    //保存视频跟音频的格式载体类
    private Frame yuvImage;
    //声道设置 一般单声道
    private int audioFormat;
    private boolean audioRecordRunning = false;

    private PushManager(Bulider bulider) {
        csv = bulider.csv;
        videoFrameRate = bulider.defaultFrameRate;
        imageWidth = bulider.imageWidth;
        imageHeight = bulider.imageHeight;
        videoCodec = bulider.videoCodec;
        sampleAudioRateInHz = bulider.sampleAudioRateInHz;
        videoFormat = bulider.avFormat;
        this.audioFormat = bulider.audioFormat;
        csv = bulider.csv;
        csv.setRecordering(recording);

        audioTask = new AudioTask(recorder, sampleAudioRateInHz, audioFormat, audioRecordRunning);
        audioThread = new Thread(audioTask);
    }

    public static class Bulider {

        private CameraSurfaceView csv;

        private Context context;

        private int imageWidth;
        private int imageHeight;

        private int defaultFrameRate = 30;
        private int sampleAudioRateInHz = 44100;
        private int videoCodec = avcodec.AV_CODEC_ID_H264;
        private String avFormat = "flv";
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        private boolean recording;

        public Bulider(Context context, CameraSurfaceView csv) {
            this.context = context;
            this.csv = csv;

            imageWidth = DisplayUtils.getScreenHeight(context);
            imageHeight = DisplayUtils.getScreenWidth(context);
        }

        public Bulider setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
            return this;
        }

        public Bulider setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
            return this;
        }

        public Bulider setDefaultFrameRate(int frameRate) {
            if (frameRate > 0) {
                this.defaultFrameRate = frameRate;
            }
            return this;
        }

        public Bulider setVideoCodec(int videoCodec) {
            this.videoCodec = videoCodec;
            return this;
        }

        public Bulider setDefaultSampleAudioRateInHz(int sampleAudioRateInHz) {
            this.sampleAudioRateInHz = sampleAudioRateInHz;
            return this;
        }

        public Bulider setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }

        public boolean isRecording() {
            return recording;
        }

        public PushManager bulider() {
            return new PushManager(this);
        }
    }

    private void initFFmpegRecord() {
        recorder = new FFmpegFrameRecorder(RTMP_PUSH_VIDEO_URL, imageWidth, imageHeight, VIDEO_CHANNEL_CODE);
        // YUV  Y表示颜色亮度  U表示色度 V表示浓度
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV444P);
        //设置视频编解码器
        recorder.setVideoCodec(videoCodec);
        //设置压缩格式
        recorder.setFormat(videoFormat);
        recorder.setSampleRate(sampleAudioRateInHz);
        //设置帧率
        recorder.setFrameRate(videoFrameRate);
        if (yuvImage == null) {
            //ffmpeg保存音频视频的类
            yuvImage = new Frame(imageWidth, imageHeight, Frame.DEPTH_UBYTE, 2);
        }
    }

    public void startRecording() {
        initFFmpegRecord();
        recording = true;
        //开始录制视频
        //开始采集音频
        try {
            recorder.start();
            audioThread.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

    }


    public void stopRecord() {
        try {
            audioThread = null;
            audioTask = null;
            recording = false;
            recorder.stop();
            recorder.release();
        } catch (FrameRecorder.Exception e) {
            recorder = null;
        }
        recorder = null;
    }

}
