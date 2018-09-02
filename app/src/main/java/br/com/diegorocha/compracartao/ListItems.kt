package br.com.diegorocha.compracartao

import org.json.JSONObject

class CategoriaItem(var json: JSONObject) {

    fun id(): Int {
        return json.getInt("id")
    }

    override fun toString(): String {
        return json.getString("descricao")
    }
}

class FaturaItem(val json: JSONObject) {
    fun id(): Int {
        return json.getInt("id")
    }

    fun getValor(): Double {
        return json.getDouble("valor_inicial")
    }

    override fun toString(): String {
        val cartao = json.getString("cartao")
        val orcamento = json.getString("orcamento")
        return "$cartao - $orcamento"
    }
}