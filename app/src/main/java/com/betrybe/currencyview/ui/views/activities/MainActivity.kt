package com.betrybe.currencyview.ui.views.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.betrybe.currencyview.R
import com.betrybe.currencyview.common.ApiIdlingResource
import com.betrybe.currencyview.data.api.OpenApiService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.betrybe.currencyview.ui.adapters.CurrencyRateAdapter
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // Pegando os elementos..
    private val containerAutoComplete:TextInputLayout by lazy { findViewById(R.id.currency_selection_input_container) }
    private val autoCompleteTextView:AutoCompleteTextView by lazy { findViewById(R.id.currency_selection_input_layout) }
    private val selectCurrencyState: MaterialTextView by lazy { findViewById(R.id.select_currency_state) }
    private val cotacaoMoeda: RecyclerView by lazy { findViewById(R.id.currency_rates_state) }

    private val carregandoMoeda: MaterialTextView by lazy { findViewById(R.id.load_currency_state) }
    private val waitingResponseState: FrameLayout by lazy { findViewById(R.id.waiting_response_state) }

    private val openApiService = OpenApiService.instance

    private var ListaMoedas:Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        iniciandoTela()
        listenEventContainer()
        styleZando()
    }

    private fun styleZando() {
        cotacaoMoeda.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val center = recyclerView.height / 2

                // Percorre todos os filhos do RecyclerView
                for (i in 0 until layoutManager.childCount) {
                    val child = layoutManager.getChildAt(i)
                    child?.let {
                        // Calcula o centro do item
                        val childCenter = (child.top + child.bottom) / 2
                        // Calcula a distância do centro
                        val distanceFromCenter = Math.abs(center - childCenter)

                        // Define o limite máximo para a escala
                        val maxDistance = recyclerView.height / 2

                        // Define o fator de escala baseado na distância
                        val scale = 1f - (distanceFromCenter.toFloat() / maxDistance)
                        // Garante que a escala não fique abaixo de 0.9
                        val adjustedScale = 0.85f + 0.15f * Math.max(scale, 0f)

                        // Aplica a escala ao item
                        child.scaleX = adjustedScale
                        child.scaleY = adjustedScale
                    }
                }
            }
        })
    }


    // Requisito 09
    private fun listenEventContainer() {
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedCurrency = autoCompleteTextView.text.toString().substring(0, 3)
            if (selectedCurrency.isNotEmpty()) {
                selectCurrencyState.visibility = View.GONE
                waitingResponseState.visibility = View.VISIBLE
                CoroutineScope(Main).launch {
                    loadCurrencyRates(selectedCurrency)
                }
            }
        }
    }


    private suspend fun loadCurrencyRates(selectedCurrency: String) {
        try {
            ApiIdlingResource.increment()
            val result = openApiService.getLatestRates(selectedCurrency)
            if (result.isSuccessful) {
                val rates = result.body()?.rates ?: emptyMap()
                waitingResponseState.visibility = View.GONE
                populateRecyclerView(rates)
            } else {
                withContext(Dispatchers.Main) {
                    waitingResponseState.visibility = View.GONE
                    showErrorDialog(result.message())
                }
            }
        } catch (e: HttpException) {
            Log.e("HttpException", e.message.toString())
        } catch (e: IOException) {
            Log.e("IOException", e.message.toString())
        } finally {
            ApiIdlingResource.decrement()
        }
    }

    private fun populateRecyclerView(rates: Map<String, Double>) {
        if (rates.isNotEmpty()) {
            val adapter = CurrencyRateAdapter(rates, ListaMoedas)
            cotacaoMoeda.layoutManager = LinearLayoutManager(this)
            cotacaoMoeda.adapter = adapter
            cotacaoMoeda.visibility = View.VISIBLE
        }
    }


    // Requisito 08
    // Configura eventos do container de AutoComplete
    private fun iniciandoTela() {
        CoroutineScope(Main).launch {
            loadSymbols()
            carregandoMoeda.visibility = View.GONE
            selectCurrencyState.visibility = View.VISIBLE
            populateAutoComplete(ListaMoedas)
        }
    }

    // Função para carregar símbolos da API
    private suspend fun loadSymbols() {
        try {
            ApiIdlingResource.increment()
            val result = openApiService.getSymbols()
            if (result.isSuccessful) {
                ListaMoedas = result.body()?.symbols ?: emptyMap()
            } else {
                withContext(Dispatchers.Main) {
                    showErrorDialog(result.message())
                }
            }
        } catch (e: HttpException) {
            Log.e("HttpException", e.message.toString())
        } catch (e: IOException) {
            Log.e("IOException", e.message.toString())
            Log.e("IVAN", e.message.toString())
        } finally {
            ApiIdlingResource.decrement()
        }
    }

    private fun populateAutoComplete(symbolsMap: Map<String, String>) {
        if (symbolsMap.isNotEmpty()) {
            val symbolsList = symbolsMap.map { "${it.key} - ${it.value}" }
            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, symbolsList)
            autoCompleteTextView.setAdapter(adapter)
            selectCurrencyState.visibility = View.VISIBLE
        }
    }


    // Exibe um diálogo de erro
    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("Trybe Cambio")
            .setMessage("Erro de requisição: $message")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
