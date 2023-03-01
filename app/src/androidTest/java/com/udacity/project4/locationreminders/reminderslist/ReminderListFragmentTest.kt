package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var reminderDataSource: ReminderDataSource
    private lateinit var applicationContext: Application
    private lateinit var remindersDao: RemindersDao

    @Before
    fun setup() {
        stopKoin()
        applicationContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    app = applicationContext,
                    dataSource = reminderDataSource
                )
            }
            single {
                RemindersLocalRepository(
                    remindersDao = remindersDao,
                    ioDispatcher = Dispatchers.IO
                ) as ReminderDataSource
            }
            single { LocalDB.createRemindersDao(applicationContext) }
        }

        startKoin {
            androidContext(applicationContext)
            modules(listOf(myModule))
        }
        remindersDao = get()
        reminderDataSource = get()

        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun clickingFab_navigateToSaveFragment() = runTest {
        val mockNavController = mock(NavController::class.java)
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        fragmentScenario.onFragment {
            it.view?.let { it1 -> Navigation.setViewNavController(it1, mockNavController) }
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mockNavController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun showReminderItem_InRecyclerView() = runTest {
        val reminderDTO = ReminderDTO(
            "Title",
            "Description",
            "Location name",
            1.0,
            1.1,
            "1"
        )
        reminderDataSource.saveReminder(reminderDTO)
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(reminderDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.description)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.location)).check(matches(isDisplayed()))

        onView(withId(R.id.title)).check(matches(withText(reminderDTO.title)))
        onView(withId(R.id.description)).check(matches(withText(reminderDTO.description)))
        onView(withId(R.id.location)).check(matches(withText(reminderDTO.location)))
    }

    @Test
    fun emptyReminder_showsNoData() = runTest {
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }


}

