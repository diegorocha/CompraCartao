package br.com.diegorocha.compracartao

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var api : API
    private var faturaId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            if (validateFields()) {
                createCompra()
            }
        }

        api = API(this)
        loadFaturas()
        loadCategorias()

        when {
            intent?.action == Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sms ->
                    parseSMS(sms)
                }
            }
            else -> {
                txtSMS.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> startSettingsActivity()
            R.id.action_faturas -> startListFaturasActivity()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startSettingsActivity(): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    private fun startListFaturasActivity(): Boolean {
        startActivity(Intent(this, ListFaturasActivity::class.java))
        return true
    }

    private fun initForm() {
        txtDescricao.text?.let { it.clear() }
        txtValor.text?.let { it.clear() }
        txtValorDolar.text?.let { it.clear() }
        txtParcelas.text?.let { it.clear() }
        txtRecorrente.isChecked = false
        txtDescricao.requestFocus()
    }

    private fun validateFields(): Boolean {
        var valid = true
        if (txtFatura.selectedItemId == AdapterView.INVALID_ROW_ID) {
            valid = false
        }
        if (txtDescricao.text.isNullOrEmpty()) {
            txtDescricao.error = getString(R.string.campoObrigatorio)
            valid = false
        }
        if (txtValor.text.isNullOrEmpty()) {
            txtValor.error = getString(R.string.campoObrigatorio)
            valid = false
        }
        if (txtCategoria.selectedItemId == AdapterView.INVALID_ROW_ID) {
            valid = false
        }
        return valid
    }

    private fun parseFloat(text: String): Float {
        return NumberFormat.getInstance().parse(text.replace(".", ",")).toFloat()
    }

    private fun parseInt(text: String): Int {
        return NumberFormat.getInstance().parse(text).toInt()
    }

    private fun getPayload(): JSONObject {
        val payload = JSONObject()
        var valorReal = parseFloat(txtValor.text.toString())
        var valorDolar: Float? = null
        var parcelas = 1
        if (!txtValorDolar.text.isNullOrEmpty()) {
            valorDolar = parseFloat(txtValorDolar.text.toString())
        }
        if (!txtParcelas.text.isNullOrBlank()) {
            parcelas = parseInt(txtParcelas.text.toString())
            if (parcelas > 1) {
                valorReal /= parcelas
            }
        }
        txtFatura.selectedItem?.let {
            val item = it as FaturaItem
            payload.put("fatura", item.id())
        }
        payload.put("descricao", txtDescricao.text)
        payload.put("valor_real", valorReal)
        valorDolar?.let {
            payload.put("valor_dolar", it)
        }
        txtCategoria.selectedItem?.let{
            val item = it as CategoriaItem
            payload.put("categoria", item.id())
        }

        payload.put("parcelas", parcelas)
        payload.put("recorrente", txtRecorrente.isChecked)
        return payload
    }

    private fun snackBar(text: String) {
        Snackbar.make(root_layout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }

    private fun parseSMS(sms: String) {
        txtSMS.text = sms
        val payload = JSONObject()
        payload.put("sms", sms)
        api.parseSMS(
                payload,
                Response.Listener { data ->
                    val descricao = data.getString("descricao_fatura")
                    val moeda = data.getString("moeda")
                    val valor = data.getDouble("valor")
                    txtDescricao.setText(descricao)
                    if (moeda == "RS") {
                        txtValor.setText(valor.toString())
                    } else {
                        txtValor.setText("")
                    }
                    txtValorDolar.setText("")
                    txtParcelas.setText("1")
                    faturaId = data.getInt("fatura_id")
                    setFatura()
                },
                Response.ErrorListener {
                    snackBar("Não foi possível processar o SMS")
                }
        )

    }

    private fun setFatura() {
        faturaId?.let { id ->
            txtFatura.getAdapter()?.let {
                val adapter = it as ArrayAdapter<*>
                for (position in 0 until adapter.getCount()) {
                    val fatura = adapter.getItem(position) as FaturaItem
                    if (fatura.id() == id) {
                        txtFatura.setSelection(position)
                    }
                }
            }
        }
    }

    private fun loadCategorias() {
        api.getCategoriasRequest(
                Response.Listener { categorias ->
                    val list = ArrayList<CategoriaItem>()
                    for (i in 0..(categorias.length() - 1)) {
                        val json = categorias.getJSONObject(i)
                        list.add(CategoriaItem(json))
                    }
                    ArrayAdapter<CategoriaItem>(this, android.R.layout.simple_spinner_item, list).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        txtCategoria.adapter = adapter
                    }
                },
                Response.ErrorListener {
                    snackBar("Não foi possível carregar as categorias")
                })

    }

    private fun loadFaturas() {
        api.getFaturasAbertasRequest(
                Response.Listener { faturas ->
                    val list = ArrayList<FaturaItem>()
                    for (i in 0..(faturas.length() - 1)) {
                        val json = faturas.getJSONObject(i)
                        list.add(FaturaItem(json))
                    }
                    ArrayAdapter<FaturaItem>(this, android.R.layout.simple_spinner_item, list).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        txtFatura.adapter = adapter
                    }
                    setFatura()
                },
                Response.ErrorListener {
                    snackBar("Não foi possível obter as faturas abertas")
                })
    }

    private fun createCompra(){
        api.createCompra(
                getPayload(),
                Response.Listener {
                    initForm()
                    snackBar("Compra inserida com sucesso")
                },
                Response.ErrorListener {
                    snackBar("Não foi possível criar a compra")
                }
        )
    }
}
