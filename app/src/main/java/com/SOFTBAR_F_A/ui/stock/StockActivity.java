package com.SOFTBAR_F_A.ui.stock;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Permisos;
import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.SesionUsuario;
import com.SOFTBAR_F_A.data.repository.StockRepository;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Pantalla de stock simple: lista solo los productos con control de stock,
 * resalta los que estan bajo minimo y permite reponer con botones +/- o fijar
 * una cantidad concreta. El descuento por venta es automatico (en el cobro).
 */
public class StockActivity extends AppCompatActivity {

    private RecyclerView lista;
    private TextView txtMensaje;
    private StockAdapter adapter;
    private final StockRepository repositorio = new StockRepository();
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        if (SesionUsuario.cargada() && !SesionUsuario.puede(Permisos.STOCK)) {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Header.aplica(this, getString(R.string.stock_title),
                getString(R.string.stock_subtitulo));

        lista = findViewById(R.id.lista_stock);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StockAdapter(new StockAdapter.Acciones() {
            @Override public void onMas(Producto p) {
                repositorio.fijarStock(p.getCodigoBarras(), p.getStock() + 1);
            }
            @Override public void onMenos(Producto p) {
                repositorio.fijarStock(p.getCodigoBarras(), p.getStock() - 1);
            }
            @Override public void onAjustar(Producto p) {
                mostrarDialogAjuste(p);
            }
        });
        lista.setAdapter(adapter);
        txtMensaje = findViewById(R.id.txt_stock_mensaje);

        mostrarMensaje(getString(R.string.stock_cargando));
        suscripcion = repositorio.escucharStock(new StockRepository.StockListener() {
            @Override
            public void onProductos(List<Producto> productos) {
                if (isFinishing() || isDestroyed()) return;
                pintar(productos);
            }

            @Override
            public void onError(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                mostrarMensaje(getString(R.string.stock_error,
                        mensaje != null ? mensaje : ""));
            }
        });
    }

    private void pintar(List<Producto> productos) {
        if (productos.isEmpty()) {
            mostrarMensaje(getString(R.string.stock_vacio));
            return;
        }
        txtMensaje.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);
        adapter.setItems(productos);
    }

    private void mostrarDialogAjuste(Producto producto) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.stock_dialog_hint);
        input.setText(String.valueOf(producto.getStock()));

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setPadding(padding, padding / 2, padding, 0);
        contenedor.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.stock_dialog_titulo))
                .setView(contenedor)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    String txt = input.getText().toString().trim();
                    int valor;
                    try {
                        valor = Integer.parseInt(txt);
                    } catch (NumberFormatException e) {
                        return;
                    }
                    repositorio.fijarStock(producto.getCodigoBarras(), valor);
                })
                .show();
    }

    private void mostrarMensaje(String mensaje) {
        lista.setVisibility(View.GONE);
        txtMensaje.setVisibility(View.VISIBLE);
        txtMensaje.setText(mensaje);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) suscripcion.remove();
    }
}
