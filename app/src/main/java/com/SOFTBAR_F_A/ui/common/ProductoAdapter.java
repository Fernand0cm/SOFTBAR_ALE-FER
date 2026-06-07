package com.SOFTBAR_F_A.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador del catalogo para RecyclerView: recicla las vistas, de modo que
 * pintar cientos de productos no infla cientos de vistas a la vez.
 */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.VH> {

    public interface OnProductoSeleccionado {
        void onProducto(Producto producto);
    }

    private final List<Producto> items = new ArrayList<>();
    private final OnProductoSeleccionado listener;

    public ProductoAdapter(OnProductoSeleccionado listener) {
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
                .inflate(R.layout.item_catalogo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Producto p = items.get(position);
        h.nombre.setText(p.getNombre());
        h.precio.setText(String.format(Locale.getDefault(), "%.2f EUR", p.getPrecio()));
        h.itemView.setOnClickListener(v -> listener.onProducto(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView precio;

        VH(@NonNull View v) {
            super(v);
            nombre = v.findViewById(R.id.txt_nombre_producto);
            precio = v.findViewById(R.id.txt_precio_producto);
        }
    }
}
