package it.polito.tdp.metroparis.model;

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
	
	
//VISITA GRAFO
	public void visitaGrafo(Fermata partenza) {
		GraphIterator<Fermata, DefaultEdge> visita= new BreadthFirstIterator<>(this.grafo, partenza);
		while(visita.hasNext()) {
			Fermata f= visita.next();
			System.out.println(f);
		}
	}
	
//CREAZIONE GRAFO	
	public void creaGrafo() {
		this.grafo = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);
		
		
	    //creo VERTICI
		MetroDAO dao= new MetroDAO();
		List<Fermata> fermate =dao.getAllFermate();
		Map<Integer, Fermata> fermateIdMap= new HashMap<Integer,Fermata>();
		for(Fermata f: fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		//this.grafo.addVertex(fermata);
		Graphs.addAllVertices(this.grafo, fermate);
		System.out.println(this.grafo);
		
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
		
		//METODO 3 [delego al database]: faccio una sola query che restituisca la coppia di fermate da collegare
		List<CoppiaId> fermateDaCollegare=dao.getCoppiaFermateConnesse();
		for(CoppiaId coppia: fermateDaCollegare) {
			this.grafo.addEdge(fermateIdMap.get(coppia.idPartenza), fermateIdMap.get(coppia.idArrivo));
		}
		
		System.out.println("Vertici: "+this.grafo.vertexSet().size());
		System.out.println("Archi: "+this.grafo.edgeSet().size());
		
		System.out.println("\n ");
		visitaGrafo(fermate.get(0));
	}
}
