package org.tentama.anchoco.anchocojar.m3u8.ffmpeg;

import java.io.File;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

/**
 * 動画変換処理
 */
@Slf4j
public class ConvertSample {

    // @see https://github.com/bytedeco/javacpp-presets/tree/master/ffmpeg

    /** 動画フォルダroot(でも相対パスでできそう) */
    private static final String movieRootPath = "C:/user/movie";

    /**
     * 変換処理
     *
     * @param targetPath     変換ファイル格納先フォルダ
     * @param targetFileName 変換元ファイル
     */
    public void convertOne(String targetPath, String targetFileName) {
        log.info("start");
        // file
        File movieFile = Paths.get(movieRootPath, targetFileName).toFile();
        // ファイル名はtargetFileNameから拡張子抜いたやつにしたい
        File outputFile = Paths.get(movieRootPath, targetPath, targetPath + ".ts").toFile();

        FFmpegLogCallback.set();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(movieFile)) {

            grabber.start();

            final int width = grabber.getImageWidth();
            final int height = grabber.getImageHeight();
            final int audioChannels = grabber.getAudioChannels();

            log.info("width : {}, height : {}, channel : {}", width, height, audioChannels);

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {

                final double frameRate = grabber.getFrameRate();
                final int videoBitRate = grabber.getVideoBitrate();

                log.info("frame rate : {}, bit rate : {}", frameRate, videoBitRate);

                // 動画の拡張子// 動画の拡張子
                recorder.setFormat("mp4");
                // codec
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                // nullだと多分default
                // recorder.setVideoQuality(1);
                // フレームレート
                recorder.setFrameRate(frameRate);
                // bitrate
                recorder.setVideoBitrate(videoBitRate);

                if (recorder.getAudioChannels() > 0) {
                    // 音声設定がある場合
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                    recorder.setAudioBitrate(grabber.getAudioBitrate());
                    recorder.setSampleRate(grabber.getSampleRate());
                    recorder.setSampleFormat(grabber.getSampleFormat());

                }

                recorder.start();

                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                }

                grabber.stop();
                recorder.stop();
            }
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
            log.error("grab error.", e);
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            log.error("recoder error.", e);
        } finally {
            log.info("end");
        }

    }

}
