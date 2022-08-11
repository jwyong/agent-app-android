package sg.com.agentapp.global.GlideAPI;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by ibrahim on 26/02/2018.
 */

@GlideModule
public class SoappGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);

        int memoryCacheSizeBytes = 1024 * 1024 * 200; // set ram 200mb
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
    }
}
