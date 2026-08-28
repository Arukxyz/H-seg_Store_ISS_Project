package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

public class PedidoProveedor {

    private Integer id;
    private int idProveedor;
    /** Solo lectura: poblado por ProveedorDAO mediante JOIN con proveedor, no se persiste. */
    private String nombreTaller;
    private String descripcion;
    private int cantidad;
    private OffsetDateTime fecha;
    private EstadoPedidoProveedor estado;
    private Integer idUsuario;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNombreTaller() { return nombreTaller; }
    public void setNombreTaller(String nombreTaller) { this.nombreTaller = nombreTaller; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public EstadoPedidoProveedor getEstado() { return estado; }
    public void setEstado(EstadoPedidoProveedor estado) { this.estado = estado; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}
