package com.somed.dkaassistant

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnticipateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.somed.dkaassistant.databinding.FragmentMainBinding
import androidx.core.content.ContextCompat
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil.setContentView

open class MainFragment : Fragment() {

    //region Variables Declaration

    private lateinit var binding: FragmentMainBinding

    private lateinit var locallyStoredAppSettingsFile: SharedPreferences

    //region Variables For Sliding Open Acid Base Calculator Panel and Swipe Down Whole Panel to Refresh
    private var isAcidBaseCalculatorShown = false //
    private var defaultCalculatorY = 0f // Default y position of calculator panel
    private var currentCalculatorY = 0f // Current y position of calculator panel
    private var deltaYCalculator = 0f // Delta y position of calculator panel
    private var firstDownTouchY = 0f // Reference First Touch Point When User Starts to Swipe
    private var currentlyCardViewDisplacedDistance = 0f // Slided Distance
    private var cancellingReset = false
    private var trackingDeltaY = 0f
    private var trackingY = 0f
    private var alreadySymbolRotatedLeft = false
    private var alreadySymbolRotatedRight = false
    private var lastAttemptToSwipe = ""
    private var lastDeltaSwipeDownY = 1f
    //endregion

    val patient: Patient = Patient()

    //region Patient Input Data
    private lateinit var iAge: String // Track user text input for age
    //private var age = 0 // Internal App calculations uses years

    private lateinit var iWeight: String

    //private var weight = 0.0 // Internal App calculations uses kg
    private var isWeightKg = true

    private lateinit var iRBS: String

    //private var rbs = 0.0 // Internal App calculations uses mg/dL
    private var isRBSMg = true

    private lateinit var iPhosphorus: String

    //private var phosphorus = 0.0 // Internal App calculations uses mg/dL
    private var isPhosphorusMg = false
    
    
    private lateinit var iCalcium: String

    //private var phosphorus = 0.0 // Internal App calculations uses mg/dL
    private var isCalciumMg = false

    private lateinit var iMagnesium: String

    //private var magnesium = 0.0 // Internal App calculations uses mg/dL
    private var isMagnesiumMg = false

    private lateinit var iPotassium: String
    //private var potassium = 0.0 // Internal App calculations uses mmol/L

    private lateinit var iSodium: String
    //private var sodium = 0 // Internal App calculations uses mmol/L

    private lateinit var iChloride: String
    //private var chloride = 0 // Internal App calculations uses mmol/L

    private lateinit var iAlbumin: String

    //private var albumin = 0.0 // Internal App calculation uses g/dL
    private var isAlbuminGDL = true

    private lateinit var ipH: String
    //private var pH = 0.0

    private lateinit var ipCO2: String
    //private var pCO2 = 0.0 // Internal App calculation uses mmHg
    //private var ispCO2kPa = false

    private lateinit var icHCO3: String
    //private var cHCO3 = 0.0 // Internal App calculation uses mmol/L

    //private var adolescentDehydrationPercentage = 0

    //endregion

    //region Validation Variables
    private var isValidAge = true
    private var lastPotentialAge: String = ""

    private var isValidWeight = true
    private var lastPotentialWeight: String = ""
    private var isWgtBeingConverted = false

    private var isValidRBS = true
    private var lastPotentialRBS: String = ""
    private var isRBSBeingConverted = false

    private var isValidPhosphorus = true
    private var lastPotentialPhosphorus: String = ""
    private var isPhosphorusBeingConverted = false
    
    private var isValidCalcium = true
    private var lastPotentialCalcium: String = ""
    private var isCalciumBeingConverted = false

    private var isValidMagnesium = true
    private var lastPotentialMagnesium: String = ""
    private var isMagnesiumBeingConverted = false

    private var isValidPotassium = true
    private var lastPotentialPotassium: String = ""

    private var isValidSodium = true
    private var lastPotentialSodium: String = ""

    private var isValidChloride = true
    private var lastPotentialChloride: String = ""

    private var isValidAlbumin = true
    private var lastPotentialAlbumin: String = ""
    private var isAlbuminBeingConverted = false

    private var isValidpH = true
    private var lastPotentialpH: String = ""

    private var isValidpCO2 = true
    private var lastPotentialpCO2: String = ""
    private var ispCO2BeingConverted = false

    //private var isValidcHCO3 = true
    private var lastPotentialcHCO3: String = ""

    var invalidRxToBeginWith = "^(0|[.])$"
    var invalidRxToBeginWith2 = "^[.]$"

    // Used to Hold Valid RegEx Pattern to Validate the String According Its Characters Length (i.e., Characters Content)
    private var vSL1 = ""
    private var vSL2 = ""
    private var vSL3 = ""
    private var vSL4 = ""
    private var vSL5 = ""

    private var errorInputBackgroundColor = Color.parseColor("#FFE8E0")
    private var defaultInputBackgroundColor = Color.parseColor("#F0F0F0")
    //endregion

    private var dayOneResultPanelClosed = true
    private var dayTwoResultPanelClosed = true
    private var dayThreeResultPanelClosed = true

    private var dpOfDevice = 0f
    var isDayPlanFrozen = false
    private var activePlanColor = 0
    private var inactivePlanColor = 0
    private lateinit var audioManager: AudioManager
    //endregion

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        setAppSettings()

        setCalculatorPanelDynamicAnimations()

        setTextInputsDynamicsToGetData()

        setResultPanelDynamics()





