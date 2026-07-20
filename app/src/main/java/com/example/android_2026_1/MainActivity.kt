package com.example.android_2026_1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GithubApi = retrofit.create(GithubApi::class.java)
}

class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var rvUsers: RecyclerView
    private val userList = ArrayList<String>()
    private lateinit var nameAdapter: NameAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etSearch = findViewById(R.id.et_search)
        btnSearch = findViewById(R.id.btn_search)
        rvUsers = findViewById(R.id.rv_users)

        rvUsers.layoutManager = LinearLayoutManager(this)
        nameAdapter = NameAdapter(userList) { selectedName: String ->
            etSearch.setText(selectedName)
            etSearch.setSelection(selectedName.length)
        }
        rvUsers.adapter = nameAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query: String = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val response: UserSearchResponse = RetrofitClient.api.searchUsers(query)
                            val items: List<UserItem>? = response.items
                            if (items != null) {
                                userList.clear()
                                for (item in items) {
                                    userList.add(item.login)
                                }
                                nameAdapter.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    userList.clear()
                    nameAdapter.notifyDataSetChanged()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSearch.setOnClickListener {
            val name: String = etSearch.text.toString()
            if (name.isNotEmpty()) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/$name?tab=repositories".toUri()
                )
                startActivity(intent)
            }
        }
    }
}

interface GithubApi {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 20
    ): UserSearchResponse
}

data class UserSearchResponse(
    @SerializedName("items") val items: List<UserItem>?
)

data class UserItem(
    @SerializedName("login") val login: String
)