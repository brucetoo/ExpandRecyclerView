package com.brucetoo.expandrecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:12
 */

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickOne(View view){
        Intent intent = new Intent();
        intent.setClass(this,RecyclerViewActivity.class);
        startActivity(intent);
    }

    public void onClickTwo(View view){
        Intent intent = new Intent();
        intent.setClass(this,ViewAnimatorActivity.class);
        startActivity(intent);
    }

    public void onClickThree(View view){
        Intent intent = new Intent();
        intent.setClass(this,ScrollActivity.class);
        startActivity(intent);
    }
}
