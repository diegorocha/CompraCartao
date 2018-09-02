package br.com.diegorocha.compracartao

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_list_faturas.*

class ListFaturasActivity : AppCompatActivity() {
    private lateinit var api : API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_faturas)
        api = API(this)
        api.getFaturasAbertasRequest(
                Response.Listener { faturas ->
                    val items = ArrayList<FaturaItem>()
                    for (i in 0..(faturas.length() - 1)) {
                        val json = faturas.getJSONObject(i)
                        items.add(FaturaItem(json))
                    }
                    val adapter = object :  ArrayAdapter<FaturaItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1, items) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                            val view = super.getView(position, convertView, parent!!)
                            val fatura = items.get(position)
                            val valor: Double = fatura.getValor()
                            view.findViewById<TextView>(android.R.id.text1).text = fatura.toString()
                            view.findViewById<TextView>(android.R.id.text2).text = "R$ %.2f".format(valor)
                            return view
                        }
                    }
                    list.adapter = adapter
                },
                Response.ErrorListener {
                    snackBar("Não foi possível obter as faturas")
                })
    }

    private fun snackBar(text: String) {
        Snackbar.make(list, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }
}
