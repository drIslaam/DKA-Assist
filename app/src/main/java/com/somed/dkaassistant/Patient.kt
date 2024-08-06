package com.somed.dkaassistant 

import android.annotation.SuppressLint
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

open class Patient {
    //region Patient Input Data
    internal var age = 0 // Internal App calculations uses years
    internal var weight = 0.0 // Internal App calculations uses kg
    internal var rbs = 0.0 // Internal App calculations uses mg/dL
    internal var phosphorus = 0.0 // Internal App calculations uses mg/dL
    internal var magnesium = 0.0 // Internal App calculations uses mg/dL
    internal var potassium = 0.0 // Internal App calculations uses mmol/L
    internal var isEmptyPotassium = true

    internal var sodium = 0 // Internal App calculations uses mmol/L
    internal var chloride = 0.0 // Internal App calculations uses mmol/L
    internal var albumin = 0.0 // Internal App calculation uses g/dL
    internal var sodiumCorrected = 0 // Sodium Corrected for Serum Glucose Concentration Change
    internal var chlorideCorrected = 0.0 // Chloride Corrected for Serum Sodium Concentration Change
    internal var calcium = 0.0 // Internal App uses mg/dL
    internal var calciumCorrected = 0.0 // Internal App uses mg/dL
    internal var bun = 0.0// Internal App uses mg/dL
    internal var urea = 0.0 // Internal App uses mg/dL
    internal var pH = 0.0
    internal var pCO2 = 0.0 // Internal App calculation uses mmHg
    internal var ispCO2kPa = false
    internal var cHCO3 = 0.0 // Internal App calculation uses mmol/L
    internal var isEmptycHCO3 = true
    internal var isValidcHCO3 = true

    //region Calculated ABG Dat
    internal var cAG = 0.0
    internal var deltaRatio = 0.0
    internal var deltaGap = 0.0
    private var expectedpCO2 = 0.0
    private var expectedHCO3 = 0.0
    private var expectedAltHCO3 = 0.0
    private var dpHdHCO3Ratio = 0.0
    private var baselineCOPDCO2 = 0.0
    private var baselineCOPDHCO3 = 0.0
    //endregion


    private var requiredTotalMMOLPotassium = 20
    private var mmolPotassiumToSubtract = 0.0
    private var adolescentDehydrationPercentage = 0

    // internal var adultDKAMaintanenceFluidRate = 125
    internal var dextroseRate = 125
    internal var insulinRate = ""
    internal var typeOfSaline = ""
    internal var typeOfDextrose = ""
    internal var abgDx = ""
    internal var resusNote = ""
    internal var resusFluidType = ""
    internal var adultDKATotalFluidDeficit = 0
    internal var adultDKAMaintanenceFluidRate = 0
    internal var adultHHSTotalFluidDeficit = 0
    internal var pedDKATotalFluidDeficit = 0
    //endregion


    //region Medical Calculations Functions

