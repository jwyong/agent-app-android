package sg.com.agentapp.global.emoji

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.PopupWindow
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.emojicons.view.*
import kotlinx.android.synthetic.main.emojipopup_chat.view.mainGrid
import sg.com.agentapp.R

class EmojiPopup(val rootView: View, val context: Context) : PopupWindow(), ViewPager.OnPageChangeListener {

    private var keyBoardHeight = 0
    private var isOpened: Boolean? = false
    private var pendingOpen: Boolean? = false

//    private var emojisPager: ViewPager? = null

    internal var onEmojiconBackspaceClickedListener: OnEmojiconBackspaceClickedListener? = null
    internal var onSoftKeyboardOpenCloseListener: OnSoftKeyboardOpenCloseListener? = null

    init {
        val customView = createCustomView()
        contentView = customView
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        setSize(WindowManager.LayoutParams.MATCH_PARENT, 225)
        setBackgroundDrawable(null)

    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    fun setOnSoftKeyboardOpenCloseListener(listener: OnSoftKeyboardOpenCloseListener) {
        this.onSoftKeyboardOpenCloseListener = listener
    }

    fun setOnEmojiconBackspaceClickedListener(listener: OnEmojiconBackspaceClickedListener) {
        this.onEmojiconBackspaceClickedListener = listener
    }

    private fun createCustomView(): View? {
//        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val emojiGrid = layoutInflater.inflate(sg.com.agentapp.R.layout.emojipopup_chat, null)

//        emojiGrid.mainGrid.adapter = EmojiAdapter(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.emojicons, null, false)
//        emojisPager.setOnPageChangeListener(this)
//        val recents = this
//        mEmojisAdapter = EmojisPagerAdapter(
//                Arrays.asList(
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
        view.mainGrid.adapter = EmojiAdapter(context)
//        mEmojiTabs = arrayOfNulls<View>(8)

//        mEmojiTabs[0] = view.findViewById(R.id.emojis_tab_0_recents)
//        mEmojiTabs[1] = view.findViewById(R.id.emojis_tab_1_people)
//        mEmojiTabs[2] = view.findViewById(R.id.emojis_tab_2_nature)
//        mEmojiTabs[3] = view.findViewById(R.id.emojis_tab_3_food)
//        mEmojiTabs[4] = view.findViewById(R.id.emojis_tab_4_sport)
//        mEmojiTabs[5] = view.findViewById(R.id.emojis_tab_5_cars)
//        mEmojiTabs[6] = view.findViewById(R.id.emojis_tab_6_elec)
//        mEmojiTabs[7] = view.findViewById(R.id.emojis_tab_7_sym)

//        for (i in mEmojiTabs.indices) {
//            mEmojiTabs[i].setOnClickListener(object : OnClickListener {
//                override fun onClick(v: View) {
//                    emojisPager.setCurrentItem(i)
//                }
//            })
//        }
//        view.findViewById(R.id.emojis_backspace).setOnTouchListener(RepeatListener(500, 50, object : OnClickListener {

        view.emojibackbtn.setOnClickListener {
            if (onEmojiconBackspaceClickedListener != null)
                onEmojiconBackspaceClickedListener!!.onEmojiconBackspaceClicked(it)
        }

//            override fun onClick(v: View) {
//                if (onEmojiconBackspaceClickedListener != null)
//                    onEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v)
//            }
//        }))
//
//        // get last selected page
//        mRecentsManager = EmojiconRecentsManager.getInstance(view.getContext())
//        var page = mRecentsManager.getRecentPage()
//        // last page was recents, check if there are recents to use
//        // if none was found, go to page 1
//        if (page == 0 && mRecentsManager.size() === 0) {
//            page = 1
//        }
//
//        if (page == 0) {
//            onPageSelected(page)
//        } else {
//            emojisPager.setCurrentItem(page, false)
//        }
        return view
//        return emojiGrid
    }

    fun setSizeForSoftKeyboard() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {

            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = getUsableScreenHeight()
            var heightDifference = screenHeight - (r.bottom - r.top)
//            val resourceId = context.resources
//                    .getIdentifier("status_bar_height",
//                            "dimen", "android")
//            if (resourceId > 0) {
//                heightDifference -= context.resources
//                        .getDimensionPixelSize(resourceId)
//                var heightDifference1 = screenHeight - (r.bottom - r.top)
//            }

            if (heightDifference > 100) {
                keyBoardHeight = heightDifference
                setSize(ViewGroup.LayoutParams.MATCH_PARENT, keyBoardHeight)
                if (isOpened == false) {
                    if (onSoftKeyboardOpenCloseListener != null)
                        onSoftKeyboardOpenCloseListener!!.onKeyboardOpen(keyBoardHeight)
                }
                isOpened = true
                if (pendingOpen!!) {
                    showAtBottom()
                    pendingOpen = false
                }
            } else {
                isOpened = false
                if (onSoftKeyboardOpenCloseListener != null)
                    onSoftKeyboardOpenCloseListener!!.onKeyboardClose()
            }
        }
    }

    fun showAtBottom() {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
    }

    fun showAtBottomPending() {
        if (isKeyBoardOpen()!!)
            showAtBottom()
        else
            pendingOpen = true
    }

    fun isKeyBoardOpen(): Boolean? {
        return isOpened
    }

    private fun getUsableScreenHeight(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val metrics = DisplayMetrics()

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)

            return metrics.heightPixels

        } else {
            return rootView.rootView.height
        }
    }

    fun setSize(width: Int, height: Int) {
        setWidth(width)
        setHeight(height)
    }

    interface OnSoftKeyboardOpenCloseListener {
        fun onKeyboardOpen(keyBoardHeight: Int)
        fun onKeyboardClose()
    }

    interface OnEmojiconBackspaceClickedListener {
        fun onEmojiconBackspaceClicked(v: View)
    }

//    private class EmojisPagerAdapter(private val views: EmojiGridView) : PagerAdapter() {
////        val recentFragment: EmojiconRecentsGridView?
////            get() {
////                for (it in views) {
////                    if (it is EmojiconRecentsGridView)
////                        return it as EmojiconRecentsGridView
////                }
////                return null
////            }
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
}

