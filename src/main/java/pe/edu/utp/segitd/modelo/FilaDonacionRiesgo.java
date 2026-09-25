package pe.edu.utp.segitd.modelo;

/**
 * Una donación (compromiso de triple impacto) que lleva más del umbral
 * de días sin avanzar de estado: PENDIENTE sin asignar a un lote, o
 * ASIGNADA a un lote que aún no se despacha. Es lo que conecta el
 * reporte con la razón de ser social de Höség: un compromiso atrasado
 * es una promesa incumplida con una comunidad o una ONG.
 */
public record FilaDonacionRiesgo(
        int idDonacion,
        String nombreProducto,
        int cantidad,
        TipoCompromiso tipo,
        EstadoDonacion estado,
        int diasEnRiesgo,
        String codigoLote,
        String comunidadNombre
) {
}