    @SuppressLint("SetTextI18n")
    fun getDKAManagement() {
        // Resetiing the previous result if any
        insulinRate = ""
        typeOfSaline = ""
        typeOfDextrose = ""
        mmolPotassiumToSubtract = 0.0
        adultDKATotalFluidDeficit = 0
        adultHHSTotalFluidDeficit = 0
        adultDKAMaintanenceFluidRate = 0
        pedDKATotalFluidDeficit = 0      
        sodiumCorrected = 0
        chlorideCorrected = 0.0
        


        if (age >= 1 && weight >= 20.0 && rbs >= 10 && potassium >= 0.1) {
            getDKAInsulinRate()
            
            // Adult DKA protocol applied at least to body weight of 50 kg
            // The concept of categorization here is based on that any normal individual should reach 50 kg by age of 14
            

            if (age >= 14 && weight > 50) {// If Adolescent (>= 14 years) or Adult (>= 18 years) and Weight >= 50 kg
                adultDKATotalFluidDeficit = (weight * 100).toInt()
                resusNote = "RESUSCITATIVE PHASE (0-24 Hours):\n(If NOT Congested/Renal/Hepatic)${if (pH in 1.0 .. 6.9) "\nURGENTLY | Consider: Diluting 100 mmol NaHCO₃${if (potassium <5) " and 20 mmol KCl" else ""} in 400 Distilled H₂O over 2 hours" else ""} \n\n0-60 min:\nIf Shocked (SBP < 90 mmHg):\nGive 500cc 0.9% NaCl over 10-15 minutes,\n(Repeat up to only x${if (((weight*30)/500).toInt()> 6) 6 else ((weight*30)/500).toInt()}, Otherwise, Start Vasopressor)\n\nOnce NOT Shocked (SBP > 90 mmHg):\n1ˢᵗ hour: Give 1000cc 0.9% NaCl over 1 hour\n\nThen, ${if (potassium > 3.3 && rbs > 70) "Start " else ""}$insulinRate, while continuing IV fluid as:\n\n02ⁿᵈ hour-03ʳᵈ hour: 500 cc/h ${getTypeOfBaseInfusionFluid()}${if (rbs <= 108) " and, Add D₁₀W at rate of 125 mL/h till RBS > 108 mg/dL" else ""}\n${if (potassium < 5.3) "(Add KCl 10 mEq from 3ʳᵈ hour)" else "(DO NOT Add KCl)"}\n\n04ᵗʰ hour-24ᵗʰ hour: ${getRemainingReusFluid()}cc/h ${getTypeOfBaseInfusionFluid()}${if (rbs <= 108) " and, Add D₁₀W at rate of 125 cc/h" else ""}\n${if (potassium < 5.3 && getRemainingReusFluid().toDouble() >= 250.0) "(Add KCl 15 mEq in each bottle on)" else ""}${if (potassium < 5.3 && getRemainingReusFluid().toDouble() < 350.0) "(Add KCl 20 mEq in each bottle on)" else ""}${if (potassium >= 5.3) "(DO NOT Add KCl)" else ""}\n\n\nPLEASE, Recheck this current plan once get updated data as IVI may change\n\nREMEMBER: Keep serum glucose between 140-180 mg/dL, until resolution of DKA\nKeep serum K⁺ between 4-5 mmol/L"
            
                adultDKAMaintanenceFluidRate = approximateRate(calculateMaintenanceFluid(weight))
                

            }else {// Adult less that 18 years old

                getDKATypeOfSaline()
                getDKATypeOfDextrose()
                
                if (potassium >= 3.3) {
                    when (rbs) {
                        in 300.0..1200.0 -> typeOfSaline =
                            "$typeOfSaline @ ${approximateRate(getInfusionFluid())} mL/h"

                        in 250.0..299.0 -> typeOfSaline =
                            "$typeOfSaline @ ${approximateRate(getInfusionFluid() * 0.75)} mL/h + \n $typeOfDextrose @ ${
                                approximateRate(getInfusionFluid() * .25)
                            } mL/h"

                        in 200.0..249.0 -> typeOfSaline =
                            "$typeOfSaline @ ${approximateRate(getInfusionFluid() * 0.5)} mL/h + \n $typeOfDextrose @ ${
                                approximateRate(getInfusionFluid() * 0.5)
                            } mL/h"

                        in 150.0..199.0 -> typeOfSaline =
                            "$typeOfSaline @ ${approximateRate(getInfusionFluid() * 0.25)} mL/h + \n $typeOfDextrose @ ${
                                approximateRate(getInfusionFluid() * 0.75)
                            } mL/h"

                        in 1.0..149.0 -> typeOfSaline =
                            "$typeOfDextrose @ ${approximateRate(getInfusionFluid())} mL/h"
                    }
                } else {
                    when (sodium) {
                        in 131..190 -> typeOfSaline =
                            "KCl 10 mEq + 200 mL 0.45% NaCl over 1 hour for 3 hours"

                        in 1..130 -> typeOfSaline =
                            "KCl 10 mEq + 200 mL 0.9% NaCl over 1 hour for 3 hours"
                    }
                }

