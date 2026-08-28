package pe.edu.utp.segitd.modelo;

import java.time.OffsetDateTime;

public class Usuario {

    private Integer id;
    private String nombre;
    private String username;
    private String passwordHash;
    private String salt;
    private RolUsuario rol;
    private boolean activo;
    private int intentosFallidos;
    private OffsetDateTime bloqueadoHasta;
    private OffsetDateTime creadoEn;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(OffsetDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
