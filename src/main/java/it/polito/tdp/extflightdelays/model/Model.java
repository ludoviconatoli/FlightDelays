package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport, Airport> visita;

	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo vertici filtrati
		Graphs.addAllVertices(grafo, dao.getVertici(idMap, x));
		for(Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())){
				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2());
				if(e == null) {
					//non c'è ancora l'arco tra i nodi
					Graphs.addEdge(grafo, r.getA1(), r.getA2(), r.getN());
				}else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		
		System.out.println("Grafo creato");
		System.out.println("Vertici: " + grafo.edgeSet().size());
		System.out.println("Archi: " + grafo.vertexSet().size());
		
	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo, a1);
		
		visita  = new HashMap<>();
		visita.put(a1, null);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			//Ci salviamo le visite
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport a3 = grafo.getEdgeSource(e.getEdge());
				Airport a4 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(a3) && !visita.containsKey(a4)) {
					visita.put(a4, a3);
				}else if(visita.containsKey(a4) && !visita.containsKey(a3)) {
					visita.put(a3, a4);
				}
				
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		while(it.hasNext()) {
			it.next();
		}
		
		percorso.add(a2);
		Airport step = a2;
		
		while(visita.get(step) != null) {
			step = visita.get(step);
			percorso.add(step);
		}
		
		return percorso;
	}
}
