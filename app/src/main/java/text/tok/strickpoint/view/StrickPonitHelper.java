package text.tok.strickpoint.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.MotionEventCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import text.tok.strickpoint.R;
import text.tok.strickpoint.utils.DisplayUtils;


public class StrickPonitHelper implements View.OnTouchListener, StrickPoint.DragStickViewListener {

    private  int dragViewLayouId;
    private Runnable viewInRangeMoveRun;
    private Runnable viewOutRangeMoveRun;
    private Runnable viewOut2InRangeUpRun;
    private Runnable viewOutRangeUpRun;
    private Runnable mViewInRangeUpRun;
    private WindowManager mWm;
    private WindowManager.LayoutParams mParams;
    private StrickPoint mStrickPoint;
    private View mDragView;
    private final Context mContext;
    private View mShowView;
    private int mStatusBarHeight;
    private float mMinFixRadius;
    private float mFixRadius;
    private float mFarthestDistance;
    private int mPathColor;
    private boolean isDragEnable;


    public StrickPonitHelper(Context mContext, View mShowView, int dragViewLayouId) {
        this.mContext = mContext;
        this.mShowView = mShowView;
        this.dragViewLayouId=dragViewLayouId;
        /**
         * 这步比较关键，当触摸到外部小圆点的时候会执行StrickPointHelper实现的onTouch方法
         */
        mShowView.setOnTouchListener(this);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;

    }
    public StrickPonitHelper(Context mContext, View mShowView, int dragViewLayouId,boolean isDragEnable) {
        this.mContext = mContext;
        this.mShowView = mShowView;
        this.dragViewLayouId=dragViewLayouId;
        /**
         * 这步比较关键，当触摸到外部小圆点的时候会执行StrickPointHelper实现的onTouch方法
         */
        mShowView.setOnTouchListener(this);
        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;
        setDragEnable(isDragEnable);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (!isDragEnable){
            return false;
        }
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            ViewParent parent = v.getParent();
            if (parent == null) {
                return false;
            }
            parent.requestDisallowInterceptTouchEvent(true);

            mStatusBarHeight = DisplayUtils.getStatusBarHeight(mShowView);
            mShowView.setVisibility(View.INVISIBLE);
         /**
          * 当手指触摸小圆点的时候这个对象将被创建，我试过不这样，直接用mShowView，
          *  动画做完以后WindowManager执行remove,mShowView再加添回其对应的父布局
          *  看着没问题，但是下次再按下这个小圆点就得不到它在屏幕上的坐标，points里面是0，0
          *  第一次计算的时候会产生误差。具体原因还在查询。
          */
            mDragView = LayoutInflater.from(mContext).inflate(dragViewLayouId, null, false);
//            文本内容复制
            copyText();

            mWm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

            mStrickPoint = new StrickPoint(mContext, mDragView, mWm);
//          初始化数据
            initStrickPointData();
//            注册拖拽过程的监听回调
            mStrickPoint.setDragStickViewListener(this);
//          开始添加的窗体让其显示
            mWm.addView(mStrickPoint, mParams);
            mWm.addView(mDragView, mParams);
        }
        /**
         * 当执行完了以上初始操作后把事件交由StrickPoint处理触摸
         */
        mStrickPoint.onTouchEvent(event);
        return true;
    }

    /**
     * 初始化StrickPoint的
     */
    private void initStrickPointData() {
 //          计算小圆点在屏幕的坐标
        int[] points = new int[2];
        mShowView.getLocationInWindow(points);
        int x = points[0] + mShowView.getWidth() / 2;
        int y = points[1] + mShowView.getHeight() / 2;
//           需要外部设置，当StrickPoint还没有执行完dispatchAttachedToWindow()时是计算不出其高度的
        mStrickPoint.setStatusBarHeight(mStatusBarHeight);
        if(mFarthestDistance>0){
            mStrickPoint.setFarthestDistance(mFarthestDistance);
        }
        if(mMinFixRadius>0){
            mStrickPoint.setMinFixRadius(mMinFixRadius);
        }
        if(mFixRadius>0){
            mStrickPoint.setFixRadius(mFixRadius);
        }
        if(mPathColor!=0){
            mStrickPoint.setPaintColor(mPathColor);
        }
//          初始化做作画的圆和控制点坐标
        mStrickPoint.setShowCanterPoint(x, y);
    }
    /**
     * 设置是否可拖动
     * @param isdragenable
     */
    public void setDragEnable(boolean isdragenable) {
        this.isDragEnable = isdragenable;
    }

    /**
     * 复制文本内容
     */
    private void copyText() {
        if(mShowView instanceof TextView &&mDragView instanceof TextView){
            ((TextView)mDragView).setText((((TextView) mShowView).getText().toString()));
        }
    }

    /**
     * 设置最大拖拽范围
     * @param mFarthestDistance px
     */
    public void setFarthestDistance(float mFarthestDistance) {
        this.mFarthestDistance = mFarthestDistance;
    }

    /**
     * 设置拖拽过程中固定圆变化的最小半径值
     * @param mMinFixRadius px
     */
    public void setMinFixRadius(float mMinFixRadius) {
        this.mMinFixRadius = mMinFixRadius;
    }

    /**
     * 设置固定圆半径
     * @param mFixRadius px
     */
    public void setFixRadius(float mFixRadius) {
        this.mFixRadius = mFixRadius;
    }

    /**
     * 设置绘制颜色
     * @param mPathColor
     */
    public void setmPathColor(int mPathColor) {
        this.mPathColor = mPathColor;
    }
    /**
     * 在范围内移动回调
     * @param dragCanterPoint 拖拽的中心坐标
     */
    @Override
    public void inRangeMove(PointF dragCanterPoint) {
        if(viewInRangeMoveRun !=null){
            viewInRangeMoveRun.run();
        }
    }

    /**
     * 在范围外移动回调
     * @param dragCanterPoint 拖拽的中心坐标
     */
    @Override
    public void outRangeMove(PointF dragCanterPoint) {
        if(viewOutRangeMoveRun !=null){
            viewOutRangeMoveRun.run();
        }
    }

    /**
     *  当移出了规定范围，最后在范围内松手的回调
     * @param dragCanterPoint
     */
    @Override
    public void out2InRangeUp(PointF dragCanterPoint) {
        removeView();
        if(viewOut2InRangeUpRun !=null){
            viewOut2InRangeUpRun.run();
        }
    }

    /**
     * 当移出了规定范围，最后在范围外松手的回调
     * @param dragCanterPoint
     */
    @Override
    public void outRangeUp(PointF dragCanterPoint) {
        removeView();
        playAnim(dragCanterPoint);
    }

    /**
     * 一直没有移动出范围，在范围内松手的回调
     * @param dragCanterPoint
     */
    @Override
    public void inRangeUp(PointF dragCanterPoint) {
        removeView();
        if(mViewInRangeUpRun !=null){
            mViewInRangeUpRun.run();
        }
    }
    /**
     * 播放移除动画(帧动画)，这个过程根据个人喜好
     * @param dragCanterPoint
     */
    private void playAnim(PointF dragCanterPoint) {
        final ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.out_anim);
        final AnimationDrawable mAnimDrawable = (AnimationDrawable) imageView
                .getDrawable();
        mParams.gravity= Gravity.TOP| Gravity.LEFT;
