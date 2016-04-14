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

public class CoWord {
  final int MAX_KEYWORDS = 500;

  // The datafile we will process
  private String dataFile = null;

  // Keeps a global count of the times a keyword is used
  private Hashtable keywordCounter = new Hashtable();

  // Keeps track of which keyword-number corresponds to which
  // keyword name
  private String keywordOrder[] = new String[MAX_KEYWORDS];

  // Used for the computations
  private int keywordCount = 0;
  private int keywordValue[] = new int[MAX_KEYWORDS];
  private int cowordValue[][] = new int[MAX_KEYWORDS][MAX_KEYWORDS];
  private float strengths[][] = new float[MAX_KEYWORDS][MAX_KEYWORDS];

  // Keeps a list of the keywords that were used per line
  private Vector lineKeySets = new Vector(200);

  // Keeps a list of the graphs generated
  private Vector cowordGraphs = null;

  public CoWord(String fileName) throws CoWordFileFailure {
    dataFile = new String(fileName); // Perform analysis on this file
    loadData();
    calculateStrengths();
  } // public CoWord(String fileName) {

  public GraphEnumeration getGraphs() {
    return new GraphEnumeration(cowordGraphs);
  }

  public void generateGraphs(int maxPass1Links, int maxLinks, int minCoWord) {
    // Get rid of old graphs
    if(cowordGraphs == null) {
      cowordGraphs = new Vector(30);
    }
    cowordGraphs.removeAllElements();

    com.objectspace.jgl.HashSet nodesAllGraphs = new com.objectspace.jgl.HashSet();

    // Start pass 1
//     System.out.println("#Starting pass1 generation of graphs");

    Coordinate ijp = new Coordinate(0,0);
    while(getNewRoot(ijp,nodesAllGraphs,minCoWord)) {
      Graph thisGraph = new Graph();
      com.objectspace.jgl.HashSet nodesThisGraph = new com.objectspace.jgl.HashSet();

      /* We found a root link */ 

//       if(nodesAllGraphs.size() > 0) {a
// 	System.out.print("#These nodes were previously taken by other graphs:\n#");
// 	for (Enumeration e = nodesAllGraphs.elements() ; e.hasMoreElements() ; ) {
// 	  Integer theTakenNode = (Integer)e.nextElement();
// 	  System.out.print(keywordOrder[theTakenNode.intValue()]+", ");
// 	}	System.out.println(" ");
//       }
      

      int linkNumber = 1;	// How many links we have 

//       System.out.println("#For new graph found root link: "+
//  			 keywordOrder[ijp.geti()]+"---"+
//  			 keywordOrder[ijp.getj()]);

      thisGraph.addEdge(ijp.geti(),ijp.getj(),1, // Add the first link to the graph
			cowordValue[ijp.geti()][ijp.getj()],
			(-1)*strengths[ijp.geti()][ijp.getj()]); 
      thisGraph.nameVertex(ijp.geti(),keywordOrder[ijp.geti()]);
      thisGraph.nameVertex(ijp.getj(),keywordOrder[ijp.getj()]);
      thisGraph.markVertex(ijp.geti(),1);
      thisGraph.markVertex(ijp.getj(),1);
      nodesThisGraph.add(new Integer(ijp.geti())); // Remember what nodes are in this graph
      nodesThisGraph.add(new Integer(ijp.getj()));

      Coordinate ijpp = new Coordinate(0,0);
      while((linkNumber < maxPass1Links) && getNextLink(ijpp,nodesThisGraph,
							nodesAllGraphs,minCoWord)) {
	linkNumber+=1;

//  	System.out.println("#  found next link ("+linkNumber+"): "+
//  			   keywordOrder[ijpp.geti()]+"---"+
//  			   keywordOrder[ijpp.getj()]);

	thisGraph.addEdge(ijpp.geti(),ijpp.getj(),1,
			  cowordValue[ijpp.geti()][ijpp.getj()],
			  (-1)*strengths[ijpp.geti()][ijpp.getj()]);
	thisGraph.nameVertex(ijpp.geti(),keywordOrder[ijpp.geti()]);
	thisGraph.nameVertex(ijpp.getj(),keywordOrder[ijpp.getj()]);

	// Mark the new vertices as being pass-1 vertices
	if(nodesThisGraph.count(new Integer(ijpp.geti()))==0) {
	  thisGraph.markVertex(ijpp.geti(),1);
	}
	if(nodesThisGraph.count(new Integer(ijpp.getj()))==0) {
	  thisGraph.markVertex(ijpp.getj(),1);
	}

	nodesThisGraph.add(new Integer(ijpp.geti()));
	nodesThisGraph.add(new Integer(ijpp.getj()));
      }
      
//       System.out.print("# These nodes were taken by this graph:\n#");
//       for (Enumeration e = nodesThisGraph.elements() ; e.hasMoreElements() ; ) {
// 	Integer theTakenNode = (Integer)e.nextElement();
// 	System.out.print(keywordOrder[theTakenNode.intValue()]+", ");
//       }
//       System.out.println(" ");

      // Add graph to global list of graphs and update the list of all 
      // nodes in all graphs
      cowordGraphs.addElement(thisGraph);
      nodesAllGraphs = nodesAllGraphs.union(nodesThisGraph);
    }

    // Done with pass1.   Start pass 2 
//     System.out.println("#Starting pass2");

    for	 (Enumeration e = cowordGraphs.elements() ; e.hasMoreElements() ; ) {
      Graph thisGraph = (Graph) e.nextElement();
      int linkNumber = thisGraph.size(); // How many links do we already have for this graph
      com.objectspace.jgl.HashSet nodesThisGraph = thisGraph.getNodeSet(); // What nodes are in this graph
      com.objectspace.jgl.HashSet pass1Nodes = thisGraph.getNodeSet(); // The pass-1 nodes

      // Restore the strenghts
      for(int i=0; i<keywordCount; i++) {
	for(int j=i+1; j<keywordCount; j++) {
	  if(strengths[i][j] < 0) {
	    strengths[i][j] = -1 * strengths[i][j];
	  }
	}	
      }
      
      Coordinate ijpp = new Coordinate(0,0);
      while((linkNumber < maxLinks) && getNextPass2Link(ijpp,nodesThisGraph,
							nodesAllGraphs, pass1Nodes,
							minCoWord)) {
	linkNumber+=1;
//  	System.out.println("#  found next link ("+linkNumber+"): "+
//  			   keywordOrder[ijpp.geti()]+"---"+
//  			   keywordOrder[ijpp.getj()]);
	thisGraph.addEdge(ijpp.geti(),ijpp.getj(),0, 
			  cowordValue[ijpp.geti()][ijpp.getj()],
			  (-1)*strengths[ijpp.geti()][ijpp.getj()]);
	thisGraph.nameVertex(ijpp.geti(),keywordOrder[ijpp.geti()]);
	thisGraph.nameVertex(ijpp.getj(),keywordOrder[ijpp.getj()]);

	// Mark the new vertices as being pass-2 veertices
	if(nodesThisGraph.count(new Integer(ijpp.geti()))==0) {
	  thisGraph.markVertex(ijpp.geti(),2);
	}
	if(nodesThisGraph.count(new Integer(ijpp.getj()))==0) {
	  thisGraph.markVertex(ijpp.getj(),2);
	}

	nodesThisGraph.add(new Integer(ijpp.geti()));
	nodesThisGraph.add(new Integer(ijpp.getj()));
      }
    }
  }

