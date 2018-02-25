/*
 * Este codigo no tiene licencia
 */
package tablero_pj1;

import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
/**
 * Pila de control de paneles.
 * @author josepablocruzbaas
 */
public class StackPaneControl extends StackPane{
    private HashMap<String,Node> screens = new HashMap<>();
    /**
     * Llama a la funcion de la clase original
     */
    public StackPaneControl(){
        super();
    }
    /**
     * Agrega una pantalla a la pila
     * @param   name        Nombre de la pantalla
     * @param   screen      Nodo de la pantalla
     */
    public void addScreen(String name, Node screen){
        screens.put(name, screen);
    }
    /**
     * Recupeara el nodo correspondiente a la pantalla
     *  @param name Nombre de la pantalla 
     *  @return Node de la pantalla.
     */
    public Node getScreen(String name){
        return screens.get(name);
    }
    /**
     *  Carga la pantalla.
     *  @param  name        Nombre de la pantalla
     *  @param  resource    Direccion al dofucmento FXML
     *  @return true si se pudo cargar, false sino.
     */
    public boolean loadScreen(String name, String resource){
        try{
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent loadScreen = (Parent) myLoader.load();
            ControlInterface myScreenControler = ((ControlInterface) myLoader.getController());
            myScreenControler.setScreenParent(this);
            addScreen(name,loadScreen);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     *  Configurar pantalla.
     *  @param  name    Nombre de la pantalla dentro de la pila.
     *  @return true, si se pudo configurar, false, sino.
     */
    public boolean setScreen(final String name){
        if(screens.get(name) != null){
            final DoubleProperty opacity = opacityProperty();
            if(!getChildren().isEmpty()){
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity,1.0)),
                        new KeyFrame(new Duration(500),new EventHandler<ActionEvent>(){
                            @Override
                            public void handle(ActionEvent t){
                                getChildren().remove(0);
                                getChildren().add(0,screens.get(name));
                                Timeline fadeIn = new Timeline(
                                        new KeyFrame(Duration.ZERO,new KeyValue(opacity,0.0)),
                                        new KeyFrame(new Duration(800),new KeyValue(opacity,1.0)));
                                fadeIn.play();
                            }
                        },new KeyValue(opacity, 0.0)));
                fade.play();
            }else{
                setOpacity(0.0);
                getChildren().add(screens.get(name));
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity,0.0)),
                        new KeyFrame(new Duration(500), new KeyValue(opacity,1.0))
                );
                fadeIn.play();
            }
            return true;
        }else{
            System.err.println("Screen hasn't been loaded!!");
            return false;
        }
    }
    /**
     * Cerrar pantalla.
     * @param   name    Nombre de la pantalla dentro de la pila.
     * @return  true, si se pudo cerrar, false, sino.
     */
    public boolean unloadScreen(String name){
        if(screens.remove(name) == null){
            System.err.println("Screen didn't exist");
            return false;
        }else{
            return true;
        }
    }
}
