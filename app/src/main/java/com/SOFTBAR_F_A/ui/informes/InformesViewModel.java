package com.SOFTBAR_F_A.ui.informes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.repository.InformesRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel de la pantalla de informes. Mantiene el dia y el metodo de pago
 * seleccionados, se suscribe a las ventas de ese dia a traves del repositorio y
 * publica el estado de UI ya filtrado. Libera la suscripcion en
 * {@link #onCleared()}.
 */
public class InformesViewModel extends ViewModel {

    private final InformesRepository repositorio;
    private final MutableLiveData<InformesUiState> estado = new MutableLiveData<>();

    private Date diaSeleccionado;
    private String metodoFiltro; // null = todos
    private List<Venta> ultimasVentas = new ArrayList<>();
    private ListenerRegistration registro;

    public InformesViewModel() {
        this(new InformesRepository());
    }

    public InformesViewModel(InformesRepository repositorio) {
        this.repositorio = repositorio;
        this.diaSeleccionado = inicioDelDia(new Date());
        estado.setValue(InformesUiState.cargando());
        suscribirse();
    }

    public LiveData<InformesUiState> getEstado() {
        return estado;
    }

    public Date getDiaSeleccionado() {
        return diaSeleccionado;
    }

    public String getMetodoFiltro() {
        return metodoFiltro;
    }

    public void setDia(Date dia) {
        this.diaSeleccionado = inicioDelDia(dia);
        estado.setValue(InformesUiState.cargando());
        suscribirse();
    }

    public void setMetodo(String metodo) {
        this.metodoFiltro = metodo;
        publicarEstado();
    }

    private void suscribirse() {
        if (registro != null) registro.remove();
        registro = repositorio.escucharVentasDelDia(diaSeleccionado,
                new InformesRepository.VentasListener() {
                    @Override
                    public void onVentas(List<Venta> ventas) {
                        ultimasVentas = ventas;
                        publicarEstado();
                    }

                    @Override
                    public void onError(String mensaje) {
                        estado.setValue(InformesUiState.error(mensaje));
                    }
                });
    }

    private void publicarEstado() {
        estado.setValue(InformesUiState.datos(filtrarPorMetodo(ultimasVentas)));
    }

    private List<Venta> filtrarPorMetodo(List<Venta> ventas) {
        if (metodoFiltro == null) return ventas;
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getMetodo() != null && v.getMetodo().equalsIgnoreCase(metodoFiltro)) {
                filtradas.add(v);
            }
        }
        return filtradas;
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
        if (registro != null) {
            registro.remove();
            registro = null;
        }
    }
}
