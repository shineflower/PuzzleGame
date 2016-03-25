package com.jackie.puzzlegame.bean;

import android.graphics.Bitmap;

/**
 * Created by Jackie on 2016/3/24.
 * 拼图游戏中每一个图片碎片
 */
public class ImagePiece {
    private int mIndex;
    private Bitmap mBitmap;

    public ImagePiece() {

    }

    public ImagePiece(int index, Bitmap bitmap) {
        this.mIndex = index;
        this.mBitmap = bitmap;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }
}
