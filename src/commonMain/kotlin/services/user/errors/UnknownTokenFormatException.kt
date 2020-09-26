package services.user.errors

class UnknownTokenFormatException(token: String, decodedToken: String) :
    Exception("Could not extract user id from token $decodedToken (raw : $token)")