import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import logging.Logger
import logging.buildLogger
import services.api.client.CandilibreClientBadTokenException
import services.booking.BookingService
import services.user.UserService
import services.user.errors.UnknownTokenFormatException
import ui.CandilbreUI

@ExperimentalCoroutinesApi
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
                    logBadToken(logger)
                } catch (e: UnknownTokenFormatException) {
                    logBadToken(logger)
                } catch (e: Throwable) {
                    logger.log("Oups, une erreur inconnue est arrivée : $e")
                }
            }
        },
        logger
    )
    runningJob?.cancel()
}

private fun logBadToken(logger: Logger) {
    logger.log("Votre lien Candilib est expiré ou n'a pas été correctement copié.")
}