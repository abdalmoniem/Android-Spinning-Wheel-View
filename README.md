# Android Spinning Wheel View #

[![jcenter](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/abicelis/PrizeWheelView/blob/master/LICENSE)
[![jcenter](https://img.shields.io/badge/platform-android-green.svg)](https://developer.android.com/index.html)
[![jitpack](https://jitpack.io/v/abdalmoniem/Android-Spinning-Wheel-View.svg)](https://jitpack.io/#abdalmoniem/Android-Spinning-Wheel-View)

## About the library

This library is used on my [Decide For Me](https://github.com/abdalmoniem/Decide-For-Me) app. It displays a rotating fling-able spinning wheel, with settable sections (the little pizza shaped slices?). 
Whenever the wheel settles after being flung, the library notifies a listening class about the winning section. 

The sections can be programatically set, and they require a custom List of Section objects.
Please see the sample application below!

<p align="center"><img alt='Spinning Wheel Demo' src='assets/spinning_wheel_demo.gif' width="40%"/></p>


## Sample application
<a target="_blank" href='https://github.com/abdalmoniem/Android-Spinning-Wheel-View/releases/download/1.1.4/app-debug.apk'><img alt='Download Sample Application' src='assets/download_button.png' width="240px"/></a>


## Gradle dependency
in your **app/build.gradle** add the following:
```javascript
	dependencies {
		implementation 'com.github.abdalmoniem:Android-Spinning-Wheel-View:1.1.4'
	}
```

in your **root/build.gradle** ***allprojects*** add the following:
```javascript
	maven {
		url 'https://jitpack.io'
	}
```
so it becomes:
```javascript
	allprojects {
		repositories {
			google()
			jcenter()
			maven {
				url 'https://jitpack.io'
			}
		}
	}
```

## Usage

1) **Add the view to your layout**
```xml
<​com​.hifnawy.spinningWheelLib.SpinningWheelView
		android:id="@+id/home_spinning_wheel_view"
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		android:layout_gravity="center" />
```

*Note that you can set **layout_width** and **layout_height** to predefined values, or one or both to **match_parent**. The View will take as much space as it can, while still being square.*



2) **In your view class (activity/fragment)**
```java
//Get the wheel view
wheelView = (SpinningWheelView) findViewById(R.id.home_prize_wheel_view);
	
//Populate a List of Sections
List<WheelSection> wheelSections = new ArrayList<>();
wheelSections.add(new WheelBitmapSection(someBitmap));
wheelSections.add(new WheelDrawableSection(R.drawable.some_drawable));
wheelSections.add(new WheelColorSection(R.color.some_color));
	
//Set those sections
wheelView.setWheelSections(wheelSections);
	
//Finally, generate wheel
wheelView.generateWheel();
```


3) **Listen for wheel events**
```java
mWheelView.setWheelEventsListener(new WheelEventsListener() {
	@Override
	public void onWheelStopped() {
		//Handle wheel stopped here
	}
		
	@Override
	public void onWheelFlung() {
		//Handle wheel flinging here
	}
		
	@Override
	public void onWheelSettled(int sectionIndex, double angle) {
		//Handle wheel settle here
	}
});

```


4) **Set even more options**
```java
wheelView.setMarkerPosition(MarkerPosition.TOP_RIGHT);
	
wheelView.setWheelBorderLineColor(R.color.border);
wheelView.setWheelBorderLineThickness(5);
	
wheelView.setWheelSeparatorLineColor(R.color.separator);
wheelView.setWheelSeparatorLineThickness(5);
	
//Set onSettled listener
wheelView.setWheelEventsListener(new WheelEventsListener() {...});
```
>Note that **wheelView.generateWheel();** must be called **after** setting all the options!!>