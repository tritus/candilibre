package ui

object UIStrings {
    const val appTitle = "Candilibre - Laissez votre ordinateur trouver votre place de permis !"
    const val logAreaInitialText = "Entrez votre lien de connexion, la date minimum et " +
            "les villes dans lesquelles vous voulez rechercher des places, et appuyez " +
            "sur \"Commencer à chercher\" !"
    const val magicLinkFieldTitle = "Copiez ici le lien de connexion envoyé par candilib :"
    const val magicLinkValidateLabel = "Go"
    const val minDatePickerTitle = "A partir de la date :"
    const val citiesListTitle = "Dans les villes :"
    const val startSearchButtonLabel = "Commencer à chercher"
    const val candilibTokenErrorLabel = "Votre lien Candilib est expiré ou n'a pas été correctement copié."
    const val userIdExtractionFromTokenErrorLabel = "Votre lien Candilib n'a pas été correctement copié."
    const val stoppingCurrentJobLabel = "Arrêt de la recherche en cours"
    fun startSearchJobMessage(minDate: String, cities: String) =
        "Début de la recherche avec les parametres suivants :\n  date minimum : $minDate\n  villes : $cities"

    fun unknownErrorLabel(error: String) = "Oups, une erreur inconnue est arrivée : $error"
}