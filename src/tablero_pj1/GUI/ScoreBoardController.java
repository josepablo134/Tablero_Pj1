/*
 * Este codigo no tiene licencia
 */
package tablero_pj1.GUI;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import jssc.SerialPortException;
import tablero_pj1.ControlInterface;
import tablero_pj1.StackPaneControl;

/**
 * FXML Controller class
 * Controlador de la interfaz principal del tablero.
 * Permite compartir datos con el tablero de juego.
 */
public class ScoreBoardController implements Initializable, ControlInterface {
    
    private AddFault_FORM aff;    
    //
    //   Todos los envios son de 4 en 4 bits
    //
    private static int DEFAULT_MIN = 10;
    private static int DEFAULT_SEC = 00;
    //<editor-fold defaultstate="collapsed" desc="Comandos Serial">
    /**
     * Comandos en bytes del interprete del tablero
     */
    public static byte CMD_Default = (byte)128;
    public static byte CMD_Play    = (byte)136;
    public static byte CMD_Pause   = (byte)144;
    public static byte CMD_Reset   = (byte)152;
    public static byte CMD_Echo    = (byte)161;
    public static byte CMD_PTR_A   = (byte)28;
    public static byte CMD_PTR_B   = (byte)60;
    public static byte CMD_PTR_OFF = (byte)30;
    
    public static byte CMD_GetMin  = (byte)64;
    public static byte CMD_GetSec  = (byte)72;
    public static byte CMD_GetDec  = (byte)80;
    public static byte CMD_GetPer  = (byte)88;
    //Se recibe en formato hex ascii el valor solicitado,
    //seguido de un fin de linea como señal de fin de transmision
    public static byte CMD_GetALL  = (byte)96;
    //Se reciben en formato hex ascci los minutos, segundos, decimas y period
    //en ese orden, con salto de linea como señal de fin de transmisión entre
    //datos
    
    public static byte CMD_TIME    = (byte)99;
    //minutos 4 bits + endflag + segundos 4 bits + endflag
    public static byte CMD_Periodo = (byte)91;
    //8 bits Periodo + ENDFLAG
    public static byte CMD_CANCEL  = (byte)255;
    public static byte CMD_END_FLAG= (byte)254;
    
    public static byte CMD_PTS_A   = (byte)3;
    public static byte CMD_PTS_B   = (byte)35;
    
    public static byte CMD_FLTS_A   = (byte)11;
    public static byte CMD_FLTS_B   = (byte)43;
    
    public static byte CMD_TEMP_FLT_A= (byte)19;
    public static byte CMD_TEMP_FLT_B= (byte)51;
    //Jugador + ENDFLAG + Falta + ENDFLAG  <-> En ese orden
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="FXML declaracion">
    @FXML
    private ImageView BG_ImageView;
    @FXML
    private Button Read_Button;
    @FXML
    private Button New_Per_Button;
    @FXML
    private Button Settings_Button;
    @FXML
    private Button Play_Button;
    @FXML
    private Button Pause_Button;
    @FXML
    private Button Default_Button;
    @FXML
    private TextArea Log_TextArea;
    @FXML
    private Button Save_PTS_TMA;
    @FXML
    private Button Save_FLTS_TMA;
    @FXML
    private Button Save_PTS_TMB;
    @FXML
    private Button Save_FLTS_TMB;
    @FXML
    private Button Save_Periodo;
    @FXML
    private Button Save_Tiempo;
    @FXML
    private Button AddFault_Button;
    @FXML
    private Button Clear_Log_Button;
    @FXML
    private RadioButton PTR_A;
    @FXML
    private RadioButton PTR_B;
    @FXML
    private TextField PTS_A_DATA;
    @FXML
    private TextField FLTS_A_DATA;
    @FXML
    private TextField PLYR_A_DATA;
    @FXML
    private TextField PTS_B_DATA;
    @FXML
    private TextField FLTS_B_DATA;
    @FXML
    private TextField PLYR_B_DATA;
    @FXML
    private TextField PERIODO_DATA;
    @FXML
    private TextField MIN_DATA;
    @FXML
    private TextField SEC_DATA;
    @FXML
    private TextField FAULT_DATA;
    //</editor-fold>
    
