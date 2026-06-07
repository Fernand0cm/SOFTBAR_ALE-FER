package com.SOFTBAR_F_A.ui.stock;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    private LinearLayout lista;
    private TextView txtMensaje;
    private final StockRepository repositorio = new StockRepository();
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        if (SesionUsuario.cargada() && !SesionUsuario.puede(Permisos.STOCK)) {
            android.widget.Toast.makeText(this, R.string.permiso_denegado,
                    android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Header.aplica(this, getString(R.string.stock_title),
                getString(R.string.stock_subtitulo));

        lista = findViewById(R.id.lista_stock);
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
        lista.removeAllViews();
        if (productos.isEmpty()) {
            mostrarMensaje(getString(R.string.stock_vacio));
            return;
        }
        txtMensaje.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Producto p : productos) {
            View item = inflater.inflate(R.layout.item_stock, lista, false);

            ((TextView) item.findViewById(R.id.txt_stock_nombre)).setText(p.getNombre());

            TextView detalle = item.findViewById(R.id.txt_stock_detalle);
            if (p.bajoStock()) {
                detalle.setText(getString(R.string.stock_bajo, p.getStock(), p.getStockMinimo()));
                detalle.setTextColor(ContextCompat.getColor(this, R.color.warning));
            } else {
                detalle.setText(getString(R.string.stock_detalle,
                        p.getStock(), p.getStockMinimo()));
                detalle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }

            item.findViewById(R.id.btn_stock_mas).setOnClickListener(v ->
                    repositorio.fijarStock(p.getCodigoBarras(), p.getStock() + 1));
            item.findViewById(R.id.btn_stock_menos).setOnClickListener(v ->
                    repositorio.fijarStock(p.getCodigoBarras(), p.getStock() - 1));
            item.findViewById(R.id.stock_info).setOnClickListener(v -> mostrarDialogAjuste(p));

            lista.addView(item);
        }
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
