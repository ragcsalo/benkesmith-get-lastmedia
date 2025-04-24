package com.benkesmith.getmediaplugin;

import org.apache.cordova.*;
import org.json.*;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.Context;

public class GetMediaPlugin extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("getLast".equals(action)) {
            final int limit = args.getInt(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONArray result = fetchLast(limit);
                        callbackContext.success(result);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        return false;
    }

    private JSONArray fetchLast(int limit) throws JSONException {
        JSONArray array = new JSONArray();
        Context context = cordova.getActivity().getApplicationContext();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MIME_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        String sortOrder = MediaStore.Files.FileColumns.DATE_TAKEN + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, sortOrder);

        if (cursor != null) {
            int fetched = 0;
            while (cursor.moveToNext() && fetched < limit) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                Uri contentUri = (mime.startsWith("image"))
                    ? Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id))
                    : Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                JSONObject obj = new JSONObject();
                obj.put("uri", contentUri.toString());
                obj.put("mimeType", mime);
                array.put(obj);
                fetched++;
            }
            cursor.close();
        }
        return array;
    }
}
