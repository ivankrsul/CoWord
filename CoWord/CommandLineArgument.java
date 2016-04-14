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

import org.apache.oro.text.perl.*;
import java.util.*;

public class CommandLineArgument{
  String name; 
  String value;

  public CommandLineArgument(String nm, String vl) {
    name = nm; value = new String(vl);
  }

  public String getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public static Vector parseArguments(String arguments[],Vector optionsAllowed) {
    Perl5Util util = new Perl5Util();
    Vector args = new Vector();

    for(int i=0;i<arguments.length;i++) {
      if(arguments[i].length() < 1) 
	continue;
      if(arguments[i].substring(0,1).compareTo("-") != 0) {
	System.err.println("Unknown option: "+arguments[i]);
	return null;
      }
//      if(util.match("/^-(\\w+)=((\\w|-|\\.|\\s)+)$/",arguments[i])) {
      if(util.match("/^-(\\w+)=((\\S)+)$/",arguments[i])) {
	if(!optionsAllowed.contains(util.group(1))) {
	  // Option not allowed
	  System.err.println("Unknown option: "+util.group(1));
	  return null;
	}
	args.addElement(new CommandLineArgument(util.group(1),util.group(2)));
      } else {
	if(util.match("/^-((\\w|-|\\.|\\s)+)$/",arguments[i])) {
	  if(!optionsAllowed.contains(util.group(1))) {
	    // Option not allowed
	    System.err.println("Unknown option: "+util.group(1));
	    return null;
	  }
	  args.addElement(new CommandLineArgument(util.group(1),""));
	} else {
	  System.err.println("Malformed option: "+arguments[i]);
	  return null;
	}
      }
    }
    return args;
  }
}
