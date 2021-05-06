package org.tentama.anchoco.anchocojar.immutablearray;

import java.util.Arrays;

/**
 * Immutable?なstring arrayを保持するPojo
 */
public class ImmutableStringArrayPojo {

    /** string array */
    private String[] hoge;

    /**
     * parameter hogeを取得する
     *
     * @return hoge
     */
    public String[] getHoge() {

        // lombok getterのような単純なgetterでは取得した配列をそのまま変更できてしまい
        // spot bugs EI_EXPOSE_REP で怒られる。
        // そのため、copyしたものを返却し中身の操作をさせない

        // ListならList.of();とかCollectionsにimmutableなのを作るのがあるのでそれを利用するといい

        return Arrays.copyOf(hoge, hoge.length);

    }

    /**
     * コンストラクタ
     *
     * @param hoge string array
     */
    public ImmutableStringArrayPojo(String[] hoge) {
        this.hoge = hoge;
    }
}
