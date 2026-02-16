package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.logInfo
import com.vibedev.bluecollar.data.AppData

import io.appwrite.Client
import org.json.JSONObject
import java.lang.Exception
import io.appwrite.services.Functions

class FunctionsService(client: Client) {

    private val functions = Functions(client)
    private val tag = "FunctionService"

    suspend fun callMakeProvider(userId: String): Boolean {
        return try {
            val body = JSONObject()
                .put("userId", userId)
                .toString()

            val execution = functions.createExecution(
                functionId = AppData.MAKE_PROVIDER_FUNCTION_ID,
                body = body,
                async = true
            )

            val responseJson = JSONObject(execution.responseBody)
            if (responseJson.optBoolean("ok") && responseJson.optBoolean("added")) {
                val teamId = responseJson.optString("teamId")
                val membershipId = responseJson.optString("membershipId")
                logInfo(tag, "User successfully made a provider. TeamId: $teamId, MembershipId: $membershipId")
                true
            } else {
                logError(tag, "Failed to make provider. Response: ${execution.responseBody}")
                false
            }
        } catch (e: Exception) {
            logError(tag, "Error calling make provider function for user $userId", e)
            false
        }
    }

    suspend fun createRequestViaFunction(customerName: String, number: String, city: String, address: String, serviceDescription: String, serviceType: String, pay: String): Boolean {
        return try {
            val body = JSONObject()
                .put("name", customerName)
                .put("number", number)
                .put("city", city)
                .put("address", address)
                .put("description", serviceDescription)
                .put("serviceType", serviceType)
                .put("pay", pay)
                .toString()

            val execution = functions.createExecution(
                functionId = AppData.CREATE_JOB_REQUEST_FUNCTION_ID,
                body = body,
                async = true
            )

            if (execution.responseBody.isEmpty()) {
                logError(tag, "Failed to create request via function. Response: ${execution.responseBody}")
                return false
            }
            val responseJson = JSONObject(execution.responseBody)
            if (responseJson.optBoolean("ok") && responseJson.optBoolean("added")) {
                logInfo(tag, "Created request via function Successfully")
                true
            } else {
                logError(tag, "Failed to create request via function. Response: ${execution.responseBody}")
                false
            }
        } catch (e: Exception) {
            logError(tag, "Error calling create request function for job ", e)
            false
        }
    }

    suspend fun acceptJob(jobId: String): Boolean {
        return try {
            val body = JSONObject()
                .put("jobId", jobId)
                .toString()

            val execution = functions.createExecution(
                functionId = AppData.ACCEPT_JOB_FUNCTION_ID,
                body = body,
                async = true
            )

            val responseJson = JSONObject(execution.responseBody)
            if (responseJson.optBoolean("ok")) {
                logInfo(tag, "Accepted request via function Successfully")
                true
            } else {
                logError(tag, "Failed to accept request via function. Response: ${execution.responseBody}")
                false
            }


        } catch (e: Exception) {
            logError(tag, "Error calling accept request function for job $jobId", e)
            false
        }
    }
}