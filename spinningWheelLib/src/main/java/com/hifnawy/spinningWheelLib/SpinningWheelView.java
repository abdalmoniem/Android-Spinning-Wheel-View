package com.hifnawy.spinningWheelLib;

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
import android.os.CountDownTimer;
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

import com.hifnawy.spinningWheelLib.exceptions.InvalidWheelSectionDataException;
import com.hifnawy.spinningWheelLib.exceptions.InvalidWheelSectionsException;
import com.hifnawy.spinningWheelLib.model.FlingDirection;
import com.hifnawy.spinningWheelLib.model.MarkerPosition;
import com.hifnawy.spinningWheelLib.model.SectionType;
import com.hifnawy.spinningWheelLib.model.WheelBitmapSection;
import com.hifnawy.spinningWheelLib.model.WheelColorSection;
import com.hifnawy.spinningWheelLib.model.WheelDrawableSection;
import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class SpinningWheelView extends RelativeLayout {
    // Final internal data
    private MediaPlayer tick;
    private MediaPlayer tada;

    // Internal data
    private float angleOffset;
    private Matrix matrix;
    private ImageView mWheel;
    private ImageView mMarker;
    private RelativeLayout mMarkerContainer;
    private int wheelHeight, wheelWidth;
    private GestureDetector gestureDetector;
    private WheelTouchListener touchListener;
    private boolean[] quadrantTouched = new boolean[]{false, false, false, false, false};
    private boolean allowRotating = true;
    private FlingRunnable flingRunnable;
    //    private boolean isReversing = false;
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
    //    private long flingDuration;
    private CustomCountDownTimer flingCountDownTimer;
    private float initialFlingDampening = Constants.INITIAL_FLING_VELOCITY_DAMPENING;
    private float flingVelocityDampening = Constants.FLING_VELOCITY_DAMPENING;
    private int globalTextSize;

    /* Constructors */
    public SpinningWheelView(Context context) {
        super(context);
        init(context);
    }

    public SpinningWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpinningWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            //onResume called
            if (flingRunnable != null) {
                flingRunnable.resume();
            }
        } else {
            // onPause() called
            if (flingRunnable != null) {
                flingRunnable.pause();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            //onResume() called
            if (flingRunnable != null) {
                flingRunnable.resume();
            }
        } else {
            // onPause() called
            if (flingRunnable != null) {
                flingRunnable.pause();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // onCreate() called
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // onDestroy() called
    }

    /* Public setters / getters */

    /**
     * @return currently set wheel sections.
     */
    public List<WheelSection> getWheelSections() {
        return mWheelSections;
    }

    /**
     * Populate a SpinningWheelView with a List of WheelSections such as
     * {@link WheelTextSection},
     * * {@link WheelBitmapSection},
     * {@link WheelColorSection} and
     * {@link WheelDrawableSection}.
     * As SpinningWheelView supports Bitmaps, Colors and Drawables.
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
            throw new InvalidWheelSectionsException("You must use setWheelSections() to set the sections of the wheel.");
        }

        mWheelSections = wheelSections;
    }

    /**
     * @return currently set font size for {@link WheelTextSection} sections.
     */
    public int getGlobalTextSize() {
        return globalTextSize;
    }

    /**
     * Set font size for all @{Link {@link WheelTextSection} sections
     *
     * @param globalTextSize {@link WheelTextSection} font size
     */
    public void setGlobalTextSize(int globalTextSize) {
        this.globalTextSize = globalTextSize;
    }

//    /**
//     * Set spinning wheel duration.
//     * NOTE that the wheel will not stop
//     * immediately after the set {@param flingDuration} but its velocity will
//     * start decelerating with a factor.
//     *
//     * @param flingDuration fling duration in milliseconds
//     * @see SpinningWheelView#setFlingVelocityDampening
//     */
//    public void setFlingDuration(long flingDuration) {
//        this.flingDuration = flingDuration;
//    }

    /**
     * @return generated {@link ImageView} of the Spinning Wheel.
     */
    public ImageView getWheelImageView() {
        return mWheel;
    }

    /**
     * @return current {@link FlingDirection} of the Spinning Wheel.
     */
    public FlingDirection getFlingDirection() {
        return flingDirection;
    }

    /**
     * Set a position of the wheel Marker. Please see
     * {@link MarkerPosition} for all the options.
     * DEFAULT VALUE: {@link MarkerPosition#TOP}
     *
     * @param markerPosition A {@link MarkerPosition}
     */
    public void setMarkerPosition(@NonNull MarkerPosition markerPosition) {
        mMarkerPosition = markerPosition;
        mMarkerContainer.setRotation(mMarkerPosition.getDegreeOffset());
    }

    /**
     * Set the initial fling dampening.
     *
     * @param dampening dampening factor
     *                  NOTE: A number between 1.0 (no dampening) and 5.0 (lots of dampening) is recommended.
     *                  Default value is {@value Constants#INITIAL_FLING_VELOCITY_DAMPENING}
     */
    public void setInitialFlingDampening(float dampening) {
        initialFlingDampening = dampening;
    }

    /**
     * Set the velocity dampening when wheel is flung.
     *
     * @param dampening dampening factor
     *                  NOTE: A number between 1 (no dampening) and 1.1 (lots of dampening) is recommended.
     *                  Default value is {@value Constants#FLING_VELOCITY_DAMPENING}
     */
    public void setFlingVelocityDampening(float dampening) {
        flingVelocityDampening = dampening;
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

    /* Public methods */

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
     * Set {@link WheelEventsListener}, a listener interface.
     * to receive events such as: onWheelStopped(), onWheelFlung() and onWheelSettled()
     *
     * @param listener {@link WheelEventsListener} to set
     */
    public void setWheelEventsListener(WheelEventsListener listener) {
        mListener = listener;
    }

    /**
     * Enable or Disable tick vibrations on wheel rotation and fling
     *
     * @param tickVibrations Enabled if {@code true} - Disabled if {@code false}
     */
    public void setTickVibrations(boolean tickVibrations) {
        this.tickVibrations = tickVibrations;

        this.vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Enable or Disable preview mode for the spinning wheel
     * Preview mode disables All {@link WheelEventsListener} listeners attached to the Spinning Wheel
     *
     * @param preview Enabled if {@code true} - Disabled if {@code false}
     */
    public void setIsPreview(boolean preview) {
        isPreview = preview;

        if (isPreview) {
            mWheel.setOnTouchListener(null);
        }
    }

    /**
     * Re-Initializes the @{Link {@link ImageView} of the spinning wheel and resets all positions of images
     */
    public void reInit() {
        mWheel = findViewById(R.id.spinningWheelImageView);
        mMarker = findViewById(R.id.spinningWheelMarkerImageView);
        mMarkerContainer = findViewById(R.id.spinningWheelContainer);

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

                    if (mCanGenerateWheel) {
                        generateWheelImage();
                    }
                }
            }
        });

        globalTextSize = 30;
    }

    /**
     * This method MUST BE CALLED AFTER all pervious settings have been set.dp.
     * DEFAULT: No border
     */
    public void generateWheel() {
        if (wheelHeight == 0) {      //If view doesn't have width/height yet
            mCanGenerateWheel = true;
        } else {
            generateWheelImage();
        }
    }

    /* Private methods */

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
     * NOTE that the wheel will not stop
     * immediately after the set {@param flingDuration} but its velocity will
     * start decelerating with a factor.
     *
     * @param velocity  the speed of the fling
     * @param clockwise the direction of the rotation.
     */
    public void flingWheel(int velocity, boolean clockwise) {
//        isReversing = false;
        doFlingWheel((clockwise ? velocity : -velocity));
    }

    /**
     * Call this method to manually fling the wheel
     *
     * @param duration  the duration of the fling in milliseconds
     * @param velocity  the speed of the fling
     * @param clockwise the direction of the rotation.
     * @see SpinningWheelView#setFlingVelocityDampening
     */
    public void flingWheel(long duration, int velocity, boolean clockwise) {
//        isReversing = false;
        doFlingWheel(duration, (clockwise ? velocity : -velocity));
    }

    private void init(Context context) {
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spinning_wheel_view, this);

        mWheel = findViewById(R.id.spinningWheelImageView);
        mMarker = findViewById(R.id.spinningWheelMarkerImageView);
        mMarkerContainer = findViewById(R.id.spinningWheelContainer);

        matrix = new Matrix();

        gestureDetector = new GestureDetector(mContext, new WheelGestureListener());
        touchListener = new WheelTouchListener();
        mWheel.setOnTouchListener(touchListener);

        globalTextSize = 30;

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

                    if (mCanGenerateWheel) {
                        generateWheelImage();
                    }
                }
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                tick = MediaPlayer.create(mContext, R.raw.click);
                tada = MediaPlayer.create(mContext, R.raw.tada);
            }
        });
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
     * @return The selected quadrant.
     */
    private int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
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
     * @return The current rotation of the wheel.
     */
    private double getCurrentRotation() {
        float[] v = new float[9];
        matrix.getValues(v);
        double angle = Math.round((Math.atan2(v[Matrix.MSKEW_X], v[Matrix.MSCALE_X]) * (180 / Math.PI)) - angleOffset) - 90f;

        if (angle > 0) {
            return angle;
        } else {
            return 360 + angle;
        }
    }

    /**
     * @return The current section.
     */
    private int getCurrentSelectedSectionIndex() {
        double sectionAngle = (360.0f / mWheelSections.size());
        double currentRotation = getCurrentRotation() + mMarkerPosition.getDegreeOffset();

        if (currentRotation > 360) {
            currentRotation = currentRotation - 360;
        }

        int selection = (int) Math.floor(currentRotation / sectionAngle);

        if (selection >= mWheelSections.size()) {     //Rounding errors occur. Limit to items-1
            selection = mWheelSections.size() - 1;
        }

        return selection;
    }

    /**
     * Generates the wheel bitmap
     */
    private void generateWheelImage() {
        mWheel.setScaleType(ImageView.ScaleType.MATRIX);

        if (mWheelSections == null) {
            throw new InvalidWheelSectionsException("You must use setWheelSections() to set the sections of the wheel.");
        }

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
                        if (sectionBitmap == null) {
                            throw new InvalidWheelSectionDataException("Invalid bitmap. WheelSection data = " + section.toString());
                        }
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

    /**
     * Flings the wheel with a set velocity
     *
     * @param velocity the velocity of the fling, if {@code velocity > 0}
     *                 fling will be CCW, if {@code velocity < 0} fling
     *                 will be CW
     */
    private void doFlingWheel(float velocity) {
        //Stop previous fling
        removeCallbacks(flingRunnable);

        //Notify fling
        if (mListener != null) {
            mListener.onWheelFlung();
        }

        //Launch new fling
        allowRotating = true;
        flingRunnable = new FlingRunnable(velocity / initialFlingDampening);
        post(flingRunnable);
    }

    /**
     * Flings the wheel with a set velocity
     *
     * @param velocity the velocity of the fling, if {@code velocity > 0}
     *                 fling will be CCW, if {@code velocity < 0} fling
     *                 will be CW
     * @param duration fling duration in milliseconds, duration will be
     *                 applied to the current fling only.
     */
    private void doFlingWheel(long duration, float velocity) {
        //Stop previous fling
        removeCallbacks(flingRunnable);

        //Notify fling
        if (mListener != null) {
            mListener.onWheelFlung();
        }

        //Launch new fling
        allowRotating = true;
        flingRunnable = new FlingRunnable(duration, velocity / initialFlingDampening);
        post(flingRunnable);
    }

    /**
     * Touch Listener for the spinning wheel to react to {@code MotionEvent.ACTION_DOWN},
     * {@code MotionEvent.ACTION_MOVE}, {@code MotionEvent.ACTION_UP}
     */
    private class WheelTouchListener implements OnTouchListener {
        private final int ANIMATION_DURATION = 100;

        private final float markerRotation = mMarker.getRotation();

        private double startAngle;
        private boolean CW = false;
        private int currentStandingSection;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    resetQuadrants();
                    startAngle = getAngle(event.getX(), event.getY());
                    allowRotating = false;
                    currentStandingSection = getCurrentSelectedSectionIndex();

                    if (rightRotationAnimation != null) {
                        rightRotationAnimation.end();
                    }
                    if (leftAnimationRotation != null) {
                        leftAnimationRotation.end();
                    }

                    leftAnimationRotation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation);
                    leftAnimationRotation.setDuration(ANIMATION_DURATION);

                    rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation + (CW ? -50f : 50f));
                    rightRotationAnimation.setDuration(ANIMATION_DURATION);
                    rightRotationAnimation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            if (!leftAnimationRotation.isRunning()) {
                                leftAnimationRotation.start();
                            }
                        }
                    });

                    if ((flingCountDownTimer != null) && flingCountDownTimer.isRunning()) {
                        flingCountDownTimer.stop();
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateWheel((float) (startAngle - currentAngle));
                    startAngle = currentAngle;


                    if (getCurrentSelectedSectionIndex() != currentStandingSection) {
                        if ((currentStandingSection == (mWheelSections.size() - 1)) && (getCurrentSelectedSectionIndex() == 0)) {
                            CW = false;
                        } else if ((currentStandingSection == 0) && (getCurrentSelectedSectionIndex() == (mWheelSections.size() - 1))) {
                            CW = true;
                        } else if (getCurrentSelectedSectionIndex() > currentStandingSection) {
                            CW = false;
                        } else if (getCurrentSelectedSectionIndex() < currentStandingSection) {
                            CW = true;
                        }

                        if (!leftAnimationRotation.isRunning()) {
                            leftAnimationRotation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation);
                            leftAnimationRotation.setDuration(ANIMATION_DURATION);
                        } else {
                            leftAnimationRotation.end();
                        }

                        if (!rightRotationAnimation.isRunning()) {
                            rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation + (CW ? -50f : 50f));
                            rightRotationAnimation.setDuration(ANIMATION_DURATION);
                            rightRotationAnimation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);

                                    if (!leftAnimationRotation.isRunning()) {
                                        leftAnimationRotation.start();
                                    }
                                }
                            });
                        } else {
                            rightRotationAnimation.end();
                        }

                        if ((rightRotationAnimation != null) && (!rightRotationAnimation.isRunning())) {
                            rightRotationAnimation.start();
                        }

                        if ((tick != null) && !tick.isPlaying()) {
                            tick.start();
                        }

                        if (tickVibrations) {
                            // small vibration
                            vibrator.vibrate(1);
                        }

                        currentStandingSection = getCurrentSelectedSectionIndex();
                    }

                    if ((flingCountDownTimer != null) && flingCountDownTimer.isRunning()) {
                        flingCountDownTimer.stop();
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    allowRotating = true;
                    if (rightRotationAnimation != null) {
                        rightRotationAnimation.end();
                    }
                    if (leftAnimationRotation != null) {
                        leftAnimationRotation.end();
                    }
                    mMarker.setRotation(markerRotation);

                    if ((flingCountDownTimer != null) && flingCountDownTimer.isRunning()) {
                        flingCountDownTimer.stop();
                    }

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

    /**
     * Gesture Listener for the spinning wheel to react to fling events
     */
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

            // the inverted rotations
            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && quadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && quadrantTouched[3])) {

                doFlingWheel((-1 * (velocityX + velocityY)));
                //post(new FlingRunnable((-1 * (velocityX + velocityY)) / initialFlingDampening));
                //if(mListener != null) {
                //    mListener.onWheelFlung();
                //}
            } else {
                // the normal rotation
                doFlingWheel((velocityX + velocityY));

                //post(new FlingRunnable((velocityX + velocityY) / initialFlingDampening));
                //if(mListener != null) {
                //  mListener.onWheelFlung();
                //}
            }

            return true;
        }
    }

    /**
     * A {@link Runnable} for animating the the wheel's fling.
     */
    private class FlingRunnable implements Runnable {
        private final int ANIMATION_DURATION = 100;

        private boolean paused = false;
        private float velocity;
        private int currentStandingSection;
        private float markerRotation = mMarker.getRotation();

        private void flingInit(float velocity) {
            this.velocity = velocity;
            this.currentStandingSection = getCurrentSelectedSectionIndex();

            if (rightRotationAnimation != null) {
                rightRotationAnimation.end();
            }
            if (leftAnimationRotation != null) {
                leftAnimationRotation.end();
            }

            if ((flingDirection == FlingDirection.CW) && (velocity < 0)) {  // wheel changed direction to CCW
                // rotate marker CW
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation + 50f);
            } else if ((flingDirection == FlingDirection.CW) && (velocity > 0)) {   // wheel is still CW
                // rotate marker CCW
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation - 50f);
            }

            if ((flingDirection == FlingDirection.CCW) && (velocity > 0)) { // wheel changed direction to CW
                // rotate marker CCW
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation - 50f);
            } else if ((flingDirection == FlingDirection.CCW) && (velocity < 0)) {  // wheel is still CCW
                // rotate marker CW
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation + 50f);
            }

            if (flingDirection == FlingDirection.STOPPED) { // wheel is stopped
                // rotate marker CCW if wheel is rotating CW (velocity > 0) and vice versa
                rightRotationAnimation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation + ((velocity > 0) ? -50f : 50f));
            }

            rightRotationAnimation.setDuration(ANIMATION_DURATION);

            leftAnimationRotation = ObjectAnimator.ofFloat(mMarker, "rotation", markerRotation);
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

            flingDirection = (velocity > 0) ? FlingDirection.CW : FlingDirection.CCW;
        }

        public FlingRunnable(float velocity) {
            flingInit(velocity);

//            if (flingDuration > 0) {
//                if (flingCountDownTimer != null) {
//                    flingCountDownTimer.cancel();
//                }
//
//                flingCountDownTimer = new CountDownTimer(flingDuration, 500) {
//                    private float flingVelocityDampeningTmp = flingVelocityDampening;
//
//                    @Override
//                    public void onTick(long l) {
//                        flingVelocityDampening = 1f;
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        flingVelocityDampening = flingVelocityDampeningTmp;
//                    }
//
//                    @Override
//                    public void onStopped() {
//                        flingVelocityDampening = flingVelocityDampeningTmp;
//                        super.cancel();
//                    }
//                };
//                flingCountDownTimer.startCountDown();
//            }
        }

        public FlingRunnable(long duration, float velocity) {
            flingInit(velocity);

            if (duration > 0) {
                if (flingCountDownTimer != null) {
                    flingCountDownTimer.cancel();
                }

                flingCountDownTimer = new CustomCountDownTimer(duration, 500) {
                    private float flingVelocityDampeningTmp = flingVelocityDampening;

                    @Override
                    public void onTick(long l) {
                        flingVelocityDampening = 1f;
                    }

                    @Override
                    public void onFinish() {
                        flingVelocityDampening = flingVelocityDampeningTmp;
                    }

                    @Override
                    public void onStopped() {
                        flingVelocityDampening = flingVelocityDampeningTmp;
                        super.cancel();
                    }
                };

                flingCountDownTimer.startCountDown();
            }
        }

        public void pause() {
            this.paused = true;
        }

        public void resume() {
            this.paused = false;
        }

        @Override
        public void run() {
            if (!this.paused) {
                if (getCurrentSelectedSectionIndex() != currentStandingSection) {
                    if ((rightRotationAnimation != null) && (!rightRotationAnimation.isRunning())) {
                        rightRotationAnimation.start();
                    }

                    currentStandingSection = getCurrentSelectedSectionIndex();

                    if ((tick != null) && !tick.isPlaying()) {
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
                    if (mListener != null) {
                        new CountDownTimer(500, 20) {
                            @Override
                            public void onTick(long l) {
                                Log.d("mn3m", "tick");
                                if (flingDirection == FlingDirection.CW) {
                                    rotateWheel(-0.1f);
                                } else if (flingDirection == FlingDirection.CCW) {
                                    rotateWheel(0.1f);
                                }
                            }

                            @Override
                            public void onFinish() {
//                            isReversing = false;
                                flingDirection = FlingDirection.STOPPED;

                                if (tada != null) {
                                    tada.start();
                                }

                                if (tickVibrations) {
                                    // long vibration
                                    vibrator.vibrate(new long[]{0, 100, 100, 1000}, -1);
                                }
                                
                                mListener.onWheelSettled(getCurrentSelectedSectionIndex(), getCurrentRotation());
                            }
                        }.start();
                    }
                }
            } else {
                postDelayed(this, 500);
            }
        }
    }
}
