package demo.com.myapplication.utils;

import android.util.Log;

/**
 * Created by LiuBin
 */
public final class LogUtils {
  public static final void log(String s) {
    Log.e("LB", s);
  }

  public static final void log(String tag, String s) {
    Log.e("LB", tag + s);
  }
}
