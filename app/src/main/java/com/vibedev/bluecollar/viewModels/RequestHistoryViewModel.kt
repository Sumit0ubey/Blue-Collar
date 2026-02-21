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

    suspend fun getJobCountForToday(isProvider: Boolean = false) : Int {
        return if (isProvider){
            AppwriteManager.requestHistory.getJobAcceptedTodayNumberAsProvider()
        } else {
            AppwriteManager.requestHistory.getRequestPostedTodayNumberAsCustomer()
        }
    }

    suspend fun getJobCountForThisWeek(isProvider: Boolean = false) : Int {
        return if (isProvider) {
            AppwriteManager.requestHistory.getJobAcceptedThisWeekNumberAsProvider()
        } else {
            AppwriteManager.requestHistory.getRequestPostedThisWeekNumberAsCustomer()
        }
    }

    suspend fun getJobCountForThisMonth(isProvider: Boolean = false) : Int {
        return if (isProvider) {
            AppwriteManager.requestHistory.getJobAcceptedThisMonthNumberAsProvider()
        } else {
            AppwriteManager.requestHistory.getRequestPostedThisMonthNumberAsCustomer()
        }
    }

    suspend fun getJobCountForThisYear(isProvider: Boolean = false) : Int {
        return if (isProvider) {
            AppwriteManager.requestHistory.getJobAcceptedThisYearNumberAsProvider()
        } else {
            AppwriteManager.requestHistory.getRequestPostedThisYearNumberAsCustomer()
        }
    }

}