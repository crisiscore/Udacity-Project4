package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var isDataLoaded: Boolean = true
    var reminderDataSource: HashMap<String, ReminderDTO> = HashMap()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if (isDataLoaded) {
                val tempReminderList: MutableList<ReminderDTO> = ArrayList()
                reminderDataSource.forEach {
                    tempReminderList.add(it.value)
                }
                Result.Success(tempReminderList)
            } else {
                throw Exception("Fail to load reminders")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDataSource[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            if (isDataLoaded) {
                reminderDataSource[id]?.let {
                    Result.Success(it)
                }
                return Result.Error("Reminder not found")
            } else {
                throw Exception("Fail to load reminder")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminderDataSource.clear()
    }

}