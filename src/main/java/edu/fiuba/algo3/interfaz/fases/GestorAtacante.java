package edu.fiuba.algo3.interfaz.fases;

import edu.fiuba.algo3.modelo.Juego;
import edu.fiuba.algo3.modelo.Jugador;
import edu.fiuba.algo3.modelo.Pais;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class GestorAtacante implements Fase {

    FaseAtaque fase;
    Juego juego;
    Scene scene;

    Collection<String> seleccionables;

    Label instruccion;

    public GestorAtacante(FaseAtaque fase){

        this.fase = fase;
        juego = fase.juego;
        scene= fase.scene;
        instruccion = (Label) scene.lookup("#instruccion");

    }

    @Override
    public void iniciar() {
        instruccion.setText("Toca el país del que quieras atacar");
        fase.setSeleccionables( juego.turnoActual().paisesConquistados() );
        agregarBotones();
    }

    @Override
    public Fase tocoBoton(Button unBoton) { return null; }

    @Override
    public void tocoPais(Node nodoPais) {
        Pais pais = (Pais) nodoPais.getUserData();
        fase.setGestor( new GestorDefensor( fase, pais ) );
    }

    private void agregarBotones() {
        HBox box = (HBox) scene.lookup("#cajaBotones");
        box.getChildren().clear();

        Button botonSiguiente = new Button("Siguiente");
        botonSiguiente.setOnAction( (e)->{
            if(juego.turnosCompletados()){
                juego.reiniciarTurnos();//TODO - Ir a reagrup
            }
            juego.siguienteTurno();
            fase.setGestor( new GestorAtacante(fase) );
        } );

        box.getChildren().add(botonSiguiente);
    }
}
