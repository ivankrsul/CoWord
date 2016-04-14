// This is part of the CoWord software. 
//
//Copyright Notice: Portions of the code in this distribution are 
//copyrighted by various parties and is marked accordingly.  
//Where code is not marked, copyright is asserted as one or more of:
//  Copyright 1997 by Purdue Research Foundation
//  Copyright 1998-2001 by Ivan Krsul.
//  Copyright 2002 by the University of Florida
//
//You may download and use this code for non-commercial purposes.   
//You may reuse this code in other non-commercial programs and 
//projects provided that this copyright notice is included, intact, 
//with all such code and code fragments.  Any sale, operation or 
//use of any portion of this code for profit or to support commercial 
//activity requires a license.  Contact Ivan Krsul <ivan@acis.ufl.edu> 
//for details.
//
//No warranty is express or implied for any of this code.  It is 
//considered experimental and is provided "as is."  Any use of this code 
//is at your own risk.

import java.lang.*;
import java.io.*;
import java.util.*;
import com.objectspace.jgl.*;

public class Graph {
  Hashtable vertices = null;	// Properties for each vertex
  com.objectspace.jgl.HashSet edges = null;
  int numberEdges = 0;
  double centrality = 0.0;
  double density = 0.0;

  public Graph() {
    vertices = new Hashtable();
    edges = new com.objectspace.jgl.HashSet();
  }

  public void calculateCentralityDensity() {
    // Density is defined as the mean of the pass-1 strengths of a network
    // Centrality is defined as the square root of the sum of the squares of
    //     the pass-2 strengths of a network.

    double min = -1.0; 
    double max = -1.0;
    double sum = 0.0;
    for	 (Enumeration e = edges.elements() ; e.hasMoreElements() ; ) {
      PEdge theEdge = (PEdge)e.nextElement();
      if(theEdge.level == 1) {
	// Pass-1 edge
	if(max < 0 || min < 0) {
	  min = theEdge.strength;
	  max = theEdge.strength;
	} else {
	  if(theEdge.strength < min)
	    min = theEdge.strength;
	  if(theEdge.strength > max)
	    max = theEdge.strength;
	}
      } else {
	sum += theEdge.strength*theEdge.strength;
      }
    }
    
    density = ((max - min) / 2.0)+min;
    centrality = Math.sqrt(sum);
  }
      
  public double getDensity() {
    return density;
  }

  public double getCentrality() {
    return centrality;
  }

  public void markVertex(int v, int val) {
    PVertex theVertex = (PVertex) vertices.get(new Integer(v));
    theVertex.setMark(val);
  }

  public void nameVertex(int v, String name) {
    PVertex theVertex = (PVertex) vertices.get(new Integer(v));
    theVertex.setName(name);
  }

  public void addEdge(int v1, int v2, int lv, int coWordVal, float strength) {
    numberEdges += 1;
    edges.add(new PEdge(v1,v2,numberEdges,lv, coWordVal, strength));
    // Add vertices if necessary
    if(!vertices.containsKey(new Integer(v1))) {
      vertices.put(new Integer(v1), new PVertex(v1));
    }
    if(!vertices.containsKey(new Integer(v2))) {
      vertices.put(new Integer(v2), new PVertex(v2));
    }
  }

  public void printGraphNeato(BufferedWriter outFile) {
    co_word.writeToOutFile(outFile,"graph G{");
    for	 (Enumeration e = vertices.elements() ; e.hasMoreElements() ; ) {
      PVertex theVertex = (PVertex)e.nextElement();
      if(theVertex.getMark()==1) {
	co_word.writeToOutFile(outFile, "    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=box];");
      } else {
	co_word.writeToOutFile(outFile, "    "+theVertex.getName()+
			   " [label=\""+theVertex.getName()+"\", fontsize=12];");
      }
    }
    for	 (Enumeration e = edges.elements() ; e.hasMoreElements() ; ) {
      PEdge theEdge = (PEdge)e.nextElement();
      PVertex v1, v2;
      
      Float rSs = new Float( ((int)(theEdge.strength*1000.00))/1000.00 ); //  Only show three decimals
      String label = new String(theEdge.number+", "+theEdge.coWordValue+", "+rSs.toString());

      v1 = (PVertex)vertices.get(new Integer(theEdge.vertex1));
      v2 = (PVertex)vertices.get(new Integer(theEdge.vertex2));
      co_word.writeToOutFileNoLF(outFile,"    "+v1.getName()+" -- "+v2.getName()+
		       " [label=\""+label+"\", fontsize=10, ");
      if(theEdge.level==1) {
	co_word.writeToOutFile(outFile, "style=bold];");
      } else {
	co_word.writeToOutFile(outFile, "style=dotted];");
      }
    }
    co_word.writeToOutFile(outFile, "}");
  }

