package com.brucetoo.expandrecyclerview;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.animator.AnimatorListener;
import com.brucetoo.expandrecyclerview.animator.ViewAnimator;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:12
 */

public class ViewAnimatorActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewanimator);

        View image = findViewById(R.id.image);
        View text = findViewById(R.id.text);

//        ViewAnimator
//            .putOn(image)
//            .alpha(0.2f)
//            .animate()
//            .translationY(-1000, 0)
//            .alpha(0,1)
//            .andAnimate(text)
//            .dp().translationX(-20, 0)
//            .decelerate()
//            .duration(2000)
//            .thenAnimate(image)
//            .scale(1f, 0.5f, 1f)
//            .accelerate()
//            .duration(1000)
//            .start();

        final String TAG = "ViewAnimator";
        ViewAnimator
            .putOn(image)
            .alpha(0.2f)
            .translation(100,100)
            .animate()
            .waitForSize()
            .dp().width(100,400)
            .dp().height(50,200)
            .translationX(0)
            .translationY(0)
            .alpha(1)
            .andAnimate(text)
            .textColor(Color.BLACK,Color.GREEN)
            .backgroundColor(Color.WHITE,Color.BLACK)
            .accelerate()
            .duration(2000)
            .thenAnimate(image)
            .rotationY(360)
            .repeatCount(3)
            .repeatMode(ValueAnimator.RESTART)
            .duration(2000)
            .onStart(new AnimatorListener.Start() {
                @Override
                public void onStart() {
                    Log.e(TAG, "onStart: ");
                }
            })
            .onEnd(new AnimatorListener.End() {
                @Override
                public void onEnd() {
                    Log.e(TAG, "onEnd: ");
                }
            })
            .thenAnimate(text)
            .custom(new AnimatorListener.Update<TextView>() {
                @Override
                public void onUpdate(TextView view, float value) {
                   view.setText("This is ViewAnimator " + value);
                }
            },1,100)
            .duration(2000)
            .start();
    }
}
