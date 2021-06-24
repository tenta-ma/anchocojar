package org.tentama.anchoco.anchocojar.m3u8.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;

/**
 * 動画変換処理
 */
@Slf4j
public class ConvertFfmpeg {

    // @see https://github.com/bytedeco/javacpp-presets/tree/master/ffmpeg

    /** 動画フォルダroot(でも相対パスでできそう) */
    private static final String movieRootPath = "C:/user/movie";

    /**
     * 変換処理
     *
     * @param targetPath     変換ファイル格納先フォルダ
     * @param targetFileName 変換元ファイル
     */
    public void createTsSegment(String targetPath, String targetFileName) {
        log.info("start");
        // file
        File movieFile = Paths.get(movieRootPath, targetFileName).toFile();
        // ファイル名はtargetFileNameから拡張子抜いたやつにしたい
        File outputFile = Paths.get(movieRootPath, targetPath, targetPath + ".m3u8").toFile();

        File outputFolder = Paths.get(movieRootPath, targetPath).toFile();

        try {

            // libraryのnativeなffmpegからのコマンド実行
            String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
            ProcessBuilder pb = new ProcessBuilder(ffmpeg,
                    "-i", movieFile.getAbsolutePath(),
                    "-c:v", "copy",
                    "-c:a", "copy",
                    "-hls_time", "10",
                    "-hls_playlist_type", "vod",
                    "-hls_segment_filename", outputFolder.getAbsolutePath() + File.separator + "hogefuga%3d.ts",
                    outputFile.getAbsolutePath());
            pb.inheritIO().start().waitFor();

        } catch (InterruptedException e) {
            log.error("InterruptedException error.", e);
        } catch (IOException e) {
            log.error("IOException error.", e);
        } finally {
            log.info("end");
        }

    }

    /**
     * サムネイル作成
     *
     * @param targetPath     変換ファイル格納先フォルダ
     * @param targetFileName 変換元ファイル
     */
    public void createThumbnail(String targetPath, String targetFileName) {
        log.info("start");
        // file
        File movieFile = Paths.get(movieRootPath, targetFileName).toFile();
        File outputFolder = Paths.get(movieRootPath, targetPath).toFile();

        try {
            String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
            // 先頭から100フレーム内で、もっともサムネイルにふさわしい絵をthumbnail.jpgに保存のような感じっぽい
            ProcessBuilder pb = new ProcessBuilder(ffmpeg,
                    "-i", movieFile.getAbsolutePath(),
                    "-vf", "thumbnail",
                    "-frames:v", "1",
                    outputFolder.getAbsolutePath() + File.separator + "thumbnail.jpg");
            pb.inheritIO().start().waitFor();

        } catch (InterruptedException e) {
            log.error("InterruptedException error.", e);
        } catch (IOException e) {
            log.error("IOException error.", e);
        } finally {
            log.info("end");
        }

    }

}