  private boolean getNewRoot(Coordinate ijp, com.objectspace.jgl.HashSet nAG, int mCW) {
    // We scan for the link that has the larger value and that has
    // the necessary minimum co-occurrence. None of the nodes in the
    // new root can be in any other graph.

    int largeri = 0; int largerj = 0;
    float largerStrength = 0;
    for(int i=0; i<keywordCount; i++) {
      for(int j=i+1; j<keywordCount; j++) {
	if( (strengths[i][j]>largerStrength) &&	// Larger strength than before
	    (cowordValue[i][j]>=mCW) && 	// the co-occurrence level is acceptable
	    (nAG.count(new Integer(i)) == 0) &&	// Vertex i is not in any other graph
	    (nAG.count(new Integer(j)) == 0)) { // Vertex j is not in any other graph
	  largeri = i; 
	  largerj = j; 
	  largerStrength = strengths[i][j]; 
	}
      }
    }

    if(largerStrength > 0) {
      // We found something

      // Make sure that this is not used again
      strengths[largeri][largerj] = -1 * strengths[largeri][largerj]; 

      // Return the coordinate of the match
      ijp.seti(largeri);	
      ijp.setj(largerj);
      return true;
    } 
    
    return false;		//  No can do... no more roots.
  }

  private boolean getNextPass2Link(Coordinate ijp, com.objectspace.jgl.HashSet nTG, 
				   com.objectspace.jgl.HashSet nAG, com.objectspace.jgl.HashSet nP1, int mCW) {
    // We scan for the link that has the larger strength and that has
    // the necessary minimum co-occurrence, and where one of the nodes
    // is in the current graph and is a pass-1 node, and where both nodes 
    // must be in some graph.

    int largeri = 0; int largerj = 0;
    float largerStrength = 0;
    for(int i=0; i<keywordCount; i++) {
      for(int j=i+1; j<keywordCount; j++) {

	if( (strengths[i][j]>largerStrength) &&	// Larger strength than before
	    (cowordValue[i][j]>=mCW) && 	// the co-occurrence level is acceptable
	    (!((nTG.count(new Integer(i)) > 0) && // The link is not in the graph already
	       (nTG.count(new Integer(j)) > 0))) &&
	    ((nTG.count(new Integer(i)) > 0) ||	// One of the nodes in in this graph.
	     (nTG.count(new Integer(j)) > 0)) && 
	    ((nP1.count(new Integer(i)) > 0) || // One of the nodes is a pass-1 node 
	     (nP1.count(new Integer(j)) > 0))&&
	    ((nAG.count(new Integer(i)) > 0) && // Both nodes are in some graph
	     (nAG.count(new Integer(j)) > 0)) ) {
	  largeri = i; 
	  largerj = j; 
	  largerStrength = strengths[i][j]; 
// 	  System.out.println("#       next larger is link "+keywordOrder[largeri]+"---"+
// 			     keywordOrder[largerj]+" with a strength of "+largerStrength);
	}
      }
    }

    if(largerStrength > 0) {
      // We found something

      // Make sure that this is not used again
      strengths[largeri][largerj] = -1 * strengths[largeri][largerj]; 

      // Return the coordinate of the match
      ijp.seti(largeri);	
      ijp.setj(largerj);
      return true;
    } 

    return false;		// Sorry... no more links for this graph
  }

