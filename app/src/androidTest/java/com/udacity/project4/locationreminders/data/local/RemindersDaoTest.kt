package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule
        get() = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var application: Application

    @Before
    fun initDatabase() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            application,
            RemindersDatabase::class.java
        ).build()
        remindersDao = remindersDatabase.reminderDao()
    }

    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }

    @Test
    fun insertReminder_getReminder_isTheSame() = runTest {
        val reminderDTO = ReminderDTO(
            "Title",
            "Description",
            "Location name",
            1.0,
            1.1,
            "1"
        )

        remindersDao.saveReminder(reminderDTO)

        val reminderResult = remindersDao.getReminderById(reminderDTO.id)?.let {
            it == reminderDTO
        }

        assertThat(reminderResult, CoreMatchers.`is`(true))
    }



}