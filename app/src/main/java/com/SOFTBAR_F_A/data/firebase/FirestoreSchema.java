package com.SOFTBAR_F_A.data.firebase;

public final class FirestoreSchema {

    private FirestoreSchema() { }

    public static final class Collections {
        public static final String COMANDAS = "comandas";
        public static final String CONFIGURACION = "configuracion";
        public static final String CONTADORES = "contadores";
        public static final String FACTURAS = "facturas";
        public static final String MESAS = "mesas";
        public static final String MOVIMIENTOS_CAJA = "movimientos_caja";
        public static final String PRODUCTOS = "productos";
        public static final String SPLASH_BACKGROUNDS = "splash_backgrounds";
        public static final String TURNOS = "turnos";
        public static final String USUARIOS = "usuarios";
        public static final String VENTAS = "ventas";

        private Collections() { }
    }

    public static final class Documents {
        public static final String CONTADOR_FACTURAS = "facturas";
        public static final String CONFIG_FISCAL = "fiscal";

        private Documents() { }
    }

    public static final class Fields {
        public static final String COMANDA_ACTIVA_ID = "comandaActivaId";
        public static final String ESTADO = "estado";
        public static final String FACTURA_ID = "facturaId";
        public static final String FECHA_APERTURA = "fechaApertura";
        public static final String FECHA_CIERRE = "fechaCierre";
        public static final String FECHA = "fecha";
        public static final String HASH_ULTIMO = "hashUltimo";
        public static final String DIFERENCIA_CAJA = "diferenciaCaja";
        public static final String EFECTIVO_CONTADO = "efectivoContado";
        public static final String EFECTIVO_ESPERADO = "efectivoEsperado";
        public static final String IMPORTE_INICIAL = "importeInicial";
        public static final String LINEAS = "lineas";
        public static final String METODO = "metodo";
        public static final String MESA_ID = "mesaId";
        public static final String MESA_NUMERO = "mesaNumero";
        public static final String NOMBRE = "nombre";
        public static final String NUMERO = "numero";
        public static final String PAGO_EFECTIVO = "pagoEfectivo";
        public static final String PAGO_TARJETA = "pagoTarjeta";
        public static final String IMPORTE_RECIBIDO = "importeRecibido";
        public static final String CAMBIO = "cambio";
        public static final String SPLASH_ACTIVE = "active";
        public static final String SPLASH_IMAGE_URL = "imageUrl";
        public static final String NIF_EMISOR = "nifEmisor";
        public static final String SERIE = "serie";
        public static final String CONTROLAR_STOCK = "controlarStock";
        public static final String STOCK = "stock";
        public static final String STOCK_MINIMO = "stockMinimo";
        public static final String TURNO_ID = "turnoId";
        public static final String ULTIMO = "ultimo";
        public static final String USUARIO_EMAIL = "usuarioEmail";
        public static final String USUARIO_UID = "usuarioUid";

        private Fields() { }
    }
}
