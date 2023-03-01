package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.CoroutinesTestRule
import com.udacity.project4.locationreminders.TestReminderMockData.reminderList
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule
        get() = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var repository: FakeDataSource
    private lateinit var application: Application

    @Before
    fun setUp() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        repository = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            app = application,
            dataSource = repository
        )
    }

    @Test
    fun loadReminders_showLoading() = runBlockingTest {
        coroutinesTestRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isEqualTo(true)
        coroutinesTestRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun loadReminders_showReminders() = runTest{
        reminderList.forEach {
            repository.saveReminder(it)
        }
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).isNotEqualTo(null)
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isEqualTo(false)
    }

    @Test
    fun loadReminders_showEmptyReminders() {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isEqualTo(true)
    }

    @Test
    fun loadRemindersWhenUnavailable() = runTest {
        repository.isDataLoaded = false
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Fail to load reminders")
    }

}