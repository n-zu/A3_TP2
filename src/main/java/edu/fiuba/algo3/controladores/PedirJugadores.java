package edu.fiuba.algo3.controladores;

import edu.fiuba.algo3.modelo.Juego;
import edu.fiuba.algo3.vista.VistaPais;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observer;

public class PedirJugadores {

    Scene scene;

    public PedirJugadores(Scene scene) throws IOException {
        this.scene = scene;
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("pedirJugadores.fxml"));
        fxmlLoader.setController(this);
        scene.setRoot(fxmlLoader.load());
    }

    @FXML
    public void iniciarJuego(ActionEvent actionEvent) throws IOException {
        System.out.println("Comenzar Juego");

        int cantJ = 3; // TODO -
        Juego juego = new Juego(cantJ, "archivos/paises.json");

        new Mapa(scene, juego);
        addPaisObservers(juego);
    }

    private void addPaisObservers(Juego juego) {
        HashMap<String, Observer> observers = new HashMap<>();
        for (Node nodo : ((Group) scene.lookup("#_root")).getChildren()) {
            /*
            if (nodo.getId().equals("fondo") || nodo.getId().startsWith("puente")) continue;
            observers.put(nodo.getId(), new VistaPais(nodo));*/
            if (nodo.getStyleClass().contains("pais")) {
                observers.put(nodo.getId(), new VistaPais(nodo));
            }
        }

        juego.addPaisObservers(observers);
    }
}