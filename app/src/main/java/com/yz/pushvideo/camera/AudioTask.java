package com.yz.pushvideo.camera;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

import java.nio.ShortBuffer;

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
 * 创建时间：2016-08-15 22:21
 * 修改人：zhangwei
 * 修改时间：2016-08-15 22:21
 * 修改备注：
 */

public class AudioTask implements Runnable {
    //采样率 推荐44.1k,32k,48k
    private int sampleAudioRateInHz;
    private AudioRecord audioRecord;
    private volatile boolean audioThreadRunning = false;
    private  boolean audioRecordRunning = false;
    private FFmpegFrameRecorder recorder;
    //声道设置
    private int audioFormat;

    public AudioTask(FFmpegFrameRecorder recorder, int sampleAudioRateInHz, int audioFormat, boolean audioRecordRunning) {
        this.recorder = recorder;
        this.sampleAudioRateInHz = sampleAudioRateInHz;
        this.audioFormat = audioFormat;
        this.audioThreadRunning = audioRecordRunning;
    }

    @Override
    public void run() {
        /**
         * 参数一 采样率：音频的采样频率，每秒钟能够采样的次数，采样率越高，音质越高
         * 参数二 声道设置  AudioFormat.CHANNEL_IN_MONO 单声道
         * 参数三 audioFormat 编码制式和采样大小：采集来的数据当然使用PCM编码(脉冲代码调制编码，
         * 即PCM编码。PCM通过抽样、量化、编码三个步骤将连续变化的模拟信号转换为数字编码。)
         * android支持的采样大小16bit 或者8bit。当然采样大小越大，那么信息量越多，音质也越高，
         * 现在主流的采样大小都是16bit，在低质量的语音传输的时候8bit足够了
         */
        //获取采集数据最小缓冲区大小
        int minBufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz, AudioFormat.CHANNEL_IN_MONO, audioFormat);
        /*
         * 参数一 音频源：指的是从哪里采集音频。
         * 参数二 同上
         * 参数三 同上
         * 参数四 获取的采集视频的最小缓冲区大小
         */
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        ShortBuffer audioData = ShortBuffer.allocate(minBufferSize);
        audioRecord.startRecording();

        while (audioThreadRunning) {
            /**
             * audioData        写入的音频录制数据。
             * offsetInShorts   目标数组 audioData 的起始偏移量。
             * sizeInShorts     请求读取的数据大小。
             */
            int readSize = audioRecord.read(audioData.array(), 0, audioData.capacity());
            audioData.limit(readSize);
            if (readSize > 0 && audioRecordRunning) {
                try {
                    recorder.recordSamples(audioData);
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }


    }
}
