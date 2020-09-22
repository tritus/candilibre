package wininet

import platform.windows.HINTERNET

data class OpenHandle(
    val handle: HINTERNET
)