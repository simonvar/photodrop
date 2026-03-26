package dev.simonvar.photodrop

import android.app.Application
import android.content.res.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.video.VideoFrameDecoder

class PhotodropApplication : Application(), SingletonImageLoader.Factory {

    val dependencies: AppDependencies by lazy { AppDependencies() }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun newImageLoader(context: coil3.PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
