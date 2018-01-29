package com.missionofseoul.seoul;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by hyunho on 2016-11-28.
 */

public class FileUtility {

    /*
    * Gets the real path from file
    * @param context
    * @param contentUri
    * @return path
    */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String imgPath = contentUri.toString();
            // content://media/external/images/media/.... 카메라로 사진을 찍을때 [겔럭시s5 테스트 확인]
            // 안드로이드 버전에 관계없이 경로가 com.android... 형식으로 집히지 않을 수 도 있음. [ 겔럭시S4 테스트 확인 ]
            if (imgPath.contains("external")) {
                return getPathForPreV19(context, contentUri);
            }
            return getPathForV19AndUp(context, contentUri);
        } else {
            return getPathForPreV19(context, contentUri);
        }
    }

    /**
     * Handles pre V19 uri's
     *
     * @param context
     * @param contentUri
     * @return
     */
    public static String getPathForPreV19(Context context, Uri contentUri) {
        String res = null;

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();

        return res;
    }

    /**
     * Handles V19 and up uri's
     *
     * @param context
     * @param contentUri
     * @return path
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathForV19AndUp(Context context, Uri contentUri) {

        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);

        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }

}
