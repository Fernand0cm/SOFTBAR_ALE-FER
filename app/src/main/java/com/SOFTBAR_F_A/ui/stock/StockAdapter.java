package com.SOFTBAR_F_A.ui.stock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de la pantalla de stock (RecyclerView): recicla las filas y resalta
 * los productos bajo minimo. Botones +/- para reponer y toque para ajustar.
 */
public class StockAdapter extends RecyclerView.Adapter<StockAdapter.VH> {

    public interface Acciones {
        void onMas(Producto producto);
        void onMenos(Producto producto);
        void onAjustar(Producto producto);
    }

    private final List<Producto> items = new ArrayList<>();
    private final Acciones listener;

    public StockAdapter(Acciones listener) {
        this.listener = listener;
    }

    public void setItems(List<Producto> nuevos) {
        items.clear();
        if (nuevos != null) items.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Producto p = items.get(position);
        h.nombre.setText(p.getNombre());
        if (p.bajoStock()) {
            h.detalle.setText(h.itemView.getContext().getString(
                    R.string.stock_bajo, p.getStock(), p.getStockMinimo()));
            h.detalle.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.warning));
        } else {
            h.detalle.setText(h.itemView.getContext().getString(
                    R.string.stock_detalle, p.getStock(), p.getStockMinimo()));
            h.detalle.setTextColor(ContextCompat.getColor(
                    h.itemView.getContext(), R.color.text_secondary));
        }
        h.mas.setOnClickListener(v -> listener.onMas(p));
        h.menos.setOnClickListener(v -> listener.onMenos(p));
        h.info.setOnClickListener(v -> listener.onAjustar(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView nombre, detalle;
        final View mas, menos, info;

        VH(@NonNull View v) {
            super(v);
            nombre = v.findViewById(R.id.txt_stock_nombre);
            detalle = v.findViewById(R.id.txt_stock_detalle);
            mas = v.findViewById(R.id.btn_stock_mas);
            menos = v.findViewById(R.id.btn_stock_menos);
            info = v.findViewById(R.id.stock_info);
        }
    }
}
