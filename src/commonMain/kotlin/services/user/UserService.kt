package services.user

import services.api.CandilibApi
import services.user.errors.MissingUserIdInServerResponseException

internal object UserService {
    suspend fun getUserId(token: String): String {
        val verificationResult = CandilibApi.verifyToken(token)
        return verificationResult.headers["X_USER_ID"]
            ?: throw MissingUserIdInServerResponseException("The user id was not found in the server response")
    }
}