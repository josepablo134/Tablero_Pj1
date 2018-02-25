/*
 * Este codigo no tiene licencia
 */
package tablero_pj1;

/**
 *  Interface de control para las pantallas del javafx FXML
 * @author josepablocruzbaas
 */
public interface ControlInterface {
    /**
     * Asigna la pantalla padre, controlador de pila de pantallas
     * @param screenPage screenPage
     */
    public void setScreenParent(StackPaneControl screenPage);
}