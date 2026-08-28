package pe.edu.utp.segitd.modelo;

public class Proveedor {

    private Integer id;
    private String nombreTaller;
    private String ruc;
    private String contacto;
    private String telefono;
    private boolean activo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreTaller() { return nombreTaller; }
    public void setNombreTaller(String nombreTaller) { this.nombreTaller = nombreTaller; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