        return binding.root
    }


    @SuppressLint("SetTextI18n")
    fun displayManagement() {

        patient.getABGDx()
        patient.getDKAManagement()

        binding.resusNote.text = highlightManagement(patient.resusNote)

        if (patient.insulinRate != "") {
            binding.tvInsulin.text = highlightManagement(patient.insulinRate)
        } else {
            binding.tvInsulin.text = ""
        }

        if (patient.typeOfSaline != "") {
            binding.tvIVInfusion.text = highlightManagement(patient.typeOfSaline)
            if (patient.typeOfDextrose != "") {
                binding.tvIVInfusion.text =
                    highlightManagement(patient.typeOfSaline + "\n" + patient.typeOfDextrose)
            }
        } else {
            binding.tvIVInfusion.text = ""
        }

        if (patient.abgDx != "") {
            binding.tvABGDx.text = highlightABGDx(
                "\n cHCO₃: ${"%.2f".format(patient.cHCO3)} | AG: ${"%.2f".format(patient.cAG)} | Δ/Δ: ${
                    "%.2f".format(
                        patient.deltaRatio
                    )
                } | Δ-Δ: ${"%.2f".format(patient.deltaGap)} \n\n ${patient.abgDx}\n"
            )
        } else {
            binding.tvABGDx.text = ""
        }


    }

    private fun iToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setTextInputsDynamicsToGetData() {
        // Set event to detect and store the Age value
        binding.ageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iAge = s.toString()

                if (iAge.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        if (isValidated("Age", iAge)) {
                            lastPotentialAge = iAge

                            if (iAge.toInt() in 14..119) {
                                patient.age = iAge.toInt()
                                isValidAge = true
                                
                                locallyStoredAppSettingsFile.edit()
                        .putString("patientAge", s.toString())
                        .apply()

                            } else {
                                // Set Error
                                isValidAge = false
                            }
                        } else {
                            if (iAge.matches(invalidRxToBeginWith.toRegex())) {
                                binding.ageInput.setText("")
                            } else {
                                binding.ageInput.setText(lastPotentialAge)
                                binding.ageInput.setSelection(iAge.length) // Move cursor to the end
                            }

                            isValidAge = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.age = 0
                    lastPotentialAge = ""
                    isValidAge = true
                    
                    locallyStoredAppSettingsFile.edit()
                        .putString("patientAge", s.toString())
                        .apply()
                }
                displayManagement()
            }
        })


        binding.ageInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidAge) {
                    binding.ageLabel.setTextColor(Color.parseColor("#D41212"))
                    binding.ageLabel.typeface = ResourcesCompat.getFont(requireContext(), R.font.made_tommy_bold)
                } else {
                    binding.ageLabel.setTextColor(Color.parseColor("#AEABAB"))
                }
            } else {
                binding.ageLabel.setTextColor(Color.parseColor("#AEABAB"))
            }
        }


        binding.weightInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iWeight = s.toString()

                if (iWeight.isNotEmpty() && !isWgtBeingConverted) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("Weight", iWeight)) {
                            lastPotentialWeight = iWeight

                            if (isWeightKg && iWeight.toDouble() in 20.0..300.0) {
                                patient.weight = iWeight.toDouble()
                                isValidWeight = true
                                
                                locallyStoredAppSettingsFile.edit()
                        .putString("patientWeight", s.toString())
                        .apply()
                            } else if (!isWeightKg && iWeight.toDouble() in 44.0..650.0) {
                                patient.weight =
                                    iWeight.toDouble().div(2.20462) // Convert lbs to kg
                                isValidWeight = true
                                
                                locallyStoredAppSettingsFile.edit()
                        .putString("patientWeight", s.toString())
                        .apply()
                            } else {
                                // Set Error
                                isValidWeight = false
                            }
                        } else {
                            if (iWeight.matches(invalidRxToBeginWith.toRegex())) {
                                lastPotentialWeight = ""
                                binding.weightInput.setText(lastPotentialWeight)
                            } else {
                                binding.weightInput.setText(lastPotentialWeight)
                                binding.weightInput.setSelection(iWeight.length) // Move cursor to the end
                            }
                            isValidWeight = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iWeight.isEmpty() && !isWgtBeingConverted) {
                    patient.weight = 0.0
                    lastPotentialWeight = ""
                    isValidWeight = true
                    
                    locallyStoredAppSettingsFile.edit()
                        .putString("patientWeight", "")
                        .apply()
                } else {
                    isWgtBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.weightLabel.setOnLongClickListener {

            isWgtBeingConverted = true
            // Toggle the unit
            isWeightKg = !isWeightKg

            locallyStoredAppSettingsFile.edit()
                .putBoolean("isWeightKg", isWeightKg)
                .apply()

            // Update the hint and suffix text based on the unit
            if (isWeightKg) {
                binding.weightLabel.text = "Kg"
            } else {
                binding.weightLabel.text = "lbs"
            }

            // Convert the entered weight to the new unit
            val enteredWeight = binding.weightInput.text.toString().toDoubleOrNull()
            val convertedWeight = if (isWeightKg) {
                enteredWeight?.div(2.20462)
            } else {
                enteredWeight?.times(2.20462)
            }

            // Update the text in the input field
            if (convertedWeight != null) {
                binding.weightInput.setText(String.format("%.1f", convertedWeight))
            }

            binding.weightInput.setSelection(binding.weightInput.text?.length ?: 0)


            true
        }
        /*
        binding.weightInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidWeight) {
                    binding.weightTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.weightTIL.hint = "Weight ⚠"
                } else {
                    binding.weightTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.weightTIL.hint = "Weight"
                }
            } else {
                binding.weightTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.weightTIL.hint = "Weight"
            }
        }
*/
        binding.rbsInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iRBS = s.toString()

                if (iRBS.isNotEmpty()) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("RBS", iRBS) && !isRBSBeingConverted) {
                            lastPotentialRBS = iRBS

                            if (isRBSMg && iRBS.toDouble() in 10.0..1200.0) {
                                patient.rbs = iRBS.toDouble()
                                isValidRBS = true
                            } else if (!isRBSMg && iRBS.toDouble() in 0.8..66.0) {
                                patient.rbs =
                                    iRBS.toDouble().times(18.0182) // Convert mmol/L to mg/dL
                                isValidRBS = true
                            } else {
                                // Set Error
                                isValidRBS = false
                            }
                        } else {
                            if (isRBSMg && iRBS.matches(invalidRxToBeginWith.toRegex())) {
                                lastPotentialRBS = ""
                                binding.rbsInput.setText(lastPotentialRBS)
                            } else {
                                binding.rbsInput.setText(lastPotentialRBS)
                                binding.rbsInput.setSelection(iRBS.length) // Move cursor to the end
                            }
                            isValidRBS = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iRBS.isEmpty() && !isRBSBeingConverted) {
                    patient.rbs = 0.0
                    lastPotentialRBS = ""
                    isValidRBS = true
                } else {
                    isRBSBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.rbsInput.setOnLongClickListener {
            if (binding.rbsInput.hasFocus()) {

                // Toggle the unit
                isRBSMg = !isRBSMg

                locallyStoredAppSettingsFile.edit()
                    .putBoolean("isRBSMg", isRBSMg)
                    .apply()

                // Update the hint and suffix text based on the unit
                if (isRBSMg) {
                    binding.rbsTIL.suffixText = "mg/dL"
                } else {
                    binding.rbsTIL.suffixText = "mmol/L"
                }

                // Convert the entered RBS to the new unit
                val enteredRBS = binding.rbsInput.text.toString().toDoubleOrNull()
                val convertedRBS = if (isRBSMg) {
                    enteredRBS?.times(18.0182)
                } else {
                    enteredRBS?.div(18.0182)
                }

                // Update the text in the input field
                if (convertedRBS != null) {
                    if (isRBSMg) {
                        binding.rbsInput.setText(convertedRBS.toInt().toString())
                    } else {
                        binding.rbsInput.setText(String.format("%.1f", convertedRBS))
                    }
                }

                binding.rbsInput.setSelection(binding.rbsInput.text?.length ?: 0)

            }
            true
        }
        binding.rbsInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidRBS) {
                    binding.rbsTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.rbsTIL.hint = "RBS ⚠"
                } else {
                    binding.rbsTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.rbsTIL.hint = "RBS"
                }
            } else {
                binding.rbsTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.rbsTIL.hint = "RBS"
            }
        }

        binding.po4Input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iPhosphorus = s.toString()

                if (iPhosphorus.isNotEmpty()) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("Phosphorus", iPhosphorus) && !isPhosphorusBeingConverted) {
                            lastPotentialPhosphorus = iPhosphorus

                            if (isPhosphorusMg && iPhosphorus.toDouble() in 0.1..15.0) {
                                patient.phosphorus = iPhosphorus.toDouble()
                                isValidPhosphorus = true
                            } else if (!isPhosphorusMg && iPhosphorus.toDouble() in 0.05..5.0) {
                                patient.phosphorus =
                                    iPhosphorus.toDouble().div(0.3229) // Convert mmol/L to mg/dL
                                isValidPhosphorus = true
                            } else {
                                // Set Error
                                isValidPhosphorus = false
                            }
                        } else {
                            if (isPhosphorusMg && iPhosphorus.matches(invalidRxToBeginWith2.toRegex())) {
                                lastPotentialPhosphorus = ""
                                binding.po4Input.setText(lastPotentialPhosphorus)
                            } else {
                                binding.po4Input.setText(lastPotentialPhosphorus)
                                binding.po4Input.setSelection(iPhosphorus.length) // Move cursor to the end
                            }
                            isValidPhosphorus = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iPhosphorus.isEmpty() && !isPhosphorusBeingConverted) {
                    patient.phosphorus = 0.0
                    lastPotentialPhosphorus = ""
                    isValidPhosphorus = true
                } else {
                    isPhosphorusBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.po4Input.setOnLongClickListener {
            if (binding.po4Input.hasFocus()) {

                // Toggle the unit
                isPhosphorusMg = !isPhosphorusMg


                locallyStoredAppSettingsFile.edit()
                    .putBoolean("isPhosphorusMg", isPhosphorusMg)
                    .apply()
                // Update the hint and suffix text based on the unit
                if (isPhosphorusMg) {
                    binding.po4TIL.suffixText = "mg/dL"
                } else {
                    binding.po4TIL.suffixText = "mmol/L"
                }

                // Convert the entered PO4 to the new unit
                val enteredPO4 = binding.po4Input.text.toString().toDoubleOrNull()
                val convertedPO4 = if (isPhosphorusMg) {
                    enteredPO4?.div(0.3229)
                } else {
                    enteredPO4?.times(0.3229)
                }

                // Update the text in the input field
                if (convertedPO4 != null) {
                    if (isPhosphorusMg) {
                        binding.po4Input.setText(String.format("%.2f", convertedPO4))
                    } else {
                        binding.po4Input.setText(String.format("%.2f", convertedPO4))
                    }
                }
                binding.po4Input.setSelection(binding.po4Input.text?.length ?: 0)
            }
            true
        }
        binding.po4Input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidPhosphorus) {
                    binding.po4TIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.po4TIL.hint = "PO₄³⁻ ⚠"
                } else {
                    binding.po4TIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.po4TIL.hint = "PO₄³⁻"
                }
            } else {
                binding.po4TIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.po4TIL.hint = "PO₄³⁻"
            }
        }

        binding.mgInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iMagnesium = s.toString()

                if (iMagnesium.isNotEmpty()) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("Magnesium", iMagnesium) && !isMagnesiumBeingConverted) {
                            lastPotentialMagnesium = iMagnesium

                            if (isMagnesiumMg && iMagnesium.toDouble() in 0.1..20.0) {
                                patient.magnesium = iMagnesium.toDouble()
                                isValidMagnesium = true
                            } else if (!isMagnesiumMg && iMagnesium.toDouble() in 0.1..10.0) {
                                patient.magnesium =
                                    iMagnesium.toDouble().div(0.41152) // Convert mmol/L to mg/dL
                                isValidMagnesium = true
                            } else {
                                // Set Error
                                isValidMagnesium = false
                            }
                        } else {
                            if (isMagnesiumMg && iMagnesium.matches(invalidRxToBeginWith2.toRegex())) {
                                lastPotentialMagnesium = ""
                                binding.mgInput.setText(lastPotentialMagnesium)
                            } else {
                                binding.mgInput.setText(lastPotentialMagnesium)
                                binding.mgInput.setSelection(iMagnesium.length) // Move cursor to the end
                            }
                            isValidMagnesium = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iMagnesium.isEmpty() && !isMagnesiumBeingConverted) {
                    patient.magnesium = 0.0
                    lastPotentialMagnesium = ""
                    isValidMagnesium = true
                } else {
                    isMagnesiumBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.mgInput.setOnLongClickListener {
            if (binding.mgInput.hasFocus()) {

                // Toggle the unit
                isMagnesiumMg = !isMagnesiumMg


                locallyStoredAppSettingsFile.edit()
                    .putBoolean("isMagnesiumMg", isMagnesiumMg)
                    .apply()

                // Update the hint and suffix text based on the unit
                if (isMagnesiumMg) {
                    binding.mgTIL.suffixText = "mg/dL"
                } else {
                    binding.mgTIL.suffixText = "mmol/L"
                }

                // Convert the entered PO4 to the new unit
                val enteredMg = binding.mgInput.text.toString().toDoubleOrNull()
                val convertedMg = if (isMagnesiumMg) {
                    enteredMg?.div(0.41152)
                } else {
                    enteredMg?.times(0.41152)
                }

                // Update the text in the input field
                if (convertedMg != null) {
                    if (isMagnesiumMg) {
                        binding.mgInput.setText(String.format("%.2f", convertedMg))
                    } else {
                        binding.mgInput.setText(String.format("%.2f", convertedMg))
                    }
                }

                binding.mgInput.setSelection(binding.mgInput.text?.length ?: 0)

            }
            true
        }
        binding.mgInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidMagnesium) {
                    binding.mgTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.mgTIL.hint = "Mg²⁺ ⚠"
                } else {
                    binding.mgTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.mgTIL.hint = "Mg²⁺"
                }
            } else {
                binding.mgTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.mgTIL.hint = "Mg²⁺"
            }
        }
        
        
        
        
        binding.calInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iCalcium = s.toString()

                if (iCalcium.isNotEmpty()) { // If Calcium field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("Calcium", iCalcium) && !isCalciumBeingConverted) {
                            lastPotentialCalcium = iCalcium

                            if (isCalciumMg && iCalcium.toDouble() in 1.0..25.0) {
                                patient.calcium = iCalcium.toDouble()
                                isValidCalcium = true
                            } else if (!isCalciumMg && iCalcium.toDouble() in 0.05..5.0) {
                                patient.calcium =
                                    iCalcium.toDouble().div(0.25) // Convert mmol/L to mg/dL
                                isValidCalcium = true
                            } else {
                                // Set Error
                                isValidCalcium = false
                            }
                        } else {
                            if (isCalciumMg && iCalcium.matches(invalidRxToBeginWith2.toRegex())) {
                                lastPotentialCalcium = ""
                                binding.calInput.setText(lastPotentialCalcium)
                            } else {
                                binding.calInput.setText(lastPotentialCalcium)
                                binding.calInput.setSelection(iCalcium.length) // Move cursor to the end
                            }
                            isValidCalcium = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iCalcium.isEmpty() && !isCalciumBeingConverted) {
                    patient.calcium = 0.0
                    lastPotentialCalcium = ""
                    isValidCalcium = true
                } else {
                    isCalciumBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.calInput.setOnLongClickListener {
            if (binding.calInput.hasFocus()) {

                // Toggle the unit
                isCalciumMg = !isCalciumMg


                locallyStoredAppSettingsFile.edit()
                    .putBoolean("isCalciumMg", isCalciumMg)
                    .apply()
                // Update the hint and suffix text based on the unit
                if (isCalciumMg) {
                    binding.calTIL.suffixText = "mg/dL"
                } else {
                    binding.calTIL.suffixText = "mmol/L"
                }

                // Convert the entered PO4 to the new unit
                val enteredCal = binding.calInput.text.toString().toDoubleOrNull()
                val convertedCal = if (isCalciumMg) {
                    enteredCal?.div(0.25)
                } else {
                    enteredCal?.times(0.25)
                }

                // Update the text in the input field
                if (convertedCal != null) {
                    if (isCalciumMg) {
                        binding.calInput.setText(String.format("%.2f", convertedCal))
                    } else {
                        binding.calInput.setText(String.format("%.2f", convertedCal))
                    }
                }
                binding.calInput.setSelection(binding.calInput.text?.length ?: 0)
            }
            true
        }
        
        binding.calInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidCalcium) {
                    binding.calTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.calTIL.hint = "Ca²⁺ ⚠"
                } else {
                    binding.calTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.calTIL.hint = "Ca²⁺"
                }
            } else {
                binding.calTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.calTIL.hint = "Ca²⁺"
            }
        }
        
        binding.potInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iPotassium = s.toString()

                if (iPotassium.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        if (isValidated("Potassium", iPotassium)) {
                            lastPotentialPotassium = iPotassium

                            if (iPotassium.toDouble() in 0.1..15.0) {
                                patient.potassium = iPotassium.toDouble()
                                isValidPotassium = true
                            } else {
                                // Set Error
                                isValidPotassium = false
                            }
                        } else {
                            if (iPotassium.matches(invalidRxToBeginWith2.toRegex())) {
                                binding.potInput.setText("")
                            } else {
                                binding.potInput.setText(lastPotentialPotassium)
                                binding.potInput.setSelection(iPotassium.length) // Move cursor to the end
                            }
                            isValidPotassium = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.potassium = 0.0
                    lastPotentialPotassium = ""
                    isValidPotassium = true
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.potInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidPotassium) {
                    binding.potTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.potTIL.hint = "K⁺ ⚠"
                } else {
                    binding.potTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.potTIL.hint = "K⁺"
                }
            } else {
                binding.potTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.potTIL.hint = "K⁺"
            }
        }

        binding.sodInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iSodium = s.toString()
                patient.sodium = 0
                patient.sodiumCorrected = 0

                if (iSodium.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        if (isValidated("Sodium", iSodium)) {
                            lastPotentialSodium = iSodium

                            if (iSodium.toInt() in 90..200) {
                                patient.sodium = iSodium.toInt()
                                isValidSodium = true
                            } else {
                                // Set Error
                                isValidSodium = false
                            }
                        } else {
                            if (iSodium.matches(invalidRxToBeginWith.toRegex())) {
                                binding.sodInput.setText("")
                            } else {
                                binding.sodInput.setText(lastPotentialSodium)
                                binding.sodInput.setSelection(iSodium.length) // Move cursor to the end
                            }
                            isValidSodium = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.sodium = 0
                    patient.sodiumCorrected = 0
                    lastPotentialSodium = ""
                    isValidSodium = true
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.sodInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidSodium) {
                    binding.sodTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.sodTIL.hint = "Na⁺ ⚠"
                } else {
                    binding.sodTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.sodTIL.hint = "Na⁺"
                }
            } else {
                binding.sodTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.sodTIL.hint = "Na⁺"
            }
        }

        binding.chlInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iChloride = s.toString()
                patient.chloride = 0.0
                patient.chlorideCorrected = 80.0

                if (iChloride.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        if (isValidated("Chloride", iChloride)) {
                            lastPotentialChloride = iChloride

                            if (iChloride.toDouble() in 70.0..150.0) {
                                patient.chloride = iChloride.toDouble()
                                isValidChloride = true
                            } else {
                                // Set Error
                                isValidChloride = false
                                patient.chlorideCorrected = 0.0
                            }
                        } else {
                            if (iChloride.matches(invalidRxToBeginWith.toRegex())) {
                                binding.chlInput.setText("")
                            } else {
                                binding.chlInput.setText(lastPotentialChloride)
                                binding.chlInput.setSelection(iChloride.length) // Move cursor to the end
                                patient.chlorideCorrected = 0.0
                            }
                            isValidChloride = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.chloride = 0.0
                    patient.chlorideCorrected = 0.0
                    lastPotentialChloride = ""
                    isValidChloride = true
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.chlInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidChloride) {
                    binding.chlTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.chlTIL.hint = "Cl⁻ ⚠"
                } else {
                    binding.chlTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.chlTIL.hint = "Cl⁻"
                }
            } else {
                binding.chlTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.chlTIL.hint = "Cl⁻"
            }
        }

        binding.albuminInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                iAlbumin = s.toString()

                if (iAlbumin.isNotEmpty()) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("Albumin", iAlbumin) && !isAlbuminBeingConverted) {
                            lastPotentialAlbumin = iAlbumin

                            if (isAlbuminGDL && iAlbumin.toDouble() in 1.0..7.0) {
                                patient.albumin = iAlbumin.toDouble()
                                isValidAlbumin = true
                            } else if (!isAlbuminGDL && iAlbumin.toDouble() in 10.0..70.0) {
                                patient.albumin =
                                    iAlbumin.toDouble().div(10) // Convert mmol/L to mg/dL
                                isValidAlbumin = true
                            } else {
                                // Set Error
                                isValidAlbumin = false
                            }
                        } else {
                            if (isAlbuminGDL && iAlbumin.matches(invalidRxToBeginWith.toRegex())) {
                                lastPotentialAlbumin = ""
                                binding.albuminInput.setText(lastPotentialAlbumin)
                            } else {
                                binding.albuminInput.setText(lastPotentialAlbumin)
                                binding.albuminInput.setSelection(iAlbumin.length) // Move cursor to the end
                            }
                            isValidAlbumin = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (iAlbumin.isEmpty() && !isAlbuminBeingConverted) {
                    patient.albumin = 0.0
                    lastPotentialAlbumin = ""
                    isValidAlbumin = true
                } else {
                    isAlbuminBeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.albuminInput.setOnLongClickListener {
            if (binding.albuminInput.hasFocus()) {

                // Toggle the unit
                isAlbuminGDL = !isAlbuminGDL

                locallyStoredAppSettingsFile.edit()
                    .putBoolean("isAlbuminGDL", isAlbuminGDL)
                    .apply()

                // Update the hint and suffix text based on the unit
                if (isAlbuminGDL) {
                    binding.albuminTIL.suffixText = "g/dL"
                } else {
                    binding.albuminTIL.suffixText = "g/L"
                }

                // Convert the entered PO4 to the new unit
                val enteredAlbumin = binding.albuminInput.text.toString().toDoubleOrNull()
                val convertedAlbumin = if (isAlbuminGDL) {
                    enteredAlbumin?.div(10)
                } else {
                    enteredAlbumin?.times(10)
                }

                // Update the text in the input field
                if (convertedAlbumin != null) {
                    if (isAlbuminGDL) {
                        binding.albuminInput.setText(String.format("%.1f", convertedAlbumin))
                    } else {
                        binding.albuminInput.setText(String.format("%.1f", convertedAlbumin))
                    }
                }
                binding.albuminInput.setSelection(binding.albuminInput.text?.length ?: 0)
            }
            true
        }
        binding.albuminInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidAlbumin) {
                    binding.albuminTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.albuminTIL.hint = "ALB ⚠"
                } else {
                    binding.albuminTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.albuminTIL.hint = "ALB"
                }
            } else {
                binding.albuminTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.albuminTIL.hint = "ALB"
            }
        }

        binding.pHInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                ipH = s.toString()

                if (ipH.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        if (isValidated("pH", ipH)) {
                            lastPotentialpH = ipH

                            if (ipH.toDouble() in 6.0..8.0) {
                                patient.pH = ipH.toDouble()
                                isValidpH = true

                            } else {
                                // Set Error
                                isValidpH = false
                            }
                        } else {
                            if (ipH.matches(invalidRxToBeginWith.toRegex())) {
                                binding.pHInput.setText("")
                            } else {
                                binding.pHInput.setText(lastPotentialpH)
                                binding.pHInput.setSelection(ipH.length) // Move cursor to the end
                            }

                            isValidpH = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.pH = 0.0
                    lastPotentialpH = ""
                    isValidpH = true
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.pHInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidpH) {
                    binding.pHTIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.pHTIL.hint = "pH ⚠"
                } else {
                    binding.pHTIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.pHTIL.hint = "pH"
                }
            } else {
                binding.pHTIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.pHTIL.hint = "pH"
            }
        }







