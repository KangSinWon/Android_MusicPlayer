package com.example.musiclibrary_project;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// External Storage에 접근하기 위해 권한을 요청하는 클래스
public class PermissionHandler {
    public static boolean isPermissionGranted(Activity mContext, String Permission, String Text, int PermissionCode) {
        if (ContextCompat.checkSelfPermission(mContext, Permission) != PackageManager.PERMISSION_GRANTED) {
            reqPermission(mContext, Text, PermissionCode, Permission);
            return false;
        }
        return true;
    }

    public static void reqPermission(Activity mContext, String Text, int PermissionCode, String Permission) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(mContext, Permission)) {
            ActivityCompat.requestPermissions(mContext, new String[]{Permission}, PermissionCode);
        }
    }
}
