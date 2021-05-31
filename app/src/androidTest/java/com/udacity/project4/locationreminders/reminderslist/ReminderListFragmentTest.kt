package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.internal.ContextUtils
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.LocationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private lateinit var viewModel: RemindersListViewModel

    private val resource = DataBindingIdlingResource()

    @Before
    fun init() {
        stopKoin()//stop the original app koin
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
        viewModel = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun register(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(resource)
    }

    @After
    fun unregister(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(resource)
    }

    @Test
    fun clickAddReminderFAB_toSaveReminderFragment() = runBlockingTest{
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        resource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder(0.0f, 0.0f, null)
        )

    }

    @Test
    fun activeReminders_DisplayedInUi() = runBlocking {
        val reminderDTO = ReminderDTO(
            "title1",
            "KSA",
            "Jeddah",
            25.6898,
            70.0548,
            "200"
        )
        repository.saveReminder(reminderDTO)

        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        resource.monitorFragment(fragmentScenario)

        onView(withText(reminderDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.description)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.location)).check(matches(isDisplayed()))

        repository.deleteAllReminders()
    }


    @Test
    fun onSwipe_getToast() = runBlocking {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        resource.monitorFragment(scenario)

        val reminderDTO = ReminderDTO(
            "title1",
            "KSA",
            "Jeddah",
            25.6898,
            70.0548,
            "200"
        )
        repository.saveReminder(reminderDTO)



        onView(withId(R.id.refreshLayout)).perform(ViewActions.swipeDown())
        onView(withText("Reminders refreshed"))
            .inRoot(withDecorView(not(`is`(getActivity(appContext)?.window?.decorView))))
            .check(matches(isDisplayed()))
        repository.deleteAllReminders()
    }

    @Test
    fun retrieveError_noReminder() = runBlocking {
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        resource.monitorFragment(fragmentScenario)

        onView(withId(R.id.refreshLayout)).perform(ViewActions.swipeDown())
        onView(withText("No reminders found"))
            .inRoot(RootMatchers.withDecorView(Matchers.not(ContextUtils.getActivity(appContext)?.window?.decorView)))
            .check(matches(isDisplayed()))

        //delete all
        repository.deleteAllReminders()
    }


}