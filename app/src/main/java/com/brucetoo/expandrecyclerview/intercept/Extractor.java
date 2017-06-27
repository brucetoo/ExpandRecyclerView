package com.brucetoo.expandrecyclerview.intercept;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 20/06/2017.
 * At 15:47
 */

public class Extractor {

    private static final String TAG = "Extractor";

    private static Extractor extractor;

    private Extractor() {
    }

    public static Extractor get() {
        if (extractor == null) {
            return extractor = new Extractor();
        }
        return extractor;
    }


//    private IconFactory.IconGenerateListener iconGenerateListener = new IconFactory.IconGenerateListener() {
//
//        @Override
//        public void onGenerated(Bitmap bitmap) {
//            Log.e(TAG, "onGenerated: bitmap -> " + bitmap);
//            notificationBean.iconBitmap = bitmap;
//        }
//    };

    @Nullable
    private static String removeSpaces(@Nullable CharSequence cs) {
        if (cs == null) return null;
        String string = cs instanceof String
            ? (String) cs : cs.toString();
        return string
            .replaceAll("(\\s+$|^\\s+)", "")
            .replaceAll("\n+", "\n");
    }

    /**
     * Removes both {@link ForegroundColorSpan} and {@link BackgroundColorSpan} from given string.
     */
    @Nullable
    private static CharSequence removeColorSpans(@Nullable CharSequence cs) {
        if (cs == null) return null;
        if (cs instanceof Spanned) {
            cs = new SpannableStringBuilder(cs);
        }
        if (cs instanceof Spannable) {
            CharacterStyle[] styles;
            Spannable spanned = (Spannable) cs;
            styles = spanned.getSpans(0, spanned.length(), TextAppearanceSpan.class);
            for (CharacterStyle style : styles) spanned.removeSpan(style);
            styles = spanned.getSpans(0, spanned.length(), ForegroundColorSpan.class);
            for (CharacterStyle style : styles) spanned.removeSpan(style);
            styles = spanned.getSpans(0, spanned.length(), BackgroundColorSpan.class);
            for (CharacterStyle style : styles) spanned.removeSpan(style);
        }
        return cs;
    }


    public void startExtractor(@NonNull Context context, @NonNull NotificationBean n, Notification notification) {

        //icon  delete icon fetcher for now
//        IconFactory.get().add(context, iconGenerateListener, n);
        final Bundle extras = getExtras(notification);

        if (extras != null) loadFromExtras(n, extras);
        if (TextUtils.isEmpty(n.titleText)
            && TextUtils.isEmpty(n.titleBigText)
            && TextUtils.isEmpty(n.messageText)
            && n.messageTextLines == null) {
            loadFromView(context, n, notification);
            if (n.messageTextLines != null && n.messageTextLines.length >= 1) {
                if (n.messageTextLines.length == 1) {
                    n.finalTitle = NotificationUtils.getAppLabelByPackageName(context, n.packageName);;
                    n.finalDesc = n.messageTextLines[0].toString();
                } else {
                    n.finalTitle = n.messageTextLines[0].toString();
                    n.finalDesc = n.messageTextLines[1].toString();
                }
            }
        } else {
            if(n.messageText != null && n.titleText != null) {
                n.finalTitle = n.titleText.toString();
                n.finalDesc = n.messageText.toString();
            }
        }
    }

