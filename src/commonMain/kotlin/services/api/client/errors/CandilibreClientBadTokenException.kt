package services.api.client.errors

class CandilibreClientBadTokenException(token: String) : Exception("Bad token $token")