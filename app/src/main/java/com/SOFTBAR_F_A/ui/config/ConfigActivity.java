package com.SOFTBAR_F_A.ui.config;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {

    private LinearLayout listaProductos;
    private TextView txtListaVacia;
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        if (com.SOFTBAR_F_A.data.SesionUsuario.cargada()
                && !com.SOFTBAR_F_A.data.SesionUsuario.puede(
                        com.SOFTBAR_F_A.data.Permisos.CONFIG)) {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Header.aplica(this, getString(R.string.config_title), getString(R.string.config_productos_subtitulo));

        listaProductos = findViewById(R.id.lista_productos);
        txtListaVacia = findViewById(R.id.txt_lista_vacia);

        Button btnEscanear = findViewById(R.id.btn_escanear);
        btnEscanear.setOnClickListener(v -> escanear());

        Button btnAltaManual = findViewById(R.id.btn_alta_manual);
        btnAltaManual.setOnClickListener(v -> mostrarDialogProducto(generarCodigoManual()));

        suscribirseAProductos();
    }

    private void escanear() {
        GmsBarcodeScannerOptions opciones = new GmsBarcodeScannerOptions.Builder()
                .build();
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, opciones);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String codigo = barcode.getRawValue();
                    if (codigo != null) {
                        mostrarDialogProducto(codigo);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show())
                .addOnCanceledListener(() -> { /* el usuario cancelo, sin accion */ });
    }

    private String generarCodigoManual() {
        return "MAN-" + System.currentTimeMillis();
    }

    private void mostrarDialogProducto(String codigo) {
        mostrarDialogProducto(codigo, null);
    }

    private void mostrarDialogProducto(String codigo, Producto existente) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);
        TextView txtCodigo = vista.findViewById(R.id.txt_codigo_dialog);
        EditText inputNombre = vista.findViewById(R.id.input_nombre);
        EditText inputPrecio = vista.findViewById(R.id.input_precio);
        RadioGroup grupoIva = vista.findViewById(R.id.radio_iva_group);
        MaterialSwitch switchActivo = vista.findViewById(R.id.switch_activo);
        Spinner spinnerCategoria = vista.findViewById(R.id.spinner_categoria);
        txtCodigo.setText(getString(R.string.dialog_codigo_prefijo, codigo));

        if (existente != null) {
            inputNombre.setText(existente.getNombre());
            inputPrecio.setText(String.format(Locale.ROOT, "%.2f", existente.getPrecio()));
            grupoIva.check(radioParaTipoIva(existente.getTipoIva()));
            switchActivo.setChecked(existente.isActivo());
            seleccionarCategoria(spinnerCategoria, existente.getCategoria());
        }

        new AlertDialog.Builder(this)
                .setTitle(existente != null
                        ? R.string.dialog_titulo_editar
                        : R.string.dialog_titulo)
                .setView(vista)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    String precioTxt = inputPrecio.getText().toString().trim();

                    if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(precioTxt)) {
                        Toast.makeText(this, R.string.dialog_error_vacio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double precio;
                    try {
                        precio = Double.parseDouble(precioTxt.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.dialog_error_precio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double tipoIva = tipoIvaSeleccionado(grupoIva.getCheckedRadioButtonId());
                    String categoria = spinnerCategoria.getSelectedItem().toString();
                    guardarProducto(new Producto(codigo, nombre, precio, tipoIva,
                            switchActivo.isChecked(), categoria));
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    private double tipoIvaSeleccionado(int idRadio) {
        if (idRadio == R.id.radio_iva_21) return 0.21;
        if (idRadio == R.id.radio_iva_4) return 0.04;
        return Producto.IVA_POR_DEFECTO;
    }

    private int radioParaTipoIva(double tipoIva) {
        if (tipoIva == 0.21) return R.id.radio_iva_21;
        if (tipoIva == 0.04) return R.id.radio_iva_4;
        return R.id.radio_iva_10;
    }

    private void seleccionarCategoria(Spinner spinner, String categoria) {
        String[] categorias = getResources().getStringArray(R.array.categorias_producto);
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equalsIgnoreCase(categoria)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void guardarProducto(Producto producto) {
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .document(producto.getCodigoBarras())
                .set(producto)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, R.string.config_guardado,
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void suscribirseAProductos() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .orderBy(FirestoreSchema.Fields.NOMBRE, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaProductos.removeAllViews();
                    if (snap.isEmpty()) {
                        txtListaVacia.setVisibility(View.VISIBLE);
                        return;
                    }
                    txtListaVacia.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (Producto p : snap.toObjects(Producto.class)) {
                        View item = inflater.inflate(R.layout.item_producto,
                                listaProductos, false);
                        String nombre = p.isActivo()
                                ? p.getNombre()
                                : getString(R.string.config_producto_inactivo, p.getNombre());
                        ((TextView) item.findViewById(R.id.txt_nombre)).setText(nombre);
                        item.setAlpha(p.isActivo() ? 1f : 0.45f);
                        ((TextView) item.findViewById(R.id.txt_codigo))
                                .setText(getString(R.string.config_codigo_categoria,
                                        p.getCodigoBarras(), p.getCategoria()));
                        ((TextView) item.findViewById(R.id.txt_precio))
                                .setText(getString(R.string.config_precio_iva,
                                        p.getPrecio(),
                                        (int) Math.round(p.getTipoIva() * 100)));
                        item.setOnClickListener(v ->
                                mostrarDialogProducto(p.getCodigoBarras(), p));
                        listaProductos.addView(item);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}
