package edu.fiuba.algo3;

import java.util.*;

import static java.lang.Math.max;

public class Jugador {
    //Esto estaría bueno que sea un diccionario con clave -> nombrePais, valor -> Pais para poder buscarlo
    // no deberia ser privado todo esto?

    HashMap<String, Pais> paisesConquistados = new HashMap<>();
    ArrayList<Tarjeta> tarjetas = new ArrayList<>();
    int numero;
    Juego juego;
    int canjesRealizados = 0;

    public Jugador(){}

    public Jugador(int numeroDeJugador, Juego claseJuego){
        numero = numeroDeJugador;
        juego = claseJuego;
    }

    public void desocupar(Pais unPais) {
        paisesConquistados.remove(unPais.nombre());
    }

    public void ocupar(Pais unPais){
        paisesConquistados.put(unPais.nombre(),unPais);
    }

    public int invadir(Pais atacante, Pais defensor) {
        // pedir un dato al jugador y devolverlo
        return 3;
    }

    public void atacar(Pais atacante, Pais defensor, int cantFichas){
        if( !paisesConquistados.containsValue(atacante) ) throw new JugadorNoTienePais(String.format("El jugador no puede atacar con el pais %s porque no es suyo",atacante.nombre()));
        if( paisesConquistados.containsValue(defensor) ) throw new PaisDelMismoPropietarioNoPuedeSerAtacado(String.format("El jugador no puede atacar a el pais %s porque ya es suyo",defensor.nombre()));
        atacante.atacar(defensor,cantFichas);
    }
    /*
    public int agregarFichas( int cantFichas ){
        Pais receptorDeFichas;
        while(cantFichas>0){
            receptorDeFichas=new Pais("input","input",null);//input
            if( paisesConquistados.contains(receptorDeFichas) ){
                receptorDeFichas.agregarFichas(1);
                cantFichas--;
            }else{
                //muestra algo o no hace nada ?
            }
        }
        return 0;
    } No habría que crear un país, hay que acceder a los que tiene conquistados el jugador
    La idea de "agregarFichas" como lo pensamos, es que se le permita el jugador agregar las fichas
    que quiera en los paises que ya tiene conquistados.

    Nico Z - Me lo imagine asi :
        El jugador clickea un pais en el mapa, si es suyo le agrega una ficha.
        Repetir hasta no tener fichas.
    En la parte donde "crea" un pais, se supone que lo recibe como el input ( Hay que ver como seria con la UI )
     */

    public void agregarFichas(int cantFichas){
        /* Algo así tendŕia que ser creemos
        while(cantFichas > 0) {
            String paisElegido = pedir pais
            int cantFichasElegidas = pedir cant de fichas
            if(paisesConquistados.containsKey(paisElegido)){
                if(cantFichasElegidas <= cantFichas){
                    paisesConquistados.get(paisElegido).agregarFichas(cantFichasElegidas);
                    cantFichas = cantFichas - cantFichasElegidas;
                }
                else{
                    error
                }
            }
            else{
                error
            }
        }
        */
        /* Z - Parece parecido a lo que plantee salvo que
            recibe el nombre del pais en vez del objeto
            pide la cant de fichas
        */
        // Hardcodeadísimo para probar
        while (cantFichas > 0) {
            for (String nombrePais : paisesConquistados.keySet()) {
                paisesConquistados.get(nombrePais).agregarFichas(1);
                cantFichas = cantFichas - 1;
                if (cantFichas == 0) break;
            }
        }
    }

    public int obtenerCantidadPaises(){
        return paisesConquistados.size();
    }



    public void turnoAtaque(){
        int cantInicialPaises = paisesConquistados.size();
        Scanner entrada = new Scanner(System.in);
        String nombreAtacante;
        String nombreDefensor;
        int cantFichas;

        while (true)  {
            cantFichas = Integer.parseInt(entrada.nextLine());
            if(cantFichas == 0) break;
            System.out.println("introduzca atacante: ");
            nombreAtacante = entrada.nextLine();
            System.out.println("introduzca defensor: ");
            nombreDefensor = entrada.nextLine();
            if( !paisesConquistados.containsKey(nombreAtacante) ) throw new JugadorNoTienePais(String.format("El jugador no puede atacar con el pais %s porque no es suyo",nombreAtacante));
            Pais atacante = paisesConquistados.get(nombreAtacante);
            Pais defensor = juego.obtenerPais(nombreDefensor);
            atacante.atacar(defensor, cantFichas);
        }

        if (paisesConquistados.size() > cantInicialPaises) {
            tarjetas.add(juego.pedirTarjeta());
        }
    }

    public void turnoReagrupacion(){

        Scanner entrada = new Scanner(System.in);
        String nombreOrigen;
        String nombreDestino;
        int cantFichas;

        while (true) {
            System.out.println("introduzca cantidad de fichas a mover: ");
            cantFichas = entrada.nextInt();
            if(cantFichas == 0) break;
            System.out.println("introduzca el origen: ");
            nombreOrigen = entrada.nextLine();
            System.out.println("introduzca el destino: ");
            nombreDestino = entrada.nextLine();

            if( !paisesConquistados.containsKey(nombreOrigen) ) throw new JugadorNoTienePais(String.format("El jugador no mover fichas desde el pais %s porque no es suyo",nombreOrigen));
            if( !paisesConquistados.containsKey(nombreDestino) ) throw new JugadorNoTienePais(String.format("El jugador no puede mover fichas al pais %s porque no es suyo",nombreDestino));

            Pais Origen = paisesConquistados.get(nombreOrigen);
            Pais Destino = paisesConquistados.get(nombreDestino);

            Origen.reagruparA(Destino, cantFichas);
        }
    }

    public void turnoColocacion(){
        int fichas = 0;

        fichas += canjearTarjetas();

        fichas += fichasPorConquista();
        agregarFichas( fichas );
    }

    public int canjearTarjetas(){
        for( Tarjeta tarjeta : tarjetas )
            if( paisesConquistados.keySet().contains( tarjeta.pais() ) )
                tarjeta.activar();

        ArrayList<Tarjeta> grupoCanjeable = Tarjeta.grupoCanjeable( tarjetas );

        if( Objects.nonNull(grupoCanjeable) ){
            tarjetas.removeAll( grupoCanjeable );
            juego.devolverTarjetas( grupoCanjeable );
            return realizarCanje();
        }

        return 0;
    }

    public int realizarCanje(){
        canjesRealizados ++;
        if( canjesRealizados == 1 ) return 4;
        if( canjesRealizados == 2 ) return 7;
        return (canjesRealizados-1)*5;
    }

    public int fichasPorConquista(){
        int fichas = 0;

        fichas += max( paisesConquistados.size()/2 ,3);

        fichas += juego.fichasSegunContinentes(paisesConquistados.keySet());

        return fichas;
    }
}