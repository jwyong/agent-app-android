package test;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import sg.com.agentapp.R;
import sg.com.agentapp.appt_tab.appt_room.AppointmentNew;
import sg.com.agentapp.global.MiscHelper;
import sg.com.agentapp.one_maps.OneMapsMain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<AppointmentNew> activityRule
            = new ActivityTestRule<AppointmentNew>(AppointmentNew.class) {
        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent();
            intent.putExtra("from", 1);
            intent.putExtra("agentid", "XXXXXX4");
            return intent;
        }
    };

    @Rule
    public ActivityTestRule<OneMapsMain> mapRule
            = new ActivityTestRule<>(OneMapsMain.class, false, false);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("sg.com.agentapp", appContext.getPackageName());
    }

    @Test
    public void register() {

        onView(withId(sg.com.agentapp.R.id.et_location))
                .perform(click());

        onView(withId(sg.com.agentapp.R.id.et_location))
                .perform(click());

        onView(withId(R.id.search))
                .perform(click());
        onView(withId(R.id.search))
                .perform(click());

        String ranStr = new MiscHelper().getRandomString(10);

        onView(withId(R.id.search))
                .perform(typeText("NA"));

        Random r = new Random();

        Espresso.closeSoftKeyboard();

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        onView(withId(R.id.layout)).perform(RecyclerViewActions.actionOnItemAtPosition(r.nextInt(4) + 1, click()));

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        onView(withId(R.id.done_btn)).perform(click());

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }


        onView(withId(sg.com.agentapp.R.id.et_subject))
                .perform(typeText(ranStr), closeSoftKeyboard());

        onView(withId(sg.com.agentapp.R.id.tv_datetime))
                .perform(click());

        try {
            Thread.sleep(5000);
        } catch (Exception e) {

            e.printStackTrace();
        }

//        try {
//            Matcher<View> okbtn = ViewMatchers.withText("OK");
//        onView(withId(com.github.florent37.singledateandtimepicker.R.id.buttonOk))
//                .check(matches(Matchers.allOf(ViewMatchers.withText("OK"), isDisplayed())));
//
//        onView(withId(com.github.florent37.singledateandtimepicker.R.id.buttonOk)).perform(click());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        onView(withId(sg.com.agentapp.R.id.edttxt_name))
//                .perform(typeText("You-Know-Who"));
//        onView(withId(sg.com.agentapp.R.id.et_contactno))
//                .perform(typeText("0000000000"), closeSoftKeyboard());
        onView(withId(sg.com.agentapp.R.id.et_note))
                .perform(typeText("woots"), closeSoftKeyboard());

        try {
            Thread.sleep(1000);
        } catch (Exception e) {

            e.printStackTrace();
        }

        onView(withId(R.id.btn_send)).perform(click());

        try {
            Thread.sleep(5000);
        } catch (Exception e) {

            e.printStackTrace();
        }



    }

}
