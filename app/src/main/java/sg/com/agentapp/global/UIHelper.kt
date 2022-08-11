package sg.com.agentapp.global

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.format.DateUtils
import android.transition.Slide
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.popup_3_icons.view.*
import sg.com.agentapp.R
import sg.com.agentapp.databinding.DialogNewapptBinding
import sg.com.agentapp.databinding.Popup3IconsBinding
import java.text.SimpleDateFormat
import java.util.*

class UIHelper {
    // hide softkeyboard (activity)
    fun hideActiKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus

        if (view == null) {
            view = View(activity)
        }

        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // hide softkeyboard (fragment)
    fun hideFragKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // convert date long to str (show time for today)
    fun dateLongToStrToday(dateLong: Long): String {
        val date = Date(dateLong)
        val format: SimpleDateFormat

        // check if today
        if (DateUtils.isToday(dateLong)) { // today - only show time (3:15pm)
            format = SimpleDateFormat("H:mma", Locale.getDefault())
        } else { // not today, show date
            format = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        }

        return format.format(date)

    }

    fun formatDateFromLong(dateLong: Long, formatStr: String): String {
        val date = Date(dateLong)
        val format = SimpleDateFormat(formatStr, Locale.getDefault())

        return format.format(date)

    }

    fun loadingDialog(activity: Activity, description: String?): Dialog {
        val miscHelper = MiscHelper()
        val screen = miscHelper.getScreenWidthAndHeight(activity)

        val xScreen16 = screen.x / 16
        val yScreen2 = screen.y / 2

        val dialog = Dialog(activity)

        val dialog_layout = LayoutInflater.from(activity)
        val dialog_view = dialog_layout.inflate(R.layout.dialog_loading, null)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialog_view)
        dialog.setCancelable(false)
        dialog.create()

        val fl_params: FrameLayout.LayoutParams
        val cv_dialog_loading = dialog_view.findViewById<CardView>(R.id.cv_dialog_loading)
        fl_params = cv_dialog_loading.layoutParams as FrameLayout.LayoutParams
        fl_params.width = yScreen2 - (xScreen16 + xScreen16)
        cv_dialog_loading.layoutParams = fl_params

        val tv_des = dialog_view.findViewById<TextView>(R.id.tv_des)
        if (description != null) {
            tv_des.text = description
        }

        dialog.show()

