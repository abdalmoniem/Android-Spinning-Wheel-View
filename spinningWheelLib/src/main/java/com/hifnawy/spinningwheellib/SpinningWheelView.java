package com.hifnawy.spinningwheellib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hifnawy.spinningwheellib.exceptions.InvalidWheelSectionDataException;
import com.hifnawy.spinningwheellib.exceptions.InvalidWheelSectionsException;
import com.hifnawy.spinningwheellib.model.FlingDirection;
import com.hifnawy.spinningwheellib.model.MarkerPosition;
import com.hifnawy.spinningwheellib.model.SectionType;
import com.hifnawy.spinningwheellib.model.WheelBitmapSection;
import com.hifnawy.spinningwheellib.model.WheelColorSection;
import com.hifnawy.spinningwheellib.model.WheelDrawableSection;
import com.hifnawy.spinningwheellib.model.WheelSection;
import com.hifnawy.spinningwheellib.model.WheelTextSection;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by abicelis on 25/7/2017.
 */

public class SpinningWheelView extends RelativeLayout {
    private static float angleOffset;
    private static Matrix matrix;
    //Internal data
    private ImageView mWheel;
    private ImageView mMarker;
    private RelativeLayout mMarkerContainer;
    private int wheelHeight, wheelWidth;
    private GestureDetector gestureDetector;
    private WheelTouchListener touchListener;
    private boolean[] quadrantTouched = new boolean[]{false, false, false, false, false};
    private boolean allowRotating = true;
    private FlingRunnable flingRunnable;
    private boolean isReversing = false;
    private FlingDirection flingDirection = FlingDirection.STOPPED;

    private ObjectAnimator rightRotationAnimation;
    private ObjectAnimator leftAnimationRotation;

    private Vibrator vibrator;

    private Context mContext;

    //Configurable options

    private List<WheelSection> mWheelSections;
    private MarkerPosition mMarkerPosition = MarkerPosition.TOP;
    private boolean mCanGenerateWheel;
    private boolean tickVibrations = false;
    private boolean isPreview = false;
    private @ColorRes
    int mWheelBorderLineColor = -1;
    private int mWheelBorderLineThickness = 10;
    private @ColorRes
    int mWheelSeparatorLineColor = -1;
    private int mWheelSeparatorLineThickness = 10;
    private WheelEventsListener mListener;
    private float initialFlingDampening = Constants.INITIAL_FLING_VELOCITY_DAMPENING;
    private float flingVelocityDampening = Constants.FLING_VELOCITY_DAMPENING;
    private int globalTextSize;

    /* Constructors and init */
    public SpinningWheelView(Context context) {
        super(context);

        mContext = context;
        init(mContext);
    }