  private boolean getNextLink(Coordinate ijp, com.objectspace.jgl.HashSet nTG, com.objectspace.jgl.HashSet nAG, int mCW) {
    // We scan for the link that has the larger strength and that has
    // the necessary minimum co-occurrence, and where one of the nodes
    // is in the current graph, and where none of the nodes is in any
    // other graph.

    int largeri = 0; int largerj = 0;
    float largerStrength = 0;
    for(int i=0; i<keywordCount; i++) {
      for(int j=i+1; j<keywordCount; j++) {

	if( (strengths[i][j]>largerStrength) &&	// Larger strength than before
	    (cowordValue[i][j]>=mCW) && 	// the co-occurrence level is acceptable
	    (nAG.count(new Integer(i)) == 0) &&	// Vertex i is not in any other graph
	    (nAG.count(new Integer(j)) == 0) && // Vertex j is not in any other graph
	    ((nTG.count(new Integer(i)) > 0) ||	// One of the nodes in in this graph.
	     (nTG.count(new Integer(j)) > 0))) {
	  largeri = i; 
	  largerj = j; 
	  largerStrength = strengths[i][j]; 
// 	  System.out.println("#       next larger is link "+keywordOrder[largeri]+"---"+
// 			     keywordOrder[largerj]+" with a strength of "+largerStrength);
	}
      }
    }

    if(largerStrength > 0) {
      // We found something

      // Make sure that this is not used again
      strengths[largeri][largerj] = -1 * strengths[largeri][largerj]; 

      // Return the coordinate of the match
      ijp.seti(largeri);	
      ijp.setj(largerj);
      return true;
    } 

    return false;		// Sorry... no more links for this graph
  }

