package com.udacity.project4

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

object TestReminderMockData {
    val reminderList: List<ReminderDTO>
        get() = listOf(
            ReminderDTO(
                title = "Title 1",
                description = "Description 1",
                location = "Location 1",
                latitude = 1.0,
                longitude = 1.0,
                id = "1"
            ),
            ReminderDTO(
                title = "Title 2",
                description = "Description 2",
                location = "Location 2",
                latitude = 1.0,
                longitude = 1.0,
                id = "2"
            )
        )
}