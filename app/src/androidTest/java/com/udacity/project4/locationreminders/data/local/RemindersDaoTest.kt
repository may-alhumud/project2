package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initialiseDatabase(){
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun insertReminderDTOAndGetById() = runBlockingTest {
        val reminderDTO = ReminderDTO(
            "title1",
            "KSA",
            "Jeddah",
            25.6898,
            70.0548,
            "200"
        )

        database.reminderDao().saveReminder(reminderDTO)

        val savedReminderDTO = database.reminderDao().getReminderById(reminderDTO.id)

        assertThat<ReminderDTO>(savedReminderDTO, notNullValue())

        assertThat(savedReminderDTO?.title, `is`(reminderDTO.title))
        assertThat(savedReminderDTO?.description, `is`(reminderDTO.description))
        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
        assertThat(savedReminderDTO?.latitude, `is`(reminderDTO.latitude))
        assertThat(savedReminderDTO?.longitude, `is`(reminderDTO.longitude))
        assertThat(savedReminderDTO?.id, `is`(reminderDTO.id))
        assertThat(savedReminderDTO?.location, `is`(reminderDTO.location))
    }

    @Test
    fun insertAndDeleteAll_GetAll() = runBlockingTest {
        val reminderDTO = ReminderDTO(
                "title1",
                "KSA",
                "Jeddah",
                25.6898,
                70.0548,
                "200"
        )

        database.reminderDao().saveReminder(reminderDTO)
        database.reminderDao().deleteAllReminders()
        val savedRemindersDTO = database.reminderDao().getReminders()
        assertThat(savedRemindersDTO.isEmpty(), `is`(true))
    }

    @Test
    fun success_insertAndGetAll() = runBlockingTest {
        val reminderDTO = ReminderDTO(
            "title2",
            "KSA",
            "Jeddah",
            25.6898,
            70.0548,
            "200"
        )

        database.reminderDao().saveReminder(reminderDTO)
        val savedRemindersDTO = database.reminderDao().getReminders()
        assertThat(savedRemindersDTO.isNotEmpty(), `is`(true))
    }

    @Test
    fun deleteAll_getAll() = runBlockingTest {
        database.reminderDao().deleteAllReminders()
        val allReminder = database.reminderDao().getReminders()
        assertThat(allReminder.isEmpty(), `is`(true))
    }

}