  private void calculateStrengths() throws CoWordFileFailure {
    // Check to see if we have space to process all the keywords
    if(keywordCounter.size() <= 3) {
      throw new CoWordFileFailure("File "+dataFile+
	": Too few keywords found.  Need more than 3.");
    } 

    if(keywordCounter.size() > MAX_KEYWORDS) {
       for (Enumeration e = keywordCounter.keys() ; e.hasMoreElements() ; ) {
	 System.out.println(new String((String)e.nextElement()));
       }
      throw new CoWordFileFailure("File "+dataFile+
	": Too many keywords found ("+keywordCounter.size()+").  Maximum is set to "+MAX_KEYWORDS);
    } 

    // load the arrays and matrices needed 
    keywordCount = 0;
    for	 (Enumeration e = keywordCounter.keys() ; e.hasMoreElements() ; keywordCount++) {
      keywordOrder[keywordCount] = new String((String)e.nextElement());
      Integer tempint = (Integer)keywordCounter.get(keywordOrder[keywordCount]);
      keywordValue[keywordCount] = tempint.intValue();
//       System.out.println("#Keyword "+keywordOrder[keywordCount]+" has a count of "+keywordValue[keywordCount]);
    }

    // We create a hashtable that will quickly map keyword-name to
    // keyword position
    Hashtable auxKeyMap = new Hashtable(keywordCount);
    for(int i=0;i<keywordCount;i++) {
      auxKeyMap.put(keywordOrder[i],new Integer(i));
    }
    
    // Calculate the co-word for each keyword.  We have the set of keywords per line
    for	(Enumeration e = lineKeySets.elements() ; e.hasMoreElements() ;) {
      com.objectspace.jgl.HashSet lineSet = (com.objectspace.jgl.HashSet) e.nextElement();
      String keyList[] = new String[lineSet.size()];
      int lineKeyCount = 0;
      for (Enumeration e2 = lineSet.elements() ; e2.hasMoreElements() ;lineKeyCount++) {
	keyList[lineKeyCount] = new String((String)e2.nextElement());
      }

      for(int i=0; i<lineKeyCount;i++) {
	for(int j=i+1;j<lineKeyCount;j++) {
	  // the keywords keyList[i] and keyList[j] have co-occurrance 
	  Integer posi = (Integer)auxKeyMap.get(keyList[i]);
	  Integer posj = (Integer)auxKeyMap.get(keyList[j]);

	  if(posi.intValue()>posj.intValue()) {
	    /* Switch. */
	    Integer temp = posi;
	    posi = posj;
	    posj = temp;
	  }

	  cowordValue[posi.intValue()][posj.intValue()]+=1; // Add one to the coword value
	}
      }
    }

    // Calculate strengths
    for(int i=0; i<keywordCount;i++) {
      for(int j=i+1;j<keywordCount;j++) {
	if(cowordValue[i][j] > 0) {
	  strengths[i][j] = (float)(cowordValue[i][j]*cowordValue[i][j]) /
	                    (float)(keywordValue[i]*keywordValue[j]);
	}
      }
    }
    
    for(int i=0; i<keywordCount;i++) {
      for(int j=i+1;j<keywordCount;j++) {
// 	System.out.println("#Co-occurrences for keys "+keywordOrder[i]+"-"+keywordOrder[j]+" = "+cowordValue[i][j]+" strength = "+strengths[i][j]);
      }
    }
  }

  private void loadData() throws CoWordFileFailure {
    // The file is a collection of lines, each containing a list of 
    // keywords that we will use for the coword analysis.
    // The first line contains the label for each keyword.
    File theFile = null;
    BufferedReader theReader = null;

    theFile = new File(dataFile);
    if(!(theFile.isFile() && theFile.exists() && theFile.canRead())) {
      // We can't work with this file. 
      throw new CoWordFileFailure("File "+dataFile+
	" does not exist, is a directory, or can't be accessed");
    }

    // Try to open the file.
    try {
      theReader = new BufferedReader(new FileReader(theFile));
    } catch (Exception e) {
      // Can't read the file.
      throw new CoWordFileFailure("Can't read file "+dataFile+": "+
	e.toString());
    }

    try {
      String line=null;
      String token=null;
      Integer count=null;
      com.objectspace.jgl.HashSet keywordsInLine = null;

      while( (line=theReader.readLine()) != null ) {
	keywordsInLine = new com.objectspace.jgl.HashSet();

	// Split the line into multiple keywords
	// comma (,) as the keyword separator
	StringTokenizer st = new StringTokenizer(line,",");
	while (st.hasMoreTokens()) {
	  token = st.nextToken();
	  token = token.trim();
	  keywordsInLine.add(token); // // Keep track of the keywords in this line

	  // Update the global count for that keyword.
	  count=(Integer)keywordCounter.get(token);
	  if(count!=null) {
	    keywordCounter.put(token,new Integer(count.intValue()+1));
	  } else {
	    keywordCounter.put(token,new Integer(1));
	  }
	}
	
	// Remember what keywords were used in that line
	lineKeySets.addElement(keywordsInLine);
      } 
    } catch (Exception e) {
      throw new CoWordFileFailure("Failed to read keyword from file "+dataFile+": "+
				  e.toString());
    } 
  }
}

