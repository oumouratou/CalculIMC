package com.example.calculimc

import org.junit.Assert.assertEquals
import org.junit.Test

class ImcCalculatorTest {

    @Test
    fun testCalculerImc_CorpulenceNormale() {
        val poids = 70.0
        val taille = 1.75
        val imc = ImcCalculator.calculerImc(poids, taille)
        val imcFormate = ImcCalculator.formaterImc(imc)

        assertEquals(22.86, imcFormate.toDouble(), 0.01)
        val (catRes, colorRes) = ImcCalculator.obtenirCategorieResId(imc)
        assertEquals(R.string.categorie_normale, catRes)
        assertEquals(R.color.imc_vert, colorRes)
    }

    @Test
    fun testCalculerImc_InsuffisancePonderale() {
        val poids = 50.0
        val taille = 1.75
        val imc = ImcCalculator.calculerImc(poids, taille)
        val imcFormate = ImcCalculator.formaterImc(imc)

        assertEquals(16.33, imcFormate.toDouble(), 0.01)
        val (catRes, colorRes) = ImcCalculator.obtenirCategorieResId(imc)
        assertEquals(R.string.categorie_insuffisance, catRes)
        assertEquals(R.color.imc_orange, colorRes)
    }

    @Test
    fun testCalculerImc_Surpoids() {
        val poids = 85.0
        val taille = 1.70
        val imc = ImcCalculator.calculerImc(poids, taille)
        val imcFormate = ImcCalculator.formaterImc(imc)

        assertEquals(29.41, imcFormate.toDouble(), 0.01)
        val (catRes, colorRes) = ImcCalculator.obtenirCategorieResId(imc)
        assertEquals(R.string.categorie_surpoids, catRes)
        assertEquals(R.color.imc_orange, colorRes)
    }

    @Test
    fun testCalculerImc_ObesiteModeree() {
        val poids = 100.0
        val taille = 1.70
        val imc = ImcCalculator.calculerImc(poids, taille)
        val imcFormate = ImcCalculator.formaterImc(imc)

        assertEquals(34.60, imcFormate.toDouble(), 0.01)
        val (catRes, colorRes) = ImcCalculator.obtenirCategorieResId(imc)
        assertEquals(R.string.categorie_obesite_moderee, catRes)
        assertEquals(R.color.imc_rouge, colorRes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCalculerImc_TailleZero_ThrowsException() {
        ImcCalculator.calculerImc(70.0, 0.0)
    }
}