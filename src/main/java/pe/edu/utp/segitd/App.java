package pe.edu.utp.segitd;

import com.formdev.flatlaf.FlatLightLaf;
import pe.edu.utp.segitd.config.AppConfig;
import pe.edu.utp.segitd.vista.LoginJFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Punto de entrada de la aplicación. */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        try {
            // Fuerza la validación de configuración aquí, con un diálogo
            // visible: un .jar de escritorio abierto con doble clic no
            // tiene consola donde el usuario pueda ver un mensaje de error.
            AppConfig.obtenerInstancia();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de configuración", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        new LoginJFrame().setVisible(true);
    }
}
