package com.ceit.management.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BlurImage
{
    private static float BITMAP_SCALE = 0.4f;
    private static float BLUR_RADIUS = 7.5f;

    private static Context context;
    private static ImageView imageView;
    private static BlurImage blurImage;
    private static Bitmap blurredImage;

    public static BlurImage with(Context ctx)
    {
        context = ctx;
        return(blurImage= new BlurImage());
    }

    public static BlurImage into(ImageView image)
    {
        imageView = image;

        return blurImage;
    }

    public static BlurImage setBlurRadius(float radius)
    {
        BLUR_RADIUS = radius;
        return blurImage;
    }

    public static BlurImage setBitmapScale(float scale)
    {
        BITMAP_SCALE = scale;
        return blurImage;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static BlurImage blurFromResource(int resource)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        BlurImage.blur(bitmap);
        return blurImage;
    }

    public static BlurImage blurFromUri(String imageUrl)
    {
        Picasso.get().load(imageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
                    {
                        BlurImage.blur(bitmap);
                        imageView.setBackground(new BitmapDrawable(context.getResources(), blurredImage));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {}

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });

        return blurImage;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static BlurImage blur(Bitmap image)
    {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        blurredImage = outputBitmap;

        return blurImage;
    }
}
