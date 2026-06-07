package com.SOFTBAR_F_A.ui.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Comprobacion puntual de conectividad. Se usa para bloquear operaciones que
 * necesitan red (cobro y rectificacion usan transacciones de Firestore, que no
 * funcionan sin conexion), evitando asi numeraciones a medias o duplicadas.
 */
public final class ConexionUtil {

    private ConexionUtil() { }

    public static boolean hayConexion(Context contexto) {
        ConnectivityManager cm = (ConnectivityManager)
                contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network red = cm.getActiveNetwork();
        if (red == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(red);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
