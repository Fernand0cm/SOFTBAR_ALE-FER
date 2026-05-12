package com.SOFTBAR_F_A.ui.turno;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.MovimientoCaja;
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.caja.CajaActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TurnoActivity extends AppCompatActivity {

    private TextView txtEstado;
    private TextView txtDetalle;
    private EditText inputImporteInicial;
    private Button btnAbrir;
    private Button btnCerrar;
    private String turnoActivoId;
    private final SimpleDateFormat fechaFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turno);
        Header.aplica(this, getString(R.string.turno_title));

        txtEstado = findViewById(R.id.txt_turno_estado);
        txtDetalle = findViewById(R.id.txt_turno_detalle);
        inputImporteInicial = findViewById(R.id.input_importe_inicial);
        btnAbrir = findViewById(R.id.btn_abrir_turno);
        btnCerrar = findViewById(R.id.btn_cerrar_turno);

        btnAbrir.setOnClickListener(v -> abrirTurno());
        btnCerrar.setOnClickListener(v ->
                startActivity(new Intent(this, CajaActivity.class)));

        cargarTurnoActivo();
    }

    private void cargarTurnoActivo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            pintarSinTurno();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.ABIERTO)
                .whereEqualTo(FirestoreSchema.Fields.USUARIO_UID, user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        pintarSinTurno();
                        return;
                    }
                    turnoActivoId = snap.getDocuments().get(0).getId();
                    Turno turno = snap.getDocuments().get(0).toObject(Turno.class);
                    pintarTurno(turno);
                });
    }

    private void abrirTurno() {
        if (turnoActivoId != null) {
            Toast.makeText(this, R.string.turno_error_ya_abierto, Toast.LENGTH_SHORT).show();
            return;
        }

        String txt = inputImporteInicial.getText().toString().trim();
        if (TextUtils.isEmpty(txt)) {
            Toast.makeText(this, R.string.turno_error_importe, Toast.LENGTH_SHORT).show();
            return;
        }

        double importe;
        try {
            importe = Double.parseDouble(txt.replace(',', '.'));
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.turno_error_importe, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Timestamp ahora = new Timestamp(new Date());
        Turno turno = new Turno(ahora, importe, user.getUid(), user.getEmail());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference turnoRef = db.collection(FirestoreSchema.Collections.TURNOS).document();

        MovimientoCaja apertura = new MovimientoCaja(
                ahora, MovimientoCaja.APERTURA, importe,
                getString(R.string.caja_mov_apertura));
        apertura.setTurnoId(turnoRef.getId());
        apertura.setUsuarioUid(user.getUid());
        apertura.setUsuarioEmail(user.getEmail());

        db.runBatch(batch -> {
                    batch.set(turnoRef, turno);
                    batch.set(db.collection(FirestoreSchema.Collections.MOVIMIENTOS_CAJA).document(),
                            apertura);
                })
                .addOnSuccessListener(unused -> {
                    turnoActivoId = turnoRef.getId();
                    pintarTurno(turno);
                    Toast.makeText(this, R.string.turno_abierto_ok, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void pintarSinTurno() {
        txtEstado.setText(R.string.turno_estado_cerrado);
        txtDetalle.setText(R.string.turno_detalle_vacio);
        inputImporteInicial.setEnabled(true);
        btnAbrir.setEnabled(true);
        btnCerrar.setEnabled(false);
    }

    private void pintarTurno(Turno turno) {
        txtEstado.setText(R.string.turno_estado_abierto);
        if (turno != null && turno.getFechaApertura() != null) {
            txtDetalle.setText(getString(R.string.turno_detalle_apertura,
                    fechaFmt.format(turno.getFechaApertura().toDate()),
                    turno.getImporteInicial()));
        }
        inputImporteInicial.setEnabled(false);
        btnAbrir.setEnabled(false);
        btnCerrar.setEnabled(true);
    }
}
