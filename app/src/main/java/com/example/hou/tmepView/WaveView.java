package com.example.hou.tmepView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.logging.LogRecord;

public class WaveView extends View {


    private static final String TAG = "WaveView";

    /**
     * 创建画笔
     */
    private final Paint mPaint = new Paint();
    private final Path mFirstPath = new Path();
    private final Path mSecondPath = new Path();

    /**
     * 振幅最低的那条线
     */
    private final Path mCenterPath = new Path();


    /**
     * 采样点的数量 越多越精细  人眼需要128个
     */
    private static final int SAMPLINt_SIZE = 128;

    private float[] mSamplingX; //采样点
    private float[] mMapX;      //映射至新坐标系的X

    private int mWidth;
    private int mHeight;
    private int mCenterHeight;
    private int mAmpLItude; //振幅
    private long startTime = System.currentTimeMillis();
    /**
     * 波峰和两条交叉路径的交点的集合，包括起点和终点，用于绘制渐变
     */
    private final float[][] mCreateAndCrossPints = new float[9][];

    private final RectF rectF = new RectF();

    private final static Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    private final int mBackGroundColor = Color.rgb(24, 33, 41);

    private final int mCenterPathColor = Color.argb(64, 255, 255, 255);


    public WaveView(Context context) {
        this(context, null);            //注意构造方法的调用
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);                             //注意构造方法的调用
    }


    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();                                              //都调用第三个 在此初始化
    }

    private void init() {
        mPaint.setDither(true);//防抖动
        mPaint.setAntiAlias(true);//抗锯齿
        for (int i = 0; i < 9; i++) {
            mCreateAndCrossPints[i] = new float[2];
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate(); //通知进行页面刷新！！！！！！！！！
            handler.sendEmptyMessageDelayed(0, 16);  //16帧是因为每秒60帧 但不一定是准确的

        }
    };


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        if (mSamplingX == null) {
            //计算128个采样点
            mWidth = getWidth();
            mHeight = getHeight();
            mCenterHeight = mHeight == 0 ? 50 : mHeight >> 1;  //右移1 = /2  (中心点)
            mAmpLItude = mWidth == 0 ? 30 : mWidth >> 3;      //右移3 =/8  且放置溢出判空

            mSamplingX = new float[SAMPLINt_SIZE + 1];   //初始化采样点 起始点和终止点都要采样
            mMapX = new float[SAMPLINt_SIZE + 1];

            float gap = mWidth / (float) SAMPLINt_SIZE;  //计算出采样点间距

            float x;
            for (int i = 0; i < SAMPLINt_SIZE; i++) {
                x = i * gap;
                mSamplingX[i] = x;

                mMapX[i] = (x / (float) mWidth) * 4 - 2;   //坐标轴转换到中心坐标轴[-2,2]区间  标定所有采样点的X
            }
            //至此 采样完毕
        }
        canvas.drawColor(Color.rgb(24, 33, 41));  //给画布设置颜色

        mFirstPath.rewind();
        mSecondPath.rewind();
        mCenterPath.rewind();  //每一帧都要复位

        mFirstPath.moveTo(0, mCenterHeight); //移到中点
        mSecondPath.moveTo(0, mCenterHeight); //移到中点
        mCenterPath.moveTo(0, mCenterHeight); //移到中点

        float offset = (System.currentTimeMillis() - startTime) / 500F; //相位设置为自变量 它每刻都在变化

        //提前声明各种临时参数
//        float x;
        float x;
        float[] xy;




        float curX;
        float curY = 0;
        float lastV = 0;

        //计算第一个采样点的y值
        float nextV = (float) (mAmpLItude * calcalate(mMapX[0],offset));

        //波形函数的绝对值  用于筛选波峰和交错点
        float absLastV,absCurV,absNextV;

        //上一个筛选出的是 波峰还是交错点
        boolean lastIsCrest = false;
        //筛选点的数量
        int crestAndCrossCount = 0;


        for (int i = 0; i <= SAMPLINt_SIZE; i++) {
            curX = mSamplingX[i];
            curY = i < SAMPLINt_SIZE ? (float) (mAmpLItude * calcalate(mMapX[i], offset)) : 0; //用函数得到Y * 振幅

            //找到正确的点后 就挨个连线
            mFirstPath.lineTo(curX, mCenterHeight + curY);       //设置路径完毕 需要用画笔画出来
            mSecondPath.lineTo(curX, mCenterHeight - curY);       //设置路径完毕 需要用画笔画出来
            mCenterPath.lineTo(curX, (mCenterHeight + curY)/5);       //设置路径完毕 需要用画笔画出来

        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(1);
        canvas.drawPath(mFirstPath, mPaint); //画到画布上
        canvas.drawPath(mSecondPath, mPaint);//画到画布上
        canvas.drawPath(mCenterPath, mPaint);

        //这是缓存值
        int savecount = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.ALL_SAVE_FLAG);  //离屏缓存

        //填充上下两条线
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(1);
        canvas.drawPath(mFirstPath, mPaint); //画到画布上
        canvas.drawPath(mSecondPath, mPaint);//画到画布上

        //绘制渐变
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(mXfermode);  //设置叠加模式
        //设置渐变图层  准备叠加
        float startX,crestY,endX;
        for (int i=2;i<crestAndCrossCount;i+=2){
            //每次隔两个点可绘制一个矩形  先计算矩形的参数
            startX = mCreateAndCrossPints[i-2][0];
            crestY = mCreateAndCrossPints[i-1][1];
            endX = mCreateAndCrossPints[i][0];

            mPaint.setShader(new LinearGradient(0, mCenterHeight + mAmpLItude,
                    mWidth, mCenterHeight - mAmpLItude,
                    Color.GREEN, Color.BLUE, Shader.TileMode.CLAMP));
            rectF.set(startX, mCenterHeight+mAmpLItude , mWidth, mCenterHeight-mAmpLItude );

            canvas.drawRect(rectF, mPaint);
        }
        /*mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(mXfermode);  //设置叠加模式
        //设置渐变图层  准备叠加
        mPaint.setShader(new LinearGradient(0, mCenterHeight + mAmpLItude,
                mWidth, mCenterHeight - mAmpLItude,
                Color.GREEN, Color.BLUE, Shader.TileMode.CLAMP));

        rectF.set(0, mCenterHeight+mAmpLItude , mWidth, mCenterHeight-mAmpLItude );

        canvas.drawRect(rectF, mPaint);*/

        //清理
        mPaint.setShader(null);
        mPaint.setXfermode(null);

        canvas.restoreToCount(savecount);   //开始叠加！！！！！ 传入前一层的图层

        mPaint.setStyle(Paint.Style.STROKE);   //重新给曲线描边
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(3);
        canvas.drawPath(mFirstPath, mPaint);

        mPaint.setColor(Color.BLUE);
        canvas.drawPath(mSecondPath, mPaint);

        mPaint.setColor(mCenterPathColor);
        canvas.drawPath(mCenterPath, mPaint);
        //一帧已经绘制完毕  若是要不停进行动画 则需要使用handler 循环绘制

        handler.sendEmptyMessageDelayed(0, 16);  //延时通知handler刷新

    }




    private double calcalate(float mapX, float offset) {      //曲线函数  得到Y值
        offset %= 2;   //整除2!!  （相位是自变量）

        double sinFuncx = Math.sin(0.75 * Math.PI * mapX - offset * Math.PI);

        double recessionFunc = Math.pow((4 / (4 + Math.pow(mapX, 4))), 2.5);

        return sinFuncx * recessionFunc;
    }


}
