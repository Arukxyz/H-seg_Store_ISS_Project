package pe.edu.utp.segitd.modelo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Producto {

    private String codigo;
    private String nombre;
    private String marca;
    private String categoria;
    private String coleccion;
    private String talla;
    private String descripcion;
    private String urlImagen;
    private BigDecimal precio;
    private int stockComercial;
    private int stockComprometido;
    private int stockMinimo;
    private boolean aplicaTripleImpacto;
    private TipoCompromiso tipoCompromiso;
    private boolean visibleWeb;
    private boolean activo;
    private OffsetDateTime creadoEn;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getColeccion() { return coleccion; }
    public void setColeccion(String coleccion) { this.coleccion = coleccion; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStockComercial() { return stockComercial; }
    public void setStockComercial(int stockComercial) { this.stockComercial = stockComercial; }

    public int getStockComprometido() { return stockComprometido; }
    public void setStockComprometido(int stockComprometido) { this.stockComprometido = stockComprometido; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isAplicaTripleImpacto() { return aplicaTripleImpacto; }
    public void setAplicaTripleImpacto(boolean aplicaTripleImpacto) { this.aplicaTripleImpacto = aplicaTripleImpacto; }

    public TipoCompromiso getTipoCompromiso() { return tipoCompromiso; }
    public void setTipoCompromiso(TipoCompromiso tipoCompromiso) { this.tipoCompromiso = tipoCompromiso; }

    public boolean isVisibleWeb() { return visibleWeb; }
    public void setVisibleWeb(boolean visibleWeb) { this.visibleWeb = visibleWeb; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }

    @Override
    public String toString() {
        return codigo + " — " + nombre;
    }
}
