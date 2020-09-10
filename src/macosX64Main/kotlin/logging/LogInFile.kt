package logging

import platform.posix.system

internal actual fun logInFile(log: String) {
    system("touch $LOGFILE_PATH")
    system("echo \"$log\" >> $LOGFILE_PATH")
}