package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.TestReminderMockData.reminderList
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule
        get() = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var application: Application

    @Before
    fun initDatabase() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            application,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDao = remindersDatabase.reminderDao()
        remindersLocalRepository = RemindersLocalRepository(remindersDao, Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }

    @Test
    fun saveReminders_getReminderById_isTheSame() = runTest {
        reminderList.forEach {
            remindersDao.saveReminder(it)
        }

        val allReminderFromDb = remindersLocalRepository.getReminders()
        val reminderResult = allReminderFromDb as Result.Success
        reminderResult.data.forEachIndexed { index, reminderFromDb ->
            val reminderItem = reminderList[index]
            val isEqual = reminderFromDb == reminderItem
            assertThat(isEqual, `is`(true))
        }
        assertThat(reminderResult.data.size, `is`(reminderList.size))
    }

    @Test
    fun getReminderById_passInvalidId() = runBlocking {
        val reminderDTO = remindersLocalRepository.getReminder("Invalid Id")
        assertThat(reminderDTO is Result.Error,`is`(true))

        val result = reminderDTO as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun getReminderById_passValidId() = runBlocking {
        val reminderId = "1"
        reminderList.forEach {
            remindersDao.saveReminder(it)
        }

        val reminderDTO = remindersLocalRepository.getReminder(reminderId)
        assertThat(reminderDTO is Result.Success,`is`(true))

        val result = reminderDTO as Result.Success
        assertThat(result.data.id, `is`(reminderId))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        reminderList.forEach {
            remindersDao.saveReminder(it)
        }

        remindersLocalRepository.deleteAllReminders()

        val allReminderFromDb = remindersLocalRepository.getReminders()
        val reminderResult = allReminderFromDb as Result.Success
        assertThat(reminderResult.data.size, `is`(0))
    }

}