package wininet

import wininet.tools.AdaptiveBuffer
import kotlinx.cinterop.*
import platform.windows.*
import kotlin.native.internal.NativePtr

object WinINetHelper {


    /**
     * Open the internet connection and prepare it for connecting.
     * We don't solve proxies and other stuff, just open the basic connection.
     */
    private fun MemScope.openInternet(name: String): OpenHandle {
        val hOpen = InternetOpenW(
            name,
            INTERNET_OPEN_TYPE_PRECONFIG_WITH_NO_AUTOPROXY.dword(),
            null,
            null,
            0.dword()
        )
        if (hOpen == null || hOpen.rawValue == NativePtr.NULL) {
            error("The internet connection cannot be opened.")
        }
        return OpenHandle(hOpen)
    }


    /**
     * Connect to specified domain and port using HTTP connection.
     */
    private fun MemScope.connectInternet(openHandle: OpenHandle, domain: String, port: Int): ConnectHandle {
        val hConnect = InternetConnectW(
            openHandle.handle,
            domain,
            port.toUShort(),
            null,
            null,
            INTERNET_SERVICE_HTTP.dword(),
            0.dword(),
            0.toULong()
        )
        if (hConnect == null || hConnect.rawValue == NativePtr.NULL) {
            error("The internet connection cannot be connected: ${GetLastError()}")
        }
        return ConnectHandle(hConnect)
    }


    /**
     *  Configure and open the request and prepare headers.
     */
    private fun MemScope.openRequest(
        connectHandle: ConnectHandle,
        path: String,
        method: String,
        secure: Boolean,
        headers: Map<String, String>
    ): RequestHandle {

        // Configure the request parameters and open it.
        val extraFlags = INTERNET_FLAG_IGNORE_REDIRECT_TO_HTTP or
                INTERNET_FLAG_IGNORE_REDIRECT_TO_HTTPS or
                INTERNET_FLAG_NO_UI or
                INTERNET_FLAG_NO_CACHE_WRITE or
                (if (secure) INTERNET_FLAG_SECURE else 0)

        val request = HttpOpenRequestW(
            connectHandle.handle,
            method,
            path,
            null,
            null,
            null,
            extraFlags.dword(),
            1.toULong()
        )

        if (request == null || request.rawValue == NativePtr.NULL) {
            error("The request cannot be opened: ${GetLastError()}")
        }

        // Apply timeouts.
        val timeout = alloc<DWORDVar>()
        timeout.value = 60000.dword()
        InternetSetOptionW(request, INTERNET_OPTION_CONNECT_TIMEOUT, timeout.ptr, sizeOf<DWORDVar>().dword())
        InternetSetOptionW(request, INTERNET_OPTION_SEND_TIMEOUT, timeout.ptr, sizeOf<DWORDVar>().dword())
        InternetSetOptionW(request, INTERNET_OPTION_RECEIVE_TIMEOUT, timeout.ptr, sizeOf<DWORDVar>().dword())

        // Add headers.
        var success = true
        headers.entries.forEach {
            val headerStr = "${it.key}: ${it.value}"
            if (HttpAddRequestHeadersW(
                    request,
                    headerStr,
                    headerStr.length.dword(),
                    (HTTP_ADDREQ_FLAG_ADD or HTTP_ADDREQ_FLAG_REPLACE.toInt()).dword()
                ) == 0
            ) success = false
        }

        if (!success) {
            error("Headers cannot be added: ${GetLastError()}")
        }

        return RequestHandle(request)
    }


    /**
     * Send the request to server along with arbitrary POST data.
     */
    private fun MemScope.sendRequest(requestHandle: RequestHandle, data: ByteArray? = null) {
        if (HttpSendRequestW(
                requestHandle.handle,
                null,
                0.dword(),
                data?.pin()?.addressOf(0),
                (data?.size ?: 0).dword()
            ) == 0
        ) {
            error("The request cannot be sent: ${GetLastError()}")
        }
    }


    /**
     * Read the HTTP response code.
     */
    private fun MemScope.getResponseCode(requestHandle: RequestHandle): Int {
        val data = alloc<DWORDVar>()
        val dataSize = alloc<DWORDVar>()
        dataSize.value = sizeOf<DWORDVar>().dword()

        if (HttpQueryInfoW(
                requestHandle.handle,
                (HTTP_QUERY_FLAG_NUMBER or HTTP_QUERY_STATUS_CODE).dword(),
                data.ptr,
                dataSize.ptr,
                null
            ) == 0
        ) {
            error("HTTP response code cannot be read: ${GetLastError()}")
        }

        return data.value.toInt()
    }

