package com.hifnawy.spinningWheelDemo;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.animation.Animator;
import android.graphics.BitmapFactory;
import android.view.animation.Animation;
import android.animation.ObjectAnimator;
import android.view.animation.AnimationUtils;
import android.animation.AnimatorListenerAdapter;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.hifnawy.spinningWheelLib.DimensionUtil;
import com.hifnawy.spinningWheelLib.SpinningWheelView;
import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.WheelEventsListener;
import com.hifnawy.spinningWheelLib.model.MarkerPosition;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;
import com.hifnawy.spinningWheelLib.model.WheelColorSection;
import com.hifnawy.spinningWheelLib.model.WheelBitmapSection;
import com.hifnawy.spinningWheelLib.model.WheelDrawableSection;

public class MainActivity extends AppCompatActivity {
    final List<WheelSection> wheelSections = new ArrayList<>();

    ImageView homeImage;
    TextView homeText;
    SpinningWheelView wheelView;

    float homeTextScaleX;
    float homeTextScaleY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeImage = findViewById(R.id.mainBackgroundImage);
        homeText = findViewById(R.id.mainTextView);

        homeTextScaleX = homeText.getScaleX();
        homeTextScaleY = homeText.getScaleY();

        Bitmap someBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abstract_1);

        //Init WheelSection list
        wheelSections.add(new WheelBitmapSection(someBitmap));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_2));
        wheelSections.add(new WheelTextSection("item #1")
                .setSectionBackgroundColor(ContextCompat.getColor(this, R.color.red))
                .setSectionForegroundColor(ContextCompat.getColor(this, R.color.white)));
        wheelSections.add(new WheelColorSection(R.color.green));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_3));
        wheelSections.add(new WheelTextSection("item #2")
                .setSectionBackgroundColor(ContextCompat.getColor(this, R.color.magenta))
                .setSectionForegroundColor(ContextCompat.getColor(this, R.color.red)));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_4));
        wheelSections.add(new WheelTextSection("item #3")
                .setSectionBackgroundColor(ContextCompat.getColor(this, R.color.green))
                .setSectionForegroundColor(ContextCompat.getColor(this, R.color.white)));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_5));
        wheelSections.add(new WheelColorSection(R.color.orange));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_6));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_7));
        wheelSections.add(new WheelColorSection(R.color.blue));
        wheelSections.add(new WheelDrawableSection(R.drawable.abstract_8));
        wheelSections.add(new WheelDrawableSection(R.drawable.example_layer_list_drawable));
        wheelSections.add(new WheelTextSection("item #4")
                .setSectionBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                .setSectionForegroundColor(ContextCompat.getColor(this, R.color.black)));

        //Init wheelView and set parameters
        wheelView = findViewById(R.id.spinningWheelView);

        wheelView.setWheelSections(wheelSections);
        wheelView.setMarkerPosition(MarkerPosition.TOP);

        wheelView.setWheelBorderLineColor(R.color.border);
        wheelView.setWheelBorderLineThickness(2);

        wheelView.setWheelSeparatorLineColor(R.color.separator);
        wheelView.setWheelSeparatorLineThickness(2);

        //Set wheelEventsListener
        wheelView.setWheelEventsListener(new WheelEventsListener() {
            @Override
            public void onWheelStopped() {
                Toast.makeText(MainActivity.this, "Wheel Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWheelFlung() {
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(homeText, "scaleX", homeTextScaleX);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(homeText, "scaleY", homeTextScaleY);

                animatorX.setDuration(500);
                animatorY.setDuration(500);

                animatorX.start();
                animatorY.start();

                Toast.makeText(MainActivity.this, "Wheel Flung", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWheelSectionChanged(int sectionIndex, double angle) {
                WheelSection section = wheelView.getWheelSections().get(sectionIndex);
                switch (section.getType()) {
                    case TEXT:
                        homeText.setText(((WheelTextSection) section).getText());
                        break;
                    default:
                        homeText.setText("Section #" + sectionIndex);
                        break;
                }

                // Toast.makeText(MainActivity.this, "Wheel Section Changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWheelSettled(int sectionIndex, double angle) {
                final int selectedIndex = sectionIndex;
                final float scaleUpFactor = 6f;
                final float scaleDownFactor = 2f;

                ObjectAnimator animatorX = ObjectAnimator.ofFloat(homeText, "scaleX", homeText.getScaleX() + DimensionUtil.convertPixelsToDp(scaleUpFactor));
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(homeText, "scaleY", homeText.getScaleY() + DimensionUtil.convertPixelsToDp(scaleUpFactor));
                animatorX.setDuration(300);
                animatorY.setDuration(300);

                animatorX.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ObjectAnimator animatorXX = ObjectAnimator.ofFloat(homeText, "scaleX",
                                homeText.getScaleX() - DimensionUtil.convertPixelsToDp(scaleDownFactor));
                        animatorXX.setDuration(300);
                        animatorXX.start();
                    }
                });

                animatorY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        ObjectAnimator animatorYY = ObjectAnimator.ofFloat(homeText, "scaleY",
                                homeText.getScaleY() - DimensionUtil.convertPixelsToDp(scaleDownFactor));
                        animatorYY.setDuration(300);
                        animatorYY.start();
                    }
                });

                animatorX.start();
                animatorY.start();

                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                homeImage.startAnimation(fadeOut);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if ((wheelSections.size() > 0) && selectedIndex >= 0) {
                            int textC;
                            WheelSection section = wheelSections.get(selectedIndex);
                            switch (section.getType()) {
                                case TEXT:
                                    homeImage.setImageDrawable(null);
                                    homeImage.setBackgroundColor(((WheelTextSection) section).getBackgroundColor());
                                    MainActivity.this.homeText.setTextColor(((WheelTextSection) section).getForegroundColor());
                                    break;
                                case BITMAP:
                                    homeImage.setImageBitmap(((WheelBitmapSection) section).getBitmap());
                                    int bmB = calculateBrightnessEstimate(((WheelBitmapSection) section).getBitmap(), 10);

                                    // Toast.makeText(MainActivity.this, "bitmap brightness: " + bmB, Toast.LENGTH_SHORT).show();

                                    textC = ~bmB;

                                    MainActivity.this.homeText.setTextColor(Color.rgb(textC, textC, textC));

                                    break;
                                case DRAWABLE:
                                    homeImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), ((WheelDrawableSection) section).getDrawableRes()));
                                    Bitmap drawableBitmap = BitmapFactory.decodeResource(getResources(), ((WheelDrawableSection) section).getDrawableRes());

                                    if (drawableBitmap != null) {
                                        int dB = calculateBrightnessEstimate(drawableBitmap, 10);

                                        // Toast.makeText(MainActivity.this, "drawable brightness: " + dB, Toast.LENGTH_SHORT).show();

                                        textC = ~dB;

                                        MainActivity.this.homeText.setTextColor(Color.rgb(textC, textC, textC));
                                    }

                                    break;
                                case COLOR:
                                    homeImage.setImageDrawable(null);
                                    homeImage.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), ((WheelColorSection) section).getColor()));

                                    int cB = calculateBrightnessEstimate(((WheelColorSection) section).getColor());

                                    textC = ~cB;

                                    MainActivity.this.homeText.setTextColor(Color.rgb(textC, textC, textC));

                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Unkown Wheel Section Type Landed", Toast.LENGTH_SHORT).show();
                            }

                            Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                            homeImage.startAnimation(fadeIn);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                Toast.makeText(MainActivity.this, "Wheel Settled", Toast.LENGTH_SHORT).show();
            }
        });

        //Finally, generate wheel background
        wheelView.generateWheel();

        wheelView.setFlingVelocityDampening(1.01f);

        ObjectAnimator scaleXAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleX", 0f, 1f);
        ObjectAnimator scaleYAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleY", 0f, 1f);

        ObjectAnimator rotationAnimator = new ObjectAnimator().ofFloat(wheelView, "rotation", 0f, 360f);

        scaleXAnimator.setDuration(300);
        scaleYAnimator.setDuration(300);

        scaleXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ObjectAnimator scaleXAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleX", 1f, 0.9f);
                scaleXAnimator.setDuration(100);

                scaleXAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        ObjectAnimator scaleXAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleX", 0.9f, 1f);
                        scaleXAnimator.setDuration(100);
                        scaleXAnimator.start();

                    }
                });

                scaleXAnimator.start();

            }
        });

        scaleYAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ObjectAnimator scaleYAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleY", 1f, 0.9f);
                scaleYAnimator.setDuration(100);

                scaleYAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        ObjectAnimator scaleYAnimator = new ObjectAnimator().ofFloat(wheelView, "scaleY", 0.9f, 1f);
                        scaleYAnimator.setDuration(100);
                        scaleYAnimator.start();

                    }
                });

                scaleYAnimator.start();

            }
        });

        rotationAnimator.setDuration(300);

        scaleXAnimator.start();
        scaleYAnimator.start();
        rotationAnimator.start();

        // wheelView.flingWheel(1000, true);
        // wheelView.flingWheel(3000, 10000, true);
    }

    public int calculateBrightnessEstimate(int color) {
        int R = (color >> 16) & 0xFF;
        int G = (color >> 8) & 0xFF;
        int B = (color >> 0) & 0xFF;

        return (R + B + G) / 3;
    }

    public int calculateBrightnessEstimate(Bitmap bitmap, int increment) {
        int R = 0;
        int G = 0;
        int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += increment) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return (R + B + G) / (n * 3);
    }

    public void nextActivity(View view) {
        startActivity(new Intent(MainActivity.this, NextActivity.class));
    }

    public void flingWheel(View view) {
        wheelView.flingWheel(1000 + (1000 * (int) Math.pow(2, new Random().nextInt(10))), (new Random().nextFloat() > 0.5));
    }
}
