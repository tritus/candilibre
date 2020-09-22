package wininet

import platform.windows.HINTERNET

data class ConnectHandle(
    val handle: HINTERNET
)

