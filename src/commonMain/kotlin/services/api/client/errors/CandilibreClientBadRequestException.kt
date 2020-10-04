package services.api.client.errors

class CandilibreClientBadRequestException(url: String, cause: Throwable) :
    Exception("Error while performing request on $url", cause)