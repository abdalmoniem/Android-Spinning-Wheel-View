package com.hifnawy.spinningWheelDemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hifnawy.spinningwheellib.SpinningWheelView;
import com.hifnawy.spinningwheellib.WheelEventsListener;
import com.hifnawy.spinningwheellib.model.MarkerPosition;
import com.hifnawy.spinningwheellib.model.WheelBitmapSection;
import com.hifnawy.spinningwheellib.model.WheelColorSection;
import com.hifnawy.spinningwheellib.model.WheelDrawableSection;
import com.hifnawy.spinningwheellib.model.WheelSection;
import com.hifnawy.spinningwheellib.model.WheelTextSection;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    final List<WheelSection> wheelSections = new ArrayList<>();
    ImageView homeImage;
    TextView homeText;
    SpinningWheelView wheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeImage = findViewById(R.id.mainBackgroundImage);
        homeText = findViewById(R.id.mainTextView);

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
        wheelView.reInit();

        wheelView.setWheelSections(wheelSections);
//        wheelView.setWheelSections(new ArrayList<WheelSection>());
        wheelView.setMarkerPosition(MarkerPosition.TOP);

        wheelView.setWheelBorderLineColor(R.color.border);
        wheelView.setWheelBorderLineThickness(2);

        wheelView.setWheelSeparatorLineColor(R.color.separator);
        wheelView.setWheelSeparatorLineThickness(2);

        //Set onSettled listener
        wheelView.setWheelEventsListener(new WheelEventsListener() {
            @Override
            public void onWheelStopped() {
                Toast.makeText(MainActivity.this, "Wheel Stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWheelFlung() {
                Toast.makeText(MainActivity.this, "Wheel Flung", Toast.LENGTH_SHORT).show();
                homeText.setTextColor(0xff000000);
            }

            @Override
            public void onWheelSectionChanged(int sectionIndex, double angle) {
//                Toast.makeText(MainActivity.this, "Wheel Section Changed", Toast.LENGTH_SHORT).show();

                WheelSection section = wheelView.getWheelSections().get(sectionIndex);
                switch (section.getType()) {
                    case TEXT:
                        homeText.setText(((WheelTextSection) section).getText());
                        break;
                    default:
                        homeText.setText("Section #" + sectionIndex);
                        break;
                }
            }

            @Override
            public void onWheelSettled(int sectionIndex, double angle) {
                final int selectedIndex = sectionIndex;
                Toast.makeText(MainActivity.this, "Wheel Settled", Toast.LENGTH_SHORT).show();

                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                homeImage.startAnimation(fadeOut);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        if ((wheelSections.size() > 0) && selectedIndex >= 0) {
                            WheelSection section = wheelSections.get(selectedIndex);
                            switch (section.getType()) {
                                case TEXT:
                                    homeImage.setImageDrawable(null);
                                    homeImage.setBackgroundColor(((WheelTextSection) section).getBackgroundColor());
                                    homeText.setTextColor(((WheelTextSection) section).getForegroundColor());
                                    break;
                                case BITMAP:
                                    homeImage.setImageBitmap(((WheelBitmapSection) section).getBitmap());
                                    break;
                                case DRAWABLE:
                                    homeImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), ((WheelDrawableSection) section).getDrawableRes()));
                                    break;
                                case COLOR:
                                    homeImage.setImageDrawable(null);
                                    homeImage.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), ((WheelColorSection) section).getColor()));
                                    break;
                            }

                            Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                            homeImage.startAnimation(fadeIn);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });

        //Finally, generate wheel background
        wheelView.generateWheel();
    }
}
