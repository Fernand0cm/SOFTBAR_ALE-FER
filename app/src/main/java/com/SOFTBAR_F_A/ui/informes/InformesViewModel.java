package com.SOFTBAR_F_A.ui.informes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.repository.InformesRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * ViewModel de la pantalla de informes. Sobrevive a los cambios de
 * configuracion (rotacion) y mantiene el estado observable mediante LiveData.
 * Se suscribe a las ventas del dia a traves del repositorio y publica el
 * estado de UI; libera la suscripcion en {@link #onCleared()}.
 */
public class InformesViewModel extends ViewModel {

    private final InformesRepository repositorio;
    private final MutableLiveData<InformesUiState> estado = new MutableLiveData<>();
    private ListenerRegistration registro;

    public InformesViewModel() {
        this(new InformesRepository());
    }

    public InformesViewModel(InformesRepository repositorio) {
        this.repositorio = repositorio;
        estado.setValue(InformesUiState.cargando());
        suscribirse();
    }

    public LiveData<InformesUiState> getEstado() {
        return estado;
    }

    private void suscribirse() {
        registro = repositorio.escucharVentasDelDia(new InformesRepository.VentasListener() {
            @Override
            public void onVentas(List<Venta> ventas) {
                estado.setValue(InformesUiState.datos(ventas));
            }

            @Override
            public void onError(String mensaje) {
                estado.setValue(InformesUiState.error(mensaje));
            }
        });
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
