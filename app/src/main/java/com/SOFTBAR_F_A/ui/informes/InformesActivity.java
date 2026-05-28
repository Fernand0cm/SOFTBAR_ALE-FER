package com.SOFTBAR_F_A.ui.informes;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.IndicadoresVentas;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class InformesActivity extends AppCompatActivity {

    private TextView kpiVentas, kpiTickets, kpiMedio, txtSinDatos;
    private TextView txtProductosSinDatos;
    private LinearLayout listaProductosVendidos;
    private BarChart grafica;
    private ListenerRegistration suscripcion;

    private String filtroMetodo = "todos";
    private String filtroFecha = "hoy";
    private List<Venta> ventasActuales = new ArrayList<>();

    private Button btnTodos, btnEfectivo, btnTarjeta, btnMixto;
    private Button btnFechaHoy, btnFechaSemana, btnFechaMes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        Header.aplica(this, getString(R.string.informes_title), getString(R.string.informes_subtitulo));

        kpiVentas = findViewById(R.id.kpi_ventas);
        kpiTickets = findViewById(R.id.kpi_tickets);
        kpiMedio = findViewById(R.id.kpi_medio);
        txtSinDatos = findViewById(R.id.txt_sin_datos);
        grafica = findViewById(R.id.grafica_horas);

        txtProductosSinDatos = findViewById(R.id.txt_productos_sin_datos);
        listaProductosVendidos = findViewById(R.id.lista_productos_vendidos);

        btnTodos = findViewById(R.id.btn_filtro_todos);
        btnEfectivo = findViewById(R.id.btn_filtro_efectivo);
        btnTarjeta = findViewById(R.id.btn_filtro_tarjeta);
        btnMixto = findViewById(R.id.btn_filtro_mixto);

        btnFechaHoy = findViewById(R.id.btn_fecha_hoy);
        btnFechaSemana = findViewById(R.id.btn_fecha_semana);
        btnFechaMes = findViewById(R.id.btn_fecha_mes);
        Button btnExportarPdf = findViewById(R.id.btn_exportar_pdf);
        btnExportarPdf.setOnClickListener(v -> exportarInformePdf());
        configurarListenersFiltros();
        configurarGrafica();

        marcarFiltroActivo(btnTodos, btnTodos, btnEfectivo, btnTarjeta, btnMixto);
        marcarFiltroActivo(btnFechaHoy, btnFechaHoy, btnFechaSemana, btnFechaMes);

        cargarVentasPorFecha();
    }

    private void configurarListenersFiltros() {
        btnTodos.setOnClickListener(v -> {
            filtroMetodo = "todos";
            marcarFiltroActivo(btnTodos, btnTodos, btnEfectivo, btnTarjeta, btnMixto);
            aplicarFiltros();
        });

        btnEfectivo.setOnClickListener(v -> {
            filtroMetodo = "efectivo";
            marcarFiltroActivo(btnEfectivo, btnTodos, btnEfectivo, btnTarjeta, btnMixto);
            aplicarFiltros();
        });

        btnTarjeta.setOnClickListener(v -> {
            filtroMetodo = "tarjeta";
            marcarFiltroActivo(btnTarjeta, btnTodos, btnEfectivo, btnTarjeta, btnMixto);
            aplicarFiltros();
        });

        btnMixto.setOnClickListener(v -> {
            filtroMetodo = "mixto";
            marcarFiltroActivo(btnMixto, btnTodos, btnEfectivo, btnTarjeta, btnMixto);
            aplicarFiltros();
        });

        btnFechaHoy.setOnClickListener(v -> {
            filtroFecha = "hoy";
            marcarFiltroActivo(btnFechaHoy, btnFechaHoy, btnFechaSemana, btnFechaMes);
            cargarVentasPorFecha();
        });

        btnFechaSemana.setOnClickListener(v -> {
            filtroFecha = "semana";
            marcarFiltroActivo(btnFechaSemana, btnFechaHoy, btnFechaSemana, btnFechaMes);
            cargarVentasPorFecha();
        });

        btnFechaMes.setOnClickListener(v -> {
            filtroFecha = "mes";
            marcarFiltroActivo(btnFechaMes, btnFechaHoy, btnFechaSemana, btnFechaMes);
            cargarVentasPorFecha();
        });
    }

    private void configurarGrafica() {
        Description desc = new Description();
        desc.setText("");
        grafica.setDescription(desc);
        grafica.setDrawGridBackground(false);
        grafica.setDrawBarShadow(false);
        grafica.setFitBars(true);
        grafica.getLegend().setEnabled(false);

        XAxis x = grafica.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        YAxis yL = grafica.getAxisLeft();
        yL.setAxisMinimum(0f);
        yL.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        grafica.getAxisRight().setEnabled(false);
    }

    private void cargarVentasPorFecha() {
        if (suscripcion != null) {
            suscripcion.remove();
            suscripcion = null;
        }

        Calendar inicio = Calendar.getInstance();

        if ("semana".equals(filtroFecha)) {
            inicio.add(Calendar.DAY_OF_YEAR, -7);
        } else if ("mes".equals(filtroFecha)) {
            inicio.set(Calendar.DAY_OF_MONTH, 1);
        }

        if ("hoy".equals(filtroFecha) || "mes".equals(filtroFecha)) {
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);
        }

        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.VENTAS)
                .whereGreaterThanOrEqualTo(FirestoreSchema.Fields.FECHA, new Timestamp(inicio.getTime()))
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    ventasActuales = snap.toObjects(Venta.class);
                    aplicarFiltros();
                });
    }

    private void aplicarFiltros() {
        List<Venta> filtradas = new ArrayList<>();

        for (Venta venta : ventasActuales) {
            if (!"todos".equals(filtroMetodo)) {
                if (venta.getMetodo() == null ||
                        !venta.getMetodo().equalsIgnoreCase(filtroMetodo)) {
                    continue;
                }
            }

            filtradas.add(venta);
        }

        pintarKpis(filtradas);
        pintarGrafica(filtradas);
        pintarVentasPorProducto(filtradas);
    }

    private void pintarKpis(List<Venta> ventas) {
        double total = IndicadoresVentas.total(ventas);
        int num = IndicadoresVentas.numeroTickets(ventas);
        double medio = IndicadoresVentas.ticketMedio(ventas);

        kpiVentas.setText(String.format(Locale.getDefault(), "%.2f EUR", total));
        kpiTickets.setText(String.valueOf(num));
        kpiMedio.setText(String.format(Locale.getDefault(), "%.2f EUR", medio));
    }

    private void pintarGrafica(List<Venta> ventas) {
        if (ventas.isEmpty()) {
            grafica.setVisibility(View.GONE);
            txtSinDatos.setVisibility(View.VISIBLE);
            return;
        }

        grafica.setVisibility(View.VISIBLE);
        txtSinDatos.setVisibility(View.GONE);

        double[] porHora = IndicadoresVentas.ventasPorHora(ventas);

        List<BarEntry> entradas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int idx = 0;

        for (int h = 8; h <= 23; h++) {
            entradas.add(new BarEntry(idx, (float) porHora[h]));
            etiquetas.add(String.format(Locale.getDefault(), "%02d", h));
            idx++;
        }

        BarDataSet set = new BarDataSet(entradas, "");
        set.setColor(ContextCompat.getColor(this, R.color.brand_600));
        set.setValueTextColor(Color.TRANSPARENT);

        BarData data = new BarData(set);
        data.setBarWidth(0.7f);
        grafica.setData(data);
        grafica.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        grafica.invalidate();
    }

    private void pintarVentasPorProducto(List<Venta> ventas) {
        listaProductosVendidos.removeAllViews();

        List<IndicadoresVentas.ProductoVendido> productos =
                IndicadoresVentas.ventasPorProducto(ventas);

        if (productos.isEmpty()) {
            txtProductosSinDatos.setVisibility(View.VISIBLE);
            return;
        }

        txtProductosSinDatos.setVisibility(View.GONE);

        int max = Math.min(productos.size(), 8);

        for (int i = 0; i < max; i++) {
            IndicadoresVentas.ProductoVendido p = productos.get(i);

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(0, dp(8), 0, dp(8));

            TextView nombre = new TextView(this);
            nombre.setText(String.format(Locale.getDefault(),
                    "%d. %s (%d uds.)",
                    i + 1,
                    p.getNombre(),
                    p.getCantidad()));
            nombre.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            nombre.setTextSize(13);

            fila.addView(nombre, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f));

            TextView total = new TextView(this);
            total.setText(String.format(Locale.getDefault(),
                    "%.2f EUR", p.getTotal()));
            total.setTextColor(ContextCompat.getColor(this, R.color.brand_600));
            total.setTextSize(13);
            total.setTypeface(total.getTypeface(), Typeface.BOLD);

            fila.addView(total);

            listaProductosVendidos.addView(fila);
        }
    }

    private void marcarFiltroActivo(Button activo, Button... botones) {
        for (Button b : botones) {
            b.setAlpha(b == activo ? 1f : 0.55f);
            b.setTypeface(null, b == activo ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
    private void exportarInformePdf() {
        List<Venta> filtradas = obtenerVentasFiltradas();

        File carpeta = new File(getCacheDir(), "informes");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivo = new File(carpeta, "informe_softbar.pdf");

        PdfDocument documento = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page pagina = documento.startPage(pageInfo);

        Canvas canvas = pagina.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(16);
        paint.setFakeBoldText(true);

        int y = 50;

        canvas.drawText("SOFTBAR - Informe de ventas", 40, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        y += 30;

        canvas.drawText("Filtro fecha: " + filtroFecha, 40, y, paint);
        y += 20;
        canvas.drawText("Filtro metodo: " + filtroMetodo, 40, y, paint);
        y += 30;

        canvas.drawText("Total vendido: " +
                String.format(Locale.getDefault(), "%.2f EUR", IndicadoresVentas.total(filtradas)), 40, y, paint);
        y += 20;

        canvas.drawText("Numero de tickets: " +
                IndicadoresVentas.numeroTickets(filtradas), 40, y, paint);
        y += 20;

        canvas.drawText("Ticket medio: " +
                String.format(Locale.getDefault(), "%.2f EUR", IndicadoresVentas.ticketMedio(filtradas)), 40, y, paint);
        y += 35;

        paint.setFakeBoldText(true);
        canvas.drawText("Ventas por producto", 40, y, paint);
        y += 25;

        paint.setFakeBoldText(false);

        List<IndicadoresVentas.ProductoVendido> productos =
                IndicadoresVentas.ventasPorProducto(filtradas);

        if (productos.isEmpty()) {
            canvas.drawText("No hay productos vendidos.", 40, y, paint);
        } else {
            int max = Math.min(productos.size(), 10);

            for (int i = 0; i < max; i++) {
                IndicadoresVentas.ProductoVendido p = productos.get(i);

                String linea = String.format(Locale.getDefault(),
                        "%d. %s - %d uds. - %.2f EUR",
                        i + 1,
                        p.getNombre(),
                        p.getCantidad(),
                        p.getTotal());

                canvas.drawText(linea, 40, y, paint);
                y += 20;
            }
        }

        documento.finishPage(pagina);

        try {
            documento.writeTo(new FileOutputStream(archivo));
            compartirPdf(archivo);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } finally {
            documento.close();
        }
    }

    private List<Venta> obtenerVentasFiltradas() {
        List<Venta> filtradas = new ArrayList<>();

        for (Venta venta : ventasActuales) {
            if (!"todos".equals(filtroMetodo)) {
                if (venta.getMetodo() == null ||
                        !venta.getMetodo().equalsIgnoreCase(filtroMetodo)) {
                    continue;
                }
            }

            filtradas.add(venta);
        }

        return filtradas;
    }

    private void compartirPdf(File archivo) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                archivo
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Compartir informe PDF"));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) suscripcion.remove();
    }
}