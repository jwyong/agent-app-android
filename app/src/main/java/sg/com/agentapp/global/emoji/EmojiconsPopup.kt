///*
// * Copyright 2014 Ankush Sachdeva
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package sg.com.agentapp.global.emoji
//
//import android.app.Activity
//import android.content.Context
//import android.graphics.Rect
//import android.os.Build
//import android.os.Handler
//import android.os.SystemClock
//import android.util.DisplayMetrics
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.View.OnClickListener
//import android.view.ViewGroup
//import android.view.ViewTreeObserver.OnGlobalLayoutListener
//import android.view.WindowManager
//import android.view.WindowManager.LayoutParams
//import android.widget.PopupWindow
//
//import androidx.viewpager.widget.PagerAdapter
//import androidx.viewpager.widget.ViewPager
//
//import java.util.Arrays
//
//import io.github.rockerhieu.emojicon.EmojiconGridView.OnEmojiconClickedListener
//import io.github.rockerhieu.emojicon.emoji.Cars
//import io.github.rockerhieu.emojicon.emoji.Electr
//import io.github.rockerhieu.emojicon.emoji.Emojicon
//import io.github.rockerhieu.emojicon.emoji.Food
//import io.github.rockerhieu.emojicon.emoji.Nature
//import io.github.rockerhieu.emojicon.emoji.People
//import io.github.rockerhieu.emojicon.emoji.Sport
//import io.github.rockerhieu.emojicon.emoji.Symbols
//
///**
// * @author Ankush Sachdeva (sankush@yahoo.co.in).
// */
//
//class EmojiconsPopup
///**
// * Constructor
// * @param rootView    The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
// * @param mContext The context of current activity.
// */
//(internal var rootView: View, internal var mContext: Context) : PopupWindow(mContext), ViewPager.OnPageChangeListener, EmojiconRecents {
//    private var mEmojiTabLastSelectedIndex = -1
//    private var mEmojiTabs: Array<View>? = null
//    private var mEmojisAdapter: PagerAdapter? = null
//    private var mRecentsManager: EmojiconRecentsManager? = null
//    private var keyBoardHeight = 0
//    private var pendingOpen: Boolean? = false
//    /**
//     *
//     * @return Returns true if the soft keyboard is open, false otherwise.
//     */
//    var isKeyBoardOpen: Boolean? = false
//        private set
//    internal var onEmojiconClickedListener: OnEmojiconClickedListener
//    internal var onEmojiconBackspaceClickedListener: OnEmojiconBackspaceClickedListener? = null
//    internal var onSoftKeyboardOpenCloseListener: OnSoftKeyboardOpenCloseListener? = null
//
//    private var emojisPager: ViewPager? = null
//
//    private val usableScreenHeight: Int
//        get() {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                val metrics = DisplayMetrics()
//
//                val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//                windowManager.defaultDisplay.getMetrics(metrics)
//
//                return metrics.heightPixels
//
//            } else {
//                return rootView.rootView.height
//            }
//        }
//
//    init {
//        val customView = createCustomView()
//        contentView = customView
//        softInputMode = LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
//        //default size
//        setSize(LayoutParams.MATCH_PARENT, 255)
//        setBackgroundDrawable(null)
//
//    }
//
//    /**
//     * Set the listener for the event of keyboard opening or closing.
//     */
//    fun setOnSoftKeyboardOpenCloseListener(listener: OnSoftKeyboardOpenCloseListener) {
//        this.onSoftKeyboardOpenCloseListener = listener
//    }
//
//    /**
//     * Set the listener for the event when any of the emojicon is clicked
//     */
//    fun setOnEmojiconClickedListener(listener: OnEmojiconClickedListener) {
//        this.onEmojiconClickedListener = listener
//    }
//
//    /**
//     * Set the listener for the event when backspace on emojicon popup is clicked
//     */
//    fun setOnEmojiconBackspaceClickedListener(listener: OnEmojiconBackspaceClickedListener) {
//        this.onEmojiconBackspaceClickedListener = listener
//    }
//
//    /**
//     * Use this function to show the emoji popup.
//     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
//     * library needs you to open the soft keyboard atleast once before calling this function.
//     * If that is not possible see showAtBottomPending() function.
//     *
//     */
//    fun showAtBottom() {
//        showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
//    }
//
//    /**
//     * Use this function when the soft keyboard has not been opened yet. This
//     * will show the emoji popup after the keyboard is up next time.
//     * Generally, you will be calling InputMethodManager.showSoftInput function after
//     * calling this function.
//     */
//    fun showAtBottomPending() {
//        if (isKeyBoardOpen!!)
//            showAtBottom()
//        else
//            pendingOpen = true
//    }
//
//    /**
//     * Dismiss the popup
//     */
//    override fun dismiss() {
//        super.dismiss()
//        EmojiconRecentsManager
//                .getInstance(mContext).saveRecents()
//    }
//
//    /**
//     * Call this function to resize the emoji popup according to your soft keyboard size
//     */
//    fun setSizeForSoftKeyboard() {
//        rootView.viewTreeObserver.addOnGlobalLayoutListener {
//            val r = Rect()
//            rootView.getWindowVisibleDisplayFrame(r)
//
//            val screenHeight = usableScreenHeight
//            var heightDifference = screenHeight - (r.bottom - r.top)
//            val resourceId = mContext.resources
//                    .getIdentifier("status_bar_height",
//                            "dimen", "android")
//            if (resourceId > 0) {
//                heightDifference -= mContext.resources
//                        .getDimensionPixelSize(resourceId)
//            }
//            if (heightDifference > 100) {
//                keyBoardHeight = heightDifference
//                setSize(LayoutParams.MATCH_PARENT, keyBoardHeight)
//                if (isKeyBoardOpen == false) {
//                    if (onSoftKeyboardOpenCloseListener != null)
//                        onSoftKeyboardOpenCloseListener!!.onKeyboardOpen(keyBoardHeight)
//                }
//                isKeyBoardOpen = true
//                if (pendingOpen!!) {
//                    showAtBottom()
//                    pendingOpen = false
//                }
//            } else {
//                isKeyBoardOpen = false
//                if (onSoftKeyboardOpenCloseListener != null)
//                    onSoftKeyboardOpenCloseListener!!.onKeyboardClose()
//            }
//        }
//    }
//
//    /**
//     * Manually set the popup window size
//     * @param width Width of the popup
//     * @param height Height of the popup
//     */
//    fun setSize(width: Int, height: Int) {
//        setWidth(width)
//        setHeight(height)
//    }
//
//    private fun createCustomView(): View {
//        val inflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view = inflater.inflate(R.layout.emojicons, null, false)
//        emojisPager = view.findViewById(R.id.emojis_pager)
//        emojisPager!!.setOnPageChangeListener(this)
//        val recents = this
//        mEmojisAdapter = EmojisPagerAdapter(
//                Arrays.asList<EmojiconGridView>(
//                        EmojiconRecentsGridView(mContext, null, null, this),
//                        EmojiconGridView(mContext, People.DATA, recents, this),
//                        EmojiconGridView(mContext, Nature.DATA, recents, this),
//                        EmojiconGridView(mContext, Food.DATA, recents, this),
//                        EmojiconGridView(mContext, Sport.DATA, recents, this),
//                        EmojiconGridView(mContext, Cars.DATA, recents, this),
//                        EmojiconGridView(mContext, Electr.DATA, recents, this),
//                        EmojiconGridView(mContext, Symbols.DATA, recents, this)
//
//                )
//        )
//        emojisPager!!.adapter = mEmojisAdapter
//        mEmojiTabs = arrayOfNulls(8)
//
//        mEmojiTabs[0] = view.findViewById(R.id.emojis_tab_0_recents)
//        mEmojiTabs[1] = view.findViewById(R.id.emojis_tab_1_people)
//        mEmojiTabs[2] = view.findViewById(R.id.emojis_tab_2_nature)
//        mEmojiTabs[3] = view.findViewById(R.id.emojis_tab_3_food)
//        mEmojiTabs[4] = view.findViewById(R.id.emojis_tab_4_sport)
//        mEmojiTabs[5] = view.findViewById(R.id.emojis_tab_5_cars)
//        mEmojiTabs[6] = view.findViewById(R.id.emojis_tab_6_elec)
//        mEmojiTabs[7] = view.findViewById(R.id.emojis_tab_7_sym)
//
//        for (i in mEmojiTabs!!.indices) {
//            mEmojiTabs!![i].setOnClickListener { emojisPager!!.currentItem = i }
//        }
//        view.findViewById(R.id.emojis_backspace).setOnTouchListener(RepeatListener(500, 50, OnClickListener { v ->
//            if (onEmojiconBackspaceClickedListener != null)
//                onEmojiconBackspaceClickedListener!!.onEmojiconBackspaceClicked(v)
//        }))
//
//        // get last selected page
//        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext())
//        var page = mRecentsManager!!.getRecentPage()
//        // last page was recents, check if there are recents to use
//        // if none was found, go to page 1
//        if (page == 0 && mRecentsManager!!.size() === 0) {
//            page = 1
//        }
//
//        if (page == 0) {
//            onPageSelected(page)
//        } else {
//            emojisPager!!.setCurrentItem(page, false)
//        }
//        return view
//    }
//
//    fun addRecentEmoji(context: Context, emojicon: Emojicon) {
//        val fragment = (emojisPager!!.adapter as EmojisPagerAdapter).recentFragment
//        fragment!!.addRecentEmoji(context, emojicon)
//    }
//
//
//    override fun onPageScrolled(i: Int, v: Float, i2: Int) {}
//
//    override fun onPageSelected(i: Int) {
//        if (mEmojiTabLastSelectedIndex == i) {
//            return
//        }
//        when (i) {
//            0, 1, 2, 3, 4, 5, 6, 7 -> {
//
//                if (mEmojiTabLastSelectedIndex >= 0 && mEmojiTabLastSelectedIndex < mEmojiTabs!!.size) {
//                    mEmojiTabs!![mEmojiTabLastSelectedIndex].isSelected = false
//                }
//                mEmojiTabs!![i].isSelected = true
//                mEmojiTabLastSelectedIndex = i
//                mRecentsManager!!.setRecentPage(i)
//            }
//        }
//    }
//
//    override fun onPageScrollStateChanged(i: Int) {}
//
//    private class EmojisPagerAdapter(private val views: List<EmojiconGridView>) : PagerAdapter() {
//        val recentFragment: EmojiconRecentsGridView?
//            get() {
//                for (it in views) {
//                    if (it is EmojiconRecentsGridView)
//                        return it as EmojiconRecentsGridView
//                }
//                return null
//            }
//
//        override fun getCount(): Int {
//            return views.size
//        }
//
//
//        override fun instantiateItem(container: ViewGroup, position: Int): Any {
//            val v = views[position].rootView
//            container.addView(v, 0)
//            return v
//        }
//
//        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
//            container.removeView(view as View)
//        }
//
//        override fun isViewFromObject(view: View, key: Any): Boolean {
//            return key === view
//        }
//    }
//
//    /**
//     * A class, that can be used as a TouchListener on any view (e.g. a Button).
//     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
//     * click is fired immediately, next before initialInterval, and subsequent before
//     * normalInterval.
//     *
//     *
//     *
//     * Interval is scheduled before the onClick completes, so it has to run fast.
//     * If it runs slow, it does not generate skipped onClicks.
//     */
//    class RepeatListener
//    /**
//     * @param initialInterval The interval before first click event
//     * @param normalInterval  The interval before second and subsequent click
//     * events
//     * @param clickListener   The OnClickListener, that will be called
//     * periodically
//     */
//    (private val initialInterval: Int, private val normalInterval: Int, private val clickListener: OnClickListener?) : View.OnTouchListener {
//
//        private val handler = Handler()
//
//        private val handlerRunnable = object : Runnable {
//            override fun run() {
//                if (downView == null) {
//                    return
//                }
//                handler.removeCallbacksAndMessages(downView)
//                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval)
//                clickListener.onClick(downView)
//            }
//        }
//
//        private var downView: View? = null
//
//        init {
//            if (clickListener == null)
//                throw IllegalArgumentException("null runnable")
//            if (initialInterval < 0 || normalInterval < 0)
//                throw IllegalArgumentException("negative interval")
//        }
//
//        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    downView = view
//                    handler.removeCallbacks(handlerRunnable)
//                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval)
//                    clickListener.onClick(view)
//                    return true
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
//                    handler.removeCallbacksAndMessages(downView)
//                    downView = null
//                    return true
//                }
//            }
//            return false
//        }
//    }
//
//    interface OnEmojiconBackspaceClickedListener {
//        fun onEmojiconBackspaceClicked(v: View)
//    }
//
//    interface OnSoftKeyboardOpenCloseListener {
//        fun onKeyboardOpen(keyBoardHeight: Int)
//        fun onKeyboardClose()
//    }
//}
