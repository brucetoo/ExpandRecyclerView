package com.brucetoo.expandrecyclerview.animator;

import android.view.View;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:23
 */

public class AnimatorListener {

    private AnimatorListener(){}

    public interface Start{
        void onStart();
    }

    public interface End {
        void onEnd();
    }

    public interface Update<V extends View>{
        void onUpdate(V view, float value);
    }
}
