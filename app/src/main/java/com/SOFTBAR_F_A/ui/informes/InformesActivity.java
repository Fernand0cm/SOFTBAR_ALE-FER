package com.SOFTBAR_F_A.ui.informes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.VentasPorProducto;
import com.SOFTBAR_F_A.ui.common.Header;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla de informes (MVVM). Observa el estado de {@link InformesViewModel} y
 * lo pinta: KPIs, desglose fiscal, grafica por hora y ranking de productos.
 * Permite filtrar por dia y por metodo de pago, y exportar un resumen.
 */
public class InformesActivity extends AppCompatActivity {

    private TextView kpiVentas, kpiTickets, kpiMedio, txtBase, txtIva, txtSinDatos;
    private TextView txtTopVacio;
    private LinearLayout listaTopProductos;
    private BarChart grafica;
    private BarChart graficaDias;
    private Button btnFecha;
    private ChipGroup chipsMetodo;
    private ChipGroup chipsTurno;
    private InformesViewModel viewModel;
    private InformesUiState estadoActual;
    private List<String> turnosActuales = new ArrayList<>();

    private final SimpleDateFormat fechaFmt =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        Header.aplica(this, getString(R.string.informes_title), getString(R.string.informes_subtitulo));

        kpiVentas = findViewById(R.id.kpi_ventas);
        kpiTickets = findViewById(R.id.kpi_tickets);
        kpiMedio = findViewById(R.id.kpi_medio);
        txtBase = findViewById(R.id.txt_base);
        txtIva = findViewById(R.id.txt_iva);
        txtSinDatos = findViewById(R.id.txt_sin_datos);
        txtTopVacio = findViewById(R.id.txt_top_vacio);
        listaTopProductos = findViewById(R.id.lista_top_productos);
        grafica = findViewById(R.id.grafica_horas);
        graficaDias = findViewById(R.id.grafica_dias);
        btnFecha = findViewById(R.id.btn_fecha);
        chipsMetodo = findViewById(R.id.chips_metodo);
        chipsTurno = findViewById(R.id.chips_turno);

        configurarGrafica(grafica);
        configurarGrafica(graficaDias);

        viewModel = new ViewModelProvider(this).get(InformesViewModel.class);

        actualizarBotonFecha();
        btnFecha.setOnClickListener(v -> abrirSelectorFecha());
        chipsMetodo.setOnCheckedStateChangeListener((group, checkedIds) ->
                viewModel.setMetodo(metodoSeleccionado(checkedIds)));
        findViewById(R.id.btn_exportar).setOnClickListener(v -> exportarResumen());

