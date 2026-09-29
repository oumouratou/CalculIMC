package com.example.calculimc

import java.util.Locale

object ImcCalculator {

    fun calculerImc(poids: Double, taille: Double): Double {
        require(taille > 0) { "La taille doit être supérieure à 0" }
        return poids / (taille * taille)
    }

    fun formaterImc(imc: Double): String {
        return String.format(Locale.US, "%.2f", imc)
    }

    fun convertirCmEnM(tailleCm: Double): Double {
        return tailleCm / 100.0
    }

    fun obtenirCategorieResId(imc: Double): Pair<Int, Int> {
        return when {
            imc < 18.5 -> R.string.categorie_insuffisance to R.color.imc_orange
            imc < 25.0 -> R.string.categorie_normale to R.color.imc_vert
            imc < 30.0 -> R.string.categorie_surpoids to R.color.imc_orange
            imc < 35.0 -> R.string.categorie_obesite_moderee to R.color.imc_rouge
            imc < 40.0 -> R.string.categorie_obesite_severe to R.color.imc_rouge
            else -> R.string.categorie_obesite_morbide to R.color.imc_rouge_fonce
        }
    }

    fun calculerValeurJauge(imc: Double): Int {
        return imc.toInt().coerceIn(10, 45)
    }
}