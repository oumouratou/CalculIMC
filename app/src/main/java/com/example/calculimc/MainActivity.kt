package com.example.calculimc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var editTextPoids: EditText
    private lateinit var editTextTaille: EditText
    private lateinit var inputLayoutTaille: TextInputLayout
    private lateinit var toggleGroupUnite: MaterialButtonToggleGroup
    private lateinit var buttonCalculer: Button
    private lateinit var buttonEffacer: Button
    private lateinit var textViewImc: TextView
    private lateinit var textViewCategorie: TextView
    private lateinit var progressBarImc: LinearProgressIndicator
    private lateinit var buttonPartager: MaterialButton
    private lateinit var containerHistorique: LinearLayout
    private lateinit var textViewHistoriqueVide: TextView
    private lateinit var buttonViderHistorique: ImageButton

    private var dernierImcFormate: String = ""
    private var derniereCategorie: String = ""
    private var estUniteCm: Boolean = false
    private val listeHistorique = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        chargerHistorique()
        setupListeners()

        if (savedInstanceState != null) {
            restaurerEtat(savedInstanceState)
        }
    }

    private fun initViews() {
        editTextPoids = findViewById(R.id.editTextPoids)
        editTextTaille = findViewById(R.id.editTextTaille)
        inputLayoutTaille = findViewById(R.id.inputLayoutTaille)
        toggleGroupUnite = findViewById(R.id.toggleGroupUnite)
        buttonCalculer = findViewById(R.id.buttonCalculer)
        buttonEffacer = findViewById(R.id.buttonEffacer)
        textViewImc = findViewById(R.id.textViewImc)
        textViewCategorie = findViewById(R.id.textViewCategorie)
        progressBarImc = findViewById(R.id.progressBarImc)
        buttonPartager = findViewById(R.id.buttonPartager)
        containerHistorique = findViewById(R.id.containerHistorique)
        textViewHistoriqueVide = findViewById(R.id.textViewHistoriqueVide)
        buttonViderHistorique = findViewById(R.id.buttonViderHistorique)

        progressBarImc.max = 45
    }

    private fun setupListeners() {
        toggleGroupUnite.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                estUniteCm = (checkedId == R.id.btnUniteCm)
                inputLayoutTaille.hint = if (estUniteCm) {
                    getString(R.string.hint_taille_cm)
                } else {
                    getString(R.string.hint_taille)
                }
            }
        }

        buttonCalculer.setOnClickListener {
            calculerIMC()
        }

        buttonEffacer.setOnClickListener {
            effacerChamps()
        }

        buttonPartager.setOnClickListener {
            partagerResultat()
        }

        buttonViderHistorique.setOnClickListener {
            effacerHistorique()
        }
    }

    private fun calculerIMC() {
        editTextPoids.error = null
        editTextTaille.error = null

        val poidsStr = editTextPoids.text.toString().trim()
        val tailleStr = editTextTaille.text.toString().trim()

        var estValide = true

        if (poidsStr.isEmpty()) {
            editTextPoids.error = getString(R.string.erreur_champ_vide)
            estValide = false
        }

        if (tailleStr.isEmpty()) {
            editTextTaille.error = getString(R.string.erreur_champ_vide)
            estValide = false
        }

        if (!estValide) {
            Toast.makeText(this, R.string.toast_erreur, Toast.LENGTH_SHORT).show()
            reinitialiserAffichageResultat()
            return
        }

        val poids = poidsStr.replace(',', '.').toDoubleOrNull()
        val taille = tailleStr.replace(',', '.').toDoubleOrNull()

        if (poids == null) {
            editTextPoids.error = getString(R.string.erreur_valeur_invalide)
            estValide = false
        } else if (poids <= 0) {
            editTextPoids.error = getString(R.string.erreur_valeur_positive)
            estValide = false
        }

        if (taille == null) {
            editTextTaille.error = getString(R.string.erreur_valeur_invalide)
            estValide = false
        } else if (taille <= 0) {
            editTextTaille.error = getString(R.string.erreur_valeur_positive)
            estValide = false
        }

        if (!estValide) {
            Toast.makeText(this, R.string.toast_erreur, Toast.LENGTH_SHORT).show()
            reinitialiserAffichageResultat()
            return
        }

        var tailleEnMetres = if (estUniteCm) ImcCalculator.convertirCmEnM(taille!!) else taille!!
        if (!estUniteCm && (tailleEnMetres > 3.0)) {
            tailleEnMetres /= 100.0
        }

        val imc = ImcCalculator.calculerImc(poids!!, tailleEnMetres)
        dernierImcFormate = String.format(Locale.getDefault(), "%.2f", imc)

        textViewImc.text = getString(R.string.resultat_imc, dernierImcFormate)

        val (catResId, colorResId) = ImcCalculator.obtenirCategorieResId(imc)
        derniereCategorie = getString(catResId)

        textViewCategorie.text = derniereCategorie
        val color = ContextCompat.getColor(this, colorResId)
        textViewCategorie.setTextColor(color)

        // Affichage de la jauge colorée
        progressBarImc.progress = ImcCalculator.calculerValeurJauge(imc)
        progressBarImc.setIndicatorColor(color)
        progressBarImc.visibility = View.VISIBLE

        // Affichage du bouton de partage
        buttonPartager.visibility = View.VISIBLE

        // Ajout à l'historique
        val uniteAffichage = if (estUniteCm) "${taille.toInt()} cm" else "${String.format(Locale.getDefault(), "%.2f", tailleEnMetres)} m"
        val entreeHistorique = "${poids.toInt()} kg / $uniteAffichage → IMC $dernierImcFormate ($derniereCategorie)"
        ajouterAHistorique(entreeHistorique)
    }

    private fun partagerResultat() {
        if (dernierImcFormate.isEmpty()) return

        val message = getString(R.string.message_partage, dernierImcFormate, derniereCategorie)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.partager_titre))
        startActivity(shareIntent)
    }

    private fun effacerChamps() {
        editTextPoids.text?.clear()
        editTextTaille.text?.clear()
        editTextPoids.error = null
        editTextTaille.error = null
        reinitialiserAffichageResultat()
        editTextPoids.requestFocus()
    }

    private fun reinitialiserAffichageResultat() {
        textViewImc.text = ""
        textViewCategorie.text = ""
        progressBarImc.visibility = View.GONE
        buttonPartager.visibility = View.GONE
        dernierImcFormate = ""
        derniereCategorie = ""
    }

    private fun ajouterAHistorique(entree: String) {
        listeHistorique.add(0, entree)
        if (listeHistorique.size > MAX_HISTORIQUE) {
            listeHistorique.removeAt(listeHistorique.size - 1)
        }
        sauvegarderHistoriqueLocal()
        afficherHistorique()
    }

    private fun effacerHistorique() {
        listeHistorique.clear()
        sauvegarderHistoriqueLocal()
        afficherHistorique()
    }

    private fun afficherHistorique() {
        containerHistorique.removeAllViews()

        if (listeHistorique.isEmpty()) {
            textViewHistoriqueVide.visibility = View.VISIBLE
            containerHistorique.visibility = View.GONE
        } else {
            textViewHistoriqueVide.visibility = View.GONE
            containerHistorique.visibility = View.VISIBLE

            val textColor = ContextCompat.getColor(this, android.R.color.black)
            for (item in listeHistorique) {
                val tv = TextView(this).apply {
                    text = "• $item"
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                    setTextColor(textColor)
                }
                containerHistorique.addView(tv)
            }
        }
    }

    private fun chargerHistorique() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedStr = prefs.getString(KEY_HISTORIQUE_STR, "") ?: ""
        listeHistorique.clear()
        if (savedStr.isNotEmpty()) {
            listeHistorique.addAll(savedStr.split("\n"))
        }
        afficherHistorique()
    }

    private fun sauvegarderHistoriqueLocal() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORIQUE_STR, listeHistorique.joinToString("\n")).apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_IMC_TEXT, textViewImc.text.toString())
        outState.putString(KEY_CAT_TEXT, textViewCategorie.text.toString())
        outState.putInt(KEY_CAT_COLOR, textViewCategorie.currentTextColor)
        outState.putInt(KEY_GAUGE_PROGRESS, progressBarImc.progress)
        outState.putBoolean(KEY_GAUGE_VISIBLE, progressBarImc.visibility == View.VISIBLE)
        outState.putBoolean(KEY_UNIT_CM, estUniteCm)
    }

    private fun restaurerEtat(savedInstanceState: Bundle) {
        val savedImcText = savedInstanceState.getString(KEY_IMC_TEXT, "")
        val savedCatText = savedInstanceState.getString(KEY_CAT_TEXT, "")
        val savedCatColor = savedInstanceState.getInt(KEY_CAT_COLOR, 0)
        val isGaugeVisible = savedInstanceState.getBoolean(KEY_GAUGE_VISIBLE, false)
        val gaugeProgress = savedInstanceState.getInt(KEY_GAUGE_PROGRESS, 0)
        estUniteCm = savedInstanceState.getBoolean(KEY_UNIT_CM, false)

        if (estUniteCm) {
            toggleGroupUnite.check(R.id.btnUniteCm)
        }

        if (savedImcText.isNotEmpty()) {
            textViewImc.text = savedImcText
            textViewCategorie.text = savedCatText
            if (savedCatColor != 0) {
                textViewCategorie.setTextColor(savedCatColor)
                progressBarImc.setIndicatorColor(savedCatColor)
            }
            if (isGaugeVisible) {
                progressBarImc.progress = gaugeProgress
                progressBarImc.visibility = View.VISIBLE
                buttonPartager.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "calcul_imc_prefs"
        private const val KEY_HISTORIQUE_STR = "key_historique_str"
        private const val KEY_IMC_TEXT = "key_imc_text"
        private const val KEY_CAT_TEXT = "key_cat_text"
        private const val KEY_CAT_COLOR = "key_cat_color"
        private const val KEY_GAUGE_PROGRESS = "key_gauge_progress"
        private const val KEY_GAUGE_VISIBLE = "key_gauge_visible"
        private const val KEY_UNIT_CM = "key_unit_cm"
        private const val MAX_HISTORIQUE = 5
    }
}