binding.patientNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try { // Set Patient Age
                        locallyStoredAppSettingsFile.edit()
                        .putString("patientName", s.toString())
                        .apply()
                    } catch (_: Exception) { }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.pCO2Input.setOnLongClickListener {
            if (binding.pCO2Input.hasFocus()) {

                // Toggle the unit
                patient.ispCO2kPa = !patient.ispCO2kPa

                locallyStoredAppSettingsFile.edit()
                    .putBoolean("ispCO2kPa", patient.ispCO2kPa)
                    .apply()

                // Update the hint and suffix text based on the unit
                if (patient.ispCO2kPa) {
                    binding.pCO2TIL.suffixText = "kPa"
                } else {
                    binding.pCO2TIL.suffixText = "mmHg"
                }

                // Convert the entered PO4 to the new unit
                val enteredpCO2 = binding.pCO2Input.text.toString().toDoubleOrNull()
                val convertedpCO2 = if (patient.ispCO2kPa) {
                    enteredpCO2?.div(7.50062)
                } else {
                    enteredpCO2?.times(7.50062)
                }

                // Update the text in the input field
                if (convertedpCO2 != null) {
                    if (patient.ispCO2kPa) {
                        binding.pCO2Input.setText(String.format("%.1f", convertedpCO2))
                    } else {
                        binding.pCO2Input.setText(convertedpCO2.toInt().toString())
                    }
                }
                binding.pCO2Input.setSelection(binding.pCO2Input.text?.length ?: 0)
            }
            true
        }




        binding.pCO2Input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ipCO2 = s.toString()

                if (ipCO2.isNotEmpty()) { // If Age field is not empty, containing no String

                    try { // Set Patient Age
                        if (isValidated("pCO2", ipCO2) && !ispCO2BeingConverted) {
                            lastPotentialpCO2 = ipCO2

                            if (!patient.ispCO2kPa && ipCO2.toDouble() in 10.0..160.0) {
                                patient.pCO2 = ipCO2.toDouble()
                                isValidpCO2 = true
                            } else if (patient.ispCO2kPa && ipCO2.toDouble() in 1.0..30.0) {
                                patient.pCO2 =
                                    ipCO2.toDouble().times(7.50062) // Convert mmHg to kPa
                                isValidpCO2 = true
                            } else {
                                // Set Error
                                isValidpCO2 = false
                            }
                        } else {
                            if (ipCO2.matches(invalidRxToBeginWith.toRegex())) {
                                lastPotentialpCO2 = ""
                                binding.pCO2Input.setText(lastPotentialpCO2)
                            } else {
                                binding.pCO2Input.setText(lastPotentialpCO2)
                                binding.pCO2Input.setSelection(ipCO2.length) // Move cursor to the end
                            }
                            isValidpCO2 = true
                        }
                    } catch (_: Exception) {
                    }
                } else if (ipCO2.isEmpty() && !ispCO2BeingConverted) {
                    patient.pCO2 = 0.0
                    lastPotentialpCO2 = ""
                    isValidpCO2 = true
                } else {
                    ispCO2BeingConverted = false
                }
                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.pCO2Input.setOnLongClickListener {
            if (binding.pCO2Input.hasFocus()) {

                // Toggle the unit
                patient.ispCO2kPa = !patient.ispCO2kPa

                locallyStoredAppSettingsFile.edit()
                    .putBoolean("ispCO2kPa", patient.ispCO2kPa)
                    .apply()

                // Update the hint and suffix text based on the unit
                if (patient.ispCO2kPa) {
                    binding.pCO2TIL.suffixText = "kPa"
                } else {
                    binding.pCO2TIL.suffixText = "mmHg"
                }

                // Convert the entered PO4 to the new unit
                val enteredpCO2 = binding.pCO2Input.text.toString().toDoubleOrNull()
                val convertedpCO2 = if (patient.ispCO2kPa) {
                    enteredpCO2?.div(7.50062)
                } else {
                    enteredpCO2?.times(7.50062)
                }

                // Update the text in the input field
                if (convertedpCO2 != null) {
                    if (patient.ispCO2kPa) {
                        binding.pCO2Input.setText(String.format("%.1f", convertedpCO2))
                    } else {
                        binding.pCO2Input.setText(convertedpCO2.toInt().toString())
                    }
                }
                binding.pCO2Input.setSelection(binding.pCO2Input.text?.length ?: 0)
            }
            true
        }
        binding.pCO2Input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!isValidpCO2) {
                    binding.pCO2TIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.pCO2TIL.hint = "pCO₂ ⚠"
                } else {
                    binding.pCO2TIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.pCO2TIL.hint = "pCO₂"
                }
            } else {
                binding.pCO2TIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.pCO2TIL.hint = "pCO₂"
            }
        }

        binding.cHCO3Input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                icHCO3 = s.toString()

                if (icHCO3.isNotEmpty()) { // If Age field is not empty, containing no String

                    /*
						 * To avoid retaining of last single number deleted,
						 * if user delete every single number individually,
						 * and suddenly text field become empty,
						 * hence we must detect empty Text Input Field to zero age value
						 */

                    try { // Set Patient Age
                        patient.isEmptycHCO3 = false
                        if (isValidated("cHCO3", icHCO3)) {
                            lastPotentialcHCO3 = icHCO3

                            if (icHCO3.toDouble() in 1.0..80.0) {
                                patient.cHCO3 = icHCO3.toDouble()
                                patient.isValidcHCO3 = true

                            } else {
                                // Set Error
                                patient.isValidcHCO3 = false
                            }
                        } else {
                            if (icHCO3.matches(invalidRxToBeginWith.toRegex())) {
                                binding.cHCO3Input.setText("")
                            } else {
                                binding.cHCO3Input.setText(lastPotentialcHCO3)
                                binding.cHCO3Input.setSelection(icHCO3.length) // Move cursor to the end
                            }

                            patient.isValidcHCO3 = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    patient.cHCO3 = 0.0
                    lastPotentialcHCO3 = ""
                    patient.isValidcHCO3 = true
                    patient.isEmptycHCO3 = true
                }
                // Check if user entered invalid HCO3
                val potentialcHCO3Input =
                    binding.cHCO3Input.text?.toString()?.toDoubleOrNull() ?: 0.0

                if (patient.pH != 0.0 && patient.pCO2 != 0.0 && (potentialcHCO3Input > patient.getaHCO3RM() + 2.0 || potentialcHCO3Input < patient.getaHCO3RM() - 2.0)) {
                    patient.cHCO3 = patient.getaHCO3RM()
                    patient.isValidcHCO3 = false
                }

                displayManagement()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.cHCO3Input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (patient.isValidcHCO3 || binding.cHCO3Input.text.toString().isEmpty()) {
                    binding.cHCO3TIL.boxBackgroundColor = defaultInputBackgroundColor
                    binding.cHCO3TIL.hint = "HCO₃"
                } else {
                    binding.cHCO3TIL.boxBackgroundColor = errorInputBackgroundColor
                    binding.cHCO3TIL.hint = "HCO₃ ⚠"
                }
            } else {
                binding.cHCO3TIL.boxBackgroundColor = defaultInputBackgroundColor
                binding.cHCO3TIL.hint = "HCO₃"
            }
        }
    }

    private fun setResultPanelDynamics() {
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        inactivePlanColor =
            ContextCompat.getColor(requireContext(), R.color.inactiveResultState)
        activePlanColor = ContextCompat.getColor(requireContext(), R.color.activeResultState)

        dpOfDevice = requireContext().resources.displayMetrics.density

        // Display Inactive TimeLine Bar
        binding.timelineInactiveBar.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to prevent multiple calls
                binding.timelineInactiveBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Set the height of another view to match the Y-coordinate of the target view
                binding.timelineInactiveBar.layoutParams =
                    (binding.timelineInactiveBar.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        // Xiaomi Device we have to subtract 13 to get the right edge of both usernameAndGreetingLinearlayout and profilePictureLinearlayout aligned
                        // But for safety we move it back further to 30


                        height = getInstantHeightOfResultPanel()
                    }
            }
        })

        // Listeners for following up Result Panel height and adjust Inactive Timeline Bar height accordingly
        binding.dynamicResultPanel.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // Update the height of the timelineInactiveBar to match the height of the dynamicResultPanel

            if (!dayOneResultPanelClosed || !dayTwoResultPanelClosed || !dayThreeResultPanelClosed) {

                binding.timelineInactiveBar.layoutParams.height = binding.dynamicResultPanel.height
                binding.timelineInactiveBar.requestLayout()
            }


        }

        binding.dayOneTitle.setOnClickListener {
            toggleDayOneOpened(false)
        }
        binding.dayTwoTitle.setOnClickListener {
            toggleDayTwoOpened(false)
        }
        binding.dayThreeTitle.setOnClickListener {
            toggleDayThreeOpened(false)
        }
    }

    //region UI Dynamics Functions

    private fun setCalculatorPanelDynamicAnimations() {

        //region Tapping On Chevron and Textview To Open Animate Acid Base Calculator Section
        binding.chevAcidBase.setOnClickListener {
            tapOpenAcidBaseCalculator()
        }

        binding.tvAcidBaseCalculatorTitle.setOnClickListener {
            tapOpenAcidBaseCalculator()
        }
        //endregion

        //region Clear Any Focus from Text Input Fields, If User Press Back Button to Close Keyboard
        clearFocusOfTextInput()
        //endregion

        //region Set Swipe Refresh For Calculator Panel
        // Wait until layout get inflated and get the default Y of Calculator Panel
        getCalculatorPanelY()

        // Respond to Swipe Calculator Panel Down to Refresh
        setSwipeRefreshAnimation()
        //endregion

        //region Set Swipe Delete For Text Input Field
        setSwipeDeleteFunctionality()
        //endregion
    }

    private fun tapOpenAcidBaseCalculator() {
        if (!isAcidBaseCalculatorShown) {
            // Rotate 180 degrees clockwise
            ObjectAnimator.ofFloat(binding.chevAcidBase, "rotation", 0f, 180f)
                .start()
            binding.lnAGAcidBaseCalculator.visibility = View.VISIBLE
            binding.calInput.imeOptions = EditorInfo.IME_ACTION_NEXT
            binding.calInput.nextFocusDownId = binding.sodInput.id
            
        } else {
            // Rotate 180 degrees counterclockwise
            ObjectAnimator.ofFloat(binding.chevAcidBase, "rotation", 180f, 0f)
                .start()
            binding.lnAGAcidBaseCalculator.visibility = View.GONE
            binding.calInput.nextFocusDownId = View.NO_ID
            binding.calInput.imeOptions = EditorInfo.IME_ACTION_DONE

        }
        isAcidBaseCalculatorShown = !isAcidBaseCalculatorShown
    }

    private fun clearFocusOfTextInput() {
        val decorView = activity?.window?.decorView
        decorView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            var alreadyOpen = false
            val defaultKeyboardHeightDP = 100
            val EstimatedKeyboardDP = defaultKeyboardHeightDP + 48
            val rect = Rect()

            override fun onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(rect)
                val estimatedKeyboardHeight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    EstimatedKeyboardDP.toFloat(),
                    decorView.resources.displayMetrics
                ).toInt()
                val heightDiff = decorView.rootView.height - (rect.bottom - rect.top)
                val isShown = heightDiff >= estimatedKeyboardHeight

                if (isShown == alreadyOpen) {
                    return
                }
                alreadyOpen = isShown
                if (!isShown) {
                    // Do something when keyboard is opened
                    binding.ageInput.clearFocus()
                    binding.weightInput.clearFocus()
                    binding.rbsInput.clearFocus()
                    binding.po4Input.clearFocus()
                    binding.mgInput.clearFocus()
                    binding.potInput.clearFocus()
                    binding.sodInput.clearFocus()
                    binding.chlInput.clearFocus()
                    binding.albuminInput.clearFocus()
                    binding.pHInput.clearFocus()
                    binding.pCO2Input.clearFocus()
                    binding.cHCO3Input.clearFocus()
                }
            }
        })
    }

    private fun getCalculatorPanelY() {
        binding.lnCalculator.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Ensure we call this only once
                binding.lnCalculator.viewTreeObserver.removeOnGlobalLayoutListener(this)
                defaultCalculatorY = binding.lnCalculator.y
                // Now you can get the dimensions or perform any other operations on linearLayout
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSwipeRefreshAnimation() {
        binding.lnCalculator.setOnTouchListener { _, sEvent ->

            currentCalculatorY = binding.lnCalculator.y

            when (sEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Toast.makeText(requireContext(), "You touched the screen", Toast.LENGTH_SHORT).show()
                    // Store the initial touch point (Y position) as the user first touch the screen
                    firstDownTouchY = sEvent.rawY
                    trackingY = firstDownTouchY
                    trackingDeltaY = sEvent.rawY - trackingY
                    // Distance the panel has to move from default Y position to user current finger position
                    deltaYCalculator = defaultCalculatorY - sEvent.rawY
                }

                MotionEvent.ACTION_UP -> {
                    //Toast.makeText(requireContext(), "You lift your finger off screen", Toast.LENGTH_SHORT).show()
                    // Return the LinearLayout to its original position when the user lifts his finger

                    binding.lnCalculator.animate()
                        .y(defaultCalculatorY)
                        .setDuration(0)
                        .start() // Return to original position

                    binding.tvReset.visibility = View.INVISIBLE

                    if (currentlyCardViewDisplacedDistance > 75 && !cancellingReset) {
                        // If user move reaches the target distance of 75f, reset the input field
                        //binding.ageInput.text = null
                        //binding.weightInput.text = null
                        
                        binding.rbsInput.text = null
                        binding.po4Input.text = null
                        binding.mgInput.text = null
                        binding.potInput.text = null
                        binding.calInput.text = null
                        binding.sodInput.text = null
                        binding.chlInput.text = null
                        binding.albuminInput.text = null
                        binding.pHInput.text = null
                        binding.pCO2Input.text = null
                        binding.cHCO3Input.text = null

                        //  isValidAge = true
                        // binding.ageTIL.boxBackgroundColor = defaultInputBackgroundColor
                        // isValidWeight = true
                        //binding.weightTIL.boxBackgroundColor = defaultInputBackgroundColor

                        isValidRBS = true
                        binding.rbsTIL.boxBackgroundColor = defaultInputBackgroundColor

                        isValidPhosphorus = true
                        binding.po4TIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidCalcium = true
                        binding.calTIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidMagnesium = true
                        binding.mgTIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidPotassium = true
                        binding.potTIL.boxBackgroundColor = defaultInputBackgroundColor
                        binding.potTIL.hint = "K⁺"

                        isValidSodium = true
                        binding.sodTIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidChloride = true
                        binding.chlTIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidAlbumin = true
                        binding.albuminTIL.boxBackgroundColor = defaultInputBackgroundColor
                        binding.albuminTIL.hint = "ALB"

                        isValidpH = true
                        binding.pHTIL.boxBackgroundColor = defaultInputBackgroundColor
                        isValidpCO2 = true
                        binding.pCO2TIL.boxBackgroundColor = defaultInputBackgroundColor
                        patient.isValidcHCO3 = true
                        binding.cHCO3TIL.boxBackgroundColor = defaultInputBackgroundColor

                        // Put the cursor inside the first field, the Age Text Input Field
                        if (patient.age == 0){
                            binding.ageInput.requestFocus()
                        }else if (patient.weight == 0.0){
                            binding.weightInput.requestFocus()
                        }else {
                            binding.rbsInput.requestFocus()
                        }
                            
                        // Force Display of Keyboard
                        val inputMethodManager =
                            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        val view = activity?.currentFocus
                        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

                        // Reset Our Calculation
                        currentlyCardViewDisplacedDistance = 0f
                        alreadySymbolRotatedLeft = false
                        alreadySymbolRotatedRight = false
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    // Toast.makeText(requireContext(), "You are keeping your finger on screen", Toast.LENGTH_SHORT).show()

                    currentlyCardViewDisplacedDistance = currentCalculatorY - defaultCalculatorY

                    lastDeltaSwipeDownY = sEvent.rawY - trackingY

                    // Check if user tries to moves upwards or downwards
                    if (trackingDeltaY > sEvent.rawY - trackingY) {
                        // User swipes to upward direction

                        if (lastAttemptToSwipe == "Down") {
                            // User suddenly reverses the swipe to upward direction
                            alreadySymbolRotatedRight = false
                            // iToast("You reversed your Swiping to upward direction")
                        }

                        lastAttemptToSwipe = "Up"
                        // iToast("You are swiping up")

                        cancellingReset = true

                        alreadySymbolRotatedLeft = false

                        /*
                        if (!alreadySymbolRotatedRight) {
                            val rotateRight = RotateAnimation(
                                0f,
                                180f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                            )
                            rotateRight.duration = 500
                            rotateRight.interpolator = LinearInterpolator()

                            binding.tvRefreshSymbol.startAnimation(rotateRight)

                            alreadySymbolRotatedRight = true
                        }
                        */

                    } else if (trackingDeltaY < sEvent.rawY - trackingY) {
                        // User swipes to downward direction

                        if (lastAttemptToSwipe == "Up") {
                            // User suddenly reverses the swipe to downward direction
                            alreadySymbolRotatedLeft = false
                            // iToast("You reversed your Swiping to downward direction")
                        }

                        lastAttemptToSwipe = "Down"
                        // iToast("You are Swiping Down")

                        cancellingReset = false

                        alreadySymbolRotatedRight = false

                        if (!alreadySymbolRotatedLeft) {

                            val rotateLeft = RotateAnimation(
                                180f,
                                0f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                            )

                            rotateLeft.duration = 300
                            rotateLeft.interpolator = LinearInterpolator()

                            binding.tvRefreshSymbol.startAnimation(rotateLeft)
                            alreadySymbolRotatedLeft = true
                        }
                    }

                    // Last recorded user finger touch point Y
                    trackingY = sEvent.rawY
                    // Current Last recorded Swiping distance
                    trackingDeltaY = sEvent.rawY - trackingY

                    // Prevents the user moving the panel upwards beyond its original position and not more than 125 downwards
                    if ((currentCalculatorY >= defaultCalculatorY && currentlyCardViewDisplacedDistance < 125f) || (cancellingReset && currentCalculatorY > defaultCalculatorY)) {

                        // Make Reset Instruction Visible behind the panel
                        binding.tvReset.visibility = View.VISIBLE

                        // Move the Calculator Panel as user moves his finger downwards or upwards
                        var newY = if (cancellingReset) {
                            binding.lnCalculator.y + lastDeltaSwipeDownY
                        } else {
                            sEvent.rawY + deltaYCalculator
                        }

                        if (newY < defaultCalculatorY) {
                            newY = defaultCalculatorY
                        }

                        binding.lnCalculator.animate()
                            .y(newY)
                            .setDuration(0)
                            .start()
                    }
                }
            }
            true
        }
    }

    private fun setSwipeDeleteFunctionality() {
        setSwipeDelete(binding.ageInput)
        setSwipeDelete(binding.weightInput)
        setSwipeDelete(binding.rbsInput)
        setSwipeDelete(binding.po4Input)
        setSwipeDelete(binding.mgInput)
        setSwipeDelete(binding.potInput)
        setSwipeDelete(binding.calInput)
        setSwipeDelete(binding.sodInput)
        setSwipeDelete(binding.chlInput)
        setSwipeDelete(binding.albuminInput)
        setSwipeDelete(binding.pHInput)
        setSwipeDelete(binding.pCO2Input)
        setSwipeDelete(binding.cHCO3Input)
        //setSwipeDelete(binding.patientNameInput)
    }

    @SuppressLint("ClickableViewAccessibility")
    protected fun setSwipeDelete(editText: EditText) {
        editText.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {

            override fun onSwipeLeft() {
                super.onSwipeLeft()

                editText.text = null
                editText.requestFocus()
                //Toast.makeText(requireContext(), "Swipe left gesture detected", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //endregion

    fun isValidated(dataType: String, dataToValidate: String): Boolean {

        var isValidated = false

        when (dataType) {
            "Age" -> {
                vSL1 = "^([1-9])$" // Allow For Only Number Input from 1 Up to 9, But Not 0
                vSL2 = "^(10|11|1[4-9]|[2-9][0-9])$" // Allowing For Age From 14 Up To 99
                vSL3 = "^(1[0-1][0-9])$" // Allowing For Age From 14 Up To 119
            }

            "Weight" -> {
                // Initially Allowing for Entering From 1 kg Up To 9 kg, Allowing 1 For Expecting User to Enter More Than 100 kg in Next Steps
                vSL1 = "^([1-9])$"
                // Initially Allowing for Entering From 10 kg Up To 99 kg, Allowing 10 For Expecting User to Enter 100 kg in Next Step
                vSL2 = "^([1-9][0-9])$"

                vSL3 = if (isWeightKg) {
                    // Allowing Weight Only From 100 kg Up To 250 kg
                    "^([1-2][0-9][0-9]|[1-9][0-9][.]|[1-2][0-9][.]|300)$"
                } else {
                    // Allowing Weight Only From 100 kg Up To 250 kg
                    "^([1-5][0-9][0-9]|[1-5][0-9][.]|6[0-5][0-9]|6[0-5][.]|6[0-5][0-9]|6[0-5][.]|66[0-3]|66[.])$"
                }
                vSL4 = if (isWeightKg) {
                    // Allowing Weight Only From 100 kg Up To 250 kg
                    "^([1-2][0-9][0-9]|[1-9][0-9][.][0-9]|[1-2][0-9][.][0-9]|300)$"
                } else {
                    // Allowing Weight Only From 100 kg Up To 250 kg
                    "^([1-5][0-9][0-9]|[1-5][0-9][.][0-9]|6[0-5][0-9]|6[0-5][.][0-9]|6[0-5][0-9]|6[0-5][.][0-9]|66[0-3]|66[.][0-9])$"
                }
            }

            "RBS" -> {
                if (isRBSMg) {
                    // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                    vSL1 = "^([1-9])$"
                    // Initially Allowing for Entering From 10 mg/dL Up To 99 mg/dL, Allowing 10 For Expecting User to Enter 100 mg/dL in Next Step
                    vSL2 = "^([1-9][0-9])$"
                    // Allowing From 100 mg/dL Up To 999 mg/dL
                    vSL3 = "^([1-9][0-9][0-9])$"
                    // Allowing From 1000 mg/dL Up To 1200 mg/dL
                    vSL4 = "^(1[0-1][0-9][0-9]|1200)$"
                } else {
                    // Initially Allowing for Entering From 1 Up To 9 Allowing 1 For Expecting User to Enter 10 in Next Steps
                    vSL1 = "^([0-9])$"
                    // Initially Allowing form 0. to 66
                    vSL2 = "^(0[.]|[1-5][0-9]|6[0-6]|[1-9][.])$"
                    // Allowing From 0.8 to 66.
                    vSL3 = "^(0[.][8-9]|[1-5][0-9][.]|6[0-6][.]|[1-9][.][0-9])$"
                    // Allowing From 0.80 to 66.9
                    vSL4 = "^(0[.][8-9][0-9]|[1-5][0-9][.][0-9]|6[0-6][.][0-9]|[1-9][.][0-9][0-9])$"
                }
            }

            "Phosphorus" -> {
                if (isPhosphorusMg) {
                    // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                    vSL1 = "^([0-9])$"
                    // Initially Allowing for 0. to 15
                    vSL2 = "^([0-9][.]|[0-5])$"
                    // Allowing From 0.1 to 15.
                    vSL3 = "^(0[.][1-9]|[1-9][.][0-9]|1[0-4][.]|15.)$"
                    // Allowing From 0.1 to 15.0
                    vSL4 = "^(0[.][1-9][0-9]|[1-9][.][0-9][0-9]|1[0-4][.][0-9]|15.0)$"
                } else {
                    // Initially Allowing for Entering From 1 Up To 5 Allowing 1 For Expecting User to Enter 10 in Next Steps
                    vSL1 = "^([0-5])$"
                    // Initially Allowing for Entering From 0 to 5
                    vSL2 = "^([0-5][.])$"
                    // Allowing From 0. to 5.
                    vSL3 = "^([0-4][.][0-9]|5.0)$"
                    // Allowing From 0.05 to 5.00
                    vSL4 = "^(0[.]0[5-9]|[0-4].[0-9][0-9]|5[.]00)$"
                }
            }
            
              "Calcium" -> {
                if (isCalciumMg) {
                    // Initially Allowing for Entering From 0.1 mg/dL Up To 25 mg/dL
                    vSL1 = "^([0-9])$"
                    // Initially Allowing for 0. to 25
                    vSL2 = "^([0-9][.]|[1][0-9]|[2][0-5])$"
                    // Allowing From 0.1 to 25.
                    vSL3 = "^(0[.][1-9]|[1-9][.][0-9]|1[0-9][.]|[2][0-5][.])$"
                    // Allowing From 0.1 to 25.0
                    vSL4 = "^(0[.][1-9][0-9]|[1-9][.][0-9][0-9]|1[0-9][.][0-9]|[2][0-5][.][0-9])$"
                } else {
                    // Initially Allowing for Entering From 0.05 Up To 6 
                    vSL1 = "^([0-6])$"
                    // Initially Allowing for Entering From 0 to 5
                    vSL2 = "^([0-6][.])$"
                    // Allowing From 0. to 5.
                    vSL3 = "^([0-5][.][0-9]|6.0)$"
                    // Allowing From 0.05 to 5.00
                    vSL4 = "^(0[.]0[5-9]|[0-5].[0-9][0-9]|6[.]00)$"
                }
            }

            "Magnesium" -> {
                if (isMagnesiumMg) {
                    // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                    vSL1 = "^([0-9])$"
                    // Initially Allowing for 0. to 20
                    vSL2 = "^([0-9][.]|[1-9][.]|1[0-9]|20)$"
                    // Allowing From 0.1 to 20.
                    vSL3 = "^(0[.][1-9]|[1-9][.][0-9]|1[0-9][.]|20.)$"
                    // Allowing From 0.1 to 20.0
                    vSL4 = "^(0[.][1-9][0-9]|[1-9][.][0-9][0-9]|1[0-9][.][0-9]|20.0)$"
                } else {
                    // Initially Allowing for Entering From 1 Up To 5 Allowing 1 For Expecting User to Enter 10 in Next Steps
                    vSL1 = "^([0-9])$"
                    // Initially Allowing for 0. to 10
                    vSL2 = "^([0-9][.]|10)$"
                    // Allowing From 0.1 to 10.
                    vSL3 = "^(0[.][1-9]|[1-9][.][0-9]|10.)$"
                    // Allowing From 0.1 to 10.0
                    vSL4 = "^(0[.][1-9][0-9]|[1-9][.][0-9][0-9]|10.0)$"
                }
            }

            "Potassium" -> {
                // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                vSL1 = "^([0-9])$"
                // Initially Allowing for 0. to 15
                vSL2 = "^([0-9][.]|[1][0-4]|15)$"
                // Allowing From 0.1 to 15.
                vSL3 = "^(0[.][1-9]|[1-9][.][0-9]|[1][0-4][.]|15[.])$"
                // Allowing From 0.1 to 15.0
                vSL4 = "^(0[.][1-9][0-9]|[1-9][.][0-9][0-9]|[1][0-4][.][0-9]|15[.]0)$"
            }

            "Sodium" -> {
                vSL1 = "^([1-2]|[8-9])$" // Allow For Only Number Input from 1 Up to 9, But Not 0
                vSL2 = "^([8-9][0-9]|1[0-9]|20)$" // Allowing For Age From 14 Up To 99
                vSL3 = "^([8-9][0-9]|1[0-9][0-9]|200)$" // Allowing For Age From 14 Up To 119
            }

            "Chloride" -> {
                vSL1 = "^([1-2]|[7-9])$" // Allow For Only Number Input from 1 Up to 9, But Not 0
                vSL2 = "^([7-9][0-9]|1[0-6])$" // Allowing For Age From 14 Up To 99
                vSL3 = "^([7-9][0-9][.]|1[0-5][0-9]|160)$" // Allowing For 14 Up To 119
                vSL4 = "^([7-9][0-9][.][1-9]|1[0-5][0-9][.]|160[.])$" // Allowing For 14 Up To 119
                vSL5 =
                    "^([7-9][0-9][.][1-9]|1[0-5][0-9][.][1-9]|160[.][1-9])$" // Allowing For 14 Up To 119
            }

            "Albumin" -> {
                if (isAlbuminGDL) {
                    // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                    vSL1 = "^([0-7])$"
                    // Initially Allowing for 0. to 20
                    vSL2 = "^([0-7][.])$"
                    // Allowing From 0.1 to 20.
                    vSL3 = "^([0-7][.][0-9])$"
                    // Allowing From 0.1 to 20.0
                    vSL4 = "^([0-7][.][0-9][0-9])$"
                } else {
                    // Initially Allowing for Entering From 1 Up To 5 Allowing 1 For Expecting User to Enter 10 in Next Steps
                    vSL1 = "^([1-7])$"
                    // Initially Allowing for 0. to 10
                    vSL2 = "^([1-6][0-9]|70)$"
                    // Allowing From 0.1 to 10.
                    vSL3 = "^([1-6][0-9][.]|70)$"
                    // Allowing From 0.1 to 10.0
                    vSL4 = "^([1-6][0-9][.][0-9]|70)$"
                }
            }

            "pH" -> {
                vSL1 = "^([6-8])$" // Allow For Only Number Input from 1 Up to 9, But Not 0
                vSL2 = "^([6-8][.])$" // Allowing For Age From 14 Up To 99
                vSL3 = "^([6-7][.][0-9]|8[.]0)$" // Allowing For Age From 14 Up To 119
                vSL4 = "^([6-7][.][0-9][0-9]|8[.]00)$" // Allowing For Age From 14 Up To 119
            }

            "pCO2" -> {
                if (!patient.ispCO2kPa) {
                    // Initially Allowing for Entering From 1 mg/dL Up To 9 mg/dL, Allowing 1 For Expecting User to Enter 10 mg/dL in Next Steps
                    vSL1 = "^([1-9])$"
                    // Initially Allowing for 0. to 20
                    vSL2 = "^([1-9][0-9]|[1-5][0-9]|16)$"
                    // Allowing From 0.1 to 20.
                    vSL3 = "^([1-9][0-9]|[1-9][0-9][.]|[1-5][0-9][0-9]|[1-5][0-9][.]|160)$"
                    // Allowing From 0.1 to 20.0
                    vSL4 =
                        "^([1-9][0-9]|[1-9][0-9][.][0-9]|[1-5][0-9][0-9]|[1-5][0-9][.][0-9]|160)$"
                } else {
                    // Initially Allowing for Entering From 1 Up To 5 Allowing 1 For Expecting User to Enter 10 in Next Steps
                    vSL1 = "^([1-9])$"
                    // Initially Allowing for 0. to 10
                    vSL2 = "^([1-9][.]|[1-2][0-9]|30)$"
                    // Allowing From 0.1 to 10.
                    vSL3 = "^([1-9][.][0-9]|[1-2][0-9][.]|30)$"
                    // Allowing From 0.1 to 10.0
                    vSL4 = "^([1-2][0-9][.][0-9]|30)$"
                }
            }

            "cHCO3" -> {
                // Initially Allowing for Entering From 1 Up To 5 Allowing 1 For Expecting User to Enter 10 in Next Steps
                vSL1 = "^([1-9])$"
                // Initially Allowing for 0. to 10
                vSL2 = "^([1-9][.]|[1-7][0-9]|80)$"
                // Allowing From 0.1 to 10.
                vSL3 = "^([1-9][.][0-9]|[1-7][0-9][.]|80)$"
                // Allowing From 0.1 to 10.0
                vSL4 = "^([1-7][0-9][.][0-9]|80)$"
            }
        }

        when (dataToValidate.length) {
            1 -> {
                isValidated = dataToValidate.matches(vSL1.toRegex())
            }

            2 -> {
                isValidated = dataToValidate.matches(vSL2.toRegex())
            }

            3 -> {
                isValidated = dataToValidate.matches(vSL3.toRegex())
            }

            4 -> {
                isValidated = dataToValidate.matches(vSL4.toRegex())
            }

            5 -> {
                isValidated = dataToValidate.matches(vSL5.toRegex())
            }
        }

        return isValidated
    }

    private fun setAppSettings() {
        locallyStoredAppSettingsFile =
            requireActivity().getSharedPreferences("DKAAssistant", Context.MODE_PRIVATE)
           
        binding.patientNameInput.text = Editable.Factory.getInstance().newEditable(locallyStoredAppSettingsFile.getString("patientName", ""))
        
        val storedAgeString = locallyStoredAppSettingsFile.getString("patientAge", "")
        val storedAge = Editable.Factory.getInstance().newEditable(storedAgeString)

        binding.ageInput.text = storedAge

        storedAgeString?.let {
            if (it.isNotEmpty()) {
                val age = it.toIntOrNull()
                if (age != null) {
                    patient.age = age
                }
            }
        }

        val storedWeightString = locallyStoredAppSettingsFile.getString("patientWeight", "")
        val storedWeight = Editable.Factory.getInstance().newEditable(storedWeightString)

        binding.weightInput.text = Editable.Factory.getInstance().newEditable(locallyStoredAppSettingsFile.getString("patientWeight", ""))
        
        storedWeightString?.let {
            if (it.isNotEmpty()) {
                val weight = it.toDoubleOrNull()
                if (weight != null) {
                    patient.weight = weight
                }
            }
        }
        
        isWeightKg = locallyStoredAppSettingsFile.getBoolean(
            "isWeightKg", true
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isWeightKg) {
            binding.weightLabel.text= "Kg"
        } else {
            binding.weightLabel.text = "lbs"
        }

        isRBSMg = locallyStoredAppSettingsFile.getBoolean(
            "isRBSMg", true
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isRBSMg) {
            binding.rbsTIL.suffixText = "mg/dL"
        } else {
            binding.rbsTIL.suffixText = "mmol/L"
        }

        isPhosphorusMg = locallyStoredAppSettingsFile.getBoolean(
            "isPhosphorusMg", false
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isPhosphorusMg) {
            binding.po4TIL.suffixText = "mg/dL"
        } else {
            binding.po4TIL.suffixText = "mmol/L"
        }


        isCalciumMg = locallyStoredAppSettingsFile.getBoolean(
            "isCalciumMg", false
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isCalciumMg) {
            binding.calTIL.suffixText = "mg/dL"
        } else {
            binding.calTIL.suffixText = "mmol/L"
        }

        isMagnesiumMg = locallyStoredAppSettingsFile.getBoolean(
            "isMagnesiumMg", false
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isMagnesiumMg) {
            binding.mgTIL.suffixText = "mg/dL"
        } else {
            binding.mgTIL.suffixText = "mmol/L"
        }

        isAlbuminGDL = locallyStoredAppSettingsFile.getBoolean(
            "isAlbuminGDL", true
        ) // true is default value for isWeightKgSP if it was empty

        // Update the hint and suffix text based on the unit
        if (isAlbuminGDL) {
            binding.albuminTIL.suffixText = "g/dL"
        } else {
            binding.albuminTIL.suffixText = "g/L"
        }

        patient.ispCO2kPa = locallyStoredAppSettingsFile.getBoolean(
            "ispCO2kPa", false
        ) // true is default value for isWeightKgSP if it was empty


        // Update the hint and suffix text based on the unit
        if (patient.ispCO2kPa) {
            binding.pCO2TIL.suffixText = "kPa"
        } else {
            binding.pCO2TIL.suffixText = "mmHg"
        }
        
        if (patient.age == 0){
            binding.ageInput.requestFocus()
        }else if (patient.weight == 0.0){
            binding.weightInput.requestFocus()
        }else {
            binding.rbsInput.requestFocus()
        }
    }

    private fun highlightABGDx(paragraph: String): SpannableStringBuilder {
        val wordsToBold = listOf(
            "Acute",
            "Chronic",
            "Acute-on-Chronic",
            "Metabolic",
            "Respiratory",
            "Anion",
            "Non-Anion",
            "Gap",
            "Acidosis",
            "Alkalosis",
            "Δ/Δ:",
            "Δ-Δ:",
            "cHCO₃",
            "AG",
            "Adequate",
            "Compensation",
            "Normal ABG"
        )
        val spannableStringBuilder = SpannableStringBuilder(paragraph)

        wordsToBold.forEach { word ->
            var index = paragraph.indexOf(word)
            while (index >= 0) {
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + word.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                index = paragraph.indexOf(word, index + 1)
            }
        }
        return spannableStringBuilder
    }

    private fun highlightManagement(paragraph: String): SpannableStringBuilder {
        val wordsToBold = listOf(
            "NaCl",
            "KCl",
            " mL ",
            "mEq",
            "Sodium",
            "Glycerophosphate",
            "Potassium Phosphate",
            "D₁₀W",
            "Urgent",
            "D₅₀W",
            "Stat",
            "Regular",
            "Insulin",
            "Rate:",
            "Hold",
            "Infusion",
            "RESUSCITATIVE PHASE (0-12 Hours):",
            "0-60 min:",
            "x",
            "If Shocked (SBP < 90 mmHg):",
            "Once NOT Shocked (SBP > 90 mmHg):",
            "500",
            "RESTORATIVE PHASE (13 hour-48 hour)"
        )
        val spannableStringBuilder = SpannableStringBuilder(paragraph)

        wordsToBold.forEach { word ->
            var index = paragraph.indexOf(word)
            while (index >= 0) {
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + word.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                index = paragraph.indexOf(word, index + 1)
            }
        }

        // Highlight numbers
        val numberPattern = "\\d+(\\.\\d+)?".toRegex() // Matches integers and decimals
        numberPattern.findAll(paragraph).forEach { matchResult ->
            val number = matchResult.value.toDoubleOrNull()
            if (number != null && number != 500.0) {
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    matchResult.range.first,
                    matchResult.range.last + 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }

        return spannableStringBuilder
    }

    private fun toggleDayOneOpened(isCallback: Boolean) {
        if (!isCallback) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }

        // Set initial and final height for result panel
        val initialHeightOfDayOneResult = binding.dayOneResultPanel.height
        var finalHeightOfDayOneResult = (91 * dpOfDevice)

        // Set initial and final height for inactive timeline bar based on the days panel baseline
        val initialHeightOfTimelineInactiveBar: Float =
            (binding.timelineInactiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineInactiveBar: Float =
            (binding.dayThreeIconBk.y + binding.dayThreeIconBk.height) + (134 * dpOfDevice)

        // Set initial and final height for active timeline bar based on the days panel top
        val initialHeightOfTimelineActiveBar: Float =
            (binding.timelineActiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineActiveBar: Float =
            (binding.dayOneTitle.y + binding.dayOneTitle.height) + (30 * dpOfDevice)

        if (!dayOneResultPanelClosed) {// User now try to close the panel
            if (!isCallback) {
                activatePlanOfDay(1, false) // Keep Day One Active
                inactivatePlanOfDay(2)
                inactivatePlanOfDay(3)
            }

            // Collapse the panel
            finalHeightOfDayOneResult = 0f
            finalHeightOfTimelineInactiveBar =
                initialHeightOfTimelineInactiveBar - binding.dayOneResultPanel.y - (65 * dpOfDevice)

            finalHeightOfTimelineActiveBar = if (isCallback) {// Keep Time line of day one active
                (binding.dayOneTitle.y + binding.dayOneTitle.height) + (130 * dpOfDevice)
            } else {
                initialHeightOfTimelineActiveBar - binding.dayOneResultPanel.y - (65 * dpOfDevice)
            }

        } else {// User try to open the panel
            if (!isCallback) {
                activatePlanOfDay(1, false) // Keep Day One Active
                inactivatePlanOfDay(2)
                inactivatePlanOfDay(3)
            }

            if (!dayTwoResultPanelClosed) {
                toggleDayTwoOpened(true)
            }
            if (!dayThreeResultPanelClosed) {
                toggleDayThreeOpened(true)
            }

        }

        // Create ValueAnimators to animate the height of both views
        val dayOneAnimator = ValueAnimator.ofInt(
            initialHeightOfDayOneResult,
            finalHeightOfDayOneResult.toInt()
        )
        val timelineInactiveBarAnimator = ValueAnimator.ofFloat(
            initialHeightOfTimelineInactiveBar,
            finalHeightOfTimelineInactiveBar
        )
        val timelineActiveBarAnimator =
            ValueAnimator.ofFloat(initialHeightOfTimelineActiveBar, finalHeightOfTimelineActiveBar)

        dayOneAnimator.duration = 500 // Duration in milliseconds
        timelineInactiveBarAnimator.duration = 500 // Duration in milliseconds
        if (!isCallback) {
            timelineActiveBarAnimator.duration = 500 // Duration in milliseconds
        } else {
            timelineActiveBarAnimator.duration = 25 // Duration in milliseconds
        }

        if (dayOneResultPanelClosed) {
            dayOneAnimator.interpolator = OvershootInterpolator()
            timelineInactiveBarAnimator.interpolator = OvershootInterpolator()
            timelineActiveBarAnimator.interpolator = OvershootInterpolator()
        } else {
            dayOneAnimator.interpolator = AnticipateInterpolator()
            timelineInactiveBarAnimator.interpolator = AnticipateInterpolator()

            if (!isCallback) {
                timelineActiveBarAnimator.interpolator = AnticipateInterpolator()
            } else {
                timelineActiveBarAnimator.interpolator = LinearInterpolator()
            }
        }

        dayOneAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = binding.dayOneResultPanel.layoutParams as RelativeLayout.LayoutParams
            layoutParams.height = animatedValue
            binding.dayOneResultPanel.layoutParams = layoutParams
        }

        timelineInactiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineInactiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineInactiveBar.requestLayout()
        }

        timelineActiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineActiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineActiveBar.requestLayout()
        }

        // Combine the animations using AnimatorSet
        val animatorSet = AnimatorSet()

        animatorSet.playTogether(
            dayOneAnimator,
            timelineInactiveBarAnimator,
            timelineActiveBarAnimator
        )

        // Update the state
        dayOneResultPanelClosed = !dayOneResultPanelClosed

        // Start the animation
        animatorSet.start()
    }

    private fun toggleDayTwoOpened(isCallback: Boolean) {
        if (!isCallback) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
        // Set initial and final height for result panel
        val initialHeightOfDayTwoResult = binding.dayTwoResultPanel.height
        var finalHeightOfDayTwoResult = (91 * dpOfDevice)

        // Set initial and final height for inactive timeline bar based on the days panel baseline
        val initialHeightOfTimelineInactiveBar: Float =
            (binding.timelineInactiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineInactiveBar: Float =
            (binding.dayThreeIconBk.y + binding.dayThreeIconBk.height) + (135 * dpOfDevice)

        // Set initial and final height for active timeline bar based on the days panel top
        val initialHeightOfTimelineActiveBar: Float =
            (binding.timelineActiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineActiveBar: Float =
            (binding.dayTwoTitle.y + binding.dayTwoTitle.height) + (65 * dpOfDevice)


        if (!dayTwoResultPanelClosed) {// User now try to close the panel
            if (isCallback && !dayThreeResultPanelClosed) {
                activatePlanOfDay(1, true) // Keep Day One Active
                activatePlanOfDay(2, false)
                inactivatePlanOfDay(3)
            }

            // Collapse the panel
            finalHeightOfDayTwoResult = 0f
            finalHeightOfTimelineInactiveBar =
                initialHeightOfTimelineInactiveBar - binding.dayTwoResultPanel.y - (65 * dpOfDevice)
            finalHeightOfTimelineActiveBar =
                initialHeightOfTimelineActiveBar - binding.dayOneResultPanel.y - (10 * dpOfDevice)
        } else {// User try to open the panel
            activatePlanOfDay(1, true) // Keep Day One Active
            activatePlanOfDay(2, false)
            inactivatePlanOfDay(3)

            if (!dayOneResultPanelClosed) {
                toggleDayOneOpened(true)
            }
            if (!dayThreeResultPanelClosed) {
                toggleDayThreeOpened(true)
            }
        }

        // Create ValueAnimators to animate the height of both views
        val dayTwoAnimator = ValueAnimator.ofInt(
            initialHeightOfDayTwoResult,
            finalHeightOfDayTwoResult.toInt()
        )
        val timelineInactiveBarAnimator = ValueAnimator.ofFloat(
            initialHeightOfTimelineInactiveBar,
            finalHeightOfTimelineInactiveBar
        )
        val timelineActiveBarAnimator =
            ValueAnimator.ofFloat(initialHeightOfTimelineActiveBar, finalHeightOfTimelineActiveBar)

        dayTwoAnimator.duration = 500 // Duration in milliseconds
        timelineInactiveBarAnimator.duration = 500 // Duration in milliseconds
        timelineActiveBarAnimator.duration = 500 // Duration in milliseconds

        if (dayTwoResultPanelClosed) {
            dayTwoAnimator.interpolator = OvershootInterpolator()
            timelineInactiveBarAnimator.interpolator = OvershootInterpolator()
            timelineActiveBarAnimator.interpolator = OvershootInterpolator()
        } else {
            dayTwoAnimator.interpolator = AnticipateInterpolator()
            timelineInactiveBarAnimator.interpolator = AnticipateInterpolator()
            timelineActiveBarAnimator.interpolator = AnticipateInterpolator()
        }

        dayTwoAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = binding.dayTwoResultPanel.layoutParams as RelativeLayout.LayoutParams
            layoutParams.height = animatedValue
            binding.dayTwoResultPanel.layoutParams = layoutParams
        }

        timelineInactiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineInactiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineInactiveBar.requestLayout()
        }

        timelineActiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineActiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineActiveBar.requestLayout()
        }

        // Combine the animations using AnimatorSet
        val animatorSet = AnimatorSet()

        animatorSet.playTogether(
            dayTwoAnimator,
            timelineInactiveBarAnimator,
            timelineActiveBarAnimator
        )

        // Update the state
        dayTwoResultPanelClosed = !dayTwoResultPanelClosed

        // Start the animation
        animatorSet.start()
    }

    private fun toggleDayThreeOpened(isCallback: Boolean) {
        if (!isCallback) {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
        // Set initial and final height for result panel
        val initialHeightOfDayThreeResult = binding.dayThreeResultPanel.height
        var finalHeightOfDayThreeResult = (91 * dpOfDevice)

        // Set initial and final height for inactive timeline bar based on the days panel baseline
        val initialHeightOfTimelineInactiveBar: Float =
            (binding.timelineInactiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineInactiveBar: Float =
            (binding.dayThreeIconBk.y + binding.dayThreeIconBk.height) + (147 * dpOfDevice)

        // Set initial and final height for active timeline bar based on the days panel top
        val initialHeightOfTimelineActiveBar: Float =
            (binding.timelineActiveBar.layoutParams.height).toFloat()
        var finalHeightOfTimelineActiveBar: Float =
            (binding.dayThreeTitle.y + binding.dayThreeTitle.height) + (91 * dpOfDevice)

        if (!dayThreeResultPanelClosed) {// User now try to close the panel
            if (!isCallback) {
                activatePlanOfDay(1, true) // Keep Day One Active
                activatePlanOfDay(2, true)
                activatePlanOfDay(3, false)
            }

            // Collapse the panel
            finalHeightOfDayThreeResult = 0f
            finalHeightOfTimelineInactiveBar =
                initialHeightOfTimelineInactiveBar - binding.dayThreeResultPanel.y - (65 * dpOfDevice)
            finalHeightOfTimelineActiveBar =
                initialHeightOfTimelineActiveBar - binding.dayThreeResultPanel.y - (10 * dpOfDevice)
        } else {// User try to open the panel
            activatePlanOfDay(1, true) // Keep Day One Active
            activatePlanOfDay(2, true)
            activatePlanOfDay(3, false)

            if (!dayOneResultPanelClosed) {
                toggleDayOneOpened(true)
            }

            if (!dayTwoResultPanelClosed) {
                toggleDayTwoOpened(true)
            }
        }

        // Create ValueAnimators to animate the height of both views
        val dayThreeAnimator = ValueAnimator.ofInt(
            initialHeightOfDayThreeResult,
            finalHeightOfDayThreeResult.toInt()
        )
        val timelineInactiveBarAnimator = ValueAnimator.ofFloat(
            initialHeightOfTimelineInactiveBar,
            finalHeightOfTimelineInactiveBar
        )
        val timelineActiveBarAnimator =
            ValueAnimator.ofFloat(initialHeightOfTimelineActiveBar, finalHeightOfTimelineActiveBar)

        dayThreeAnimator.duration = 500 // Duration in milliseconds
        timelineInactiveBarAnimator.duration = 500 // Duration in milliseconds
        timelineActiveBarAnimator.duration = 500 // Duration in milliseconds

        if (dayThreeResultPanelClosed) {
            dayThreeAnimator.interpolator = OvershootInterpolator()
            timelineInactiveBarAnimator.interpolator = OvershootInterpolator()
            timelineActiveBarAnimator.interpolator = OvershootInterpolator()
        } else {
            dayThreeAnimator.interpolator = AnticipateInterpolator()
            timelineInactiveBarAnimator.interpolator = AnticipateInterpolator()
            timelineActiveBarAnimator.interpolator = AnticipateInterpolator()
        }

        dayThreeAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams =
                binding.dayThreeResultPanel.layoutParams as RelativeLayout.LayoutParams
            layoutParams.height = animatedValue
            binding.dayThreeResultPanel.layoutParams = layoutParams
        }

        timelineInactiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineInactiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineInactiveBar.requestLayout()
        }

        timelineActiveBarAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.timelineActiveBar.layoutParams.height = animatedValue.toInt()
            binding.timelineActiveBar.requestLayout()
        }

        // Combine the animations using AnimatorSet
        val animatorSet = AnimatorSet()

        animatorSet.playTogether(
            dayThreeAnimator,
            timelineInactiveBarAnimator,
            timelineActiveBarAnimator
        )

        // Update the state
        dayThreeResultPanelClosed = !dayThreeResultPanelClosed

        // Start the animation
        animatorSet.start()
    }

    private fun inactivatePlanOfDay(day: Int) {
        when (day) {
            1 -> {// Set the background color using the inactive colors from colors.xml
                binding.dayOneTitle.setBackgroundColor(inactivePlanColor)
                binding.ivDayOneTri.setBackgroundResource(R.drawable.triangle_shape_inactive)
                binding.dayOneIconBk.setBackgroundResource(R.drawable.progress_item_circle_inactive)
                binding.dayOneIcon.visibility = View.VISIBLE
                binding.dayOneCompleted.visibility = View.GONE

            }

            2 -> {// Set the background color using the inactive colors from colors.xml
                binding.dayTwoTitle.setBackgroundColor(inactivePlanColor)
                binding.ivDayTwoTri.setBackgroundResource(R.drawable.triangle_shape_inactive)
                binding.dayTwoIconBk.setBackgroundResource(R.drawable.progress_item_circle_inactive)
                binding.dayTwoIcon.visibility = View.VISIBLE
                binding.dayTwoCompleted.visibility = View.GONE

            }

            3 -> {// Set the background color using the inactive colors from colors.xml
                binding.dayThreeTitle.setBackgroundColor(inactivePlanColor)
                binding.ivDayThreeTri.setBackgroundResource(R.drawable.triangle_shape_inactive)
                binding.dayThreeIconBk.setBackgroundResource(R.drawable.progress_item_circle_inactive)
                binding.dayThreeIcon.visibility = View.VISIBLE
                binding.dayThreeCompleted.visibility = View.GONE
            }
        }
    }

    private fun activatePlanOfDay(day: Int, isCompleted: Boolean) {
        when (day) {
            1 -> {// Set the background color using the active colors from colors.xml
                binding.dayOneTitle.setBackgroundColor(activePlanColor)
                binding.ivDayOneTri.setBackgroundResource(R.drawable.triangle_shape_active)
                binding.dayOneIconBk.setBackgroundResource(R.drawable.progress_item_circle_active)

                if (isCompleted) {
                    binding.dayOneIcon.visibility = View.GONE
                    binding.dayOneCompleted.visibility = View.VISIBLE
                } else {
                    binding.dayOneIcon.visibility = View.VISIBLE
                    binding.dayOneCompleted.visibility = View.GONE
                }
            }

            2 -> {// Set the background color using the active colors from colors.xml
                binding.dayTwoTitle.setBackgroundColor(activePlanColor)
                binding.ivDayTwoTri.setBackgroundResource(R.drawable.triangle_shape_active)
                binding.dayTwoIconBk.setBackgroundResource(R.drawable.progress_item_circle_active)

                if (isCompleted) {
                    binding.dayTwoIcon.visibility = View.GONE
                    binding.dayTwoCompleted.visibility = View.VISIBLE
                } else {
                    binding.dayTwoIcon.visibility = View.VISIBLE
                    binding.dayTwoCompleted.visibility = View.GONE
                }
            }

            3 -> {// Set the background color using the active colors from colors.xml
                binding.dayThreeTitle.setBackgroundColor(activePlanColor)
                binding.ivDayThreeTri.setBackgroundResource(R.drawable.triangle_shape_active)
                binding.dayThreeIconBk.setBackgroundResource(R.drawable.progress_item_circle_active)

                if (isCompleted) {
                    binding.dayThreeIcon.visibility = View.GONE
                    binding.dayThreeCompleted.visibility = View.VISIBLE
                } else {
                    binding.dayThreeIcon.visibility = View.VISIBLE
                    binding.dayThreeCompleted.visibility = View.GONE
                }
            }
        }
    }

    fun getInstantHeightOfResultPanel(): Int {
        // Convert pixels that return from measureWidth of Linearlayout to dp

        // Get the position of the target view
        val dayThreeTitleLocation = IntArray(2)
        val timelineInactiveBarLocation = IntArray(2)
        binding.dayThreeTitle.getLocationOnScreen(dayThreeTitleLocation)
        binding.timelineInactiveBar.getLocationInWindow(timelineInactiveBarLocation)
        val dayThreeTitleY = dayThreeTitleLocation[1]
        val progressBarInactiveY = timelineInactiveBarLocation[1]

        return ((dayThreeTitleY - progressBarInactiveY) * 1.1).toInt() + binding.timelineInactiveBar.height
    }
}