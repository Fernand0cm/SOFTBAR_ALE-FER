package com.SOFTBAR_F_A.ui.config;

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

/**
 * Adaptador del catalogo en Configuracion (RecyclerView): recicla las vistas y
 * marca los productos inactivos. Al tocar una fila se edita el producto.
 */
public class ProductoConfigAdapter
        extends RecyclerView.Adapter<ProductoConfigAdapter.VH> {

    public interface OnEditar {
        void onEditar(Producto producto);
    }

    private final List<Producto> items = new ArrayList<>();
    private final OnEditar listener;

    public ProductoConfigAdapter(OnEditar listener) {
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
                .inflate(R.layout.item_producto, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Producto p = items.get(position);
        String nombre = p.isActivo()
                ? p.getNombre()
                : h.itemView.getContext().getString(R.string.config_producto_inactivo, p.getNombre());
        h.nombre.setText(nombre);
        h.itemView.setAlpha(p.isActivo() ? 1f : 0.45f);
        h.codigo.setText(h.itemView.getContext().getString(
                R.string.config_codigo_categoria, p.getCodigoBarras(), p.getCategoria()));
        h.precio.setText(h.itemView.getContext().getString(
                R.string.config_precio_iva, p.getPrecio(), Math.round(p.getTipoIva() * 100)));
        h.itemView.setOnClickListener(v -> listener.onEditar(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView nombre, codigo, precio;

        VH(@NonNull View v) {
            super(v);
            nombre = v.findViewById(R.id.txt_nombre);
            codigo = v.findViewById(R.id.txt_codigo);
            precio = v.findViewById(R.id.txt_precio);
        }
    }
}
