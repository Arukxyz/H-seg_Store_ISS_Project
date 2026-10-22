package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

/** Bitácora de inventario: ningún stock se modifica sin una fila aquí (RNF-06 / sección 9.3). */
public class MovimientoInventario {

    private Integer id;
    private String codigoProducto;
    private TipoStock tipoStock;
    private TipoMovimiento tipoMovimiento;
    private int cantidad;
    private String motivo;
    /** Origen del movimiento: como máximo uno de los tres (CHECK en la base). */
    private Integer idVenta;
    private Integer idLote;
    private Integer idPedidoProveedor;
    private OrigenSistema origenSistema;
    private Integer idUsuario;
    private OffsetDateTime fecha;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public TipoStock getTipoStock() { return tipoStock; }
    public void setTipoStock(TipoStock tipoStock) { this.tipoStock = tipoStock; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public Integer getIdLote() { return idLote; }
    public void setIdLote(Integer idLote) { this.idLote = idLote; }

    public Integer getIdPedidoProveedor() { return idPedidoProveedor; }
    public void setIdPedidoProveedor(Integer idPedidoProveedor) { this.idPedidoProveedor = idPedidoProveedor; }

    public OrigenSistema getOrigenSistema() { return origenSistema; }
    public void setOrigenSistema(OrigenSistema origenSistema) { this.origenSistema = origenSistema; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }
}
