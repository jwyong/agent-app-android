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

package io.github.rockerhieu.emojicon.emoji;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;

public class Emojicon implements Serializable {
    private static final long serialVersionUID = 3L;
    private String emoji;
    @DrawableRes
    private int resource;

    private Emojicon() {
    }

    //    public Emojicon(@NonNull final int[] codePoints, @DrawableRes final int resource) {
//        this(codePoints, resource, new Emojicon[0]);
//    }
//    public Emojicon(final int codePoint, @DrawableRes final int resource) {
//        this(codePoint, resource, new Emojicon[0]);
//    }
//    public static Emojicon fromCodePoint(@NonNull final int[] codePoints, @DrawableRes final int resource) {
//        Emojicon emoji = new Emojicon();
//        emoji.emoji = Arrays.toString(codePoints);
//        emoji.resource = resource;
//        return emoji;
//    }
//
//    public static Emojicon fromCodePoint(int codePoints, @DrawableRes final int resource) {
//        Emojicon emoji = new Emojicon();
//        emoji.emoji = newString(codePoints);
//        emoji.resource = resource;
//        return emoji;
//
//    }

    public static Emojicon fromCodePoint(int codePoints){
        Emojicon emoji = new Emojicon();
        emoji.emoji = newString(codePoints);
        return emoji;
    }

    public static Emojicon fromCodePoint(int[] codePoints){
        Emojicon emoji = new Emojicon();
        emoji.emoji = Arrays.toString(codePoints);
        return  emoji;
    }

    //    public static Emojicon arrayCodePoint(int[] codePoint) {
//        Emojicon emoji = new Emojicon();
//        Log.d("jason" , "haha1 " +Arrays.toString(codePoint));
//        Log.d("jason" Character.toChars(codePoint))
//        emoji.emoji = Arrays.toString(codePoint);
//
//        return emoji;
//    }
//
    public static Emojicon fromChar(char ch) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = Character.toString(ch);
        return emoji;
    }

    //
    public static Emojicon fromChars(String chars) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = chars;
        return emoji;
    }

    //
    public Emojicon(String emoji) {
        this.emoji = emoji;
    }

    //
    public String getEmoji() {
        return emoji;
    }

    //
    @Override
    public boolean equals(Object o) {
        return o instanceof Emojicon && emoji.equals(((Emojicon) o).emoji);
    }

    //
    @Override
    public int hashCode() {
        return emoji.hashCode();
    }

    //
    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }

    }
//
//    public final String aNewString(int[] codePoint){
//        if (Character.charCount(codePoint) == 1) {
//            return String.valueOf(codePoint);
//        } else {
//            return new String(Character.toChars(codePoint));
//        }
//
//    }

//    public static final String ArrayString(final int codePoint , final int codeColor) {
//
//
//        this.codePoint = codePoint;
//
//    }

}
