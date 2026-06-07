package com.SOFTBAR_F_A.ui.common;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Catalogo de productos con filtro de categorias por chips, pintado en un
 * RecyclerView (rejilla de 2 columnas) que recicla las vistas. Encapsula la
 * presentacion que antes estaba duplicada en comanda y barra: la pantalla solo
 * aporta las vistas y que hacer al pulsar un producto.
 */
public class CatalogoCategorias {

    public interface OnProductoSeleccionado {
        void onProducto(Producto producto);
    }

    private final Context context;
    private final ChipGroup chips;
    private final TextView textoVacio;
    private final ProductoAdapter adapter;

    private List<Producto> catalogo = new ArrayList<>();
    private String categoriaSeleccionada; // null = todas

    public CatalogoCategorias(Context context, RecyclerView lista, ChipGroup chips,
                              TextView textoVacio, OnProductoSeleccionado listener) {
        this.context = context;
        this.chips = chips;
        this.textoVacio = textoVacio;
        this.adapter = new ProductoAdapter(listener::onProducto);
        lista.setLayoutManager(new GridLayoutManager(context, 2));
        lista.setAdapter(adapter);
    }

    /** Actualiza el catalogo (productos ya filtrados por activo) y repinta. */
    public void setProductos(List<Producto> productos) {
        this.catalogo = productos != null ? productos : new ArrayList<>();
        construirChips();
        aplicarFiltro();
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
            aplicarFiltro();
        });
        return chip;
    }

    private void aplicarFiltro() {
        List<Producto> filtrados = filtrar();
        adapter.setItems(filtrados);
        textoVacio.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Producto> filtrar() {
        if (categoriaSeleccionada == null) return catalogo;
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : catalogo) {
            if (categoriaSeleccionada.equals(p.getCategoria())) filtrados.add(p);
        }
        return filtrados;
    }
}