    private StackPaneControl myController;
    /**
     * Initializes the controller class.
     * @param   url url
     * @param   rb  rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        FAULT_DATA.setEditable(false);
        PLYR_B_DATA.setEditable(false);
        PLYR_A_DATA.setEditable(false);
    }
    /**
    * Set the screen parent.    
    *  @param screenPage    screenPage
    */
    @Override
    public void setScreenParent(StackPaneControl screenPage){
        myController = screenPage;
    }
/**
* Evento de Configuracion. Inicia la pantalla de configuracion.
* @param evt    evt
*/
    @FXML
    public void EventSettings_Button(ActionEvent evt){
        gotoConfiguration();
    }
/**
 * Restaurar los datos en pantalla a su estado original.
 */
    private void restartScreen(){
        this.PTS_A_DATA.textProperty().setValue("000");
        this.FLTS_A_DATA.textProperty().setValue("00");
        this.PLYR_A_DATA.textProperty().setValue("--");
        this.PTS_B_DATA.textProperty().setValue("000");
        this.FLTS_B_DATA.textProperty().setValue("00");
        this.PLYR_B_DATA.textProperty().setValue("--");
        this.FAULT_DATA.textProperty().setValue("--");
        this.PERIODO_DATA.textProperty().setValue("1");
        this.MIN_DATA.textProperty().setValue(""+this.DEFAULT_MIN);
        this.SEC_DATA.textProperty().setValue("0"+this.DEFAULT_SEC);
    }
/**
*   Ir a configuracion.
*   Cambia la pantalla actual por la de configuracion
*/
    private void gotoConfiguration(){
        myController.setScreen(tablero_pj1.Tablero_Pj1.screen2ID);
    }
/**
*   Activa la pantalla actual.
*/  
    public void enable(){
        this.myController.setDisable(false);
    }
/**
*   Envia el dato en tramas de 4 bits cada una por el puerto serie.
*   @param      data    Dato a descomponer para su envio.
*/  
    private void send(int data){
        try{
            //tramas de 4 bits
            tablero_pj1.Tablero_Pj1.COM.write( (byte) (( data&0xF000)>>12 ));
            tablero_pj1.Tablero_Pj1.COM.write( (byte) (( data&0xF00)>>8 ));
            tablero_pj1.Tablero_Pj1.COM.write( (byte) (( data&0xF0)>>4 ));
            tablero_pj1.Tablero_Pj1.COM.write( (byte) ( data&0xF) );
            //Fin de trama
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(Exception e){
            alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }
    }
/**
*   Genera una ventana de alerta.
*    @param  Title   Titulo de la alerta.
*    @param  Header  Encabezado de la alerta.
*    @param  Text    Descripcion de la alerta.
*/
    public void alert(String Title,String Header,String Text){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Text);

        alert.showAndWait();
    }
/**
 *  Genera una ventana de confirmación.
 *  @param Title Titulo de la alerta.
 *  @param Header Encabezado de la alerta.
 *  @param Content Descripción de la alerta.
 * 
 */ public boolean confirmAlert(String Title,String Header,String Content){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }
    }
/**
*   Actualiza los datos en pantalla de cierto equipo
*    segun el numero de jugador y la falta cometida.
*    @param     fault   Falta comentida.
*    @param     player  Numero de jugador.
*    @param     TEAM    Equipo A(True) o B(False)
*/
    public void FaultAndPlayer(int fault,int player,boolean TEAM){
        if(TEAM){
            int a = Integer.parseInt(
                    this.FLTS_A_DATA.textProperty().getValue());
            //a += fault;
            this.PLYR_A_DATA.textProperty().setValue(""+player);
            this.FLTS_A_DATA.textProperty().setValue(""+a);
            this.FAULT_DATA.textProperty().setValue(""+fault);
            this.PLYR_B_DATA.textProperty().setValue("--");
            log(tablero_pj1.Messages.MSG_PLYR_AND_FLT_A_UPDATED);
        }else{
            int a = Integer.parseInt(
                    this.FLTS_B_DATA.textProperty().getValue());
            //a += fault;
            this.PLYR_B_DATA.textProperty().setValue(""+player);
            this.FLTS_B_DATA.textProperty().setValue(""+a);
            this.FAULT_DATA.textProperty().setValue(""+fault);
            this.PLYR_A_DATA.textProperty().setValue("--");
            log(tablero_pj1.Messages.MSG_PLYR_AND_FLT_B_UPDATED);
        }
    }
/**
*  Escribe en pantalla el estado del sistema.
*   @param      msg     Estado del sistema
*/
    private void log(String msg){
        Log_TextArea.textProperty()
                    .setValue(
                            Log_TextArea
                                    .textProperty().get()
                    +msg+"\n");
        Log_TextArea.setScrollTop(Double.MAX_VALUE);
    }
/**
*  Evento de boton play
*  Envia el comando PLAY por el puerto serie
*  @param   evt  evt    
*/  
    @FXML
    public void EventPlay_Button(ActionEvent evt){
        try{
            tablero_pj1.Tablero_Pj1.COM.write(CMD_Play);
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
            log("Error de transmisión.");
        }catch(NullPointerException npe){
            log("Por favor establezca "
                    + "comunicación desde el panel de configuración");
        }
    }    
