package mx.ivancastro.android_search_by_image.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connection {
    /**
     * Checks if the internet connection is available.
     * @param context
     * @return
     */
    private boolean checkInternetConnection (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((connectivityManager != null) && (networkInfo != null)) {
            if (networkInfo.isAvailable() && networkInfo.isConnected()) return true;
            else return false;
        } else {
            return false;
        }
    }
}
