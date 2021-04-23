package com.example.storesample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)


//        val galleryAdapter = GalleryAdapter {
//            Snackbar.make(binding.rvLocal, "uri: $it", Snackbar.LENGTH_SHORT).show()
//        }

//        binding.rvLocal.also {
//            it.layoutManager = GridLayoutManager(this, 2)
//            it.adapter = galleryAdapter
//        }

//        binding.tvLoadImage.setOnClickListener {
//            if (haveStoragePermission()) {
//                queryImages()
//            } else {
//                requestPermission()
//            }
//        }
//        val actionOpenMultiDocuments = DocumentsUtil.registerOpenMultiDocuments(this) {
//            galleryAdapter.submitList(it)
//        }
//        binding.tvPickImage.setOnClickListener {
//            viewModel.saveRandomImageFromInternet()
//            actionOpenMultiDocuments.launch(arrayOf("image/*"))
//        }

//        viewModel.images.observe(this, Observer {
//            galleryAdapter.submitList(it)
//        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}