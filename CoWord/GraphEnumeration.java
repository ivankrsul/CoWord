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

import java.util.*;

public class GraphEnumeration implements Enumeration {

  private Vector graphVector = null;
  private int elementNumber = 0;

  public GraphEnumeration(Vector gV) {
    graphVector = gV;
  }

  public boolean hasMoreElements() {
    if(graphVector == null) {
      return false;
    }
    
    if(graphVector.size() > elementNumber) {
      return true;
    }

    return false;
  }

  public Object nextElement() {
    Object theGraph = (Object)graphVector.elementAt(elementNumber);
    elementNumber += 1;
    return theGraph;
  }
}
