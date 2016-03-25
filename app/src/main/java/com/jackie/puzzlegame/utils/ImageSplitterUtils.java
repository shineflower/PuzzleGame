package com.jackie.puzzlegame.utils;

import android.graphics.Bitmap;

import com.jackie.puzzlegame.bean.ImagePiece;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jackie on 2016/3/24.
 * 图片切片工具类
 */
public class ImageSplitterUtils {
    /**
     * @param bitmap  原始图片
     * @param piece   切成piece * piece 块
     * @return        图片块集合
     */
    public static List<ImagePiece> splitImage(Bitmap bitmap, int piece) {
        List<ImagePiece> imagePieces = new ArrayList<>();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int pieceWidth = Math.min(width, height) / piece;

        for (int i = 0; i < piece; i++) {
            for (int j = 0; j < piece; j++) {
                ImagePiece imagePiece = new ImagePiece();
                imagePiece.setIndex(j + i * piece);

                //切图
                int x = j * pieceWidth;
                int y = i * pieceWidth;

                imagePiece.setBitmap(Bitmap.createBitmap(bitmap, x, y, pieceWidth, pieceWidth));
                imagePieces.add(imagePiece);
            }
        }

        return imagePieces;
    }
}
