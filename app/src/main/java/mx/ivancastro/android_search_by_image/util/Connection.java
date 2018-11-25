package mx.ivancastro.android_search_by_image.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Connection {
    /**
     * Checks if the internet connection is available.
     * @param context of the activity from where is call.
     * @return true if is connected
     */
    public boolean checkInternetConnection (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.isAvailable() && networkInfo.isConnected();
        } else {
            Toast.makeText(context, "No tienes conexión a Internet.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
