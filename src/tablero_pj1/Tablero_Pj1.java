/*
 * Este codigo no tiene licencia
 */
//
//  MODIFICACION 1
//  FECHA 23 - MARZO - 2017
//
//  1 - RESTRINGIR LA LONGITUD DE LAS ENTRADAS DEL SCOREBOARD
//
package tablero_pj1;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import scoreboard_communication.Communication;
/**
 * Aplicacion controladora del sistema tablero.
 * @author josepablocruzbaas
 */
public class Tablero_Pj1 extends Application{
    public static String screen1ID      = "ScoreBoard";
    public static String screen1File    = "/tablero_pj1/GUI/ScoreBoard.fxml";
    public static String screen2ID      = "Configuration";
    public static String screen2File    = "/tablero_pj1/GUI/Configuration.fxml";
    public static Communication COM;
    public static String SERIALREAD     = "";
    /**
     * Inicializa la aplicacion
     * @param   stage   stage
     * @throws  Exception si existe algun error al cargar la aplicacion 
     */
    @Override    
    public void start(Stage stage)throws Exception{
        StackPaneControl mainContainer = new StackPaneControl();
        COM = new Communication();
        mainContainer.loadScreen(screen1ID, screen1File);
        mainContainer.loadScreen(screen2ID, screen2File);
        mainContainer.setScreen(screen1ID);
        
        Group root = new Group();
        
        root.getChildren().addAll(mainContainer);
        Scene scene = new Scene(root);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
          @Override
          public void handle(WindowEvent we) {
              System.out.println("Stage is closing");
              try{
                COM.disconnect();
              }catch(NullPointerException e){
                  System.out.println("Ningun puerto que cerrar");
              }catch(Exception e){
                  e.printStackTrace();
              }finally{
                System.exit(0);
              }
          }
        });  
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setResizable(false);
        stage.show();
    }
    /**
     * Codigo principal del sistema
     * @param args Argumentos de la aplicación
     */
    public static void main(String[] args){
        launch(args);
    }
}