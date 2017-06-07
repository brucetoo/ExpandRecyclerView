package com.brucetoo.expandrecyclerview;

import android.view.View;

/**
 * Created by Bruce Too
 * On 23/02/2017.
 * At 17:04
 */

public class MoveHelper {

    public static int[] moveViewRight2Left(View fromView,View toView,int margin,float scale){
        int[] deltaDis = new int[2];
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        toView.getLocationOnScreen(loc1);
        loc1[0] += toView.getWidth() + margin;
        loc1[1] += (int)(fromView.getHeight() * (1 - scale) / 2);
        fromView.getLocationOnScreen(loc2);
        deltaDis[0] = loc2[0] - loc1[0];
        deltaDis[1] = loc2[1] - loc1[1];
        return deltaDis;
    }

    public static int[] moveViewLeft2Right(View fromView,View toView,int margin,float scale){
        int[] deltaDis = new int[2];
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        toView.getLocationOnScreen(loc1);
        loc1[0] = (int) (loc1[0] - fromView.getWidth() * scale - margin + toView.getWidth());
        fromView.getLocationOnScreen(loc2);
        deltaDis[0] = loc1[0] - loc2[0];
        deltaDis[1] = loc2[1] - loc1[1];
        return deltaDis;
    }
}
