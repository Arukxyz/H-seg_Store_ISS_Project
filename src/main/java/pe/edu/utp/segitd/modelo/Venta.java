package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Venta {

    private Integer id;
    private String codigoComprobante;
    private OffsetDateTime fecha;
    private Integer idCliente;
    /** Solo lectura: poblado por VentaDAO mediante JOIN con cliente, no se persiste. */
    private String clienteNombre;
    private Integer idUsuario;
    private OrigenVenta origen;
    private EstadoVenta estado;
    private BigDecimal total;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigoComprobante() { return codigoComprobante; }
    public void setCodigoComprobante(String codigoComprobante) { this.codigoComprobante = codigoComprobante; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public OrigenVenta getOrigen() { return origen; }
    public void setOrigen(OrigenVenta origen) { this.origen = origen; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
