import api.client.CandilibreClientBadTokenException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import logging.buildLogger
import services.BookingService
import ui.CandilbreUI

fun main() {
    var runningJob: Job? = null
    val ui = CandilbreUI()
    val logger = buildLogger()
    ui.show(
        { token, cities, minDate ->
            runningJob?.let {
                logger.log("Arrêt de la recherche en cours")
                it.cancel()
            }
            logger.log("Début de la recherche avec les parametres suivants :\n  token : $token,\n  date minimum : $minDate\n  villes : $cities")
            runningJob = GlobalScope.launch {
                try {
                    BookingService().tryBooking(logger, token, cities, minDate)
                } catch (e: CandilibreClientBadTokenException) {
                    logger.log("Votre lien Candilib est expiré ou n'a pas été correctement copié.")
                }
            }
        },
        logger
    )
    runningJob?.cancel()
}