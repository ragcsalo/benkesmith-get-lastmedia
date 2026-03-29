package com.benkesmith.getmediaplugin;

import org.apache.cordova.*;
import org.json.*;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.Context;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;

public class GetMediaPlugin extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("getLast".equals(action)) {
            final int limit = args.getInt(0);
            cordova.getThreadPool().execute(() -> {
                try {
                    JSONArray result = fetchLast(limit);
                    callbackContext.success(result);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            });
            return true;
        }
        return false;
    }

    private JSONArray fetchLast(int limit) throws JSONException, IOException {
        JSONArray array = new JSONArray();
        Context ctx = cordova.getActivity().getApplicationContext();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] proj = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
        };

        // Use DATE_ADDED for consistent sorting
        String sort = MediaStore.Images.Media.DATE_ADDED + " DESC";

        Cursor cursor = ctx.getContentResolver().query(uri, proj, null, null, sort);

        if (cursor != null) {
            int fetched = 0;
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

            while (cursor.moveToNext() && fetched < limit) {
                long id = cursor.getLong(idColumn);
                String mime = cursor.getString(mimeColumn);
                String path = cursor.getString(pathColumn);
                String filename = cursor.getString(nameColumn);

                // Convert DATE_ADDED (seconds) to milliseconds
                long timestamp = cursor.getLong(dateAddedColumn) * 1000;

                File file = new File(path);
                if (!file.exists()) continue;

                Uri contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                try (InputStream in = ctx.getContentResolver().openInputStream(contentUri)) {
                    if (in != null) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }

                        String b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);

                        JSONObject obj = new JSONObject();
                        obj.put("id", id);
                        obj.put("mimeType", mime);
                        obj.put("path", path);
                        obj.put("filename", filename);
                        obj.put("timestamp", timestamp);
                        obj.put("base64", b64);
                        array.put(obj);
                    }
                } catch (Exception e) {
                    continue;
                }
                fetched++;
            }
            cursor.close();
        }
        return array;
    }
}
