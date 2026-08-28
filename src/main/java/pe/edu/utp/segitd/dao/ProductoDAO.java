package pe.edu.utp.segitd.dao;

import pe.edu.utp.segitd.modelo.Producto;
import pe.edu.utp.segitd.modelo.TipoCompromiso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de producto, incluido el ajuste atómico de los dos
 * saldos de stock. La aritmética de stock siempre se delega a Postgres
 * (sección 9.1): nunca se lee, resta en memoria y escribe.
 */
public final class ProductoDAO {

    public Producto crear(Producto producto, Connection conexion) throws SQLException {
        String sql = """
                INSERT INTO producto
                    (codigo, nombre, marca, categoria, coleccion, talla, descripcion, url_imagen,
                     precio, stock_comercial, stock_minimo, aplica_triple_impacto, tipo_compromiso,
                     visible_web, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getMarca());
            ps.setString(4, producto.getCategoria());
            ps.setString(5, producto.getColeccion());
            ps.setString(6, producto.getTalla());
            ps.setString(7, producto.getDescripcion());
            ps.setString(8, producto.getUrlImagen());
            ps.setBigDecimal(9, producto.getPrecio());
            ps.setInt(10, producto.getStockComercial());
            ps.setInt(11, producto.getStockMinimo());
            ps.setBoolean(12, producto.isAplicaTripleImpacto());
            ps.setString(13, producto.getTipoCompromiso() == null ? null : producto.getTipoCompromiso().name());
            ps.setBoolean(14, producto.isVisibleWeb());
            ps.setBoolean(15, producto.isActivo());
            ps.executeUpdate();
        }
        return producto;
    }

    public Optional<Producto> buscarPorCodigo(String codigo, Connection conexion) throws SQLException {
        String sql = "SELECT * FROM producto WHERE codigo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public boolean existeCodigo(String codigo, Connection conexion) throws SQLException {
        String sql = "SELECT 1 FROM producto WHERE codigo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Producto> listarActivos(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM producto WHERE activo = TRUE ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public int contarActivos(Connection conexion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE activo = TRUE";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int contarBajoStockMinimo(Connection conexion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE activo = TRUE AND stock_comercial <= stock_minimo";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Producto> listarBajoStockMinimo(Connection conexion) throws SQLException {
        String sql = "SELECT * FROM producto WHERE activo = TRUE AND stock_comercial <= stock_minimo ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        }
    }

    public void actualizarDatos(Producto producto, Connection conexion) throws SQLException {
        String sql = """
                UPDATE producto
                   SET nombre = ?, marca = ?, categoria = ?, coleccion = ?, talla = ?,
                       descripcion = ?, url_imagen = ?, precio = ?, stock_minimo = ?,
                       aplica_triple_impacto = ?, tipo_compromiso = ?, visible_web = ?
                 WHERE codigo = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getMarca());
            ps.setString(3, producto.getCategoria());
            ps.setString(4, producto.getColeccion());
            ps.setString(5, producto.getTalla());
            ps.setString(6, producto.getDescripcion());
            ps.setString(7, producto.getUrlImagen());
            ps.setBigDecimal(8, producto.getPrecio());
            ps.setInt(9, producto.getStockMinimo());
            ps.setBoolean(10, producto.isAplicaTripleImpacto());
            ps.setString(11, producto.getTipoCompromiso() == null ? null : producto.getTipoCompromiso().name());
            ps.setBoolean(12, producto.isVisibleWeb());
            ps.setString(13, producto.getCodigo());
            ps.executeUpdate();
        }
    }

    public void desactivar(String codigo, Connection conexion) throws SQLException {
        String sql = "UPDATE producto SET activo = FALSE WHERE codigo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    /**
     * Descuenta stock comercial de forma atómica. Devuelve false si no había
     * stock suficiente (la fila no se actualiza): el llamador debe lanzar la
     * excepción de negocio y hacer rollback.
     */
    public boolean descontarStockComercial(String codigo, int cantidad, Connection conexion) throws SQLException {
        String sql = """
                UPDATE producto
                   SET stock_comercial = stock_comercial - ?
                 WHERE codigo = ? AND stock_comercial >= ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, codigo);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    public void incrementarStockComercial(String codigo, int cantidad, Connection conexion) throws SQLException {
        String sql = "UPDATE producto SET stock_comercial = stock_comercial + ? WHERE codigo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, codigo);
            ps.executeUpdate();
        }
    }

    public void incrementarStockComprometido(String codigo, int cantidad, Connection conexion) throws SQLException {
        String sql = "UPDATE producto SET stock_comprometido = stock_comprometido + ? WHERE codigo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, codigo);
            ps.executeUpdate();
        }
    }

    /** Descuenta stock comprometido de forma atómica. Devuelve false si no había saldo suficiente. */
    public boolean descontarStockComprometido(String codigo, int cantidad, Connection conexion) throws SQLException {
        String sql = """
                UPDATE producto
                   SET stock_comprometido = stock_comprometido - ?
                 WHERE codigo = ? AND stock_comprometido >= ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, codigo);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setMarca(rs.getString("marca"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setColeccion(rs.getString("coleccion"));
        producto.setTalla(rs.getString("talla"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setUrlImagen(rs.getString("url_imagen"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setStockComercial(rs.getInt("stock_comercial"));
        producto.setStockComprometido(rs.getInt("stock_comprometido"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setAplicaTripleImpacto(rs.getBoolean("aplica_triple_impacto"));
        String tipoCompromiso = rs.getString("tipo_compromiso");
        producto.setTipoCompromiso(tipoCompromiso == null ? null : TipoCompromiso.valueOf(tipoCompromiso));
        producto.setVisibleWeb(rs.getBoolean("visible_web"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
        return producto;
    }
}
