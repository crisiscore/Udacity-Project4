package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.text.TextUtils
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.CoroutinesTestRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule
        get() = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var repository: FakeDataSource
    private lateinit var application: Application

    private val reminder = ReminderDataItem(
        title = "Title",
        description = "Description",
        location = "Location name",
        latitude = 1.0,
        longitude = 1.1,
        id = "id"
    )

    @Before
    fun setUp() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        repository = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            app = application,
            dataSource = repository
        )
    }

    @Test
    fun saveReminder_showLoading() = runTest {
        coroutinesTestRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isEqualTo(true)
        coroutinesTestRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun validateEnteredData_emptyTitle_showSnackBar() = runTest {
        val invalidReminder = ReminderDataItem(
            title = null,
            description = "Description",
            location = "Location",
            latitude = 1.0,
            longitude = 1.1,
            id = "id"
        )
        saveReminderViewModel.validateAndSaveReminder(invalidReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun validateEnteredData_emptyLocation_showSnackBar() = runTest {
        val invalidReminder = ReminderDataItem(
            title = "Title",
            description = "Description",
            location = null,
            latitude = 1.0,
            longitude = 1.1,
            id = "id"
        )
        saveReminderViewModel.validateAndSaveReminder(invalidReminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun validateAndSaveReminder() = runTest {
        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showToast.value).isEqualTo(application.getString(R.string.reminder_saved))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue()).isEqualTo(
            NavigationCommand.Back
        )
    }
}