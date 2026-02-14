package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel
import com.vibedev.bluecollar.data.JobHistory
import com.vibedev.bluecollar.manager.AppwriteManager

class RequestHistoryViewModel : ViewModel() {

    suspend fun getTodayRequestHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        return AppwriteManager.requestHistory.getTodaysJobHistory(limit, reverseOrder)
    }

    suspend fun getThisWeekRequestHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory>{
        return AppwriteManager.requestHistory.getThisWeeksJobHistory(limit, reverseOrder)
    }

    suspend fun getThisMonthRequestHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        return AppwriteManager.requestHistory.getThisMonthsJobHistory(limit, reverseOrder)
    }

    suspend fun getThisYearRequestHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        return AppwriteManager.requestHistory.getThisYearsJobHistory(limit, reverseOrder)
    }

    suspend fun getRequestHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        return AppwriteManager.requestHistory.getJobHistory(limit, reverseOrder)
    }

}