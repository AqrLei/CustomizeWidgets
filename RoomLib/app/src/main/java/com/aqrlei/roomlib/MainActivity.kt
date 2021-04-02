package com.aqrlei.roomlib

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.aqrlei.roomlib.data.source.local.entity.User
import com.aqrlei.roomlib.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory((application as RoomApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel.allUsers.observe(this, Observer {
            Log.d("AqrLei", "All: ${it.toTypedArray().contentToString()}")
        })

        userViewModel.categoryUsers("like").observe(this,
            Observer {
                Log.d("AqrLei", "Like: ${it.toTypedArray().contentToString()}")
            }
        )

        userViewModel.categoryUsers("test").observe(this,
            Observer {
                Log.d("AqrLei", "test: ${it.toTypedArray().contentToString()}")
            }
        )

        binding.btInsert.setOnClickListener { insert() }
        binding.btUpdate.setOnClickListener { update() }
        binding.btDelete.setOnClickListener { delete() }
        binding.btUpdateCategory.setOnClickListener { updateCategory() }

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
            Log.d("AqrLei","content: ${it}")
            it?.let { uri ->

            }
        }

        // MQS7N19530025511
        binding.btGet.setOnClickListener {
            getContent.launch("audio/*")
        }

    }

    fun insert() {
        userViewModel.insert(
            User(
                uid = 2,
                mediaName = "like",
                artistName = "like",
                path = "like",
                category = "like"
            )
        )
    }

    fun delete() {
        userViewModel.delete(User(uid = 2))
    }

    fun update() {
        userViewModel.update(
            User(
                uid = 2,
                mediaName = "update",
                artistName = "update",
                path = "update",
                category = "test"
            )
        )
    }

    fun updateCategory() {
        userViewModel.updateCategory(1, "like")
    }
}