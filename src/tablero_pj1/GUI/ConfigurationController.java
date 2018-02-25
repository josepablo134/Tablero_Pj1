/*
 * Este codigo no tiene licencia
 */
package tablero_pj1.GUI;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import jssc.SerialPortException;
import scoreboard_communication.Communication;
import tablero_pj1.ControlInterface;
import tablero_pj1.StackPaneControl;

/**
 * FXML Configuracion del sistema
 *
 * @author josepablocruzbaas
 */
public class ConfigurationController implements Initializable, ControlInterface {
    private StackPaneControl myController;
    // <editor-fold defaultstate="collapsed" desc="FXML declaracion">
    @FXML
    private Button Attach_Button;
    @FXML
    private Button ScanPorts_Button;
    @FXML
    private Button ComeBack_Button;
    @FXML
    private Button SendCommand_Button;
    @FXML
    private Button Refresh_Button;
    @FXML
    private RadioButton At_RadioButton;
    @FXML
    private Pane    ATPane;
    @FXML
    private TextArea Respon_TextArea;
    @FXML
    private TextField Command_TextField;
    @FXML
    private ComboBox AvailablePorts_Box;
    @FXML
    private ImageView Status_ImageView;
    @FXML
    private ImageView Loading_ImageView;
    // </editor-fold>
    
    private boolean COM_Connected;
    /**
     * Initializes the controller class.
     * @param url   url
     * @param rb    rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AvailablePorts_Box.getItems()
                .setAll(Arrays.asList(tablero_pj1.Tablero_Pj1.COM.getPorts()));
        ATPane.disableProperty().setValue(Boolean.TRUE);
        COM_Connected = false;
        Status_ImageView.visibleProperty().setValue(Boolean.FALSE);
    }
    /**
     * Carga la pantalla padre para regresar al sistema principal.
     * @param   screenPage  screenPage
     */
    @Override
    public void setScreenParent(StackPaneControl screenPage){
        myController = screenPage;
    }
    /**
     * Enviar comando cargado en la entrada de texto.
     * @param   event   event
     */
    @FXML
    public void EventSendCommand_Button(ActionEvent event){
        //Enviar el string por el puerto serial
        try{
            tablero_pj1.Tablero_Pj1.COM.print(
                this.Command_TextField.textProperty().get()
            );
        }catch(SerialPortException spe){
            alert("Error de transmisión.","Datos no trasnmitidos."
                    ,"Al perecer el puerto no es alcanzable"
                            + ", intentelo mas tarde.");
        }
    }
    /**
     * Escanea los puerto serie disponibles y los enlista en el sistema.
     * @param   event   event
     */
    @FXML
    public void EventScanPorts_Button(ActionEvent event){
        Loading_ImageView.visibleProperty().setValue(Boolean.TRUE);
        AvailablePorts_Box.getItems()
                .setAll(Arrays.asList(tablero_pj1.Tablero_Pj1.COM.getPorts()));
        Loading_ImageView.visibleProperty().setValue(Boolean.FALSE);
    }
    /**
     * Establece conexion con el puerto serie seleccionado.
     * @param   event   event
     */
    @FXML
    public void EventAttach_Button(ActionEvent event){
        try{
            tablero_pj1.Tablero_Pj1.COM.connect(
            AvailablePorts_Box
                .getSelectionModel().getSelectedItem().toString()
            );
            COM_Connected = true;
        }catch(SerialPortException spe){
            alert("Error de conexión.","No se conecto el puerto "+AvailablePorts_Box
                .getSelectionModel().getSelectedItem().toString(),
                    "Al parecer hubo un error estableciendo comunicación"
                  + " con el puerto, verifique que el puerto es alcanzable e"
                            + " intente mas tarde.");
            COM_Connected = false;
        }catch(NullPointerException npe){
            alert("Error de conexión.","No se conectó el puerto.",
                    "Es necesario seleccionar un puerto para "
                            + "establecer una conexión.");
            COM_Connected = false;
        }finally{
            Loading_ImageView.visibleProperty().setValue(Boolean.FALSE);
            if(COM_Connected){
                Status_ImageView.visibleProperty().setValue(Boolean.TRUE);
            }else{
                try{
                    tablero_pj1.Tablero_Pj1.COM.disconnect();
                }catch(SerialPortException | NullPointerException e){
                    System.out.println(e.toString());
                }finally{
                    Status_ImageView.visibleProperty().setValue(Boolean.FALSE);
                    Status_ImageView.visibleProperty().setValue(Boolean.FALSE);
                    tablero_pj1.Tablero_Pj1.COM = new Communication();
                }
            }
        }
    }
    /**
     * Activa el estado de echo en el sistema del tablero.
     * @param   event   event
     */
    @FXML
    public void EventAt_RadioButton(ActionEvent event){
        if(!this.COM_Connected){
            alert("Información.","Ningún puerto conectado."
                    ,"Es necesario establecer una comunicación "
                            + "con el puerto serial antes de continuar.");
            At_RadioButton.selectedProperty().setValue(Boolean.FALSE);
            return;
        }
        if(At_RadioButton.isSelected()){
            ATPane.disableProperty().setValue(Boolean.FALSE);
            try{
                activateEcho();
            }catch(SerialPortException spe){
            alert("Error de transmisión.","Datos no trasnmitidos."
                    ,"Al perecer el puerto no es alcanzable"
                            + ", intentelo mas tarde.");
            }
        }else{
            ATPane.disableProperty().setValue(Boolean.TRUE);
            try{
                deactivateEcho();
            }catch(SerialPortException spe){
            alert("Error de transmisión.","Datos no trasnmitidos."
                    ,"Al perecer el puerto no es alcanzable"
                            + ", intentelo mas tarde.");
            }
        }
    }
    /**
     * Actualiza los datos recibidos por el puerto serie en el area de texto.
     * @param   event   event
     */
    @FXML
    public void EventRefresh_Button(ActionEvent event){
        try{
            this.Respon_TextArea.textProperty().setValue(
                tablero_pj1.Tablero_Pj1.COM.readString()
            );
        }catch(SerialPortException spe){
            alert("Error de lectura.","Datos no recibidos correctamente."
                    ,"Al parecer no es posible obtener datos del puerto, "
                            + "verificar alcance y volver a intentar.");
        }
    }
    /**
     * Regresa a la pantalla principal del sistema.
     * @param   event   event
     */
    @FXML
    public void EventComeBack_Button(ActionEvent event){
        this.gotoScoreboard();
    }
    /**
     * Regresa a la pantalla principal del sistema.
     */
    private void gotoScoreboard(){
        myController.setScreen(tablero_pj1.Tablero_Pj1.screen1ID);
    }
    /**
     * Activar el sistema de echo, verificar conexión.
     * @throws SerialPortException.
     * Excepcion si el sistema del puerto serie encuentra error.
     */
    private void activateEcho()throws SerialPortException{
            tablero_pj1.Tablero_Pj1.COM.write((byte)164);
    }
    /**
     * Desactivar el sistema de echo, verificar conexión.
     * @throws SerialPortException.
     * Excepcion si el sistema del puerto serie encuentra error.
     */
    private void deactivateEcho()throws SerialPortException{
            tablero_pj1.Tablero_Pj1.COM.write((byte)254);
    }
    /**
     * Genera una alerta en pantalla.
     * @param   Title   Titulo de la alerta.
     * @param   Header  Encabezado de la alerta.
     * @param   Text    Descripcion de la alerta.
     */
    private void alert(String Title,String Header,String Text){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Text);

        alert.showAndWait();
    }
}
