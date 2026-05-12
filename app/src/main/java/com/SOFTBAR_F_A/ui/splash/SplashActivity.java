package com.SOFTBAR_F_A.ui.splash;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.home.HomeActivity;
import com.SOFTBAR_F_A.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private static final String COLECCION_FONDOS = "splash_backgrounds";
    private static final String CAMPO_URL = "imageUrl";
    private static final String CAMPO_ACTIVA = "active";
    private static final long SPLASH_DELAY_MS = 1400;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ImageView fondoSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fondoSplash = findViewById(R.id.img_splash_background);

        cargarFondoAleatorio();
        handler.postDelayed(this::continuar, SPLASH_DELAY_MS);
    }

    private void cargarFondoAleatorio() {
        FirebaseFirestore.getInstance()
                .collection(COLECCION_FONDOS)
                .whereEqualTo(CAMPO_ACTIVA, true)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) return;

                    List<String> urls = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        String url = doc.getString(CAMPO_URL);
                        if (url != null && !url.trim().isEmpty()) {
                            urls.add(url);
                        }
                    }
                    if (urls.isEmpty()) return;

                    String elegida = urls.get(new Random().nextInt(urls.size()));
                    cargarBitmap(elegida);
                });
    }

    private void cargarBitmap(String urlImagen) {
        executor.execute(() -> {
            Bitmap bitmap = descargarBitmap(urlImagen);
            if (bitmap == null || isFinishing() || isDestroyed()) return;
            handler.post(() -> fondoSplash.setImageBitmap(bitmap));
        });
    }

    private Bitmap descargarBitmap(String urlImagen) {
        HttpURLConnection conexion = null;
        try {
            URL url = new URL(urlImagen);
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setConnectTimeout(2500);
            conexion.setReadTimeout(2500);
            conexion.setInstanceFollowRedirects(true);
            try (InputStream input = conexion.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (conexion != null) conexion.disconnect();
        }
    }

    private void continuar() {
        Intent intent;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }
}
