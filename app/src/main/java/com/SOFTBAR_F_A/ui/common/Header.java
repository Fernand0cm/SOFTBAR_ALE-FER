package com.SOFTBAR_F_A.ui.common;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.SOFTBAR_F_A.R;

public class Header {

    private Header() { }

    public static void aplica(Activity activity, String titulo) {
        aplica(activity, titulo, null);
    }

    public static void aplica(Activity activity, String titulo, String subtitulo) {
        TextView t = activity.findViewById(R.id.header_title);
        TextView s = activity.findViewById(R.id.header_subtitle);
        View back = activity.findViewById(R.id.btn_back);

        if (t != null) t.setText(titulo);
        if (s != null) {
            if (subtitulo == null) {
                s.setVisibility(View.GONE);
            } else {
                s.setVisibility(View.VISIBLE);
                s.setText(subtitulo);
            }
        }
        if (back != null) back.setOnClickListener(v -> activity.finish());
    }
}
