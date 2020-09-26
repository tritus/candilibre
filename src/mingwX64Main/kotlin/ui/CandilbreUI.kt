package ui

import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import libui.ktx.*
import logging.Logger
import kotlin.native.internal.valuesForEnum


class CandilbreUI {
    private val logSeparator = "\n\n"

    @ExperimentalCoroutinesApi
    fun show(
        onStartClicked: (String, List<City>, Instant) -> Unit,
        logger: Logger
    ) = appWindow(
        title = "Candilibre - Laissez votre ordinateur trouver votre place de permis !",
        width = 800,
        height = 600
    ) {
        hbox {
            stretchy = true

            vbox {
                stretchy = true
                val logTextArea = textarea {
                    stretchy = true
                    value = "Entrez votre lien de connexion, la date minimum et " +
                            "les villes dans lesquelles vous voulez rechercher des places, et appuyez " +
                            "sur \"Commencer à chercher\" !"
                }
                CoroutineScope(newSingleThreadContext("loggingObserver")).launch {
                    logger.read { log(logTextArea, it) }
                }
            }

            vbox {
                label("Copiez ici le lien de connexion envoyé par candilib :")
                val emailLink = textfield(false)

                separator()

                label("A partir de la date :")
                val minDatePicker = datepicker()

                separator()

                label("Dans les villes :")
                val cityCheckBoxes = City.values().map {
                    checkbox(it.serverName)
                }

                separator()

                button("Commencer à chercher") {
                    action {
                        val token = emailLink.value
                            .trim()
                            .split("https://beta.interieur.gouv.fr/candilib/candidat?token=")
                            .firstOrNull { it.isNotBlank() }
                        val cities = cityCheckBoxes
                            .filter { it.value }
                            .map { box -> City.values().find { box.label == it.serverName } }
                        val minDate = minDatePicker.textValue()
                            .split("/")
                            .let { LocalDateTime("20${it[2]}".toInt(), it[0].toInt(), it[1].toInt(), 0, 0, 0, 0) }
                            .toInstant(PARIS_TIMEZONE)

                        onStartClicked(token ?: "", cities.filterNotNull(), minDate)
                    }
                }
            }
        }
    }

    private fun log(logArea: TextArea, logString: String) {
        logArea.append(logSeparator)
        logArea.append(logString)
    }
}