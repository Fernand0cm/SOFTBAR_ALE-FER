package com.SOFTBAR_F_A.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Pinta el catalogo de productos en una rejilla con un filtro de categorias por
 * chips. Encapsula toda la presentacion (rejilla, chips y filtrado) que antes
 * estaba duplicada en la comanda y en la barra: la pantalla solo aporta las
 * vistas y que hacer al pulsar un producto.
 */
public class CatalogoCategorias {

    public interface OnProductoSeleccionado {
        void onProducto(Producto producto);
    }

    private final Context context;
    private final GridLayout grid;
    private final ChipGroup chips;
    private final TextView textoVacio;
    private final OnProductoSeleccionado listener;

    private List<Producto> catalogo = new ArrayList<>();
    private String categoriaSeleccionada; // null = todas

    public CatalogoCategorias(Context context, GridLayout grid, ChipGroup chips,
                              TextView textoVacio, OnProductoSeleccionado listener) {
        this.context = context;
        this.grid = grid;
        this.chips = chips;
        this.textoVacio = textoVacio;
        this.listener = listener;
    }

    /** Actualiza el catalogo (productos ya filtrados por activo) y repinta. */
    public void setProductos(List<Producto> productos) {
        this.catalogo = productos != null ? productos : new ArrayList<>();
        construirChips();
        pintarGrid(filtrar());
    }

    private void construirChips() {
        chips.removeAllViews();
        LinkedHashSet<String> categorias = new LinkedHashSet<>();
        for (Producto p : catalogo) categorias.add(p.getCategoria());

        chips.addView(crearChip(context.getString(R.string.historial_todas_categorias), null));
        for (String categoria : categorias) {
            chips.addView(crearChip(categoria, categoria));
        }
    }

    private Chip crearChip(String texto, String valorCategoria) {
        Chip chip = new Chip(context);
        chip.setText(texto);
        chip.setCheckable(true);
        boolean seleccionado = (valorCategoria == null && categoriaSeleccionada == null)
                || (valorCategoria != null && valorCategoria.equals(categoriaSeleccionada));
        chip.setChecked(seleccionado);
        chip.setOnClickListener(v -> {
            categoriaSeleccionada = valorCategoria;
            pintarGrid(filtrar());
        });
        return chip;
    }

    private List<Producto> filtrar() {
        if (categoriaSeleccionada == null) return catalogo;
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : catalogo) {
            if (categoriaSeleccionada.equals(p.getCategoria())) filtrados.add(p);
        }
        return filtrados;
    }

    private void pintarGrid(List<Producto> productos) {
        grid.removeAllViews();
        if (productos.isEmpty()) {
            textoVacio.setVisibility(View.VISIBLE);
            return;
        }
        textoVacio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(context);
        float density = context.getResources().getDisplayMetrics().density;
        int margen = (int) (6 * density);

        for (Producto p : productos) {
            View item = inflater.inflate(R.layout.item_catalogo, grid, false);
            ((TextView) item.findViewById(R.id.txt_nombre_producto)).setText(p.getNombre());
            ((TextView) item.findViewById(R.id.txt_precio_producto))
                    .setText(String.format(Locale.getDefault(), "%.2f EUR", p.getPrecio()));
            item.setOnClickListener(v -> listener.onProducto(p));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(margen, margen, margen, margen);
            item.setLayoutParams(params);

            grid.addView(item);
        }
    }
}
