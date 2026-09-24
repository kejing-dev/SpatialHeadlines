package com.headline.news.platform

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.pico.spatial.ui.foundation.dsl.launch
import com.headline.news.mainApp

class SpatialApplication : Application(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
