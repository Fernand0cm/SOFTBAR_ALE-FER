package com.SOFTBAR_F_A.ui.informes;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.common.Header;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla de informes. Sigue el patron MVVM: no accede a Firestore
 * directamente, sino que observa el estado publicado por {@link InformesViewModel}
 * y se limita a pintarlo, gestionando los estados de carga, vacio y error.
 */
public class InformesActivity extends AppCompatActivity {

    private TextView kpiVentas, kpiTickets, kpiMedio, txtSinDatos;
    private BarChart grafica;
    private InformesViewModel viewModel;

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

        configurarGrafica();

        viewModel = new ViewModelProvider(this).get(InformesViewModel.class);
        viewModel.getEstado().observe(this, this::render);
    }

    private void render(InformesUiState estado) {
        switch (estado.tipo) {
            case CARGANDO:
                mostrarMensaje(getString(R.string.informes_cargando));
                break;
            case ERROR:
                mostrarMensaje(getString(R.string.informes_error,
                        estado.error != null ? estado.error : ""));
                break;
            case DATOS:
                pintarKpis(estado);
                if (estado.isVacio()) {
                    mostrarMensaje(getString(R.string.informes_sin_datos));
                } else {
                    grafica.setVisibility(View.VISIBLE);
                    txtSinDatos.setVisibility(View.GONE);
                    pintarGrafica(estado.ventasPorHora);
                }
                break;
        }
    }

    private void mostrarMensaje(String mensaje) {
        grafica.setVisibility(View.GONE);
        txtSinDatos.setVisibility(View.VISIBLE);
        txtSinDatos.setText(mensaje);
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

    private void pintarKpis(InformesUiState estado) {
        kpiVentas.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.total));
        kpiTickets.setText(String.valueOf(estado.numeroTickets));
        kpiMedio.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.ticketMedio));
    }

    private void pintarGrafica(double[] porHora) {
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
}
