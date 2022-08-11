package sg.com.agentapp.global.GlideAPI;

import com.bumptech.glide.annotation.GlideExtension;

/**
 * Created by ibrahim on 26/02/2018.
 */

@GlideExtension
public class SoappGlideExtension {
    static com.bumptech.glide.load.Transformation transformation;

    private SoappGlideExtension() {


    }

//    @GlideOption
//    public static RequestOptions forFoodNearbyOption(RequestOptions requestOptions, String s) {
//
//        requestOptions
//
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
//
//        return requestOptions;
//    }
//
//    @GlideType(Object.class)
//    public static void forFoodNearbyBuilder(RequestBuilder requestBuilder) {
//
//    }
//
//    @GlideOption
//    public static void ProfileImageGlide(RequestOptions requestOptions) {
//
//        transformation = new RoundedCorners(30);
//
//        requestOptions.transform(transformation);
//    }

}
