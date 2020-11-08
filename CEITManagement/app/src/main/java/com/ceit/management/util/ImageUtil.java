package com.ceit.management.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil
{
    public static String imageToBase64(Bitmap image)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);

        return Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap imageUriToBitmap(Context context, Uri uri)
    {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is, null, null);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
