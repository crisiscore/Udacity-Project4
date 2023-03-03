package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.ToastManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(ToastManager.getIdlingResource())
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(ToastManager.getIdlingResource())
    }

    @Test
    fun clickingAddReminder_openbtn_save_reminderScreen() = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun validate_EnteredData() = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)


        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.btn_save_reminder)).perform(click())

        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun click_BackButton() = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)


        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.btn_save_reminder)).perform(click())

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
        Espresso.pressBack()
        activityScenario.close()
    }

    @Test
    fun selectLocationValidat_showSnackBar() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(replaceText(appContext.getString(R.string.reminder_title)))
        onView(withId(R.id.reminderDescription)).perform(replaceText(appContext.getString(R.string.reminder_desc)))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.snackbar_text)).check(matches(withText(appContext.getString(R.string.err_select_location))))
        scenario.close()
    }

    @Test
    fun btn_save_reminderValidateTitle_showSnackBar() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).perform(replaceText(appContext.getString(R.string.reminder_desc)))
        onView(withId(R.id.btn_save_reminder)).perform(click())
        onView(withId(R.id.snackbar_text)).check(matches(withText(appContext.getString(R.string.err_enter_title))))
        scenario.close()
    }

    @Test
    fun btn_save_reminderValidateLocation_showSnackBar() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(replaceText(appContext.getString(R.string.reminder_title)))
        onView(withId(R.id.reminderDescription)).perform(replaceText(appContext.getString(R.string.reminder_desc)))
        onView(withId(R.id.btn_save_reminder)).perform(click())
        onView(withId(R.id.snackbar_text)).check(matches(withText(appContext.getString(R.string.err_select_location))))
        scenario.close()
    }

    @Test
    fun addSelectedLocation_saveReminder_showToastMessage() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(replaceText(appContext.getString(R.string.reminder_title)))
        onView(withId(R.id.reminderDescription)).perform(replaceText(appContext.getString(R.string.reminder_desc)))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.btn_save_reminder)).perform(click())

        onView(withText("Geofence Added"))
            .inRoot(withDecorView(not(getActivity(scenario)?.window?.decorView)))
            .check(
                matches(isDisplayed())
            )

        ToastManager.increment()
        onView(withText("Reminder Saved !"))
            .inRoot(withDecorView(not(getActivity(scenario)?.window?.decorView)))
            .check(
                matches(isDisplayed())
            )
        scenario.close()
    }


    private fun getActivity(scenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        scenario.onActivity {
            activity = it
        }
        return activity
    }

}

