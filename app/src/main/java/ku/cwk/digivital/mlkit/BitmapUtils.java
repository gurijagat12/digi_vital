/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ku.cwk.digivital.mlkit;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utils functions for bitmap conversions.
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";


    /**
     * Rotates a bitmap if it is converted from a bytebuffer.
     */
    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    @Nullable
    public static Bitmap getBitmapFromContentUri(ContentResolver contentResolver, Uri imageUri)
            throws IOException {
        Bitmap decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
        if (decodedBitmap == null) {
            return null;
        }
        int orientation = getExifOrientationTag(contentResolver, imageUri);

        int rotationDegrees = 0;
        boolean flipX = false;
        boolean flipY = false;
        // See e.g. https://magnushoff.com/articles/jpeg-orientation/ for a detailed explanation on each
        // orientation.
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotationDegrees = 90;
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                flipY = true;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = -90;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotationDegrees = -90;
                flipX = true;
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                // No transformations necessary in this case.
        }

        return rotateBitmap(decodedBitmap, rotationDegrees, flipX, flipY);
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        // We only support parsing EXIF orientation tag from local file on the device.
        // See also:
        // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())
                && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }

        ExifInterface exif;
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream == null) {
                return 0;
            }

            exif = new ExifInterface(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "failed to open file to read rotation meta data: " + imageUri, e);
            return 0;
        }

        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }
}
