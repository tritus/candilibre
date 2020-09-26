package services.user

import services.user.errors.UnknownTokenFormatException
import tools.Base64.base64decoded

object UserService {
    fun getUserId(token: String): String {
        val decodedToken = token.base64decoded
        return Regex("\"id\":\"([^\"]*)\"")
            .find(decodedToken)
            ?.groups
            ?.get(1)
            ?.value
            ?: throw UnknownTokenFormatException(token, decodedToken)
    }
}