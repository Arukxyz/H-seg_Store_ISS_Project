package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

public class LoteDonacion {

    private Integer id;
    private String codigoLote;
    private int idComunidad;
    /** Solo lectura: poblado por LoteDAO mediante JOIN con comunidad, no se persiste. */
    private String comunidadNombre;
    private int idOng;
    /** Solo lectura: poblado por LoteDAO mediante JOIN con ong, no se persiste. */
    private String ongNombre;
    private Integer idUsuarioResponsable;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaDespacho;
    private EstadoLote estado;
    private String observaciones;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }

    public int getIdComunidad() { return idComunidad; }
    public void setIdComunidad(int idComunidad) { this.idComunidad = idComunidad; }

    public String getComunidadNombre() { return comunidadNombre; }
    public void setComunidadNombre(String comunidadNombre) { this.comunidadNombre = comunidadNombre; }

    public int getIdOng() { return idOng; }
    public void setIdOng(int idOng) { this.idOng = idOng; }

    public String getOngNombre() { return ongNombre; }
    public void setOngNombre(String ongNombre) { this.ongNombre = ongNombre; }

    public Integer getIdUsuarioResponsable() { return idUsuarioResponsable; }
    public void setIdUsuarioResponsable(Integer idUsuarioResponsable) { this.idUsuarioResponsable = idUsuarioResponsable; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public OffsetDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(OffsetDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }

    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
