package com.cryoggen.locationreminder.statistics

import android.app.Application
import androidx.lifecycle.*
import com.cryoggen.locationreminder.data.Result
import com.cryoggen.locationreminder.data.Result.Error
import com.cryoggen.locationreminder.data.Result.Success
import com.cryoggen.locationreminder.data.Reminder
import com.cryoggen.locationreminder.data.source.RemindersRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the statistics screen.
 */
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    // Note, for testing and architecture purposes, it's bad practice to construct the repository
    // here. We'll show you how to fix this during the codelab
    private val remindersRepository = RemindersRepository.getRepository(application)

    private val reminders: LiveData<Result<List<Reminder>>> = remindersRepository.observeReminders()
    private val _dataLoading = MutableLiveData<Boolean>(false)
    private val stats: LiveData<StatsResult?> = reminders.map {
        if (it is Success) {
            getActiveAndCompletedStats(it.data)
        } else {
            null
        }
    }

    val activeRemindersPercent = stats.map {
        it?.activeRemindersPercent ?: 0f }
    val completedRemindersPercent: LiveData<Float> = stats.map { it?.completedRemindersPercent ?: 0f }
    val dataLoading: LiveData<Boolean> = _dataLoading
    val error: LiveData<Boolean> = reminders.map { it is Error }
    val empty: LiveData<Boolean> = reminders.map { (it as? Success)?.data.isNullOrEmpty() }

    fun refresh() {
        _dataLoading.value = true
            viewModelScope.launch {
                remindersRepository.observeReminders()
                _dataLoading.value = false
            }
    }
}
