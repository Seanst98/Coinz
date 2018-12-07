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
public class mainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        mActivityRule.getActivity().getSupportFragmentManager().beginTransaction();
    }

    @Test
    public void testTextDisplayed() {
        onView(withText("Login")).check(matches(isDisplayed()));
        onView(withText("Create Account")).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateAccountButtonClick() {
        onView(withId(R.id.createAccButton)).perform(click());
        onView(withId(R.id.enterEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword2)).check(matches(isDisplayed()));
        onView(withText("Create Account")).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginButtonClick() {
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.enterEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.enterPassword)).check(matches(isDisplayed()));
        onView(withText("Login")).check(matches(isDisplayed()));
    }

}
