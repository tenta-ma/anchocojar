package org.tentama.anchoco.anchocojar.m3u8;

import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoInfo;
import ws.schild.jave.info.VideoSize;

/**
 * 動画変換を行う
 *
 * @see ws.schild.jave jave2
 */
@Slf4j
public class Mpeg2Ts {

    /** エンコーダー */
    private final Encoder encoder;

    /** 分割したときの動画の長さ(s) */
    private final float duration;

    /** mpeg2tsで利用される標準的な音声コーデック(aac) */
    private static final String audioCodec = "aac";

    /** mp4で利用される標準的な動画コーデック(MPEG-2 Video) */
    private static final String videoCodec = "libx264";

    /** コンストラクタ */
    public Mpeg2Ts() {
        encoder = new Encoder();
        final float len = 10;
        duration = len;
    }

    /**
     * tsファイルを作成する
     */
    public void convert() {

        log.info("start.");

        try {
            // file
            URL sourceFile = Mpeg2Ts.class.getClassLoader().getResource("movie/source/sample60s.mp4");
            log.debug(sourceFile.toURI().toString());
            File movieFile = Paths.get(sourceFile.toURI()).toFile();

            MultimediaObject mediaObject = new MultimediaObject(movieFile);
            MultimediaInfo mediaInfo = mediaObject.getInfo();

            // ミリ秒で取れる(60秒の動画であれば60000s)
            log.debug("duration : {}", mediaInfo.getDuration());
            log.debug("format : {}", mediaInfo.getFormat());

            // 動画フォーマットを設定
            EncodingAttributes attrs = new EncodingAttributes();
            // 動画秒数
            attrs.setDuration(duration);

            // 音声の変換情報を設定
            attrs.setAudioAttributes(createAudioAttributes(mediaInfo));
            // 動画の変換情報を設定
            attrs.setVideoAttributes(createVideoAttributes(mediaInfo));

            // 変換を実施(milli secondをsecondにしておく)
            final BigDecimal movieDurationSecond = BigDecimal.valueOf(mediaInfo.getDuration()).scaleByPowerOfTen(-3);

            BigDecimal movieOffsetSecond = BigDecimal.ZERO;
            AtomicInteger fileIndex = new AtomicInteger(1);
            do {

                attrs.setOffset(movieOffsetSecond.floatValue());

                // 指定ファイル名_index.tsで作成
                File dest = new File("C:/user/movie/sample60s/dest60s_" + fileIndex.getAndIncrement() + ".ts");

                encoder.encode(mediaObject, dest, attrs);

                // 変換結果を検証
                if (!dest.exists() || dest.length() == 0) {
                    log.error("encode failer.");
                    return;
                }

                movieOffsetSecond = movieOffsetSecond.add(BigDecimal.valueOf(duration));

                // offsetが動画開始秒 > 動画長になるまで繰り返し
            } while (movieOffsetSecond.compareTo(movieDurationSecond) < 0);

        } catch (URISyntaxException e) {
            log.error("ファイルが見つからない", e);
        } catch (InputFormatException e) {
            log.error("適切な動画でない？", e);
        } catch (EncoderException e) {
            log.error("encodeに失敗", e);
        } finally {
            log.info("end.");
        }

    }

    /**
     * 変換元動画と同じ動画設定を行う<br>
     * 変換元動画に動画設定がない場合、空の動画設定を返却する
     *
     * @param mediaInfo 変換元動画
     * @return 動画設定
     */
    private VideoAttributes createVideoAttributes(MultimediaInfo mediaInfo) {

        VideoAttributes attributes = new VideoAttributes();
        // 動画圧縮変換時ビデオコーデック
        attributes.setCodec(videoCodec);

        VideoInfo videoInfo = mediaInfo.getVideo();
        if (videoInfo == null) {
            log.debug("videoInfo is null");
            return attributes;
        }

        // 動画圧縮変換時ビデオビットレート
        attributes.setBitRate(videoInfo.getBitRate());
        // 動画圧縮変換時ビデオフレームレート
        attributes.setFrameRate((int) videoInfo.getFrameRate());

        log.debug("video decoder : {}", videoInfo.getDecoder());
        log.debug("video bit rate : {}", videoInfo.getBitRate());
        log.debug("video frame rate : {}", videoInfo.getFrameRate());

        final VideoSize videoSize = videoInfo.getSize();
        if (videoSize != null) {
            // 動画圧縮変換時ビデオ幅,動画圧縮変換時ビデオ高さ
            attributes.setSize(new VideoSize(videoSize.getWidth(), videoSize.getHeight()));
            log.debug("video height size : {}", videoSize.getHeight());
            log.debug("video width size : {}", videoSize.getWidth());
        }

        return attributes;
    }

    /**
     * 変換元動画と同じ音声設定を行う<br>
     * 変換元動画に音声設定がない場合、空の音声設定を返却する
     *
     * @param mediaInfo 変換元動画
     * @return 音声設定
     */
    private AudioAttributes createAudioAttributes(MultimediaInfo mediaInfo) {

        AudioAttributes attributes = new AudioAttributes();
        // 動画圧縮変換時オーディオコーデック
        attributes.setCodec(audioCodec);

        AudioInfo audioInfo = mediaInfo.getAudio();
        if (audioInfo == null) {
            log.debug("audioInfo is null");
            return attributes;
        }

        // 動画圧縮変換時オーディオビットレート
        attributes.setBitRate(audioInfo.getBitRate());
        // 動画圧縮変換時オーディオチャンネル
        attributes.setChannels(audioInfo.getChannels());
        // 動画圧縮変換時オーディオサンプリングレート
        attributes.setSamplingRate(audioInfo.getSamplingRate());

        log.debug("audio decoder : {}", audioInfo.getDecoder());
        log.debug("audio bit rate : {}", audioInfo.getBitRate());
        log.debug("audio sampling rate : {}", audioInfo.getSamplingRate());

        return attributes;
    }

}