                resusNote =
                    "Note:\nIf patient is NOW in Resuscitation Phase: (0-60 minute)\n\nIf Shocked (HR, capillary refill time):\nGive Patient ${(20 * weight).toInt()} mL of 0.9% NaCl over 15 minutes\nIf required, give further ${(10 * weight).toInt()} mL boluses up to a max. 4 times\nat which point inotropes should be considered\nThis bolus should NOT be subtracted from the fluid maintenance\n\nIf Non-shocked patients:\n${(10 * weight).toInt()} mL bolus of 0.9% NaCl over 60 minutes\nThese boluses SHOULD be subtracted from the fluid maintenance"
            }
        }else{
            resusNote = ""
        }
    }
    
    
        
        
    
    private fun getTypeOfBaseInfusionFluid() : String{
        if (sodium != 0){
            sodiumCorrected = getCorrectedSodium(sodium)
            if (chloride != 0.0){
                // Normal sodium 142, Normal Chloride 104-108
                chlorideCorrected = chloride*(142.0/sodiumCorrected)
            }
        }
        
        return when {
            // Low sodium and Low chloride or no value entered
            ((sodiumCorrected in 1 .. 134 && chlorideCorrected in 1.0 .. 108.0) && (rbs > 250.0 || rbs < 108.0)) || ((sodiumCorrected == 0 && chlorideCorrected == 0.0) && (rbs > 250.0 || rbs < 108.0))   -> "0.9% NaCl"
            ((sodiumCorrected in 1 .. 134 && chlorideCorrected in 1.0 .. 108.0) && rbs in 108.0..250.0) || ((sodiumCorrected == 0 && chlorideCorrected == 0.0) && rbs in 108.0 .. 250.0) -> "D₅W in 0.9% NaCl"
            
            // High sodium and High chloride
            ((sodiumCorrected >= 135 || chlorideCorrected > 108.0) && (rbs > 250.0 || rbs < 108))  -> "0.45% NaCl"
            ((sodiumCorrected >= 135 || chlorideCorrected > 108.0) && rbs in 108.0 .. 250.0) -> "D₅W in 0.45% NaCl"
            
            else -> "0.9% NaCl"
        }
    }
    
    fun getRemainingReusFluid () : String {
        var estimatedRate = approximateRate(((adultDKATotalFluidDeficit-2000)/21).toDouble())
        if (estimatedRate > 250 && rbs <= 200){
            estimatedRate = 250
        }
        return estimatedRate.toString()
    }
    
    private fun getDKAInsulinRate() {
        // According to Al-Baha Protocol
        insulinRate = if (potassium < 3.3 && rbs >= 70) {
            "Hold Insulin Infusion, Perform ECG, Give KCl 20 mmol + ${getTypeOfBaseInfusionFluid()} 500 mL Stat over 2 hours (may need to repeat) and recheck serum K⁺ level"
        } else if (potassium < 3.3 && rbs in 1.0..69.0) {
            "Hold Insulin Infusion,\n Urgent D₅₀W 50 mL Stat, and Give KCl 20 mmol + ${getTypeOfBaseInfusionFluid()} 500 mL Stat over 30 minutes (may need to repeat) and recheck serum K⁺ level"
        } else{
            when (rbs) {
                in 201.0..1200.0 -> "Regular Insulin Infusion Rate:\n${"%.1f".format(weight * 0.1)} mL/h (0.1 u/kg/h)\n Consider increase the rate by 1 unit/h if "
                in 175.0..200.0 -> "Regular Insulin Infusion Rate: \n${"%.1f".format(weight * 0.05)} mL/h (0.05 u/kg/h)"
                in 150.0..174.0 -> "Regular Insulin Infusion Rate: \n${"%.1f".format(weight * 0.04)} mL/h (0.04 u/kg/h)"
                in 125.0..149.0 -> "Regular Insulin Infusion Rate: \n${"%.1f".format(weight * 0.03)} mL/h (0.03 u/kg/h)"
                in 70.0..124.0 -> "Regular Insulin Infusion Rate: \n${"%.1f".format(weight * 0.02)} mL/h (0.02 u/kg/h)"
                else -> "Hold Insulin Infusion,\nUrgent D₅₀W 50 mL Stat"
            }
        }
    }
    
    

    private fun getDKATypeOfSaline() {
        val potassiumToSubtract = getMMOLPotassiumToSubtract()
        val sodiumCorrected = getCorrectedSodium(sodium)
        
        typeOfSaline = when {
            // Low sodium and Low chloride and high potassium and low phosphorus
            sodiumCorrected < 135 && chloride < 106 && potassium >= 5.3 && phosphorus in 0.1..2.8 -> "0.9% NaCl 500mL + Sodium Glycerophosphate ${getPhosDose()} mL at $adultDKAMaintanenceFluidRate mL/h"
            // Low sodium and Low chloride and low potassium and high phosphorus
            sodiumCorrected < 135 && chloride < 106 && potassium < 5.3 && (phosphorus >= 2.8 || phosphorus == 0.0) -> "0.9% NaCl 500mL + KCl $requiredTotalMMOLPotassium mEq at $adultDKAMaintanenceFluidRate mL/h"
            // Low sodium and Low chloride and low potassium and low phosphorus but no KCl requied
            sodiumCorrected < 135 && chloride < 106 && potassium < 5.3 && phosphorus in 0.1..2.8 && potassiumToSubtract > 15 -> "0.9% NaCl 500mL + KCl $requiredTotalMMOLPotassium mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Sodium Glycerophosphate ${getPhosDose()} mL + 0.9% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \n\n OR \n\n 0.9% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Potassium Phosphate ${getPhosDose() / 3} mL + 0.9% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h"
            // Low sodium and Low chloride and low potassium and low phosphorus but KCl requied
            sodiumCorrected < 135 && chloride < 106 && potassium < 5.3 && phosphorus in 0.1..2.8 && potassiumToSubtract <= 15 -> "0.9% NaCl 500mL + KCl $requiredTotalMMOLPotassium mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Sodium Glycerophosphate ${getPhosDose()} mL + 0.9% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \n" +
                    "\n" +
                    " OR \n" +
                    "\n" +
                    " 0.9% NaCl 500mL + KCl ${requiredTotalMMOLPotassium - potassiumToSubtract} mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Potassium Phosphate ${getPhosDose() / 3} mL + 0.9% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h"


            // High sodium or high chloride and high potassium and low phosphorus
            (sodiumCorrected >= 135 || chloride >= 106) && potassium >= 5.3 && phosphorus in 0.1..2.8 -> "0.45% NaCl 500mL + Sodium Glycerophosphate ${getPhosDose()} mL at $adultDKAMaintanenceFluidRate mL/h"
            // High sodium or high chloride and low potassium and high phosphorus
            (sodiumCorrected >= 135 || chloride >= 106) && potassium < 5.3 && (phosphorus >= 2.8 || phosphorus == 0.0) -> "0.45% 500mL NaCl + KCl $requiredTotalMMOLPotassium mEq at $adultDKAMaintanenceFluidRate mL/h"
            // High sodium or high chloride and low potassium and low phosphorus
            (sodiumCorrected >= 135 || chloride >= 106) && potassium < 5.3 && phosphorus in 0.1..2.8 && potassiumToSubtract > 15 -> "0.45% NaCl 500mL + KCl $requiredTotalMMOLPotassium mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Sodium Glycerophosphate ${getPhosDose()} mL + 0.45% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \n\n OR \n\n 0.45% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Potassium Phosphate ${getPhosDose() / 3} mL + 0.45% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h"
            // High sodium or high chloride and low potassium and low phosphorus
            (sodiumCorrected >= 135 || chloride >= 106) && potassium < 5.3 && phosphorus in 0.1..2.8 && potassiumToSubtract <= 15 -> "0.45% NaCl 500mL + KCl $requiredTotalMMOLPotassium mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Sodium Glycerophosphate ${getPhosDose()} mL + 0.45% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h \n" +
                    "\n" +
                    " OR \n" +
                    "\n" +
                    " 0.45% NaCl 500mL + KCl ${requiredTotalMMOLPotassium - potassiumToSubtract} mEq at ${adultDKAMaintanenceFluidRate / 2} mL/h \nand Potassium Phosphate ${getPhosDose() / 3} mL + 0.45% NaCl 500mL at ${adultDKAMaintanenceFluidRate / 2} mL/h"

            // High sodium and high potassium and high phosphorus
            (sodiumCorrected >= 135 || chloride > 106) && potassium >= 5.3 && phosphorus >= 2.8 -> "0.45% NaCl 500mL at $adultDKAMaintanenceFluidRate mL/h"

            // Low sodium and low chloride and high potassium and high phosphorus
            else -> "0.9% NaCl 500mL at $adultDKAMaintanenceFluidRate mL/h"
        }
    }

    private fun getDKATypeOfDextrose() {
        typeOfDextrose = if (rbs < 200) "D₁₀W 500mL at $dextroseRate mL/h" else ""
    }
    
    private fun getMMOLPotassiumToSubtract(): Int {
        return (getPhosDose() / 3 * 4.4).toInt()
    }
    


    private fun getPhosDose(): Int {
        val dose = when (phosphorus) {
            in 1.4..2.8 -> 0.2 * weight
            in 1.0..1.4 -> 0.3 * weight
            in 0.1..1.0 -> 0.4 * weight
            else -> 0.0
        }
        return dose.toInt()
    }

    

    private fun approximateRate(number: Double): Int {
        val approximateNumber = number - (number % 10) + if (number % 10 >= 5) 10 else 0
        return approximateNumber.toInt()
    }


    private fun getCorrectedSodium(oSodium:Int) : Int{
        var cSodium = 0
        if (rbs > 100 && oSodium != 0) {
            cSodium = (oSodium + (0.020 * (rbs - 100))).toInt()
        }else {
            cSodium = oSodium
        }
        
        return cSodium
    }
    
    

    private fun getDehydrationPercentage(): Int {
        adolescentDehydrationPercentage = when {
            pH in 7.2..7.29 || cHCO3 in 10.0..14.9 -> 5
            pH in 7.1..7.19 || cHCO3 in 5.0..9.9 -> 5
            pH in 6.8..7.09 || cHCO3 in 0.0..4.9 -> 10
            else -> 0 // default value if none of the conditions are met
        }

        return adolescentDehydrationPercentage
    }

    // Function to calculate fluid deficit based on dehydration level and weight
    private fun calculateFluidDeficit(): Double {
        return (getDehydrationPercentage() * weight * 10) - (weight * 10)
    }

    // Function to calculate maintenance fluid based on body weight using the Holliday-Segar formula return mL/day
    private fun calculateMaintenanceFluid(): Double {
        val maintenanceFluid = when {
            weight <= 10 -> 100 * weight
            weight <= 20 -> 1000 + (weight - 10) * 50
            else -> 1500 + (weight - 20) * 20
        }
        return maintenanceFluid
    }

    // Function to calculate hourly fluid rate
    private fun getInfusionFluid(): Double {
        return (calculateFluidDeficit() / 48) + (calculateMaintenanceFluid() / 24)
    }

    fun getInfusionFluid(weight: Double): Int {

        var fluidRequirement: Double = if (weight <= 10.0) {
            weight * 4 // 4 mL/kg/hour for the first 10 kg
        } else if (weight <= 20.0) {
            40 + (weight - 10) * 2 // 40 mL/hour for the first 10 kg, plus 2 mL/kg/hour for every kg above 10 kg
        } else {
            60 + (weight - 20) * 1 // 60 mL/hour for the first 20 kg, plus 1 mL/kg/hour for every kg above 20 kg
        }

// Limit the fluid requirement to a maximum of 100 mL/hour
        fluidRequirement = fluidRequirement.coerceAtMost(100.0)

        return approximateRate(fluidRequirement)
    }
    
    private fun calculateMaintenanceFluid(weight: Double): Double {
        return when {
            weight <= 10 -> weight * 4
            weight <= 20 -> 40 + (weight - 10) * 2
            else -> 60 + (weight - 20) * 1
        }
    }


    private fun approximateRateBak(number: Int): Int {
        val remainder = number % 10
        val approximateNumber = if (remainder < 5) {
            number - remainder // Round down to the nearest multiple
        } else {
            number - remainder + 10 // Round up to the nearest multiple
        }

        return approximateNumber
    }
    
    
    //endregion

    @SuppressLint("SetTextI18n")
    internal fun getABGDx() {
        // Resetting previous ABG Diagnosis if any
        abgDx = ""

        cAG = 0.0
        deltaRatio = 0.0
        deltaGap = 0.0

        // Check if the user entered a valid pH and pCO2 Values
        if (pH != 0.0 && pCO2 != 0.0) {

            // Check if the user entered a valid HCO3 Value, and Calculate cHCO3 if Not
            if (isEmptycHCO3 || !isValidcHCO3) {
                cHCO3 = getaHCO3RM()
            }

            // Check if the user entered Sodium and Chloride Values, to Calculate cAG
            if (sodium != 0 && chloride != 0.0) {
                // Check if the user entered a Albumin Value to calculated Corrected Anion Gap
                cAG = if (albumin != 0.0) {
                    sodium - (chloride + cHCO3) + (2.5 * (4.5 - albumin))
                } else {
                    sodium - (chloride + cHCO3)
                }

                deltaRatio = (cAG - 12) / (24 - cHCO3)
                deltaGap = sodium.toDouble() - chloride.toDouble() - 36.0
            }

            // Check and Classify the ABG into 4 Primary Disorders
            if (pH in 7.38..7.42 && pCO2 in 38.0..42.0 && cHCO3.roundToInt() in 22..26) {

                abgDx = "Normal ABG"

            } else if (pH <= 7.40 && pCO2 <= 40) {
                abgDx = "Metabolic Acidosis"

                if (cAG != 0.0) {
                    abgDx = getDeltaRatioDx()
                }

                expectedpCO2 = (cHCO3 * 1.5) + 8

                if (pCO2 > expectedpCO2 + 2) {
                    abgDx = "$abgDx, with Respiratory Acidosis"
                } else if (pCO2 < expectedpCO2 - 2) {
                    abgDx = "$abgDx, with Respiratory Alkalosis"
                }

            } else if (pH <= 7.40 && pCO2 >= 40) {

                abgDx = "Respiratory Acidosis"

                dpHdHCO3Ratio = (740 - pH * 100) / (pCO2 - 40)

                when {
                    dpHdHCO3Ratio in 0.29..0.31 -> {
                        /*
                        // The expected serum HC0₃ for this chronic process,
                        // If there is appropriate (complete) chronic renal compensation
                        // (that requires several days (4 to 7).
                        // So, the patient's past baseline value for PaCO₂ should be 40 mmHg,
                        // before developing COPD,
                        // and the baseline HC0₃ is 24 mEq/L, before renal compensation.
                        // Note that: In practice renal compensation becomes less efficient as the
                        // PaCO₂ rises above 80 mm Hg, but we assume 100% efficiency to reduce the
                        // complexity.
                        */
                        expectedHCO3 = 24 + (0.4 * (pCO2 - 40.0))

                        abgDx = when {
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Chronic Respiratory Acidosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Chronic Respiratory Acidosis, with Metabolic Acidosis"
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Chronic Respiratory Acidosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Chronic Respiratory Acidosis, with Metabolic Alkalosis"
                            else -> "Chronic Respiratory Acidosis, with Adequated Metabolic Compensation"
                        }
                    }

                    dpHdHCO3Ratio in 0.79..0.81 -> {
                        /*
                        // The expected serum HC0₃ for this acute process,
                        // If there is appropriate (complete) acute renal compensation
                        // (that requires only about 24 hours).
                        // So the patient's baseline values for PaCO₂ should be 40 mmHg,
                        // and the target baseline HC0₃ is 24 mEq/L.
                        */
                        expectedHCO3 = 24 + (0.1 * (pCO2 - 40.0))

                        abgDx = when {
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Acute Respiratory Acidosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Acute Respiratory Acidosis, with Metabolic Acidosis"
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Acute Respiratory Acidosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Acute Respiratory Acidosis, with Metabolic Alkalosis"
                            else -> "Acute Respiratory Acidosis, with Adequated Metabolic Compensation"
                        }
                    }

                    dpHdHCO3Ratio in 0.31..0.79 -> {

                        baselineCOPDCO2 = 4 * (cHCO3 - 10 - (0.10 * pCO2))
                        baselineCOPDHCO3 = 24 + 0.4 * (baselineCOPDCO2 - 40.0)
                        expectedHCO3 =
                            24 + (0.4 * (baselineCOPDCO2 - 40.0)) + (0.1 * (pCO2 - baselineCOPDCO2))
                        expectedAltHCO3 = 24 + (0.1 * (pCO2 - 40.0))

                        // Convert pCO2 to kPa to be dispalyed
                        if (ispCO2kPa) {
                            baselineCOPDCO2 /= 7.50062
                        }

                        if (!ispCO2kPa) {
                            abgDx = when {
                                cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${baselineCOPDCO2.toInt()} mmHg, Expected baseline HCO₃: ${
                                    "%.1f".format(
                                        baselineCOPDHCO3
                                    )
                                }), with " + getDeltaRatioDx()

                                cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${baselineCOPDCO2.toInt()} mmHg, Expected basline HCO₃: ${
                                    "%.1f".format(
                                        baselineCOPDHCO3
                                    )
                                }), with Metabolic Acidosis"

                                cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${baselineCOPDCO2.toInt()} mmHg, Expected basline HCO₃: ${
                                    "%.1f".format(
                                        baselineCOPDHCO3
                                    )
                                }), with " + getDeltaRatioDx()

                                cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${baselineCOPDCO2.toInt()} mmHg, Expected basline HCO₃: ${
                                    "%.1f".format(
                                        baselineCOPDHCO3
                                    )
                                }), with Metabolic Alkalosis"

                                else -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${baselineCOPDCO2.toInt()} mmHg, Expected basline HCO₃: ${
                                    "%.1f".format(
                                        baselineCOPDHCO3
                                    )
                                }), with Adequate Metabolic Compensation${if (cAG > 12) ", and Hidden Anion Gap Acidosis" else ""}"
                            }
                        } else {
                            abgDx = when {
                                cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${
                                    "%.1f".format(
                                        baselineCOPDCO2
                                    )
                                } kPa, Expected basline HCO₃: ${"%.1f".format(baselineCOPDHCO3)}), with " + getDeltaRatioDx()

                                cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${
                                    "%.1f".format(
                                        baselineCOPDCO2
                                    )
                                } kPa, Expected basline HCO₃: ${"%.1f".format(baselineCOPDHCO3)}), with Metabolic Acidosis"

                                cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${
                                    "%.1f".format(
                                        baselineCOPDCO2
                                    )
                                } kPa, Expected basline HCO₃: ${"%.1f".format(baselineCOPDHCO3)}), with " + getDeltaRatioDx()

                                cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${
                                    "%.1f".format(
                                        baselineCOPDCO2
                                    )
                                } kPa, Expected basline HCO₃: ${"%.1f".format(baselineCOPDHCO3)}), with Metabolic Alkalosis"

                                else -> "Acute-on-Chronic Respiratory Acidosis (Expected basline pCO₂: ${
                                    "%.1f".format(
                                        baselineCOPDCO2
                                    )
                                } kPa, Expected basline HCO₃: ${"%.1f".format(baselineCOPDHCO3)}), with Adequate Metabolic Compensation${if (cAG > 12) ", and Hidden Anion Gap Acidosis" else ""}"
                            }
                        }
                        
                        if (baselineCOPDCO2 < 45) {
                            abgDx = ""
                        }else {
                            abgDx =abgDx + ", Or, "
                        }

                        // Alternative Diagnosis
                        abgDx = when {
                            cHCO3 < expectedAltHCO3 - 1 && cAG != 0.0 -> "Acute Respiratory Acidosis on top of underlying Metabolic Alkalosis, with " + getDeltaRatioDx()

                            cHCO3 < expectedAltHCO3 - 1 && cAG == 0.0 -> "Acute Respiratory Acidosis on top of underlying Metabolic Alkalosis, with Metabolic Acidosis"

                            cHCO3 > expectedAltHCO3 + 1 && cAG > 12.0 -> "Acute Respiratory Acidosis on top of underlying Metabolic Alkalosis, with " + getDeltaRatioDx()

                            cHCO3 > expectedAltHCO3 + 1 && cAG <= 12.0 -> "Acute Respiratory Acidosis on top of underlying Metabolic Alkalosis"

                            else -> "Acute Respiratory Acidosis on top of underlying Metabolic Alkalosis, with Adequate Metabolic Compensation${if (cAG > 12) ", and underlying Anion Gap Acidosis" else ""}"
                        }
                    }

                    dpHdHCO3Ratio < 0.29 -> {
                        expectedHCO3 = 24 + (0.4 * (pCO2 - 40.0))

                        abgDx =
                            if (cHCO3.toInt() < expectedHCO3.toInt() && (sodium != 0 || chloride != 0.0)) {
                                "Chronic Respiratory Acidosis, with Metabolic Acidosis"
                            } else if (cHCO3.toInt() < expectedHCO3.toInt() && sodium != 0 && chloride != 0.0) {
                                "Chronic Respiratory Acidosis, with " + getDeltaRatioDx()
                            } else if (cHCO3.toInt() > expectedHCO3.toInt() && sodium != 0 && chloride != 0.0) {
                                "Chronic Respiratory Acidosis, with Metabolic Alkalosis${if (cAG > 12) ", and underlying ${getDeltaRatioDx()}" else ""}"
                            }else{
                                "Chronic Respiratory Acidosis"
                            }
                    }

                    dpHdHCO3Ratio > 0.81 -> {
                        expectedHCO3 = 24 + (0.1 * (pCO2 - 40.0))

                        abgDx =
                            if (cHCO3.toInt() < expectedHCO3.toInt() && sodium != 0 && chloride != 0.0) {
                                "Acute Respiratory Acidosis, with " + getDeltaRatioDx()
                            } else {
                                "Acute Respiratory Acidosis, with Metabolic Acidosis"
                            }
                    }
                }

            } else if (pH >= 7.40 && pCO2 >= 40) {
                abgDx = "Metabolic Alkalosis"

                expectedpCO2 = (0.7 * cHCO3) + 20.0

                if (cAG > 12) {
                    abgDx = "$abgDx, with " + getDeltaRatioDx()
                }

                if (pCO2 > expectedpCO2 + 1.5) {
                    abgDx = "$abgDx, \n with Respiratory Acidosis"
                } else if (pCO2 < expectedpCO2 - 1.5) {
                    abgDx = "$abgDx, \n with Respiratory Alkalosis"
                }
            } else if (pH >= 7.40 && pCO2 <= 40) {
                abgDx = "Respiratory Alkalosis"

                dpHdHCO3Ratio = ((pH * 100) - 740) / (40 - pCO2)

                when {
                    dpHdHCO3Ratio in 0.16..0.18 -> {
                        expectedHCO3 = 24 - (0.5 * (40.0 - pCO2))

                        abgDx = when {
                            cHCO3 < expectedHCO3 - 2 && cAG != 0.0 -> "Chronic Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3 < expectedHCO3 - 2 && cAG == 0.0 -> "Chronic Respiratory Alkalosis, with Metabolic Acidosis"
                            cHCO3 > expectedHCO3 + 2 && cAG > 12.0 -> "Chronic Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3 > expectedHCO3 + 2 && cAG <= 12.0 -> "Chronic Respiratory Alkalosis, with Metabolic Alkalosis"
                            else -> "Chronic Respiratory Alkalosis, with Adequate Metabolic Compensation"
                        }
                    }

                    dpHdHCO3Ratio in 0.79..0.81 -> {
                        expectedHCO3 = 24 - (0.2 * (40.0 - pCO2))

                        abgDx = when {
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Acute Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Acute Respiratory Alkalosis, with Metabolic Acidosis"
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Acute Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Acute Respiratory Alkalosis, with Metabolic Alkalosis"
                            else -> "Acute Respiratory Alkalosis, with Adequate Metabolic Compensation"
                        }
                    }

                    dpHdHCO3Ratio in 0.18..0.79 -> {
                        expectedHCO3 = 24 - (0.2 * (40.0 - pCO2))

                        abgDx = when {
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG != 0.0 -> "Partially Compensated Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() < expectedHCO3.toInt() && cAG == 0.0 -> "Partially Compensated Respiratory Alkalosis, with Metabolic Acidosis"
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG > 12.0 -> "Partially Compensated Respiratory Alkalosis, with " + getDeltaRatioDx()
                            cHCO3.toInt() > expectedHCO3.toInt() && cAG <= 12.0 -> "Partially Compensated Respiratory Alkalosis, with Metabolic Alkalosis"
                            else -> "Partially Compensated Respiratory Alkalosis, with Adequate Metabolic Compensation"
                        }
                    }

                    dpHdHCO3Ratio < 0.16 -> {
                        expectedHCO3 = 24 - (0.5 * (40.0 - pCO2))

                        abgDx =
                            if (cHCO3.toInt() < expectedHCO3.toInt() && sodium != 0 && chloride != 0.0) {
                                "Chronic Respiratory Alkalosis, with " + getDeltaRatioDx()
                            } else {
                                "Chronic Respiratory Alkalosis, with Metabolic Acidosis"
                            }
                    }

                    dpHdHCO3Ratio > 0.81 -> {
                        expectedHCO3 = 24 - (0.2 * (40.0 - pCO2))

                        abgDx =
                            if (cHCO3.toInt() < expectedHCO3.toInt() && (sodium == 0 || chloride == 0.0)) {
                                "Acute Respiratory Alkalosis, with Metabolic Acidosis"
                            } else if ((cHCO3.toInt() < expectedHCO3.toInt() || cAG > 12) && sodium != 0 && chloride != 0.0) {
                                "Acute Respiratory Alkalosis, with " + getDeltaRatioDx()
                            } else {
                                "Acute Respiratory Alkalosis, with Metabolic Alkalosis"
                            }
                    }
                }
            }
        }
    }

    private fun getDeltaRatioDx(): String {
        return when {
            deltaRatio in 0.0..0.40 && cAG > 12 -> "Anion and Non-Anion Gap Acidosis"
            deltaRatio in 0.0..0.40 && cAG <= 12 -> "Non-Anion Gap Acidosis"
            deltaRatio > 0.40 && deltaRatio < 0.80 -> "Anion and Non-Anion Gap Acidosis"
            deltaRatio >= 0.80 && deltaRatio < 1.0 -> getDeltaGapDx()
            deltaRatio in 1.0..2.0 -> "Anion Gap Acidosis"
            deltaRatio > 2 -> "Anion Gap Metabolic Acidosis, and underlying Metabolic Alkalosis"
            deltaRatio.isInfinite() -> getDeltaGapDx() // If cHCO3 is 24 mmol/L and the difference would be zero, we can not divide by zero
            deltaRatio < 0 && cAG > 12 -> "Anion Gap Acidosis" // Negative Delta Ratio indicates Metabolic Acidosis hidden within Primary Metabolic Alkalosis
            deltaRatio < 0 && cAG <= 12 -> "Non-Anion Gap Acidosis" // Negative Delta Ratio indicates Metabolic Acidosis hidden within Primary Metabolic Alkalosis
            else -> ""
        }
    }

    private fun getDeltaGapDx(): String {
        return when {
            deltaGap <= -6.0 -> "Anion and Non-Anion Gap Acidosis"
            deltaGap in -5.99..5.99 -> "Anion Gap Acidosis"
            deltaGap >= 6.0 -> "Anion Gap Metabolic Acidosis, and underlying Metabolic Alkalosis"
            else -> ""
        }
    }

    internal fun getaHCO3RM(): Double {
        /*
        * This Equation is the Version Used in Radiometer ABL Analyzers, This
        * Equation Includes Ions Hydrogen Carbonate, Carbonate, and Carbamate
        * in the Plasma, Totally Known As cHCO3-(P).
        *
        * At temperature of 37 ℃, the "CO₂ Solubility Coefficient" is 0.231
        * mmol.l⁻¹.kPa⁻¹ (i.e., 0.0308 mmol.l⁻¹.mmHg⁻¹), pK is not
        * constant, but varies with different pH values.
        *
        * 760 mmHg equals 101.325 kPa, therefore the conversion factor from
        * mmHg to kPa is (101.325/760 = 0.133322368 or 1/7.5)
     */

        // Calculation of Dissociation Constant
        val pK = 6.125 - log10(1 + 10.0.pow(pH - 8.7))
        return 0.230 * pCO2 * (101.325 / 760) * 10.0.pow(pH - pK)
    }
}