    /**
     * Read response headers
     */
    private fun MemScope.getResponseHeaders(requestHandle: RequestHandle): ByteArray {
        val dataSize = alloc<DWORDVar>()
        dataSize.value = 0u

        if (HttpQueryInfoW(
                requestHandle.handle,
                HTTP_QUERY_RAW_HEADERS_CRLF.dword(),
                null,
                dataSize.ptr,
                null
            ) == 0
        ) {
            if (GetLastError() == ERROR_INSUFFICIENT_BUFFER.toUInt()) {
                // There are headers so we allocate a ByteArray of proper size to read it.
                val data = ByteArray(dataSize.value.toInt())
                data.usePinned {
                    HttpQueryInfoW(
                        requestHandle.handle,
                        HTTP_QUERY_RAW_HEADERS_CRLF.dword(),
                        it.addressOf(0),
                        dataSize.ptr,
                        null
                    )
                }
                return data
            } else {
                error("HTTP headers cannot be read: ${GetLastError()}")
            }
        } else {
            // There was no header
            return ByteArray(0)
        }
    }

    /**
     * Read the whole response data.
     */
    private fun MemScope.getResponseData(requestHandle: RequestHandle): ByteArray {
        val dwFileSize = 16384 // 16kB reading buffer
        val buffer = ByteArray(dwFileSize)
        val outputBuffer = AdaptiveBuffer()

        while (true) {

            val dwBytesRead = alloc<DWORDVar>()

            val bRead = buffer.usePinned {
                InternetReadFile(
                    requestHandle.handle,
                    it.addressOf(0),
                    dwFileSize.dword(),
                    dwBytesRead.ptr
                )
            }

            if (dwBytesRead.value.toInt() == 0) break

            if (bRead == 0) {
                error("Cannot read received data: ${GetLastError()}")
            }

            outputBuffer.write(buffer, 0, dwBytesRead.value.toInt())
        }

        return outputBuffer.toByteArray()
    }


    fun <T : Any?> request(
        url: String,
        method: String,
        headers: Map<String, String>,
        postData: ByteArray?,
        callback: (responseCode: Int, responseHeaders: ByteArray, responseData: ByteArray) -> T
    ): T {

        // Parse URL to get domain, port and path.
        val protocol = url.substringBefore("://").toLowerCase()
        if (protocol.isBlank()) error("Missing protocol in URL.")

        val noProtocol = url.substringAfter("://")
        val domainAndPort = noProtocol.substringBefore("/")

        val port = if (domainAndPort.contains(":")) {
            domainAndPort.substringAfter(":").toIntOrNull() ?: error("Port cannot be converted to number.")
        } else {
            if (protocol == "http") 80 else if (protocol == "https") 443 else error("Unknown protocol: $protocol")
        }

        val domain = domainAndPort.substringBefore(":")
        val path = "/" + noProtocol.substringAfter("/", "")
        val secure = protocol == "https"


        // Perform the request.
        val response = memScoped {
            var openHandle: OpenHandle? = null
            var connectHandle: ConnectHandle? = null
            var requestHandle: RequestHandle? = null

            try {

                openHandle = openInternet("Localazy")
                connectHandle = connectInternet(openHandle, domain, port)
                requestHandle = openRequest(connectHandle, path, method, secure, headers)
                sendRequest(requestHandle, postData)

                // Read response code and data.
                val responseCode = getResponseCode(requestHandle)
                val responseHeaders = getResponseHeaders(requestHandle)
                val responseData = getResponseData(requestHandle)

                Response(
                    code = responseCode,
                    headers = responseHeaders,
                    data = responseData
                )
            } finally {
                // Close all used handles.
                requestHandle?.let { InternetCloseHandle(it.handle) }
                connectHandle?.let { InternetCloseHandle(it.handle) }
                openHandle?.let { InternetCloseHandle(it.handle) }
            }
        }

        // Invoke callback for the received data.
        return callback(response.code, response.headers, response.data)
    }

}