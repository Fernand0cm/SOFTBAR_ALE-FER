package com.SOFTBAR_F_A.ui.informes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SOFTBAR_F_A.data.ComparativaDias;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.repository.InformesRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * ViewModel de la pantalla de informes. Mantiene el dia, el metodo de pago y el
 * turno seleccionados, se suscribe a las ventas de ese dia y publica el estado
 * de UI ya filtrado. Mantiene ademas una grafica comparativa de los ultimos
 * dias. Libera las suscripciones en {@link #onCleared()}.
 */
public class InformesViewModel extends ViewModel {

    public static final int DIAS_COMPARATIVA = 7;

    private final InformesRepository repositorio;
    private final MutableLiveData<InformesUiState> estado = new MutableLiveData<>();
    private final MutableLiveData<List<String>> turnos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<double[]> comparativa =
            new MutableLiveData<>(new double[DIAS_COMPARATIVA]);

    private Date diaSeleccionado;
    private String metodoFiltro; // null = todos
    private String turnoFiltro;  // null = todos
    private List<Venta> ultimasVentas = new ArrayList<>();
    private ListenerRegistration registroDia;
    private ListenerRegistration registroComparativa;

    public InformesViewModel() {
        this(new InformesRepository());
    }

    public InformesViewModel(InformesRepository repositorio) {
        this.repositorio = repositorio;
        this.diaSeleccionado = inicioDelDia(new Date());
        estado.setValue(InformesUiState.cargando());
        suscribirseDia();
        suscribirseComparativa();
    }

    public LiveData<InformesUiState> getEstado() {
        return estado;
    }

    public LiveData<List<String>> getTurnos() {
        return turnos;
    }

    public LiveData<double[]> getComparativa() {
        return comparativa;
    }

    public Date getDiaSeleccionado() {
        return diaSeleccionado;
    }

    public String getMetodoFiltro() {
        return metodoFiltro;
    }

    public void setDia(Date dia) {
        this.diaSeleccionado = inicioDelDia(dia);
        this.turnoFiltro = null; // los turnos cambian con el dia
        estado.setValue(InformesUiState.cargando());
        suscribirseDia();
    }

    public void setMetodo(String metodo) {
        this.metodoFiltro = metodo;
        publicarEstado();
    }

    public void setTurno(String turnoId) {
        this.turnoFiltro = turnoId;
        publicarEstado();
    }

    private void suscribirseDia() {
        if (registroDia != null) registroDia.remove();
        registroDia = repositorio.escucharVentasDelDia(diaSeleccionado,
                new InformesRepository.VentasListener() {
                    @Override
                    public void onVentas(List<Venta> ventas) {
                        ultimasVentas = ventas;
                        turnos.setValue(turnosDistintos(ventas));
                        publicarEstado();
                    }

                    @Override
                    public void onError(String mensaje) {
                        estado.setValue(InformesUiState.error(mensaje));
                    }
                });
    }

    private void suscribirseComparativa() {
        registroComparativa = repositorio.escucharUltimosDias(DIAS_COMPARATIVA,
                new InformesRepository.VentasListener() {
                    @Override
                    public void onVentas(List<Venta> ventas) {
                        comparativa.setValue(ComparativaDias.totalesUltimosDias(
                                ventas, DIAS_COMPARATIVA, new Date()));
                    }

                    @Override
                    public void onError(String mensaje) {
                        // La comparativa es secundaria: si falla, no rompe la pantalla.
                    }
                });
    }

    private void publicarEstado() {
        estado.setValue(InformesUiState.datos(filtrar(ultimasVentas)));
    }

    private List<Venta> filtrar(List<Venta> ventas) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : ventas) {
            if (metodoFiltro != null
                    && (v.getMetodo() == null || !v.getMetodo().equalsIgnoreCase(metodoFiltro))) {
                continue;
            }
            if (turnoFiltro != null && !turnoFiltro.equals(v.getTurnoId())) {
                continue;
            }
            filtradas.add(v);
        }
        return filtradas;
    }

    private List<String> turnosDistintos(List<Venta> ventas) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (Venta v : ventas) {
            if (v.getTurnoId() != null) ids.add(v.getTurnoId());
        }
        return new ArrayList<>(ids);
    }

    private Date inicioDelDia(Date dia) {
        Calendar c = Calendar.getInstance();
        c.setTime(dia);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registroDia != null) {
            registroDia.remove();
            registroDia = null;
        }
        if (registroComparativa != null) {
            registroComparativa.remove();
            registroComparativa = null;
        }
    }
}
