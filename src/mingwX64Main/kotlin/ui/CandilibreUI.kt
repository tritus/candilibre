package ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import libui.ktx.*
import ui.UIStrings.appTitle
import ui.UIStrings.citiesListTitle
import ui.UIStrings.logAreaInitialText
import ui.UIStrings.magicLinkFieldTitle
import ui.UIStrings.magicLinkValidateLabel
import ui.UIStrings.minDatePickerTitle
import ui.UIStrings.startSearchButtonLabel
import viewmodels.CandilibreViewModel
import ui.model.UIDepartmentData
import ui.model.UIParamsSectionData

class CandilibreUI {
    private val viewModel = CandilibreViewModel()

    private var viewModelSubscriptions: Job? = null

    // Views
    private var logTextArea: TextArea? = null
    private var parametersBox: VBox? = null
    private var magicLinkTextField: TextField? = null
    private var magicLinkValidateButton: Button? = null
    private var cityCheckBoxes = emptyList<Checkbox>()

    fun start() = appWindow(appTitle, 800, 600) {
        createContent()
        subscribeToViewModel()
    }

    private fun onViewWillQuit() {
        viewModelSubscriptions?.cancel()

        logTextArea = null
        parametersBox = null
        magicLinkTextField = null
        magicLinkValidateButton = null
    }

    private fun subscribeToViewModel() {
        viewModelSubscriptions?.cancel()
        viewModelSubscriptions = CoroutineScope(newSingleThreadContext("viewModelObservation")).launch {
            launch {
                viewModel.loggingAreaContent.collect { message ->
                    withContext(Dispatchers.Default) { logTextArea?.append(message) }
                }
            }
            //launch {
            //    viewModel.magicLinkAreaIsEditable.collect { isEditable ->
            //        magicLinkTextField?.readonly = !isEditable
            //        magicLinkValidateButton?.apply { if (isEditable) enable() else disable() }
            //    }
            //}
            //launch { viewModel.paramsSectionData.collect { parametersBox?.createParamsSection(it) } }
        }
    }

    private fun Window.createContent() {
        hbox {
            stretchy = true

            vbox {
                stretchy = true
                logTextArea = textarea { stretchy = true }
            }

            parametersBox = vbox {
                label(magicLinkFieldTitle)
                hbox {
                    magicLinkTextField = textfield(false) { stretchy = true }
                    magicLinkValidateButton = button(magicLinkValidateLabel) {
                        action {
                            viewModel.onValidateMagicLinkClicked(magicLinkTextField?.value ?: "")
                        }
                    }
                }
            }
        }

        onShouldQuit {
            onViewWillQuit()
            true
        }
    }

    private fun VBox.createParamsSection(data: UIParamsSectionData) {
        separator()

        label(minDatePickerTitle)
        val minDatePicker = datepicker()

        separator()

        createCitiesSection(data)

        separator()

        button(startSearchButtonLabel) {
            action {
                viewModel.onStartSearchClicked(
                    magicLinkTextField?.value ?: "",
                    cityCheckBoxes.filter { it.value }.map { it.label },
                    minDatePicker.textValue()
                )
            }
        }
    }

    private fun VBox.createCitiesSection(data: UIParamsSectionData) {
        label(citiesListTitle)
        hbox {
            data.departments.map { departmentData ->
                createDepartmentSection(departmentData)
            }
        }
    }

    private fun HBox.createDepartmentSection(departmentData: UIDepartmentData) {
        vbox {
            label(departmentData.departmentLabel)
            departmentData.citiesLabels.forEach { cityLabel ->
                checkbox(cityLabel).also { cityCheckBoxes += it }
            }
        }
    }
}