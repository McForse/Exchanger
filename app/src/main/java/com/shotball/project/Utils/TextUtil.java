package com.shotball.project.Utils;

import android.webkit.URLUtil;

import java.net.URL;
import java.util.regex.Pattern;

public class TextUtil {

    public static boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);

        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isUrl(String url) {
        return URLUtil.isValidUrl(url);
        /*try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }*/
    }

}
