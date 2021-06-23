package org.tentama.anchoco.anchocojar.m3u8.perser;

import io.lindstrom.m3u8.model.MediaPlaylist;
import io.lindstrom.m3u8.model.MediaSegment;
import io.lindstrom.m3u8.model.PlaylistType;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tentama.anchoco.anchocojar.m3u8.jave2.Mpeg2Ts;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

/**
 * m3u8ファイルを作成する処理
 */
@Slf4j
public class M3u8Perser {

    // @see https://github.com/carlanton/m3u8-parser

    /** 動画フォルダroot(でも相対パスでできそう) */
    private static final String movieRootPath = "C:/user/movie";

    /** プロトコルの指定 */
    private static final int EXT_X_VERSION = 3;

    /** 最大間隔(基本的には分割ファイルの秒数を指定すればよい) */
    private static final int EXT_X_TARGETDURATION = 10;

    /** index順に作成するため0(先頭のファイルがindex:0(最初)),ない場合defaultで0が設定されるためなくても挙動は同じ */
    private static final int EXT_X_MEDIA_SEQUENCE = 0;

    /** mpeg2拡張子 ts */
    private static final String MPEG2_EXTENSION = ".ts";

    /**
     * m3u8ファイルの作成<br>
     * {@link Mpeg2Ts#convert()}とかでtsファイル群を作成した、という前提で処理
     *
     * @param targetPath tsファイルをおいてあるフォルダ
     */
    public static void createM3u8(String targetPath) {

        try {
            log.info("start");

            // 動画の入っているフォルダ(tsファイルのみ入っている想定)
            Path moviePath = Paths.get(movieRootPath, targetPath);

            MediaPlaylist mediaPlaylist = MediaPlaylist.builder()
                    .version(EXT_X_VERSION)
                    .targetDuration(EXT_X_TARGETDURATION)
                    .mediaSequence(EXT_X_MEDIA_SEQUENCE)
                    // 動画
                    .playlistType(PlaylistType.VOD)
                    .ongoing(false)
                    .addAllMediaSegments(createMediaSegments(moviePath))
                    .build();

            MediaPlaylistParser parser = new MediaPlaylistParser();

            log.info(parser.writePlaylistAsString(mediaPlaylist));

            // file 作成
            Path m3u8FilePath = Paths.get(movieRootPath, targetPath, targetPath + ".m3u8");
            if (Files.notExists(m3u8FilePath)) {
                Files.createFile(m3u8FilePath);
            }
            // 既存のファイルがあっても強制新規作成(エラーでもいい気もする？)
            Files.write(m3u8FilePath, parser.writePlaylistAsBytes(mediaPlaylist), StandardOpenOption.TRUNCATE_EXISTING);

        } catch (EncoderException e) {
            // include InputFormatException.
            log.error("ファイルがおかしい。", e);
        } catch (IOException e) {
            log.error("m3u8ファイル作成に失敗。", e);
        } finally {
            log.info("end");
        }

    }

    private static List<MediaSegment> createMediaSegments(Path moviePath) throws EncoderException {

        File[] movieFiles = moviePath.toFile().listFiles();
        if (movieFiles == null) {
            log.warn("フォルダがない。 {}", moviePath.toFile().toString());
            return List.of();
        }

        List<MediaSegment> segmentList = new ArrayList<>();

        for (File movieFile : movieFiles) {
            log.debug("file name : {}", movieFile.getName());

            if (!StringUtils.endsWith(movieFile.getName(), MPEG2_EXTENSION)) {
                log.warn("mpeg2ではないファイルのため、処理しないで続行。 {}", movieFile.getName());
                continue;
            }

            MultimediaObject mediaObject = new MultimediaObject(movieFile);
            MultimediaInfo mediaInfo = mediaObject.getInfo();
            // 変換を実施(milli secondをsecondにしておく)
            final BigDecimal movieDurationSecond = BigDecimal.valueOf(mediaInfo.getDuration()).scaleByPowerOfTen(-3);

            MediaSegment segment = MediaSegment.builder()
                    .duration(movieDurationSecond.doubleValue())
                    // 相対パス(絶対パスならストリーミングサーバーのパスを付与)
                    .uri(movieFile.getName())
                    .build();

            segmentList.add(segment);
        }

        log.debug("segment file count is {}", segmentList.size());

        return segmentList;
    }

}
