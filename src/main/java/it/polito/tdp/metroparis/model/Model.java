package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private Graph <Fermata, DefaultEdge> grafo;
	
	private List <Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	public List <Fermata> getFermate(){
		MetroDAO dao = new MetroDAO();
		
		if(this.fermate==null)
			this.fermate= dao.getAllFermate();
		
		this.fermateIdMap = new HashMap<Integer,Fermata>();
		for(Fermata f: this.fermate) {
			this.fermateIdMap.put(f.getIdFermata(), f);
		}
		return this.fermate;
	}
	
	
//CALCOLO CAMMINO	
	public List <Fermata> calcolaPercorso(Fermata partenza, Fermata arrivo){
		creaGrafo();
		Map<Fermata, Fermata> alberoInverso = visitaGrafo(partenza);
		Fermata corrente=arrivo;
		List<Fermata> percorso= new ArrayList<>();
		
		while(corrente!=null) {
			percorso.add(0, corrente); //aggiungo nodo precedente finchè questo non è null ==>partenza
			corrente=alberoInverso.get(corrente);
		}
		return percorso;
	}
	
	
//VISITA GRAFO
	public Map<Fermata, Fermata>  visitaGrafo(Fermata partenza) {
		GraphIterator<Fermata, DefaultEdge> visita= new BreadthFirstIterator<>(this.grafo, partenza);
		//registro il percorso
		
		Map<Fermata, Fermata> alberoInverso = new HashMap<>();
		alberoInverso.put(partenza, null);
		
		visita.addTraversalListener(new RegistraAlberoDiVisita(alberoInverso, this.grafo));
		while(visita.hasNext()) {
			//Fermata f=
			visita.next();
		}
		
		return alberoInverso;
		/*
		List<Fermata> percorso = new ArrayList<>();
		while(fermata!=null) {
			fermata=alberoInverso.get(fermata);
			percorso.add(fermata);
		}*/
		
		
	}
	
//CREAZIONE GRAFO	
	private void creaGrafo() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);
		
	    //creo VERTICI
		MetroDAO dao= new MetroDAO();
		
		//this.grafo.addVertex(fermata);
		Graphs.addAllVertices(this.grafo, fermate);
		System.out.println(this.grafo);
	/*	
	//creo ARCHI
		//METODO 1 : itero su ogni coppia di vertici, metodo lungo
		for(Fermata partenza: fermate) {
			for(Fermata arrivo: fermate ) {
				if(dao.isFermateConnesse(partenza, arrivo)){  //esiste almeno una connessione
			    	this.grafo.addEdge(partenza, arrivo);
		    	}
			}
		}
		
		//METODO2: per ogni vertice , cerco i vertici ad esso adiacenti
		for( Fermata partenza: fermate) {
			List<Integer> idConnesse= dao.getIdFermateConnesse(partenza);
			for(Integer id: idConnesse) {
				Fermata arrivo =null;
				for(Fermata f:fermate) {
					if(f.getIdFermata()==id){
						arrivo=f; //fermata che possiede quell' id
						break;
					}
				}
				this.grafo.addEdge(partenza, arrivo);
			}
		}
		
		//METODO 2b: il DAO restituisce un elenco di oggetti fermata
		for(Fermata partenza:fermate) {
			List<Fermata> arrivi= dao.getFermateConnesse(partenza);
			for(Fermata arrivo :arrivi) {
				this.grafo.addEdge(partenza, arrivo);
			}
		  }
		
		//METODO 2c [IDENTITY MAP]: il DAO restituisce un elenco di id numerici,
		//che converto in oggetti tramite una Map<Integer,Fermata (preferita)
		for(Fermata partenza:fermate) {
			List<Integer> idConnesse= dao.getIdFermateConnesse(partenza);
			for(int id : idConnesse) {
				Fermata arrivo= fermateIdMap.get(id);
				this.grafo.addEdge(partenza, arrivo);
			}
		}
	*/	
		
		
		//METODO 3 [delego al database]: faccio una sola query che restituisca la coppia di fermate da collegare
		List<CoppiaId> fermateDaCollegare=dao.getCoppiaFermateConnesse();
		for(CoppiaId coppia: fermateDaCollegare) {
			this.grafo.addEdge(fermateIdMap.get(coppia.idPartenza), fermateIdMap.get(coppia.idArrivo));
		}
		
		/*
		System.out.println("Vertici: "+this.grafo.vertexSet().size());
		System.out.println("Archi: "+this.grafo.edgeSet().size());
		
		System.out.println("\n ");
		visitaGrafo(fermate.get(0));
		*/
	}
}
