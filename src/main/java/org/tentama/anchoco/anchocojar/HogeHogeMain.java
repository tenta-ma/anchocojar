package org.tentama.anchoco.anchocojar;

import lombok.extern.slf4j.Slf4j;
import org.tentama.anchoco.anchocojar.m3u8.ffmpeg.ConvertFfmpeg;

/**
 * initialize用のテストクラス
 */
@Slf4j
public class HogeHogeMain {

    /**
     * main
     *
     * @param args プログラム引数
     */
    public static void main(String[] args) {
        log.info("hoge hoge main.");

        String targetPath = "img0017";

//        Mpeg2Ts mpegTs = new Mpeg2Ts();

        // mpegTs.convert(targetPath, targetPath + ".mov");

//        targetPath = "video000";
//        mpegTs.convert(targetPath, targetPath + ".ts");

        new ConvertFfmpeg().convertNativeCommand(targetPath, targetPath + ".mov");

        // M3u8Perser.createM3u8(targetPath);
    }

}
