package com.brucetoo.expandrecyclerview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

        ViewAnimator
            .animate(image)
            .waitForSize()
            .dp().width(100,400)
            .dp().height(50,200)
            .andAnimate(text)
            .textColor(Color.BLACK,Color.GREEN)
            .backgroundColor(Color.WHITE,Color.BLACK)
            .accelerate()
            .duration(2000)
            .start();
    }
}