    /**
     * Gets a bundle with additional data from notification.
     */
    @Nullable
    @SuppressLint("NewApi")
    private Bundle getExtras(@NonNull Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return notification.extras;
        }
        try {
            Field field = notification.getClass().getDeclaredField("extras");
            field.setAccessible(true);
            return (Bundle) field.get(notification);
        } catch (Exception e) {
            Log.w(TAG, "Failed to access extras on Jelly Bean.");
            return null;
        }
    }

    @Nullable
    private CharSequence[] doIt(@Nullable CharSequence[] lines) {
        if (lines != null) {
            // Filter empty lines.
            ArrayList<CharSequence> list = new ArrayList<>();
            for (CharSequence msg : lines) {
                msg = removeSpaces(msg);
                if (!TextUtils.isEmpty(msg)) {
                    list.add(removeColorSpans(msg));
                }
            }

            // Create new array.
            if (list.size() > 0) {
                return list.toArray(new CharSequence[list.size()]);
            }
        }
        return null;
    }

    //-- LOADING FROM EXTRAS --------------------------------------------------

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void loadFromExtras(@NonNull NotificationBean n, @NonNull Bundle extras) {
        n.titleBigText = extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        n.titleText = extras.getCharSequence(Notification.EXTRA_TITLE);
        n.infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
        n.subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        n.summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            n.messageBigText = removeColorSpans(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        }
        n.messageText = removeColorSpans(extras.getCharSequence(Notification.EXTRA_TEXT));

        CharSequence[] lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        n.messageTextLines = doIt(lines);
    }

    //-- LOADING FROM VIEWS ---------------------------------------------------

    private void loadFromView(@NonNull Context context, @NonNull NotificationBean n, Notification notification) {
        ViewGroup view;
        {
            Context contextNotify = NotificationUtils.createContext(context, n);
            if (contextNotify == null) return;
            RemoteViews rvs = notification.contentView;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                rvs = notification.bigContentView == null
                    ? notification.contentView
                    : notification.bigContentView;
            }

            // Try to load the view from remote views.
            LayoutInflater inflater = (LayoutInflater) contextNotify.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            try {
                view = (ViewGroup) inflater.inflate(rvs.getLayoutId(), null);
                rvs.reapply(contextNotify, view);
            } catch (Exception e) {
                return;
            }
        }

        ArrayList<TextView> textViews = new RecursiveFinder<>(TextView.class).expand(view);
        removeClickableViews(textViews);
        removeSubtextViews(context, textViews);

        // No views
        if (textViews.size() == 0)
            return;

        TextView title = findTitleTextView(textViews);
        textViews.remove(title); // no need of title view anymore
        n.titleText = title.getText();

        // No views
        if (textViews.size() == 0)
            return;

        // Pull all other texts and merge them.
        int length = textViews.size();
        CharSequence[] messages = new CharSequence[length];
        for (int i = 0; i < length; i++) messages[i] = textViews.get(i).getText();
        n.messageTextLines = doIt(messages);
    }


    private void removeClickableViews(@NonNull ArrayList<TextView> textViews) {
        for (int i = textViews.size() - 1; i >= 0; i--) {
            TextView child = textViews.get(i);
            if (child.isClickable() || child.getVisibility() != View.VISIBLE) {
                textViews.remove(i);
                break;
            }
        }
    }

    private void removeSubtextViews(@NonNull Context context,
                                    @NonNull ArrayList<TextView> textViews) {
        float subtextSize = context.getResources().getDimension(R.dimen.notification_subtext_size);
        for (int i = textViews.size() - 1; i >= 0; i--) {
            final TextView child = textViews.get(i);
            final String text = child.getText().toString();
            if (child.getTextSize() == subtextSize
                // empty textviews
                || text.matches("^(\\s*|)$")
                // clock textviews
                || text.matches("^\\d{1,2}:\\d{1,2}(\\s?\\w{2}|)$")) {
                textViews.remove(i);
            }
        }
    }

    @NonNull
    private TextView findTitleTextView(@NonNull ArrayList<TextView> textViews) {
        // The idea is that title text is the
        // largest one.
        TextView largest = null;
        for (TextView textView : textViews) {
            if (largest == null || textView.getTextSize() > largest.getTextSize()) {
                largest = textView;
            }
        }
        assert largest != null; // cause the count of views is always >= 1
        return largest;
    }

    private static class RecursiveFinder<T extends View> {

        private final ArrayList<T> list;
        private final Class<T> clazz;

        public RecursiveFinder(@NonNull Class<T> clazz) {
            this.list = new ArrayList<>();
            this.clazz = clazz;
        }

        public ArrayList<T> expand(@NonNull ViewGroup viewGroup) {
            int offset = 0;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i + offset);

                if (child == null) {
                    continue;
                }

                if (clazz.isAssignableFrom(child.getClass())) {
                    //noinspection unchecked
                    list.add((T) child);
                } else if (child instanceof ViewGroup) {
                    expand((ViewGroup) child);
                }
            }
            return list;
        }
    }
}