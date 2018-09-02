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

class CompraItem(val json: JSONObject){
    fun getValor(): Double {
        return json.getDouble("valor_inicial")
    }

    fun getRecorrente(): Boolean {
        return json.getBoolean("recorrente")
    }

    fun getCategoria(): String {
        return json.getString("categoria")
    }

    override fun toString(): String {
        val descricao = json.getString("descricao")
        var parcelaText = ""
        val parcelas = json.getInt("parcelas")
        if (parcelas > 1){
            val parcela = json.getInt("parcela_atual")
            parcelaText = "$parcela/$parcelas"
        }
        return "$descricao $parcelaText"
    }
}