/**
*  Evento de boton pause
*  Envia el comando PAUSA por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventPause_Button(ActionEvent evt){
        try{
            tablero_pj1.Tablero_Pj1.COM.write(CMD_Pause);
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
            log("Error de transmisión.");
        }catch(NullPointerException npe){
            log("Por favor establezca "
                    + "comunicación desde el panel de configuración.");
        }
    }
/**
*  Evento de boton default
*  Envia el comando DEFAULT por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventDefault_Button(ActionEvent evt){
        if(!confirmAlert("¡Advertencia!",
                "¿Seguro que desea reiniciar el marcador?",
                "Usted está a punto de borrar por completo el"
                + " contenido del marcador, ¿esta seguro de continuar?"))
        {
            return;
        }
        try{
            tablero_pj1.Tablero_Pj1.COM.write(CMD_Default);
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
            log("Error de transmisión.");
        }catch(NullPointerException npe){
            log("Por favor establezca "
                    + "comunicación desde el panel de configuración.");
        }finally{
            restartScreen();
        }
    }
/**
*  Evento de boton AddFault
*  Despliega en pantalla el formulario para agregar una nueva fala compuesta
*  @param   evt  evt    
*/      
    @FXML
    public void EventAddFault_Button(ActionEvent evt){
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddFault_FORM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddFault_FORM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddFault_FORM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddFault_FORM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        */
        //</editor-fold>

        //Enviar dato.
        //Crear dato modificable para recibir informacion.
        //Desactivar el tablero virtual y esperar al formulario.
        if(aff==null){
            aff = new AddFault_FORM();
            aff.setParent(this);
        }
        aff.setVisible(true);
        this.myController.setDisable(true);
    }
/**
*  Evento de boton Save_PTS_TMA
*  Envia los puntos del equipo A por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventSave_PTS_TMA(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.PTS_A_DATA.textProperty()
                            .getValue());
            if(a>999){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_PTS_A);
            //Enviamos los datos
            send(a);
            
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(Exception e){
            alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            log(tablero_pj1.Messages.MSG_PTS_A_UPDATED);
        }
    }
/**
*  Evento de boton Save_FLTS_TMA
*  Envia las faltas del equipo A por el puerto serie
*  @param   evt  evt    
*/            
    @FXML
    public void EventSave_FLTS_TMA(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.FLTS_A_DATA.textProperty()
                            .getValue());
            if(a>99){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_FLTS_A);
            //Enviamos los datos
            send(a);
            
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(NumberFormatException nfe){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            log(tablero_pj1.Messages.MSG_FLTS_A_UPDATED);
        }
    }
/**
*  Evento de boton Save_PTS_TMB
*  Envia los puntos del equipo B por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventSave_PTS_TMB(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.PTS_B_DATA.textProperty()
                            .getValue());
            if(a>999){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_PTS_B);
            //Enviamos los datos
            send(a);
            
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(NumberFormatException nfe){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            log(tablero_pj1.Messages.MSG_PTS_B_UPDATED);
        }
    }
/**
*  Evento de boton Save_FLTS_TMA
*  Envia las faltas del equipo B por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventSave_FLTS_TMB(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.FLTS_B_DATA.textProperty()
                            .getValue());
            if(a>99){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_FLTS_B);
            //Enviamos los datos
            send(a);
            
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(NumberFormatException nfe){
            alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            log(tablero_pj1.Messages.MSG_FLTS_B_UPDATED);
        }
    }
/**
*  Evento de boton Save_Periodo
*  Envia el periodo del cronometro por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventSave_Periodo(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.PERIODO_DATA.textProperty()
                            .getValue());
            if(a>9){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_Periodo);
            //Enviamos los datos
            send(a);
            
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(NumberFormatException nfe){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            this.FLTS_A_DATA.setText("00");
            this.FLTS_B_DATA.setText("00");
            log(tablero_pj1.Messages.MSG_PERIOD_UPDATED);
        }
    }
/**
*  Evento de boton Save_Tiempo
*  Envia los minutos y segundos del cronometro por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventSave_Tiempo(ActionEvent evt){
        try{
            int a = Integer.parseInt(this.MIN_DATA.textProperty()
                            .getValue());
            int b = Integer.parseInt(this.SEC_DATA.textProperty()
                            .getValue());
            if(a>59 || b>59){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
                return;
            }
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_TIME);
            //Enviamos los datos
            send(a);
            send(b);
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }catch(NumberFormatException nfe){
                alert("Entrada invalida","Ingrese un valor valido!"
                    ,"Varifique la entrada y vuelva a intentarlo!");
        }finally{
            log(tablero_pj1.Messages.MSG_TIME_UPDATED);
        }
    }
/**
*  Evento de boton Clear_log_Button
*  Limpia el log en pantalla.
*  @param   evt  evt    
*/      
    @FXML
    public void EventClear_Log_Button(ActionEvent evt){
        Log_TextArea.textProperty()
                    .setValue("");
    }
