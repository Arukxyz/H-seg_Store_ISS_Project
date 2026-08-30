package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

/**
 * Tabla bisagra del sistema: une la línea de venta que originó el
 * compromiso social con el lote que finalmente lo entrega.
 */
public class Donacion {

    private Integer id;
    private int idDetalleVenta;
    private String codigoProducto;
    /** Solo lectura: poblado por DonacionDAO mediante JOIN con producto, no se persiste. */
    private String nombreProducto;
    private int cantidad;
    private TipoCompromiso tipo;
    private EstadoDonacion estado;
    private Integer idLote;
    private OffsetDateTime creadoEn;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public TipoCompromiso getTipo() { return tipo; }
    public void setTipo(TipoCompromiso tipo) { this.tipo = tipo; }

    public EstadoDonacion getEstado() { return estado; }
    public void setEstado(EstadoDonacion estado) { this.estado = estado; }

    public Integer getIdLote() { return idLote; }
    public void setIdLote(Integer idLote) { this.idLote = idLote; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
