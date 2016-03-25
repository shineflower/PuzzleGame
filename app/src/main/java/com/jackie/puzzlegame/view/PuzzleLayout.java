package com.jackie.puzzlegame.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jackie.puzzlegame.R;
import com.jackie.puzzlegame.bean.ImagePiece;
import com.jackie.puzzlegame.utils.ImageSplitterUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Jackie on 2016/3/24.
 * 拼图游戏的面板
 */
public class PuzzleLayout extends RelativeLayout implements View.OnClickListener {
    private int mColumn = 3;
    /**
     * 容器的内边距
     */
    private int mPadding;
    /**
     * 每个图片块的间距(横、纵) dp
     */
    private int mMargin = 3;

    private ImageView[] mPuzzleItems;

    private int mItemWidth;
    /**
     * 游戏图片
     */
    private Bitmap mBitmap;

    private List<ImagePiece> mImagePieces;

    /**
     * 游戏面板的宽度
     */
    private int mWidth;

    private boolean mOnce;

    //判断游戏是否成功
    private boolean mIsGameSuccess;
    private boolean mIsGameOver;
    private boolean mIsGamePause;

    private int mLevel = 1;
    private static final int MSG_TIME_CHANGED = 1;
    private static final int MSG_NEXT_LEVEL = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_CHANGED:
                    if (mIsGameSuccess || mIsGameOver || mIsGamePause) {
                        return;
                    }

                    if (mOnPuzzleGameListener != null) {
                        mOnPuzzleGameListener.timeChanged(mTime);
                    }

                    if (mTime == 0) {
                        mIsGameOver = true;
                        mOnPuzzleGameListener.gameOver();
                        return;
                    }

