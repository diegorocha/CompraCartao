package br.com.diegorocha.compracartao

import android.content.Context
import android.preference.PreferenceManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class API(private val context: Context) {
    private lateinit var baseUrl: String
    private lateinit var token: String

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.getString("url", "https://orcamento.diegorocha.com.br")?.let {
            baseUrl = it
        }
        prefs.getString("token", null)?.let {
            token = it
        }
    }

    private fun getRequestHeaders() : HashMap<String, String>{
        val headers = HashMap<String, String>()
        headers.put("Authorization", "Token $token")
        headers.put("Content-Type", "application/json")
        return headers
    }

    private fun getUrl(endpoint: String) : String {
        return "$baseUrl$endpoint"
    }

    private fun createRequestJson(method: Int, endpoint: String, payload: JSONObject?, response: Response.Listener<JSONObject>, error: Response.ErrorListener): JsonObjectRequest {
        return object : JsonObjectRequest(
                method,
                getUrl(endpoint),
                payload,
                response,
                error) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders()
            }
        }
    }

    private fun createRequestJsonArray(method: Int, endpoint: String, payload: JSONArray?, response: Response.Listener<JSONArray>, error: Response.ErrorListener): JsonArrayRequest {
        return object : JsonArrayRequest(
                method,
                getUrl(endpoint),
                payload,
                response,
                error) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return getRequestHeaders()
            }
        }
    }

    fun getFatura(id: Int, response: Response.Listener<JSONObject>, error: Response.ErrorListener) {
        val request = createRequestJson(
                Request.Method.GET,
                "/api/cartao/fatura/$id/",
                null,
                response,
                error)
        VolleyFactory.getInstance(context).addToRequestQueue(request)
    }

    fun parseSMS(payload: JSONObject?, response: Response.Listener<JSONObject>, error: Response.ErrorListener) {
        val request = createRequestJson(
                Request.Method.POST,
                "/api/cartao/sms/parse/",
                payload,
                response,
                error)
        VolleyFactory.getInstance(context).addToRequestQueue(request)
    }

    fun createCompra(payload: JSONObject?, response: Response.Listener<JSONObject>, error: Response.ErrorListener) {
        val request = createRequestJson(
                Request.Method.POST,
                "/api/cartao/compra/",
                payload,
                response,
                error)
        VolleyFactory.getInstance(context).addToRequestQueue(request)
    }

    fun getFaturasAbertasRequest(response: Response.Listener<JSONArray>, error: Response.ErrorListener) {
        val request = createRequestJsonArray(
                Request.Method.GET,
                "/api/cartao/fatura/abertas/",
                null,
                response,
                error)
        VolleyFactory.getInstance(context).addToRequestQueue(request)
    }

    fun getCategoriasRequest(response: Response.Listener<JSONArray>, error: Response.ErrorListener) {
        val request = createRequestJsonArray(
                Request.Method.GET,
                "/api/categoria/",
                null,
                response,
                error)
        VolleyFactory.getInstance(context).addToRequestQueue(request)
    }

}

class VolleyFactory constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleyFactory? = null
        fun getInstance(context: Context) =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: VolleyFactory(context)
                }
    }
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}