    public SpinningWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init(mContext);
    }

    public SpinningWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        init(mContext);
    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }






    /* Public setters */

    /**
     * @return The current rotation of the wheel.
     */
    private static double getCurrentRotation() {
        float[] v = new float[9];
        matrix.getValues(v);
        double angle = Math.round((Math.atan2(v[Matrix.MSKEW_X], v[Matrix.MSCALE_X]) * (180 / Math.PI)) - angleOffset) - 90f;

        if (angle > 0) {
            return angle;
        } else {
            return 360 + angle;
        }
    }

    public void reInit() {
        mMarker = null;

        globalTextSize = 30;

        mWheel = findViewById(R.id.prize_wheel_view_wheel);
        mMarker = findViewById(R.id.prize_wheel_view_marker);
        mMarkerContainer = findViewById(R.id.prize_wheel_view_marker_container);

        mMarker.setVisibility(View.VISIBLE);

        mWheel.setScaleType(ImageView.ScaleType.MATRIX);

        matrix = new Matrix();

        gestureDetector = new GestureDetector(mContext, new WheelGestureListener());
        touchListener = new WheelTouchListener();
        mWheel.setOnTouchListener(touchListener);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (wheelHeight == 0 || wheelWidth == 0) {

                    //Grab the shortest of the container dimensions
                    int minDimen = wheelHeight = Math.min(getHeight(), getWidth());

                    //Apply the margin for the marker. Use those dimensions for the wheel
                    wheelHeight = wheelWidth = minDimen - (int) (DimensionUtil.convertDpToPixel(Constants.WHEEL_MARGIN_FOR_MARKER_DP) * 2);

                    //Resize the wheel's imageview
                    mWheel.getLayoutParams().height = wheelHeight;
                    mWheel.getLayoutParams().width = wheelWidth;
                    mWheel.requestLayout();

                    //Resize the marker's container
                    mMarkerContainer.getLayoutParams().height = minDimen;
                    mMarkerContainer.getLayoutParams().width = minDimen;
                    mMarkerContainer.requestLayout();
                    mMarkerContainer.setRotation(mMarkerPosition.getDegreeOffset());
//                    mMarkerContainer.setY(mMarkerContainer.getY() + (wheelHeight / 2));
//                    mMarkerContainer.setY(mMarkerContainer.getY() + 5f);

                    if (mCanGenerateWheel) {
                        generateWheelImage();
                    }
                }
            }
        });
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.prize_wheel_view, this);

        globalTextSize = 30;

        mWheel = findViewById(R.id.prize_wheel_view_wheel);
        mMarker = findViewById(R.id.prize_wheel_view_marker);
        mMarkerContainer = findViewById(R.id.prize_wheel_view_marker_container);

        mMarker.setVisibility(View.VISIBLE);

        mWheel.setScaleType(ImageView.ScaleType.MATRIX);

        matrix = new Matrix();

        gestureDetector = new GestureDetector(context, new WheelGestureListener());
        touchListener = new WheelTouchListener();
        mWheel.setOnTouchListener(touchListener);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (wheelHeight == 0 || wheelWidth == 0) {

                    //Grab the shortest of the container dimensions
                    int minDimen = wheelHeight = Math.min(getHeight(), getWidth());

                    //Apply the margin for the marker. Use those dimensions for the wheel
                    wheelHeight = wheelWidth = minDimen - (int) (DimensionUtil.convertDpToPixel(Constants.WHEEL_MARGIN_FOR_MARKER_DP) * 2);

                    //Resize the wheel's imageview
                    mWheel.getLayoutParams().height = wheelHeight;
                    mWheel.getLayoutParams().width = wheelWidth;
                    mWheel.requestLayout();

                    //Resize the marker's container
                    mMarkerContainer.getLayoutParams().height = minDimen;
                    mMarkerContainer.getLayoutParams().width = minDimen;
                    mMarkerContainer.requestLayout();
                    mMarkerContainer.setRotation(mMarkerPosition.getDegreeOffset());
//                    mMarkerContainer.setY(mMarkerContainer.getY() + (wheelHeight / 2));
//                    mMarkerContainer.setY(mMarkerContainer.getY() + 5f);

                    if (mCanGenerateWheel) {
                        generateWheelImage();
                    }
                }
            }
        });
    }

    /**
     * Populate a SpinningWheelView with a List of WheelSections such as
     * {@link com.hifnawy.spinningwheellib.model.WheelBitmapSection},
     * {@link com.hifnawy.spinningwheellib.model.WheelColorSection} and
     * {@link com.hifnawy.spinningwheellib.model.WheelDrawableSection}.
     * As SpinningWheelView supports Bitmaps, Colors and Drawables.
     * NOTE: Enter at least {@value com.hifnawy.spinningwheellib.Constants#MINIMUM_WHEEL_SECTIONS}
     * WheelSections and at most {@value com.hifnawy.spinningwheellib.Constants#MAXIMUM_WHEEL_SECTIONS}.
     *
     * @param wheelSections a List of WheelSections
     */
    public void setWheelSections(List<WheelSection> wheelSections) {
//        if (wheelSections == null || wheelSections.size() < Constants.MINIMUM_WHEEL_SECTIONS || wheelSections.size() > Constants.MAXIMUM_WHEEL_SECTIONS)
//            throw new InvalidWheelSectionsException();

//        for (WheelSection ws : wheelSections) {
//            Log.d("mn3m", ws.toString());
//        }

        if (wheelSections == null) {
            throw new InvalidWheelSectionsException();
        }

        mWheelSections = wheelSections;
    }

    public List<WheelSection> getWheelSections() {
        return mWheelSections;
    }

    /**
     * Set a position of the wheel Marker. Please see
     * {@link com.hifnawy.spinningwheellib.model.MarkerPosition} for all the options.
     * DEFAULT VALUE: {@link com.hifnawy.spinningwheellib.model.MarkerPosition#TOP}
     *
     * @param markerPosition A {@link com.hifnawy.spinningwheellib.model.MarkerPosition}
     */
    public void setMarkerPosition(@NonNull MarkerPosition markerPosition) {
        mMarkerPosition = markerPosition;
        mMarkerContainer.setRotation(mMarkerPosition.getDegreeOffset());
    }

    /**
     * Set the initial fling dampening.
     * NOTE: A number between 1.0 (no dampening) and 5.0 (lots of dampening) is recommended. Default 3.0
     */
    public void setInitialFlingDampening(float dampening) {
        initialFlingDampening = dampening;
    }

    /**
     * Set the velocity dampening when wheel is flung.
     * NOTE: A number between 1 (no dampening) and 1.1 (lots of dampening) is recommended. Default 1.06
     */
    public void setFlingVelocityDampening(float dampening) {
        initialFlingDampening = dampening;
    }

    /**
     * Set the color of the wheel's border.
     * DEFAULT: No border
     */
    public void setWheelBorderLineColor(@ColorRes int color) {
        mWheelBorderLineColor = color;
    }

    /**
     * Set the thickness of the wheel's border in dp.
     * DEFAULT: No border
     */
    public void setWheelBorderLineThickness(int thickness) {
        if (thickness >= 0)
            mWheelBorderLineThickness = thickness;
    }

    /**
     * Set the color of the wheel section separator lines.
     * DEFAULT: No border
     */
    public void setWheelSeparatorLineColor(@ColorRes int color) {
        mWheelSeparatorLineColor = color;
    }

    /**
     * Set the thickness of the wheel section separator lines in dp.
     * DEFAULT: No border
     */
    public void setWheelSeparatorLineThickness(int thickness) {
        if (thickness >= 0)
            mWheelSeparatorLineThickness = thickness;
    }

    /**
     * Set {@link com.hifnawy.spinningwheellib.WheelEventsListener}, a listener interface.
     * to receive events such as: onWheelStopped(), onWheelFlung() and onWheelSettled()
     */
    public void setWheelEventsListener(WheelEventsListener listener) {
        mListener = listener;
    }

    /**
     * This method MUST BE CALLED AFTER all pervious settings have been set.dp.
     * DEFAULT: No border
     */
    public void generateWheel() {
        if (wheelHeight == 0)        //If view doesn't have width/height yet
            mCanGenerateWheel = true;
        else
            generateWheelImage();
    }

    public void setTickVibrations(Context context, boolean tickVibrations) {
        this.tickVibrations = tickVibrations;

        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setIsPreview(boolean preview) {
        isPreview = preview;

        if (isPreview) {
            mWheel.setOnTouchListener(null);
        }
    }

    public int getGlobalTextSize() {
        return globalTextSize;
    }

    public void setGlobalTextSize(int globalTextSize) {
        this.globalTextSize = globalTextSize;
    }

    public ImageView getWheelImageView() {
        return mWheel;
    }

    public FlingDirection getFlingDirection() {
        return flingDirection;
    }

    /* Internal methods */

    /**
     * Call this method to manually stop the wheel
     */
    public void stopWheel() {

//        Log.d("mn3m", flingDirection + " <> " + !isReversing  + " >< " + (flingDirection != FlingDirection.STOPPED));

        allowRotating = false;

//        if (!isReversing && (flingDirection != FlingDirection.STOPPED)) {
//            isReversing = true;
//            Log.d("mn3m", "reversing breaks...");
//            if (flingDirection == FlingDirection.CCW) {
//                doFlingWheel(5000);
////                flingWheel(5000, false);
//            } else if (flingDirection == FlingDirection.CW) {
//                doFlingWheel(-5000);
////                flingWheel(5000, true);
//            }
//        }
    }

    /**
     * Call this method to manually fling the wheel
     *
     * @param velocity  the speed of the fling
     * @param clockwise the direction of the rotation.
     */
    public void flingWheel(int velocity, boolean clockwise) {
        isReversing = false;
        doFlingWheel((clockwise ? -velocity : velocity));
    }

    /**
     * Rotate the wheel.
     *
     * @param degrees The degrees, the wheel should get rotated.
     */
    private void rotateWheel(float degrees) {
        matrix.postRotate(degrees, wheelWidth / 2, wheelHeight / 2);
        mWheel.setImageMatrix(matrix);
    }

    /**
     * Reset touch quadrants to false.
     */
    private void resetQuadrants() {
        for (int i = 0; i < quadrantTouched.length; i++) {
            quadrantTouched[i] = false;
        }
    }

    /**
     * @return The current section.
     */
    private int getCurrentSelectedSectionIndex() {
        double sectionAngle = (360.0f / mWheelSections.size());
        double currentRotation = getCurrentRotation() + mMarkerPosition.getDegreeOffset();

        if (currentRotation > 360)
            currentRotation = currentRotation - 360;

        int selection = (int) Math.floor(currentRotation / sectionAngle);

        if (selection >= mWheelSections.size())      //Rounding errors occur. Limit to items-1
            selection = mWheelSections.size() - 1;

        return selection;
    }

    /**
     * Generates the wheel bitmap
     */
    private void generateWheelImage() {
        if (mWheelSections == null)
            throw new InvalidWheelSectionsException("You must use setWheelSections() to set the sections of the wheel.");

        // _______________________
        // |                     |
        // |                     |
        // |          ^ -----    |
        // |        *   *   <------ startAngle
        // |      *       *      |
        // |    *      <----*------ sweepAngle
        // |       *  _  *       |
        // _______________________

        int wheelSectionCount = mWheelSections.size() > 0 ? mWheelSections.size() : 1;
        float sweepAngle = (360.0f / wheelSectionCount);
        float startAngle = 0 /*90 - sweepAngle/2*/;
        angleOffset = startAngle;                   //save angle, will be used later


        //Init whitePaint for masking
        Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(1f);
        whitePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //Init maskPaint for erasing unmasked (transparent) sections
        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        //Init mask and result canvases
        Bitmap mask = Bitmap.createBitmap(wheelWidth, wheelHeight, Bitmap.Config.ARGB_8888);
        Bitmap result = Bitmap.createBitmap(wheelWidth, wheelHeight, Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);
        Canvas maskCanvas = new Canvas(mask);

        //Draw mask arc
//        RectF box = new RectF(0, 0, wheelWidth, wheelHeight);
        RectF box = new RectF(2, 2, wheelWidth - 2, wheelHeight - 2);

        maskCanvas.drawArc(box, startAngle, sweepAngle, true, whitePaint);

//        resultCanvas.drawRect(box, whitePaint);

        //Get a Rect enclosing the mask
        Rect drawnMaskRect = ImageUtil.cropTransparentPixelsFromImage(mask);

        if (mWheelSections.size() >= 1) {
            for (int i = 0; i < mWheelSections.size(); i++) {
                WheelSection section = mWheelSections.get(i);
//            Log.d(getClass().getName(), section.toString());

                //If drawing a color, process is much simpler, handle it here
                if (section.getType().equals(SectionType.TEXT)) {

                    String text = ((WheelTextSection) section).getText();

                    Typeface typeface = Typeface.create("casual", Typeface.NORMAL);

                    TextPaint textPaint = new TextPaint();
                    textPaint.setAntiAlias(true);
                    textPaint.setColor(((WheelTextSection) section).getForegroundColor());
                    textPaint.setTypeface(typeface);
                    textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    textPaint.setTextSize(globalTextSize);

                    Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    colorPaint.setColor(((WheelTextSection) section).getBackgroundColor());
                    colorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

//                Paint colort = new Paint(Paint.ANTI_ALIAS_FLAG);
//                colort.setColor(0xffff0000);
//                colort.setStyle(Paint.Style.STROKE);
//                colort.setStrokeWidth(DimensionUtil.convertDpToPixel(mWheelSeparatorLineThickness));

                    int r = Math.min(wheelWidth, wheelHeight) / 2;

                    double t1 = Math.toRadians(angleOffset) + (2 * Math.PI * (i + 0) / mWheelSections.size());
                    double t2 = Math.toRadians(angleOffset) + (2 * Math.PI * (i + 1) / mWheelSections.size());

//                Log.d(getClass().getName(), "Mid Angle: " + Math.toDegrees((t1 + t2) / 2));

                    int centerX = wheelWidth / 2;
                    int centerY = wheelHeight / 2;
                    int xs = (int) Math.round(centerX + r * Math.cos((t1 + t2) / 2));
                    int ys = (int) Math.round(centerY + r * Math.sin((t1 + t2) / 2));

                    Rect textBounds = new Rect();

                    textPaint.getTextBounds(text, 0, text.length(), textBounds);

                    Path path = new Path();
                    path.moveTo(centerX, centerY);
                    path.lineTo(xs, ys);

                    resultCanvas.save();
                    resultCanvas.rotate(sweepAngle * i, resultCanvas.getWidth() / 2, resultCanvas.getHeight() / 2);
                    resultCanvas.drawArc(box, startAngle, sweepAngle, true, colorPaint);
                    resultCanvas.restore();

//                while (textBounds.width() >= (r - 100)) {
//                    textPaint.setTextSize(textPaint.getTextSize() - 4);
//                    textPaint.getTextBounds(text, 0, text.length(), textBounds);
//                }

//                resultCanvas.drawLine(centerX, centerY, xs, ys, colort);
                    resultCanvas.drawTextOnPath(text, path, -textBounds.exactCenterX() + (r * 0.4f), -textBounds.exactCenterY(), textPaint);

                    continue;
                }
                if (section.getType().equals(SectionType.COLOR)) {
                    int colorRes = ((WheelColorSection) section).getColor();

                    Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    colorPaint.setColor(ContextCompat.getColor(getContext(), colorRes));
                    colorPaint.setStrokeWidth(1f);
                    colorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    resultCanvas.save();
                    resultCanvas.rotate(sweepAngle * i, resultCanvas.getWidth() / 2, resultCanvas.getHeight() / 2);
                    resultCanvas.drawArc(box, startAngle, sweepAngle, true, colorPaint);
                    resultCanvas.restore();
                    //Rotate the canvas sweepAngle degrees, around its center.
//                resultCanvas.rotate(sweepAngle, resultCanvas.getWidth()/2, resultCanvas.getHeight()/2);

                    continue;
                }


                //Grab the bitmap for this section
                Bitmap sectionBitmap;
                switch (section.getType()) {
                    case BITMAP:
                        sectionBitmap = ((WheelBitmapSection) section).getBitmap();
                        if (sectionBitmap == null)
                            throw new InvalidWheelSectionDataException("Invalid bitmap. WheelSection data = " + section.toString());
                        break;
                    case DRAWABLE:
                        //Try to get bitmap drawable (jpg, png) or xml based drawable (xml, layer-list, etc)
                        int drawableRes = ((WheelDrawableSection) section).getDrawableRes();
                        Drawable d = ContextCompat.getDrawable(getContext(), drawableRes);
                        sectionBitmap = ImageUtil.drawableToBitmap(d);

                        if (sectionBitmap == null) {

                            try {
                                //Try to get the name
                                String resourceEntryName = getResources().getResourceEntryName(drawableRes);
                                throw new InvalidWheelSectionDataException("Problem generating bitmap from drawable. Resource name='" + resourceEntryName + "', Resource ID=" + drawableRes);
                            } catch (Resources.NotFoundException e) {
                                throw new InvalidWheelSectionDataException("Problem generating bitmap from drawable. Could not find resource. Resource ID=" + drawableRes);
                            }
                        }
                        break;
                    default:
                        throw new InvalidWheelSectionDataException("Unexpected SectionType error. Please report this error. Section data=" + section.toString());
                }

                //Get center cropped bitmap
                Bitmap sectionBitmapCropped = ImageUtil.getCenterCropBitmap(sectionBitmap, drawnMaskRect.width(), drawnMaskRect.height());

                //Create a blank temp bitmap to work on
                Bitmap temp = Bitmap.createBitmap(wheelWidth, wheelHeight, Bitmap.Config.ARGB_8888);
                Canvas tempCanvas = new Canvas(temp);


                //Draw cropped sectionBitmap image onto temp, then draw mask on it
                tempCanvas.drawBitmap(sectionBitmapCropped, null, drawnMaskRect, null);
                tempCanvas.drawBitmap(mask, 0, 0, maskPaint);

                resultCanvas.save();
                //Rotate the canvas sweepAngle degrees, around its center.
                resultCanvas.rotate(sweepAngle * i, resultCanvas.getWidth() / 2, resultCanvas.getHeight() / 2);

                //Draw masked image to resultCanvas
                resultCanvas.drawBitmap(temp, 0, 0, new Paint());
                resultCanvas.restore();
            }

            //If a wheel separator line color was set
            if ((mWheelSeparatorLineColor != -1) && (mWheelSections.size() > 1)) {
                Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
                color.setColor(ContextCompat.getColor(getContext(), mWheelSeparatorLineColor));
                color.setStyle(Paint.Style.STROKE);
                color.setStrokeWidth(DimensionUtil.convertDpToPixel(mWheelSeparatorLineThickness));

//            Paint colort = new Paint(Paint.ANTI_ALIAS_FLAG);
//            colort.setColor(0xff000000);
//            colort.setStyle(Paint.Style.STROKE);
//            colort.setStrokeWidth(DimensionUtil.convertDpToPixel(mWheelSeparatorLineThickness));

                int r = Math.min(wheelWidth, wheelHeight) / 2;
                int centerX = wheelWidth / 2;
                int centerY = wheelHeight / 2;

                for (int i = 0; i < mWheelSections.size(); i++) {
                    double t1 = Math.toRadians(angleOffset) + (2 * Math.PI * i / mWheelSections.size());
//                double t2 = Math.toRadians(angleOffset) + (2 * Math.PI * (i + 1) / mWheelSections.size());
//                double t3 =  (t1 + t2) / 2;
                    int x = (int) Math.round(centerX + r * Math.cos(t1));
                    int y = (int) Math.round(centerY + r * Math.sin(t1));

//                int xt = (int) Math.round(centerX + r / 2 * Math.cos(t3));
//                int yt = (int) Math.round(centerY + r / 2 * Math.sin(t3));

                    resultCanvas.drawLine(centerX, centerY, x, y, color);
//                resultCanvas.drawLine(centerX, centerY, xt, yt, colort);

                }
            }


            //If a wheelBorder line color was set
            if (mWheelBorderLineColor != -1) {
                Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
                color.setColor(ContextCompat.getColor(getContext(), mWheelBorderLineColor));
                color.setAlpha(150);
                color.setStyle(Paint.Style.STROKE);
                color.setStrokeWidth(DimensionUtil.convertDpToPixel(mWheelBorderLineThickness));

//                Paint colorS = new Paint(Paint.ANTI_ALIAS_FLAG);
//                colorS.setColor(0xffff0000);
//                colorS.setStyle(Paint.Style.STROKE);
//                colorS.setStrokeWidth(5);

                int r = Math.min(wheelWidth, wheelHeight) / 2;
                int r2 = r - (int) (DimensionUtil.convertDpToPixel(mWheelBorderLineThickness) / 2) + 2;

                int centerX = wheelWidth / 2;
                int centerY = wheelHeight / 2;

//            int stopX = (int) (centerX + r2 * Math.cos(Math.toRadians(angleOffset) + (2 * Math.PI * 3 / mWheelSections.size())));
//            int stopY = (int) (centerY + r2 * Math.sin(Math.toRadians(angleOffset) + (2 * Math.PI * 3 / mWheelSections.size())));
//
//            resultCanvas.drawLine(centerX, centerY, stopX, stopY, colorS);
//            resultCanvas.drawCircle(wheelWidth / 2, wheelHeight / 2, (Math.min(wheelWidth, wheelHeight) - mWheelBorderLineThickness) / 2, color);
                resultCanvas.drawCircle(centerX, centerY, r2, color);
            }
        } else {
            int r = Math.min(wheelWidth, wheelHeight) / 2;
            int centerX = wheelWidth / 2;
            int centerY = wheelHeight / 2;

            Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colorPaint.setColor(0x0a000000);
            colorPaint.setStrokeWidth(1f);
            colorPaint.setStyle(Paint.Style.FILL);
            resultCanvas.drawCircle(centerX, centerY, r, colorPaint);

            //If a wheelBorder line color was set
            if (mWheelBorderLineColor != -1) {
                Paint color = new Paint(Paint.ANTI_ALIAS_FLAG);
                color.setColor(0x22000000);
                color.setStyle(Paint.Style.STROKE);
                color.setStrokeWidth(DimensionUtil.convertDpToPixel(mWheelBorderLineThickness));


                int r2 = r - (int) (DimensionUtil.convertDpToPixel(mWheelBorderLineThickness) / 2) + 2;
                resultCanvas.drawCircle(centerX, centerY, r2, color);
            }
        }

        mWheel.setImageBitmap(result);
    }


    private void doFlingWheel(float velocity) {

        //Stop previous fling
        removeCallbacks(flingRunnable);

        //Notify fling
        if (mListener != null)
            mListener.onWheelFlung();

        //Launch new fling
        allowRotating = true;
        flingRunnable = new FlingRunnable(velocity / initialFlingDampening);
        post(flingRunnable);
    }


    private class WheelTouchListener implements OnTouchListener {

        private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    resetQuadrants();
                    startAngle = getAngle(event.getX(), event.getY());
                    allowRotating = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateWheel((float) (startAngle - currentAngle));
                    startAngle = currentAngle;
                    break;

                case MotionEvent.ACTION_UP:
                    allowRotating = true;
                    break;
            }

            // set the touched quadrant to true
            quadrantTouched[getQuadrant(event.getX() - (wheelWidth / 2), wheelHeight - event.getY() - (wheelHeight / 2))] = true;

            //Notify gesture detector
            gestureDetector.onTouchEvent(event);

            return true;
        }

        /**
         * @return The angle of the unit circle with the image view's center
         */
        private double getAngle(double xTouch, double yTouch) {
            double x = xTouch - (wheelWidth / 2d);
            double y = wheelHeight - yTouch - (wheelHeight / 2d);

            return (Math.atan2(y, x) * 180) / Math.PI;
        }

    }

    private class WheelGestureListener implements GestureDetector.OnGestureListener {


        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // get the quadrant of the start and the end of the fling
            int q1 = getQuadrant(e1.getX() - (wheelWidth / 2), wheelHeight - e1.getY() - (wheelHeight / 2));
            int q2 = getQuadrant(e2.getX() - (wheelWidth / 2), wheelHeight - e2.getY() - (wheelHeight / 2));

            // the inversed rotations
            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && quadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && quadrantTouched[3])) {

                doFlingWheel((-1 * (velocityX + velocityY)));
                //post(new FlingRunnable( (-1 * (velocityX + velocityY)) / initialFlingDampening ));
                //if(mListener != null)
                //mListener.onWheelFlung();
            } else {
                // the normal rotation
                doFlingWheel((velocityX + velocityY));

                //post(new FlingRunnable( (velocityX + velocityY) / initialFlingDampening ));
                //if(mListener != null)
                //mListener.onWheelFlung();
            }

            return true;
        }


    }


    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    private class FlingRunnable implements Runnable {
        private final int ANIMATION_DURATION = 100;
        MediaPlayer tick = MediaPlayer.create(getContext(), R.raw.click);
        MediaPlayer tada = MediaPlayer.create(getContext(), R.raw.tada);
        private float velocity;
        private int currentStandingSection;

        public FlingRunnable(float velocity) {
            this.velocity = velocity;
            this.currentStandingSection = getCurrentSelectedSectionIndex();


            boolean change = false;

            if ((flingDirection == FlingDirection.CW) && (velocity > 0)) {
                change = true;
            }
            if ((flingDirection == FlingDirection.CCW) && (velocity < 0)) {
                change = true;
            }
            if (flingDirection == FlingDirection.STOPPED) {
                change = true;
            }

            flingDirection = (velocity < 0) ? FlingDirection.CW : FlingDirection.CCW;

            if (change) {
                if (rightRotationAnimation != null) {
                    rightRotationAnimation.end();
                }
                if (leftAnimationRotation != null) {
                    leftAnimationRotation.end();
                }
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", mMarker.getRotation() + ((velocity < 0) ? 50f : -50f));
                rightRotationAnimation.setDuration(ANIMATION_DURATION);

                leftAnimationRotation = ObjectAnimator.ofFloat(mMarker, "rotation", mMarker.getRotation());
                leftAnimationRotation.setDuration(ANIMATION_DURATION);

                rightRotationAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        if (!leftAnimationRotation.isRunning()) {
                            leftAnimationRotation.start();
                        }
                    }
                });
            }
        }

        @Override
        public void run() {
            if (getCurrentSelectedSectionIndex() != currentStandingSection) {
                if ((rightRotationAnimation != null) && (!rightRotationAnimation.isRunning())) {
                    rightRotationAnimation.start();
                }

                currentStandingSection = getCurrentSelectedSectionIndex();

                if (!tick.isPlaying()) {
                    tick.start();
                }

                if (tickVibrations) {
                    // small vibration
                    vibrator.vibrate(1);
                }
            }

            if (mListener != null) {
                if (mWheelSections.size() > 0) {
                    mListener.onWheelSectionChanged(getCurrentSelectedSectionIndex(), getCurrentRotation());
                }
            }

            if (!allowRotating) {        //Fling has been stopped, so stop now.
                if (mListener != null) {
                    mListener.onWheelStopped();
                }
            } else if (Math.abs(velocity) > 5) {
                rotateWheel(velocity / 75);
                velocity /= flingVelocityDampening;

                // post this instance again
                post(this);
            } else {
                tada.start();

                if (tickVibrations) {
                    // long vibration
                    vibrator.vibrate(new long[]{0, 100, 100, 1000}, -1);
                }

                isReversing = false;
                flingDirection = FlingDirection.STOPPED;

                if (mListener != null) {
                    mListener.onWheelSettled(getCurrentSelectedSectionIndex(), getCurrentRotation());
                }
            }
        }
    }
}
