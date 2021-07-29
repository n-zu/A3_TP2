package edu.fiuba.algo3.modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.fiuba.algo3.errores.NoExisteJugadorConNumeroIndicado;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static java.util.Collections.shuffle;

public class Juego extends Observable {

    private final ArrayList<Jugador> jugadores;
    private Turnos turnos;
    private final Mapa mapa;
    private ArrayList<Tarjeta> tarjetas;
    private boolean finalizado;
    private ArrayList<Objetivo> objetivosSecretos;
    private ObjetivoComun objetivoComun;

    public Juego(int cantJugadores, String archivoPaises, String archivoObjetivos) throws FileNotFoundException {
        this.jugadores = new ArrayList<>();
        this.mapa = new Mapa(archivoPaises);
        this.crearTarjetas();

        for ( int numJugador = 0 ; numJugador < cantJugadores; numJugador++ ) {
            this.jugadores.add(new Jugador(numJugador + 1, this));
        }

        this.tarjetas = new ArrayList<Tarjeta>();
        turnos = new Turnos(jugadores);

        this.mapa.repartirPaises(this.jugadores);
        crearObjetivos();
        repartirObjetivos();
    }

    private void crearTarjetas() throws FileNotFoundException {
        this.tarjetas = new ArrayList<>();

        FileReader lector = new FileReader("archivos/tarjetas.json");

        JsonElement elementoJson = JsonParser.parseReader(lector);
        JsonArray tarjetasArregloJson = elementoJson.getAsJsonArray();

        for (int i = 0; i < tarjetasArregloJson.size(); i++) {
            JsonObject tarjetaJsonObj =tarjetasArregloJson.get(i).getAsJsonObject();

            Pais pais = this.mapa.obtenerPais(tarjetaJsonObj.get("Pais").getAsString());
            this.tarjetas.add(new Tarjeta(pais, new Simbolo(tarjetaJsonObj.get("Simbolo").getAsString())));
        }

    }

    private void crearObjetivos() throws FileNotFoundException {
        this.objetivosSecretos = new ArrayList<>();

        FileReader lector = new FileReader("archivos/objetivos.json");

        JsonObject objetoJson = JsonParser.parseReader(lector).getAsJsonObject();
        crearObjetivoComun(objetoJson.get("objetivoComun").getAsJsonArray());
        crearObjetivosOcupacion(objetoJson.get("objetivoOcupacion").getAsJsonArray());
        crearObjetivosDestruccion(objetoJson.get("objetivoDestruccion").getAsJsonArray());
    }

    private void crearObjetivoComun(JsonArray objetivoComunJson) {
        JsonObject objetivoComunJsonObj = objetivoComunJson.get(0).getAsJsonObject();
        int cantidad = objetivoComunJsonObj.get("CantidadAConquistar").getAsInt();
        this.objetivoComun = new ObjetivoComun(cantidad, mapa);
    }

    private void crearObjetivosOcupacion(JsonArray objetivosOcupacionJson) {
        for (int i = 0; i < objetivosOcupacionJson.size(); i++) {
            JsonObject objetivoOcupacionJsonObj = objetivosOcupacionJson.get(i).getAsJsonObject();
            JsonArray arregloJsonContinentes = objetivoOcupacionJsonObj.get("Continentes").getAsJsonArray();
            JsonArray arregloJsonCantidadesPorContinente = objetivoOcupacionJsonObj.get("CantidadPorContinente").getAsJsonArray();
            HashMap<String, Integer> cantidadesPorContinente = new HashMap<>();

            for (int j = 0; j < arregloJsonContinentes.size(); j++) {

                String cantidad = arregloJsonCantidadesPorContinente.get(j).getAsString();
                String continente = arregloJsonContinentes.get(j).getAsString();

                if(cantidad.equals("*")) {
                    cantidadesPorContinente.put(continente, mapa.cantidadDePaises(continente));
                }else{
                    cantidadesPorContinente.put(continente, Integer.parseInt(cantidad));
                }
            }
            this.objetivosSecretos.add(new ObjetivoOcupacion(cantidadesPorContinente, mapa));
        }
    }

    private void crearObjetivosDestruccion(JsonArray objetivosDestruccionJson) {
        for (int i = 0; i < objetivosDestruccionJson.size(); i++) {
            JsonObject objetivoDestruccionJson = objetivosDestruccionJson.get(i).getAsJsonObject();

            int numJugador = objetivoDestruccionJson.get("JugadorADerrotar").getAsInt();

            this.objetivosSecretos.add(new ObjetivoDestruccion(numJugador, mapa));
        }
    }

    public void repartirObjetivos(){

        shuffle(objetivosSecretos);

        for(Jugador jugador: jugadores){
            Objetivo objetivoSecreto = objetivosSecretos.remove(0);

            if(objetivoSecreto instanceof ObjetivoDestruccion){
                ((ObjetivoDestruccion) objetivoSecreto).verificarJugadorADerrotar(jugador, jugadores);
            }
            //FIXME: esta horrible, hay que verificar el jugador a derrotar una vez asignado el objetivo
            // sin castear en lo posible

            jugador.asignarObjetivos(objetivoComun, objetivoSecreto);
        }
    }

    public Pais obtenerPais(String nombrePais){
        return mapa.obtenerPais(nombrePais);
    } //FIXME: privado? se usa en tests

    public Tarjeta pedirTarjeta() {

        Random rand = new Random();

        return this.tarjetas.get(rand.nextInt(this.tarjetas.size()));
    }

    public void devolverTarjetas(ArrayList<Tarjeta> tarjetas ) {

        for (Tarjeta tarjeta : tarjetas ) {
            tarjeta.desactivar();
            this.tarjetas.add(tarjeta);
        }
    }

    public int fichasSegunContinentes(Set<String> paises ) {
        return mapa.fichasSegunContinentes(paises);
    }

    public ArrayList<Jugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    public Jugador obtenerJugadorConNumero(int numeroDeJugador) {
        for (Jugador jugador : jugadores) {
            if(jugador.numero() == numeroDeJugador) return jugador;
        }
        throw new NoExisteJugadorConNumeroIndicado(String.format("No existe jugador con numero %d", numeroDeJugador));
    }

    //Esto es horrible - x suerte fdelu va a refactorizar - 🤔 tmb se podria resolver con un metodo tipo add controlers 🤔
    public void agregarFichas(String nombrePais, int cantFichas) {
        mapa.obtenerPais(nombrePais).agregarFichas(cantFichas);
    }

    public void addPaisObservers(HashMap<String, Observer> observers) {
        mapa.addObservers(observers);
        mapa.notificarObservers();
    }

    public void atacar(String paisAtacante, String paisDefensor, int cantFichas) {
        mapa.atacar(paisAtacante, paisDefensor, cantFichas);
    }

    public boolean turnosCompletados() {
        return turnos.turnosCompletados();
    }

    public Jugador turnoActual() {
        return turnos.turnoActual();
    }

    public Jugador siguienteTurno() {
        return turnos.siguienteTurno();
    }

    public void reiniciarTurnos() {
        turnos.reiniciarTurnos();
    }

}
