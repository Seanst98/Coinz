package com.kiwi.moon.coinz;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

//***********************************
//Had to write the tests myself because
//esspresso testing wouldn't work for me
//as it mainatined a 7000 millisecond delay
//for every touch
//***********************************
@RunWith(AndroidJUnit4.class)
@LargeTest
public class createAccountScreenTest {

    @Rule
    public ActivityTestRule<createAccActivity> mActivityRule =
            new ActivityTestRule<>(createAccActivity.class);

    @Before
    public void init() {
        mActivityRule.getActivity().getSupportFragmentManager().beginTransaction();
    }

    @Test
    public void testTextDisplayed() {
        onView(withText("Create Account")).check(matches(isDisplayed()));
        onView(withId(R.id.enterEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword2)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateAccountCardClick() {
        onView(withId(R.id.cardView)).perform(click()).check(matches(isDisplayed()));
    }


}