        return dialog
    }

    fun dialog2btn(context: Context, message: String, positiveText: String,
                   negativeText: String, positiveFunc: Runnable, negativeFunc: Runnable?, isCancellable: Boolean): Dialog {

        val dialog = Dialog(context)
        val dialogLayout = LayoutInflater.from(context)
        val dialogView = dialogLayout.inflate(R.layout.dialog2btn, null)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.setCancelable(isCancellable)
        dialog.create()

        val dialogMsg = dialog.findViewById<TextView>(R.id.dialog_msg)
        val dialog_negative = dialog.findViewById<TextView>(R.id.negative_button)
        val dialog_positive = dialog.findViewById<TextView>(R.id.positive_button)

        dialogMsg.text = message

        //btn negative set function
        dialog_negative.text = negativeText

        //btn positive set function
        dialog_positive.text = positiveText

        //set OnclickListener
        dialog_negative.setOnClickListener {
            negativeFunc?.run()
            dialog.dismiss()
        }

        dialog_positive.setOnClickListener {
            positiveFunc.run()
            dialog.dismiss()
        }

        dialog.show()

        return dialog
    }

    fun dialog2Btns1EditTxt(context: Context?, title: Int, label: Int, observableField: ObservableField<String>, negativeFunction: Runnable?, isCancellable: Boolean) {

        val dialog = Dialog(context)
        val dialogLayout = LayoutInflater.from(context)
        val dialogView = dialogLayout.inflate(R.layout.dialog2btn_1edit_txt, null)

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.setCancelable(isCancellable)
        dialog.create()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        // bind views
        val dialogTitle = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialogLabel = dialog.findViewById<TextView>(R.id.dialog_label)
        val dialogField = dialog.findViewById<TextView>(R.id.dialog_field)
        val dialogOkBtn = dialog.findViewById<TextView>(R.id.positive_button)
        val dialogCancelBtn = dialog.findViewById<TextView>(R.id.negative_button)

        // set values
        dialogTitle.text = context?.getString(title)
        dialogLabel.text = context?.getString(label)
        dialogField.text = observableField.get()
        dialogField.requestFocus()

        // set onclick
        dialogOkBtn.setOnClickListener { v ->
            observableField.set(dialogField.text.toString())
            dialog.dismiss()
        }
        dialogCancelBtn.setOnClickListener { v ->
            negativeFunction?.run()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun btmDialog2Items(
            context: Context,
            btn1Text: Int,
            btn2Text: Int,
            btn1Func: Runnable,
            btn2Func: Runnable,
            isCancellable: Boolean
    ): BottomSheetDialog {
        val btmSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.btm_sheet_2_items, null)

        btmSheetDialog.setContentView(view)
        btmSheetDialog.setCancelable(isCancellable)
        btmSheetDialog.create()
        btmSheetDialog.window?.findViewById<View>(R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)

        val btn1 = view.findViewById<Button>(R.id.btn_1)
        btn1.text = context.getString(btn1Text)
        btn1.setOnClickListener {
            btn1Func.run()
            btmSheetDialog.dismiss()
        }

        val btn2 = view.findViewById<Button>(R.id.btn_2)
        btn2.text = context.getString(btn2Text)
        btn2.setOnClickListener {
            btn2Func.run()
            btmSheetDialog.dismiss()
        }

        btmSheetDialog.show()

        return btmSheetDialog
    }

    // chatroom btm popup
    fun popup3Icons(context: Context, gravityInt: Int, xOffsetDp: Int, yOffsetDp: Int, btn1Action: Runnable?,
                    btn2Action: Runnable?, btn3Action: Runnable?): PopupWindow {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: Popup3IconsBinding = DataBindingUtil.inflate(layoutInflater, R.layout.popup_3_icons, null, false)
        val view = binding.root

        val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.BOTTOM
            popupWindow.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.BOTTOM
            popupWindow.exitTransition = slideOut

        }

        TransitionManager.beginDelayedTransition(binding.rootLayout)
        popupWindow.showAtLocation(
                view, // Location to display popup window
                gravityInt, // Exact position of layout to display popup
                xOffsetDp, // X offset
                yOffsetDp // Y offset
        )

        // btn actions
        view.camera.setOnClickListener {
            btn1Action?.run()
            popupWindow.dismiss()
        }

        view.media.setOnClickListener {
            btn2Action?.run()
            popupWindow.dismiss()
        }

        view.document.setOnClickListener {
            btn3Action?.run()
            popupWindow.dismiss()
        }

        return popupWindow
    }

    // get soft btn height for those with it
    fun getSoftButtonsBarHeight(activity: Activity): Int {
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        return if (realHeight > usableHeight)
            realHeight - usableHeight
        else
            0
    }

    // new appt popup (agent or non-agent)
    fun dialogNewAppt(context: Context, isCancellable: Boolean, agentBtnAction: Runnable, nonAgentBtnAction: Runnable) {
        val binding: DialogNewapptBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_newappt, null, false)
        val dialog = Dialog(context)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.setCancelable(isCancellable)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.create()

        //set OnclickListener
        binding.tvApptAgent.setOnClickListener {
            agentBtnAction.run()
            dialog.dismiss()
        }

        binding.tvApptNonagent.setOnClickListener {
            nonAgentBtnAction.run()
            dialog.dismiss()
        }

        dialog.show()
    }

    // gradient backgrounds
    fun gradientBgWhiteBig(context: Context, view: View, whiteSize: Int) {
        val colourIntArray: IntArray

        when (whiteSize) {
            0 -> { // small (e.g. registration)
                colourIntArray = intArrayOf(
                        ContextCompat.getColor(context, R.color.primaryBackground),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.primaryBackground)
                )
            }

            1 -> { // mid (e.g. findAgent)
                colourIntArray = intArrayOf(
                        ContextCompat.getColor(context, R.color.primaryBackground),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.primaryBackground)
                )
            }

            2 -> { // big
                colourIntArray = intArrayOf(
                        ContextCompat.getColor(context, R.color.primaryBackground),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.primaryBackground)
                )
            }

            else -> { // no btm
                colourIntArray = intArrayOf(
                        ContextCompat.getColor(context, R.color.primaryBackground),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white),
                        ContextCompat.getColor(context, R.color.white)
                )
            }
        }

        val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colourIntArray
        )

        view.background = gradientDrawable
    }
}
