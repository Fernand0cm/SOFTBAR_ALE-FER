package com.SOFTBAR_F_A.ui.common;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialogo y utilidades compartidas para personalizar una linea de comanda
 * (modificadores predefinidos + nota libre). Lo usan tanto la comanda de mesa
 * como la barra rapida, evitando duplicar la logica.
 */
public final class PersonalizacionLinea {

    private PersonalizacionLinea() { }

    /** Texto resumido de modificadores y nota para mostrar bajo la linea. */
    public static String describir(LineaComanda linea) {
        StringBuilder sb = new StringBuilder();
        List<String> mods = linea.getModificadores();
        if (mods != null && !mods.isEmpty()) {
            sb.append(TextUtils.join(", ", mods));
        }
        String nota = linea.getNota();
        if (nota != null && !nota.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(nota.trim());
        }
        return sb.toString();
    }

    /**
     * Abre el dialogo de personalizacion. Al guardar, actualiza la linea y
     * ejecuta {@code onGuardado} (persistir o repintar segun la pantalla).
     */
    public static void mostrarDialogo(Context context, LineaComanda linea, Runnable onGuardado) {
        View vista = LayoutInflater.from(context).inflate(R.layout.dialog_linea, null);
        ChipGroup chips = vista.findViewById(R.id.chips_modificadores);
        EditText inputNota = vista.findViewById(R.id.input_nota);

        String[] opciones = context.getResources().getStringArray(R.array.modificadores_linea);
        List<String> actuales = linea.getModificadores();
        for (String opcion : opciones) {
            Chip chip = new Chip(context);
            chip.setText(opcion);
            chip.setCheckable(true);
            chip.setChecked(actuales != null && actuales.contains(opcion));
            chips.addView(chip);
        }
        if (linea.getNota() != null) {
            inputNota.setText(linea.getNota());
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.linea_dialog_titulo)
                .setView(vista)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    List<String> seleccion = new ArrayList<>();
                    for (int i = 0; i < chips.getChildCount(); i++) {
                        Chip chip = (Chip) chips.getChildAt(i);
                        if (chip.isChecked()) seleccion.add(chip.getText().toString());
                    }
                    linea.setModificadores(seleccion);
                    String nota = inputNota.getText().toString().trim();
                    linea.setNota(nota.isEmpty() ? null : nota);
                    onGuardado.run();
                })
                .show();
    }
}
