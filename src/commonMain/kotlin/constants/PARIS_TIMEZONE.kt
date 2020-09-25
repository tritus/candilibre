package constants

import kotlinx.datetime.TimeZone
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal val PARIS_TIMEZONE = TimeZone.of("Europe/Paris")