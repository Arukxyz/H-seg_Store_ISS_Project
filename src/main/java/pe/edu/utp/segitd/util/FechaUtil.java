package pe.edu.utp.segitd.util;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** Utilidades para los selectores de fecha de las pantallas con filtros por rango. */
public final class FechaUtil {

    private FechaUtil() {
    }

    public static JSpinner crearSpinnerFecha(int diasDesdeHoy) {
        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_MONTH, diasDesdeHoy);
        SpinnerDateModel modelo = new SpinnerDateModel(calendario.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(modelo);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    public static OffsetDateTime inicioDelDia(Date fecha) {
        LocalDate local = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    public static OffsetDateTime finDelDia(Date fecha) {
        LocalDate local = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.atTime(LocalTime.of(23, 59, 59)).atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
