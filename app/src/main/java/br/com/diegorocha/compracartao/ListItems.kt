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

class FaturaItem(var json: JSONObject) {
    fun id(): Int {
        return json.getInt("id")
    }

    override fun toString(): String {
        val cartao = json.getString("cartao")
        val orcamento = json.getString("orcamento")
        return "$cartao - $orcamento"
    }
}