                    mTime--;
                    mHandler.sendEmptyMessageDelayed(MSG_TIME_CHANGED, 1000);
                    break;
                case MSG_NEXT_LEVEL:
                    mLevel++;
                    if (mOnPuzzleGameListener != null) {
                        mOnPuzzleGameListener.nextLevel(mLevel);
                    } else {
                        nextLevel();
                    }
                    break;
            }
        }
    };

    public interface OnPuzzleGameListener {
        void nextLevel(int nextLevel);
        void timeChanged(int currentTime);
        void gameOver();
    }

    private OnPuzzleGameListener mOnPuzzleGameListener;

    public void setOnPuzzleGameListener(OnPuzzleGameListener onPuzzleGameListener) {
        this.mOnPuzzleGameListener = onPuzzleGameListener;
    }

    private boolean mIsTimeEnabled = false;
    private int mTime;

    /**
     * 设置是否开启时间
     *
     * @param isTimeEnabled
     */
    public void setTimeEnabled(boolean isTimeEnabled)
    {
        this.mIsTimeEnabled = isTimeEnabled;
    }

    public PuzzleLayout(Context context) {
        this(context, null);
    }

    public PuzzleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());

        if (!mOnce) {
            //进行切图，已经排序
            initBitmap();
            //设置每个Item的宽高和位置属性
            initItem();
            //判断是否开启时间
            checkTimeEnabled();

            mOnce = true;
        }

        setMeasuredDimension(mWidth, mWidth);
    }

    private void init() {
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mPadding = min(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    private int min(int... params) {
        int min = params[0];
        for (int param : params) {
            if (param < min) {
                min = param;
            }
        }

        return min;
    }

    private void initBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        }

        mImagePieces = ImageSplitterUtils.splitImage(mBitmap, mColumn);

        //使用sort完成乱序
//        Collections.sort(mImagePieces, new Comparator<ImagePiece>() {
//            @Override
//            public int compare(ImagePiece a, ImagePiece b) {
//                return Math.random() > 0.5 ? 1 : -1;
//            }
//        });

        Collections.shuffle(mImagePieces);
    }

    private void initItem() {
        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        mPuzzleItems = new ImageView[mColumn * mColumn];

        //生成item，设置rule
        for (int i = 0; i < mPuzzleItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mImagePieces.get(i).getBitmap());
            item.setId(i + 1);

            //在Item的tag中存储了index
            item.setTag(i + "_" + mImagePieces.get(i).getIndex());

            mPuzzleItems[i] = item;

            // 设置Item间横向间隙，通过rightMargin
            RelativeLayout.LayoutParams params = new LayoutParams(mItemWidth, mItemWidth);

            //如果不是最后一列
            if ((i + 1) % mColumn != 0) {
                params.rightMargin = mMargin;
            }

            //如果不是第一列
            if (i % mColumn != 0) {
                params.addRule(RelativeLayout.RIGHT_OF, mPuzzleItems[i - 1].getId());
            }

            //如果不是第一行，设置top margin和rule
            if ((i + 1) > mColumn) {
                params.topMargin = mMargin;
                params.addRule(RelativeLayout.BELOW, mPuzzleItems[i - mColumn].getId());
            }

            addView(item, params);
        }
    }

    private void checkTimeEnabled() {
        if (mIsTimeEnabled) {
            // 根据当前等级设置时间
            countTimeBasedLevel();
            mHandler.sendEmptyMessage(MSG_TIME_CHANGED);
        }
    }

    private void countTimeBasedLevel() {
        mTime = (int) (Math.pow(2, mLevel) * 60);
    }

    private ImageView mFirstItem;
    private ImageView mSecondItem;

    @Override
    public void onClick(View v) {
        if (mIsAniming) {
            return;
        }

        //两次点击同一个ImageView
        if (mFirstItem == v) {
            mFirstItem.setColorFilter(null);
            mFirstItem = null;
            return;
        }

        if (mFirstItem == null) {
            mFirstItem = (ImageView) v;
            mFirstItem.setColorFilter(Color.parseColor("#55FF0000"));
        } else {
            mSecondItem = (ImageView) v;
            exchange();
        }
    }

    /**
     * 动画层
     */
    private RelativeLayout mAnimLayout;
    private boolean mIsAniming;

    private void exchange() {
        mFirstItem.setColorFilter(null);
        
        //构造动画层
        setupAnimationLayout();

        //将要交换的两个Item复制到动画层中
        ImageView firstView = new ImageView(getContext());
        final Bitmap firstBitmap = mImagePieces.get(getImageIdByTag((String) mFirstItem.getTag())).getBitmap();
        firstView.setImageBitmap(firstBitmap);
        LayoutParams firstParams = new LayoutParams(mItemWidth, mItemWidth);
        firstParams.leftMargin = mFirstItem.getLeft() - mPadding;
        firstParams.topMargin = mFirstItem.getTop() - mPadding;
        firstView.setLayoutParams(firstParams);
        mAnimLayout.addView(firstView);

        ImageView secondView = new ImageView(getContext());
        final Bitmap secondBitmap = mImagePieces.get(getImageIdByTag((String) mSecondItem.getTag())).getBitmap();
        secondView.setImageBitmap(secondBitmap);
        LayoutParams secondParams = new LayoutParams(mItemWidth, mItemWidth);
        secondParams.leftMargin = mSecondItem.getLeft() - mPadding;
        secondParams.topMargin = mSecondItem.getTop() - mPadding;
        secondView.setLayoutParams(secondParams);
        mAnimLayout.addView(secondView);

        // 设置动画
        TranslateAnimation animFirst = new TranslateAnimation(0, mSecondItem.getLeft() - mFirstItem.getLeft(), 0, mSecondItem.getTop() - mFirstItem.getTop());
        animFirst.setDuration(300);
        animFirst.setFillAfter(true);
        firstView.startAnimation(animFirst);

        TranslateAnimation animSecond = new TranslateAnimation(0, mFirstItem.getLeft() - mSecondItem.getLeft(), 0, mFirstItem.getTop() - mSecondItem.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        secondView.startAnimation(animSecond);

        //监听动画
        animFirst.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFirstItem.setVisibility(View.INVISIBLE);
                mSecondItem.setVisibility(View.INVISIBLE);

                mIsAniming = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String) mFirstItem.getTag();
                String secondTag = (String) mSecondItem.getTag();

                mFirstItem.setImageBitmap(secondBitmap);
                mSecondItem.setImageBitmap(firstBitmap);

                mFirstItem.setTag(secondTag);
                mSecondItem.setTag(firstTag);

                mFirstItem.setVisibility(View.VISIBLE);
                mSecondItem.setVisibility(View.VISIBLE);

                mFirstItem = mSecondItem = null;

                mIsAniming = false;

                checkSuccess();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * 是否过关
     */
    private void checkSuccess() {
        boolean isSuccess = true;

        for (int i = 0; i < mPuzzleItems.length; i++) {
            ImageView item = mPuzzleItems[i];
            if (getImageIndexByTag((String) item.getTag()) != i) {
                isSuccess = false;
                break;
            }
        }

        if (isSuccess) {
            mIsGameSuccess = true;
            mHandler.removeMessages(MSG_TIME_CHANGED);
            mHandler.sendEmptyMessage(MSG_NEXT_LEVEL);
        }
    }

    /**
     * 根据tag获取Id
     *
     * @param tag
     * @return
     */
    public Integer getImageIdByTag(String tag)
    {
        String[] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    public Integer getImageIndexByTag(String tag)
    {
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }

    private void setupAnimationLayout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        } else {
            mAnimLayout.removeAllViews();
        }
    }

    public void restart() {
        mIsGameOver = false;
        mColumn--;
        nextLevel();
    }

    public void pause() {
        mIsGamePause = true;
        mHandler.removeMessages(MSG_TIME_CHANGED);
    }

    public void resume() {
        if (mIsGamePause) {
            mIsGamePause = false;

            mHandler.sendEmptyMessage(MSG_TIME_CHANGED);
        }
    }

    public void nextLevel() {
        removeAllViews();
        mAnimLayout = null;
        mColumn++;
        mIsGameSuccess = false;
        checkTimeEnabled();
        initBitmap();
        initItem();
    }
}
