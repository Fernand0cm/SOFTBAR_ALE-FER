package com.SOFTBAR_F_A.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Dinero;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.SesionUsuario;
import com.SOFTBAR_F_A.data.Usuario;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.repository.RectificacionRepository;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.common.PersonalizacionLinea;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Locale;

public class TicketActivity extends AppCompatActivity {

    public static final String EXTRA_VENTA_ID = "ventaId";
    public static final String EXTRA_FACTURA_ID = "facturaId";

    private TextView txtTicketNumero;
    private TextView txtTicketMesa;
    private LinearLayout listaTicketLineas;
    private TextView txtTicketTotal;
    private TextView txtTicketBase;
    private TextView txtTicketIva;
    private TextView txtTicketPago;
    private TextView txtTicketPagoEfectivo;
    private TextView txtTicketPagoTarjeta;
    private TextView txtTicketCambio;
    private Button btnRectificar;
    private Factura facturaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        Header.aplica(this, getString(R.string.ticket_title));

        btnRectificar = findViewById(R.id.btn_rectificar);
        btnRectificar.setVisibility(View.GONE);
        btnRectificar.setOnClickListener(v -> confirmarRectificacion());

        Button btnImprimir = findViewById(R.id.btn_imprimir);
        btnImprimir.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnEmail = findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnCerrar = findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> finish());

        txtTicketNumero = findViewById(R.id.txt_ticket_numero);
        txtTicketMesa = findViewById(R.id.txt_ticket_mesa);
        listaTicketLineas = findViewById(R.id.lista_ticket_lineas);
        txtTicketTotal = findViewById(R.id.txt_ticket_total);
        txtTicketBase = findViewById(R.id.txt_ticket_base);
        txtTicketIva = findViewById(R.id.txt_ticket_iva);
        txtTicketPago = findViewById(R.id.txt_ticket_pago);
        txtTicketPagoEfectivo = findViewById(R.id.txt_ticket_pago_efectivo);
        txtTicketPagoTarjeta = findViewById(R.id.txt_ticket_pago_tarjeta);
        txtTicketCambio = findViewById(R.id.txt_ticket_cambio);

        cargarTicket();
    }

    private void cargarTicket() {
        String ventaId = getIntent().getStringExtra(EXTRA_VENTA_ID);
        String facturaId = getIntent().getStringExtra(EXTRA_FACTURA_ID);

        if (ventaId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.VENTAS)
                    .document(ventaId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Venta venta = doc.toObject(Venta.class);
                        if (venta == null) return;
                        pintarVenta(venta);
                        cargarFacturaVerifactu(
                                venta.getFacturaId() != null ? venta.getFacturaId() : facturaId);
                    });
            return;
        }

        cargarFacturaVerifactu(facturaId);
    }

    private void cargarFacturaVerifactu(String facturaId) {
        if (facturaId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.FACTURAS)
                    .document(facturaId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Factura f = doc.toObject(Factura.class);
                        if (f == null || f.getUrlValidacion() == null) return;
                        pintarFactura(f);
                        pintarIva(f);
                        pintarVerifactu(f);
                        configurarRectificacion(f);
                    });
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.FACTURAS)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) return;
                    Factura f = snap.getDocuments().get(0).toObject(Factura.class);
                    if (f == null || f.getUrlValidacion() == null) return;
                    pintarFactura(f);
                    pintarIva(f);
                    pintarVerifactu(f);
                    configurarRectificacion(f);
                });
    }

    private void configurarRectificacion(Factura factura) {
        facturaActual = factura;
        boolean rolAutorizado = SesionUsuario.cargada()
                && (Usuario.ADMIN.equals(SesionUsuario.rol())
                    || Usuario.CAJA.equals(SesionUsuario.rol()));
        boolean rectificable = !factura.esRectificativa();
        btnRectificar.setVisibility(rolAutorizado && rectificable ? View.VISIBLE : View.GONE);
    }

    private void confirmarRectificacion() {
        if (facturaActual == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.ticket_rectificar_titulo)
                .setMessage(R.string.ticket_rectificar_mensaje)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.ticket_rectificar, (d, w) -> rectificar())
                .show();
    }

    private void rectificar() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        if (!com.SOFTBAR_F_A.ui.common.ConexionUtil.hayConexion(this)) {
            Toast.makeText(this, R.string.cobro_sin_conexion, Toast.LENGTH_LONG).show();
            return;
        }
        btnRectificar.setEnabled(false);

        new RectificacionRepository().rectificar(facturaActual, user,
                new RectificacionRepository.RectificacionCallback() {
                    @Override
                    public void onExito(String ventaId, String facturaId) {
                        if (isFinishing() || isDestroyed()) return;
                        Intent intent = new Intent(TicketActivity.this, TicketActivity.class);
                        intent.putExtra(EXTRA_VENTA_ID, ventaId);
                        intent.putExtra(EXTRA_FACTURA_ID, facturaId);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onYaRectificada() {
                        if (isFinishing() || isDestroyed()) return;
                        btnRectificar.setEnabled(true);
                        Toast.makeText(TicketActivity.this,
                                R.string.ticket_ya_rectificada, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSinTurno() {
                        if (isFinishing() || isDestroyed()) return;
                        btnRectificar.setEnabled(true);
                        Toast.makeText(TicketActivity.this,
                                R.string.cobro_error_sin_turno, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (isFinishing() || isDestroyed()) return;
                        btnRectificar.setEnabled(true);
                        Toast.makeText(TicketActivity.this,
                                mensaje != null ? mensaje
                                        : getString(R.string.ticket_rectificar_error),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void pintarVenta(Venta venta) {
        pintarDatosTicket(
                venta.getFacturaId(),
                venta.getMesaNumero(),
                venta.getLineas(),
                venta.getTotal(),
                venta.getMetodo(),
                venta.getPagoEfectivo(),
                venta.getPagoTarjeta(),
                venta.getCambio());
    }

    private void pintarFactura(Factura factura) {
        pintarDatosTicket(
                factura.getNumero(),
                factura.getMesaNumero(),
                factura.getLineas(),
                factura.getTotal(),
                factura.getMetodo(),
                factura.getPagoEfectivo(),
                factura.getPagoTarjeta(),
                factura.getCambio());
    }

    private void pintarDatosTicket(String numero, int mesaNumero,
                                   List<LineaComanda> lineas,
                                   double total, String metodo,
                                   double pagoEfectivo, double pagoTarjeta,
                                   double cambio) {
        if (txtTicketNumero != null) {
            txtTicketNumero.setText(numero != null ? numero : "");
        }
        if (txtTicketMesa != null) {
            txtTicketMesa.setText(mesaNumero > 0
                    ? String.valueOf(mesaNumero)
                    : "-");
        }
        if (txtTicketTotal != null) {
            txtTicketTotal.setText(String.format(Locale.getDefault(),
                    "%.2f EUR", total));
        }
        if (txtTicketPago != null) {
            txtTicketPago.setText(metodo != null ? metodo : "");
        }
        if (txtTicketPagoEfectivo != null) {
            txtTicketPagoEfectivo.setText(formatoImporte(pagoEfectivo));
        }
        if (txtTicketPagoTarjeta != null) {
            txtTicketPagoTarjeta.setText(formatoImporte(pagoTarjeta));
        }
        if (txtTicketCambio != null) {
            txtTicketCambio.setText(formatoImporte(cambio));
        }
        pintarLineas(lineas);
    }

    private void pintarIva(Factura factura) {
        double cuota = factura.getCuotaIva();
        double base = Dinero.restar(factura.getTotal(), cuota);
        if (txtTicketBase != null) txtTicketBase.setText(formatoImporte(base));
        if (txtTicketIva != null) txtTicketIva.setText(formatoImporte(cuota));
    }

    private String formatoImporte(double importe) {
        return String.format(Locale.getDefault(), "%.2f EUR", importe);
    }

    private void pintarLineas(List<LineaComanda> lineas) {
        if (listaTicketLineas == null) return;
        listaTicketLineas.removeAllViews();
        if (lineas == null || lineas.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText(R.string.comanda_lineas_vacias);
            vacio.setTextColor(getColor(R.color.text_secondary));
            vacio.setTextSize(13);
            listaTicketLineas.addView(vacio);
            return;
        }

        for (LineaComanda linea : lineas) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(0, dp(3), 0, dp(3));

            TextView cantidad = new TextView(this);
            cantidad.setText(String.format(Locale.getDefault(),
                    "%dx", linea.getCantidad()));
            cantidad.setTextColor(getColor(R.color.brand_600));
            cantidad.setTextSize(13);
            cantidad.setTypeface(cantidad.getTypeface(), android.graphics.Typeface.BOLD);
            fila.addView(cantidad, new LinearLayout.LayoutParams(dp(32),
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            LinearLayout infoLinea = new LinearLayout(this);
            infoLinea.setOrientation(LinearLayout.VERTICAL);

            TextView nombre = new TextView(this);
            nombre.setText(linea.getNombre());
            nombre.setTextColor(getColor(R.color.text_primary));
            nombre.setTextSize(13);
            infoLinea.addView(nombre);

            String detalle = PersonalizacionLinea.describir(linea);
            if (!detalle.isEmpty()) {
                TextView txtDetalle = new TextView(this);
                txtDetalle.setText(detalle);
                txtDetalle.setTextColor(getColor(R.color.text_muted));
                txtDetalle.setTextSize(11);
                infoLinea.addView(txtDetalle);
            }

            fila.addView(infoLinea, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView subtotal = new TextView(this);
            subtotal.setText(String.format(Locale.getDefault(),
                    "%.2f", linea.subtotal()));
            subtotal.setTextColor(getColor(R.color.text_primary));
            subtotal.setTextSize(13);
            fila.addView(subtotal, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            listaTicketLineas.addView(fila);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void pintarVerifactu(Factura factura) {
        ImageView qr = findViewById(R.id.img_qr_verifactu);
        TextView numero = findViewById(R.id.txt_numero_factura);
        TextView hash = findViewById(R.id.txt_hash_factura);

        qr.setImageBitmap(GeneradorQrVerifactu.generarBitmap(
                factura.getUrlValidacion(), 480));

        if (factura.esRectificativa()) {
            numero.setText(getString(R.string.verifactu_factura, factura.getNumero())
                    + "\n" + getString(R.string.verifactu_rectificativa,
                        factura.getFacturaRectificadaNumero()));
        } else {
            numero.setText(getString(R.string.verifactu_factura, factura.getNumero()));
        }

        String h = factura.getHashActual();
        if (h != null && h.length() > 16) h = h.substring(0, 16) + "...";
        hash.setText(getString(R.string.verifactu_hash, h));
    }
}
