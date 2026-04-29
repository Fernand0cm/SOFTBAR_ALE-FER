package com.SOFTBAR_F_A.ui.informes;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.IndicadoresVentas;
import com.SOFTBAR_F_A.data.Venta;
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

public class InformesActivity extends AppCompatActivity {

    private TextView kpiVentas, kpiTickets, kpiMedio, txtSinDatos;
    private BarChart grafica;
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        kpiVentas = findViewById(R.id.kpi_ventas);
        kpiTickets = findViewById(R.id.kpi_tickets);
        kpiMedio = findViewById(R.id.kpi_medio);
        txtSinDatos = findViewById(R.id.txt_sin_datos);
        grafica = findViewById(R.id.grafica_horas);

        configurarGrafica();

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());

        cargarVentasDelDia();
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

    private void cargarVentasDelDia() {
        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        suscripcion = FirebaseFirestore.getInstance()
                .collection("ventas")
                .whereGreaterThanOrEqualTo("fecha", new Timestamp(inicio.getTime()))
                .orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    List<Venta> ventas = snap.toObjects(Venta.class);
                    pintarKpis(ventas);
                    pintarGrafica(ventas);
                });
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

        // Mostrar solo el rango horario tipico de un bar (8-23)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) suscripcion.remove();
    }
}