//        这里得到的是其真实的大小，因为此时还得不到其测量值
        int intrinsicWidth = imageView.getDrawable().getIntrinsicWidth();
        int intrinsicHeight = imageView.getDrawable().getIntrinsicHeight();

        mParams.x= (int) dragCanterPoint.x-intrinsicWidth/2;
        mParams.y= (int) dragCanterPoint.y-intrinsicHeight/2-mStatusBarHeight;
        mParams.width= WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height= WindowManager.LayoutParams.WRAP_CONTENT;
//      获取播放一次帧动画的总时长
        long duration = getAnimDuration(mAnimDrawable);

        mWm.addView(imageView, mParams);
        mAnimDrawable.start();
//        由于帧动画不能定时停止，只能采用这种办法
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAnimDrawable.stop();
                imageView.clearAnimation();
                mWm.removeView(imageView);
                if (viewOutRangeUpRun != null) {
                    viewOutRangeUpRun.run();
                }
            }
        },duration);
    }

    /**
     * 得到帧动画的摧毁时间
     * @param mAnimDrawable
     * @return
     */
    private long getAnimDuration(AnimationDrawable mAnimDrawable) {
        long duration=0;
        for(int i=0;i<mAnimDrawable.getNumberOfFrames();i++){
             duration += mAnimDrawable.getDuration(i);
        }
        return duration;
    }

    
    private void removeView() {
        if (mWm != null && mStrickPoint.getParent() != null && mDragView.getParent() != null) {
            mWm.removeView(mStrickPoint);
            mWm.removeView(mDragView);
        }
    }
    
    public Runnable getViewInRangeMoveRun() {
        return viewInRangeMoveRun;
    }

    public Runnable getViewOutRangeMoveRun() {
        return viewOutRangeMoveRun;
    }

    public Runnable getViewOut2InRangeUpRun() {
        return viewOut2InRangeUpRun;
    }
    /**
     * view在范围内移动指此此Runnable
     * @param viewInRangeMoveRun
     */
    public void setViewInRangeMoveRun(Runnable viewInRangeMoveRun) {
        this.viewInRangeMoveRun = viewInRangeMoveRun;
    }

    /**
     * view在范围外移动执行此Runnable
     * @param viewOutRangeMoveRun
     */
    public void setViewOutRangeMoveRun(Runnable viewOutRangeMoveRun) {
        this.viewOutRangeMoveRun = viewOutRangeMoveRun;
    }

    /**
     * view移出过范围，最后在范围内松手执行次Runnable
     * @param viewOut2InRangeUpRun
     */
    public void setViewOut2InRangeUpRun(Runnable viewOut2InRangeUpRun) {
        this.viewOut2InRangeUpRun = viewOut2InRangeUpRun;
    }

    /**
     * view没有移出过范围，在范围内松手
     * @param mViewInRangeUpRun
     */
    public void setViewInRangeUpRun(Runnable mViewInRangeUpRun) {
        this.mViewInRangeUpRun = mViewInRangeUpRun;
    }

    /**
     * view移出范围，最后在范围外松手
     * @param viewOutRangeUpRun
     */
    public void setViewOutRangeUpRun(Runnable viewOutRangeUpRun) {
        this.viewOutRangeUpRun = viewOutRangeUpRun;
    }


    public Runnable getViewOutRangeUpRun() {
        return viewOutRangeUpRun;
    }

    public Runnable getmViewInRangeUpRun() {
        return mViewInRangeUpRun;
    }
}
