package ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import libui.ktx.*
import tools.newDispatcher
import ui.UIStrings.appTitle
import ui.UIStrings.citiesListTitle
import ui.UIStrings.magicLinkFieldTitle
import ui.UIStrings.minDatePickerTitle
import ui.UIStrings.startSearchButtonLabel
import ui.model.UIDepartmentData
import ui.model.UIParamsSectionData
import viewmodels.CandilibreViewModel

class CandilibreUI {
    private val viewModel = CandilibreViewModel()

    private val lifecycleScope = CoroutineScope(newDispatcher("viewModelObservation"))

    // Views
    private var logTextArea: TextArea? = null
    private var parametersBox: VBox? = null
    private var magicLinkTextField: TextField? = null
    private var cityCheckBoxes = mutableListOf<Checkbox>()

    fun start() = appWindow(appTitle, 800, 600) {
        createContent(viewModel.paramsSectionData)
        subscribeToViewModel()
    }

    private fun onViewWillQuit() {
        lifecycleScope.cancel()

        logTextArea = null
        parametersBox = null
        magicLinkTextField = null
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            launch {
                viewModel.loggingAreaContent.collect { message ->
                    withContext(Dispatchers.Main) { logTextArea?.append(message) }
                }
            }
        }
    }

    private fun Window.createContent(data: UIParamsSectionData) {
        hbox {
            stretchy = true

            vbox {
                stretchy = true
                logTextArea = textarea { stretchy = true }
            }

            parametersBox = vbox {
                label(magicLinkFieldTitle)
                magicLinkTextField = textfield(false)

                createParamsSection(data)
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
                checkbox(cityLabel).also { cityCheckBoxes.add(it) }
            }
        }
    }
}