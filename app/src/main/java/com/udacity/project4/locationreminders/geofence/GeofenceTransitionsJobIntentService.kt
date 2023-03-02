package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private val dataSource: ReminderDataSource by inject()

    companion object {
        private const val JOB_ID = 567

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.d("GeofenceTransitionsJob", "Error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == GEOFENCE_TRANSITION_ENTER && geofencingEvent.triggeringGeofences.isNotEmpty()) {
            Log.d("GeofenceTransitionsJob", "Enter: ${geofencingEvent.triggeringLocation}")
            triggerAndSendNotification(geofencingEvent.triggeringGeofences)
        }else{
            Log.d("GeofenceTransitionsJob", "Never expired: $geofenceTransition")
        }
    }

    private fun triggerAndSendNotification(triggeringGeofences: List<Geofence>) {

        triggeringGeofences.forEach {
            val requestId = it.requestId
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                val result = dataSource.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    Log.d("GeofenceTransitionsJob", "${result.data.title}")
                    val reminderDTO = result.data
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
                Log.d("GeofenceTransitionsJob", "Enter: not found")
            }
        }
//        sendNotification(
//            this@GeofenceTransitionsJobIntentService, ReminderDataItem(
//                "Geofence Notification",
//                "Inside Geofence",
//                "Triggered by Geofence",
//                0.4, 0.5
//            )
//        )
    }
}