        viewModel.getEstado().observe(this, this::render);
        viewModel.getTurnos().observe(this, this::construirChipsTurno);
        viewModel.getComparativa().observe(this, this::pintarComparativa);
    }

    private String metodoSeleccionado(List<Integer> checkedIds) {
        if (checkedIds.isEmpty()) return null;
        int id = checkedIds.get(0);
        if (id == R.id.chip_metodo_efectivo) return getString(R.string.cobro_efectivo);
        if (id == R.id.chip_metodo_tarjeta) return getString(R.string.cobro_tarjeta);
        if (id == R.id.chip_metodo_mixto) return getString(R.string.cobro_mixto);
        return null;
    }

    private void abrirSelectorFecha() {
        Calendar c = Calendar.getInstance();
        c.setTime(viewModel.getDiaSeleccionado());
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar elegido = Calendar.getInstance();
            elegido.set(year, month, day);
            viewModel.setDia(elegido.getTime());
            actualizarBotonFecha();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarBotonFecha() {
        btnFecha.setText(getString(R.string.informes_fecha,
                fechaFmt.format(viewModel.getDiaSeleccionado())));
    }

    private void render(InformesUiState estado) {
        estadoActual = estado;
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
                pintarTopProductos(estado.topProductos);
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

    private void configurarGrafica(BarChart chart) {
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setFitBars(true);
        chart.getLegend().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        YAxis yL = chart.getAxisLeft();
        yL.setAxisMinimum(0f);
        yL.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        chart.getAxisRight().setEnabled(false);
    }

    private void construirChipsTurno(List<String> turnoIds) {
        turnosActuales = turnoIds;
        chipsTurno.removeAllViews();

        Chip chipTodos = new Chip(this);
        chipTodos.setText(R.string.informes_turno_todos);
        chipTodos.setCheckable(true);
        chipTodos.setChecked(true);
        chipTodos.setOnClickListener(v -> viewModel.setTurno(null));
        chipsTurno.addView(chipTodos);

        for (int i = 0; i < turnoIds.size(); i++) {
            String turnoId = turnoIds.get(i);
            Chip chip = new Chip(this);
            chip.setText(getString(R.string.informes_turno_n, i + 1));
            chip.setCheckable(true);
            chip.setOnClickListener(v -> viewModel.setTurno(turnoId));
            chipsTurno.addView(chip);
        }
    }

    private void pintarComparativa(double[] totales) {
        List<BarEntry> entradas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        SimpleDateFormat dm = new SimpleDateFormat("dd/MM", Locale.getDefault());
        int dias = totales.length;
        for (int i = 0; i < dias; i++) {
            entradas.add(new BarEntry(i, (float) totales[i]));
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, -(dias - 1 - i));
            etiquetas.add(dm.format(c.getTime()));
        }

        BarDataSet set = new BarDataSet(entradas, "");
        set.setColor(ContextCompat.getColor(this, R.color.brand_600));
        set.setValueTextColor(Color.TRANSPARENT);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        graficaDias.setData(data);
        graficaDias.getXAxis().setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        graficaDias.invalidate();
    }

    private void pintarKpis(InformesUiState estado) {
        kpiVentas.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.total));
        kpiTickets.setText(String.valueOf(estado.numeroTickets));
        kpiMedio.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.ticketMedio));
        txtBase.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.base));
        txtIva.setText(String.format(Locale.getDefault(), "%.2f EUR", estado.iva));
    }

    private void pintarTopProductos(List<VentasPorProducto.Item> top) {
        listaTopProductos.removeAllViews();
        if (top == null || top.isEmpty()) {
            txtTopVacio.setVisibility(View.VISIBLE);
            return;
        }
        txtTopVacio.setVisibility(View.GONE);

        int limite = Math.min(top.size(), 5);
        for (int i = 0; i < limite; i++) {
            VentasPorProducto.Item item = top.get(i);
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(0, dp(4), 0, dp(4));

            TextView nombre = new TextView(this);
            nombre.setText(item.nombre);
            nombre.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            nombre.setTextSize(14);
            fila.addView(nombre, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView detalle = new TextView(this);
            detalle.setText(getString(R.string.informes_top_item, item.cantidad, item.importe));
            detalle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            detalle.setTextSize(13);
            detalle.setGravity(Gravity.END);
            fila.addView(detalle);

            listaTopProductos.addView(fila);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void pintarGrafica(double[] porHora) {
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

    private void exportarResumen() {
        if (estadoActual == null || estadoActual.tipo != InformesUiState.Tipo.DATOS) return;

        String metodo = viewModel.getMetodoFiltro() != null
                ? viewModel.getMetodoFiltro()
                : getString(R.string.informes_metodo_todas);

        String resumen = getString(R.string.informes_resumen,
                fechaFmt.format(viewModel.getDiaSeleccionado()),
                metodo,
                estadoActual.total,
                estadoActual.numeroTickets,
                estadoActual.ticketMedio,
                estadoActual.base,
                estadoActual.iva);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.informes_resumen_titulo));
        intent.putExtra(Intent.EXTRA_TEXT, resumen);
        startActivity(Intent.createChooser(intent, getString(R.string.informes_exportar)));
    }
}
