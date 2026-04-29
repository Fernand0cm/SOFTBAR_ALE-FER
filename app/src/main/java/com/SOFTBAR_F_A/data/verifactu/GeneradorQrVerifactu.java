package com.SOFTBAR_F_A.data.verifactu;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Genera la URL y el bitmap del QR Verifactu, segun el formato publicado por la
 * AEAT (entorno de pruebas):
 *
 *   https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR
 *      ?nif=...&numserie=...&fecha=...&importe=...
 */
public class GeneradorQrVerifactu {

    private static final String BASE_URL =
            "https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR";

    private GeneradorQrVerifactu() { }

    public static String construirUrl(String nifEmisor,
                                       String numeroSerie,
                                       String fechaDdMmYyyy,
                                       double importe) {
        try {
            return BASE_URL
                    + "?nif=" + URLEncoder.encode(nifEmisor, "UTF-8")
                    + "&numserie=" + URLEncoder.encode(numeroSerie, "UTF-8")
                    + "&fecha=" + URLEncoder.encode(fechaDdMmYyyy, "UTF-8")
                    + "&importe=" + URLEncoder.encode(
                            HashVerifactu.formatoImporte(importe), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 no disponible", e);
        }
    }

    public static Bitmap generarBitmap(String contenido, int tamano) {
        try {
            BitMatrix matrix = new QRCodeWriter()
                    .encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano);
            Bitmap bmp = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < tamano; x++) {
                for (int y = 0; y < tamano; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            throw new IllegalStateException("No se pudo generar el QR", e);
        }
    }
}
