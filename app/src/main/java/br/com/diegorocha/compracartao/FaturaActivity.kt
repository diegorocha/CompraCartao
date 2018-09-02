package br.com.diegorocha.compracartao

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_fatura.*

class FaturaActivity : AppCompatActivity() {
    private lateinit var api : API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fatura)

        intent?.getIntExtra("fatura", 0)?.let{
            api = API(this)
            api.getFatura(
                    it,
                    Response.Listener { fatura ->
                        val compras = fatura.getJSONArray("compras")
                        val items = ArrayList<CompraItem>()
                        for (i in 0..(compras.length() - 1)) {
                            val json = compras.getJSONObject(i)
                            items.add(CompraItem(json))
                        }
                        val adapter = object :  ArrayAdapter<CompraItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1, items) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                val view = super.getView(position, convertView, parent!!)
                                val compra = items.get(position)
                                val valor: Double = compra.getValor()
                                val categoria = compra.getCategoria()
                                var info = "R$ %.2f - $categoria".format(valor)
                                if(compra.getRecorrente()){
                                    info += " - Recorrente"
                                }
                                view.findViewById<TextView>(android.R.id.text1).text = compra.toString()
                                view.findViewById<TextView>(android.R.id.text2).text = info
                                return view
                            }
                        }
                        list.adapter = adapter
                    },
                    Response.ErrorListener {
                        snackBar("Não foi possível obter a fatura")
                    })

        }
    }

    private fun snackBar(text: String) {
        Snackbar.make(list, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }
}
