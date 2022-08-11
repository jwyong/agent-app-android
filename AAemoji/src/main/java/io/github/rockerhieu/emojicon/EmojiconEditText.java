/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.rockerhieu.emojicon;


import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.CallSuper;
import androidx.annotation.DimenRes;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconEditText extends EditText {
    private int mEmojiconSize;
    private boolean mUseSystemDefault = false;

    public EmojiconEditText(Context context) {
        super(context);
        mEmojiconSize = (int) getTextSize();

    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiconEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize);
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
     }
}

//extends AppCompatEditText implements EmojiEditTextInterface {
//private float emojiSize;
//
//public EmojiconEditText(final Context context) {
//        this(context, null);
//        }
//
//public EmojiconEditText(final Context context, final AttributeSet attrs) {
//        super(context, attrs);
//
//        if (!isInEditMode()) {
//        EmojiManager.getInstance().verifyInstalled();
//        }
//
//final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
//final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
//
//        if (attrs == null) {
//        emojiSize = defaultEmojiSize;
//        } else {
//final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmojiEditText);
//
//        try {
//        emojiSize = a.getDimension(R.styleable.EmojiEditText_emojiSize, defaultEmojiSize);
//        } finally {
//        a.recycle();
//        }
//        }
//
//        setText(getText());
//        }
//
//@Override @CallSuper
//protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
//final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
//final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
//        EmojiManager.getInstance().replaceWithImages(getContext(), getText(), emojiSize, defaultEmojiSize);
//        }
//
//@Override public float getEmojiSize() {
//        return emojiSize;
//        }
//
//@Override @CallSuper public void backspace() {
//final KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
//        dispatchKeyEvent(event);
//        }
//
//@Override @CallSuper public void input(final Emoji emoji) {
//        if (emoji != null) {
//final int start = getSelectionStart();
//final int end = getSelectionEnd();
//
//        if (start < 0) {
//        append(emoji.getUnicode());
//        } else {
//        getText().replace(Math.min(start, end), Math.max(start, end), emoji.getUnicode(), 0, emoji.getUnicode().length());
//        }
//        }
//        }
//
//@Override public final void setEmojiSize(@Px final int pixels) {
//        setEmojiSize(pixels, true);
//        }
//
//@Override public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
//        emojiSize = pixels;
//
//        if (shouldInvalidate) {
//        setText(getText());
//        }
//        }
//
//@Override public final void setEmojiSizeRes(@DimenRes final int res) {
//        setEmojiSizeRes(res, true);
//        }
//
//@Override public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
//        setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
//        }
//        }