/**
*  Evento de boton PTR_A
*  Envia el estao del puntero de saque del equipo A por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventPTR_A(ActionEvent evt){
            //Activa el RadioButton, si el otro esta apagado,
            //Si el otro esta encendido,
                //envia el comando apagado y desactiva el otro RadioButton
        try{
            if(PTR_A.selectedProperty().getValue()){
                if(PTR_B.selectedProperty().getValue()){
                    //Esta seleccionado B
                    //Se reemplaza por A
                    PTR_B.selectedProperty().setValue(false);
                        //Apagamos PTR 
                    // tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_OFF);
                    //Encendemos PTR A
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_A);
                }else{
                    //Encendemos PTR A
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_A);
                }
            }else{
                if(!PTR_B.selectedProperty().getValue()){
                    //Apagamos PTR 
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_OFF);
                }
            }
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
            
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }
    }
/**
*  Evento de boton PTR_B
*  Envia el estao del puntero de saque del equipo B por el puerto serie
*  @param   evt  evt    
*/      
    @FXML
    public void EventPTR_B(ActionEvent evt){
        //00 1 11 10 0 Encender ptr equipo B
        //00 1 11 11 0 Apagar PTR s
            //Activa el RadioButton, si el otro esta apagado,
            //Si el otro esta encendido,
                //envia el comando apagado y desactiva el otro RadioButton
        try{
            if(PTR_B.selectedProperty().getValue()){
                if(PTR_A.selectedProperty().getValue()){
                    //Esta seleccionado B
                    //Se reemplaza por A
                    PTR_A.selectedProperty().setValue(false);
                        //Apagamos PTR 
                    //  tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_OFF);
                    //Encendemos PTR A
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_B);
                }else{
                    //Encendemos PTR A
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_B);
                }
            }else{
                if(!PTR_A.selectedProperty().getValue()){
                    //Apagamos PTR 
                    tablero_pj1.Tablero_Pj1.COM.write(CMD_PTR_OFF);
                }
            }
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }
    }
/**
*  Evento de boton Read
*  Lee un string del puerto serie y lo escribe en el log
*  @param   evt  evt    
*/      
    @FXML
    public void EventRead_Button(ActionEvent evt){
        try{
            log(tablero_pj1.Tablero_Pj1.COM.readString());
        }catch(SerialPortException spe){
            alert("Error de lectura.","Datos no recibidos correctamente."
                    ,"Al parecer no es posible obtener datos del puerto, "
                            + "verificar alcance y volver a intentar.");
        }
    }
/*
*    MOD 1-1
*   Restringir la longitud de una cadena dada
**/
    @FXML
    public void EventNew_Per_Button(ActionEvent evt){
        if(!confirmAlert("¡Advertencia!",
                "¿Seguro que desea iniciar un nuevo periodo?",
                "Usted está a punto de borrar los puntos y reiniciar el"
                        + " reloj en un nuevo periodo de juego, ¿esta seguro"
                        + " de continuar?"))
        {
            return;
        }
        try{
//Aumentar en uno el periodo
            int a;
            try{
                a = Integer.parseInt(this.PERIODO_DATA.textProperty()
                            .getValue())+1;
            }catch(NumberFormatException nfe){
                a=1;
            }
            if(a>9){
                a=1;
            }
                //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_Periodo);
                //Enviamos los datos
            send(a);
                //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
            try{
                java.util.concurrent.TimeUnit.MILLISECONDS.sleep(100);
            }catch(java.lang.InterruptedException ie){
                alert("Error interno."
                        ,"Al parecer algo anda mal con java."
                        ,"Si el sistema no realizo el comando"+
                    "esperado, sera mejor que reinicies el programa.");
            }
//Resetear tiempo
            //Preparamos el interprete
            tablero_pj1.Tablero_Pj1.COM.write(CMD_TIME);
            //Enviamos los datos
            send(this.DEFAULT_MIN);
            send(this.DEFAULT_SEC);
            //Finalizamos el comando
            tablero_pj1.Tablero_Pj1.COM.write(CMD_END_FLAG);
//Resetear faltas
                this.FLTS_A_DATA.setText("00");
                this.FLTS_B_DATA.setText("00");
                this.MIN_DATA.setText(""+this.DEFAULT_MIN);
                this.SEC_DATA.setText("0"+this.DEFAULT_SEC);
                this.PERIODO_DATA.setText(""+a);
        }catch(SerialPortException spe){
                log("Error de transmisión.");
        }catch(NullPointerException npe){
                log("Por favor establezca "
            + "comunicación desde el panel de configuración.");
        }
    }
}
