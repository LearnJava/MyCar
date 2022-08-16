package ru.konstantin.mycar.animation.rally.SceneView;

import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.FIRST_POINT_X;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.FIRST_POINT_Y;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.INITIAL_VELOCITY;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.MIN_RADIUS_SEARCH_CAR;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.PI_IN_GRAD;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.SECOND_POINT_X;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.SECOND_POINT_Y;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.THIRD_POINT_X;
import static ru.konstantin.mycar.animation.rally.utils.ConstantsKt.THIRD_POINT_Y;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import ru.konstantin.mycar.R;

public class AnimationPathView extends View {
    /** Исходные данные */ //region
    private Paint mPaint;
    private Path mPath;
    private Path mTouchPath;
    private Bitmap mBitmap;
    private PathMeasure mPathMeasure;
    private Matrix mMatrix;

    private int mOffsetX, mOffsetY;
    private float mPathLength;
    // Расстояние, проходимое за один шаг
    private float mStep;
    // Общее проходимое расстояние
    private float mDistance;

    private float[] mPosition;
    private float[] mTan;

    // Текущий угол
    private float mCurAngle;
    // Конечный угол
    private float mTargetAngle;
    // Угол на каждом шагу
    private float mStepAngle;
    // Доступ к параметрам дисплея для определения координат его углов
    private Display display = ((WindowManager) getContext().
                                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    // Текущие координаты машинки
    private float mCurX, mCurY;
    // Признак нажатия на машинку
    private Boolean isCarTapped = false;
    // Точки, определяющие нашу кривую
    private List<PointF> aPoints = new ArrayList<PointF>();
    //endregion

    public AnimationPathView(Context context) {
        super(context);
        init();
    }

    public AnimationPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationPathView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.m_car_red);
        mOffsetX = mBitmap.getWidth() / 2;
        mOffsetY = mBitmap.getHeight();

        // Создание произвольного пути
        mTouchPath = new Path();
        // Создание начального заданного пути по условию ДЗ
        mPath = new Path();
        aPoints.add(new PointF(FIRST_POINT_X, display.getHeight() - FIRST_POINT_Y));
        aPoints.add(new PointF(SECOND_POINT_X, SECOND_POINT_Y));
        aPoints.add(new PointF(display.getWidth() + THIRD_POINT_X, THIRD_POINT_Y));
        PointF point = aPoints.get(0);
        mPath.moveTo(point.x, point.y);
        for(int i = 0; i < aPoints.size() - 1; i++){
            point = aPoints.get(i);
            PointF next = aPoints.get(i+1);
            mPath.quadTo(point.x, point.y, (next.x + point.x) / 2, (point.y + next.y) / 2);
        }
        mPathMeasure = new PathMeasure(mPath, false);
        mPathLength = mPathMeasure.getLength();

        mStep = INITIAL_VELOCITY;
        mDistance = 0;

        mStepAngle = 1;
        mCurAngle = 0;
        mTargetAngle = 0;

        // Получение начальных координат машинки
        mPosition = new float[2];
        mTan = new float[2];
        mPathMeasure.getPosTan(0f, mPosition, mTan);
        mCurX = mPosition[0];
        mCurY = mPosition[1];

        mMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Проверка было ли нажатие на машинку или нет
        if (!isCarTapped) {
            mMatrix.postTranslate(mCurX, mCurY);
            canvas.drawBitmap(mBitmap, mMatrix, null);
            return;
        }

        if (mPath.isEmpty()) {
            return;
        }

        canvas.drawPath(mPath, mPaint);
        mMatrix.reset();

        if ((mTargetAngle - mCurAngle) > mStepAngle) {
            mCurAngle += mStepAngle;
            mMatrix.postRotate(mCurAngle, mOffsetX, mOffsetY);
            mMatrix.postTranslate(mCurX, mCurY);
            canvas.drawBitmap(mBitmap, mMatrix, null);
            // Перерисовать экран
            invalidate();
        } else if ((mCurAngle - mTargetAngle) > mStepAngle) {
            mCurAngle -= mStepAngle;
            mMatrix.postRotate(mCurAngle, mOffsetX, mOffsetY);
            mMatrix.postTranslate(mCurX, mCurY);
            canvas.drawBitmap(mBitmap, mMatrix, null);
            // Перерисовать экран
            invalidate();
        } else {
            mCurAngle = mTargetAngle;
            if (mDistance < mPathLength) {
                mPathMeasure.getPosTan(mDistance, mPosition, mTan);

                mTargetAngle = (float) (Math.atan2(mTan[1], mTan[0]) * PI_IN_GRAD / Math.PI);
                mMatrix.postRotate(mCurAngle, mOffsetX, mOffsetY);

                mCurX = mPosition[0] - mOffsetX;
                mCurY = mPosition[1] - mOffsetY;
                mMatrix.postTranslate(mCurX, mCurY);

                canvas.drawBitmap(mBitmap, mMatrix, null);

                mDistance += mStep;
                // Перерисовать экран
                invalidate();
            } else {
                // mDistance = 0;
                mMatrix.postRotate(mCurAngle, mOffsetX, mOffsetY);
                mMatrix.postTranslate(mCurX, mCurY);
                canvas.drawBitmap(mBitmap, mMatrix, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isCarTapped) {
                    mTouchPath.reset();
                    mTouchPath.moveTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isCarTapped) {
                    mTouchPath.lineTo(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isCarTapped) {
                    mTouchPath.lineTo(event.getX(), event.getY());
                    mPath = new Path(mTouchPath);

                    mPathMeasure = new PathMeasure(mPath, false);
                    mPathLength = mPathMeasure.getLength();
                    // Очистка начальных переменных
                    mStep = INITIAL_VELOCITY;
                    mDistance = 0;
                    mCurX = 0;
                    mCurY = 0;
                    mStepAngle = 1;
                    mCurAngle = 0;
                    mTargetAngle = 0;
                    // Перерисовать экран
                    invalidate();
                } else {
                    if (Math.sqrt((mCurX - event.getX()) * (mCurX - event.getX()) +
                            (mCurY - event.getY()) * (mCurY - event.getY())) <
                            MIN_RADIUS_SEARCH_CAR) {
                        isCarTapped = true;
                        // Перерисовать экран
                        invalidate();
                    }
                }
                break;
        }

        return true;
    }
}