package com.brucetoo.expandrecyclerview.animator;

import android.view.View;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:23
 */

public class AnimatorListener {

    private AnimatorListener(){}

    interface Start{
        void onStart();
    }

    interface End {
        void onEnd();
    }

    interface Update<V extends View>{
        void onUpdate(V view, float value);
    }
}
