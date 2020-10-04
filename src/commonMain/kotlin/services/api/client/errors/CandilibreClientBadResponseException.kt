package services.api.client.errors

class CandilibreClientBadResponseException(code: Int, url: String, body: String) :
    Exception("BAD RESPONSE $code from $url : $body")