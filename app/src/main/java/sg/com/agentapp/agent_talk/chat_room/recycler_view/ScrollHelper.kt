package sg.com.agentapp.agent_talk.chat_room.recycler_view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrollHelper {
    private lateinit var recyclerView: RecyclerView
    private var keyBoardScrolled: Boolean = false
    private var firstVisibleItem: Int = 0

    // setup scroll listener and other related UX
    fun setupScrollUX(recyclerView: RecyclerView, rvLayoutManager: LinearLayoutManager) {
        this.recyclerView = recyclerView

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                firstVisibleItem = rvLayoutManager.findFirstVisibleItemPosition()
            }

        })
    }

    // auto scroll when keyboard up
    fun detectKeyboard(rootView: View) {
        //set softkeyboard open/close listener
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!keyBoardScrolled) { //scroll to position only once
                    scrollToBtm()

                    keyBoardScrolled = true
                }

            } else {
                if (keyBoardScrolled) { //scroll to position only once
                    //keyboard is closed
                    scrollToBtm()

                    keyBoardScrolled = false
                }
            }
        }
    }

    // scroll to btm if user hvnt scrolled
    fun scrollToBtm() {
        if (firstVisibleItem <= 0) {
            recyclerView.smoothScrollToPosition(0)
        }
    }
}