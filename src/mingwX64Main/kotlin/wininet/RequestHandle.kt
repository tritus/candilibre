package wininet

import platform.windows.HINTERNET

data class RequestHandle(
    val handle: HINTERNET
)