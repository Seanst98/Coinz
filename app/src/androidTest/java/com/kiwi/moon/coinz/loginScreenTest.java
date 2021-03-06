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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class loginScreenTest {

    @Rule
    public ActivityTestRule<loginActivity> mActivityRule =
            new ActivityTestRule<>(loginActivity.class);

    @Before
    public void init() {
        mActivityRule.getActivity().getSupportFragmentManager().beginTransaction();
    }

    @Test
    public void testTextDisplayed() {
        onView(withText("Login")).check(matches(isDisplayed()));
        onView(withId(R.id.enterEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginCardClick() {
        onView(withId(R.id.cardView)).perform(click()).check(matches(isDisplayed()));
    }
}