  public void printGraphNeato2(BufferedWriter outFile, int num) {
    for	 (Enumeration e = vertices.elements() ; e.hasMoreElements() ; ) {
      PVertex theVertex = (PVertex)e.nextElement();
      if(theVertex.getMark()==1) {
	switch(num) {
	case 1:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=ellipse];");
	  break;
	case 2:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=box];");
	  break;
	case 3:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=diamond];");
	  break;
	case 4:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=4, distortion=.7];");
	  break;
	case 5:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=5];");
	  break;
	case 6:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=circle];");
	  break;
	case 7:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=3, peripheries=3];");
	  break;
	case 8:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=5, peripheries=3];");
	  break;
	case 9:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=4, distortion=.7, style=filled, color=gray];");
	  break;
	case 10:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=5, style=filled, color=gray];");
	  break;
	case 11:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=diamond, style=filled, color=gray];");
	  break;
	case 12:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=5, peripheries=3, style=filled, color=gray];");
	  break;
	case 13:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, style=filled, color=gray];");
	  break;
	case 14:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=3, stile=filled, color=gray];");
	  break;
	case 15:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=box, style=filled, color=gray];");
	  break;
	case 16:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12, shape=polygon, sides=3, peripheries=3, style=filled, color=gray];");
	  break;
	default:
	  co_word.writeToOutFile(outFile,"    "+theVertex.getName()+" [label=\""+theVertex.getName()+"\", fontsize=12];");
	  break;
	}
      } else {
	co_word.writeToOutFile(outFile,"    "+theVertex.getName()+
			   " [label=\""+theVertex.getName()+"\", fontsize=12];");
      }
    }
    for	 (Enumeration e = edges.elements() ; e.hasMoreElements() ; ) {
      PEdge theEdge = (PEdge)e.nextElement();
      PVertex v1, v2;
      
      Float rSs = new Float( ((int)(theEdge.strength*1000.00))/1000.00 ); //  Only show three decimals
      // Only print a label if both nodes are pass-1 nodes
      v1 = (PVertex)vertices.get(new Integer(theEdge.vertex1));
      v2 = (PVertex)vertices.get(new Integer(theEdge.vertex2));

      String label = null;
      if((v1.getMark()==1)&&(v2.getMark()==1)) {
	label = new String(theEdge.number+", "+theEdge.coWordValue+", "+rSs.toString());
      } else {
	label = new String("");
      }

      co_word.writeToOutFileNoLF(outFile,"    "+v1.getName()+" -- "+v2.getName()+
		       " [label=\""+label+"\", fontsize=10, ");
      if(theEdge.level==1) {
	co_word.writeToOutFile(outFile,"style=bold];");
      } else {
	co_word.writeToOutFile(outFile,"style=dotted];");
      }
    }
  }

  // Returns the number of edges in the graph
  public int size() {
    return edges.size();
  }

  public com.objectspace.jgl.HashSet getNodeSet() {
    com.objectspace.jgl.HashSet theSet = new com.objectspace.jgl.HashSet();

    for	 (Enumeration e = edges.elements() ; e.hasMoreElements() ; ) {
      PEdge theEdge = (PEdge)e.nextElement();
      theSet.add(new Integer(theEdge.vertex1));
      theSet.add(new Integer(theEdge.vertex2));
    }
    
    return theSet;
  }
}


class PVertex {
  private String name = null;
  private int mark = 0;
  private int number = 0;

  public PVertex(int v) {
    number = v;
    name = new String("[["+v+"]]");	// Name is just it's number
  }

  public int getNumber() {
    return number;
  }

  public String getName() {
    return name;
  }

  public int getMark() {
    return mark;
  }

  public void setName(String nm) {
    name = new String(nm);
  }

  public void setMark(int mk) {
    mark = mk;
  }

  public int hashCode() {
    return number+100;
  }
}

class PEdge {
  public int vertex1 = 0;
  public int vertex2 = 0;
  public int number = 0;	
  public int level = 0;
  public int coWordValue = 0;
  public float strength = 0;

  public PEdge(int v1, int v2, int num, int lev, int cw, float str) {
    vertex1 = v1; vertex2 = v2; number = num; level = lev; 
    coWordValue = cw; strength = str;
  }

  public int hashCode() {
    int larger, smaller;
    if(vertex1 > vertex2) {
      larger = vertex1; smaller = vertex2;
    } else {
      larger = vertex2; smaller = vertex1;
    }
    return smaller*10000+larger;
  }
}
