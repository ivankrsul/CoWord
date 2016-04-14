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

public class co_word {
    public static String dataFile = "data.keywords";
    public static String outDataFile = "keyword.result";
    public static int maxPass1Links = 3;
    public static int maxLinks = 10;
    public static int minCoWord = 3;
    public static String outString = null;
    
    public static void main(String args[]) {
        
        Vector argsAllowed = new Vector();
        
        // Parameters are maxpass1links, maxlinks, mincoword
        argsAllowed.addElement("maxPass1Links");
        argsAllowed.addElement("maxLinks");
        argsAllowed.addElement("minCoword");
        argsAllowed.addElement("inDatafile");
        argsAllowed.addElement("outDatafile");
        argsAllowed.addElement("h");
        argsAllowed.addElement("help");
        
        if(args.length > 0) {
            Vector arguments = CommandLineArgument.parseArguments(args,argsAllowed);
            if(arguments != null) {
                for(Enumeration e = arguments.elements(); e.hasMoreElements(); ) {
                    CommandLineArgument theArg = (CommandLineArgument)e.nextElement();
                    if(theArg.getName().compareTo("h")==0) {
                        System.out.println("Valid parameters are:");
                        System.out.println("   -maxPassLinks=nnn (default is 3)");
                        System.out.println("   -maxLinks=nnn (default is 10)");
                        System.out.println("   -minCoword=nnn (default is 3)");
                        System.out.println("   -inDatafile=filename (default is data.keywords");
                        System.out.println("   -outDatafile=filename (default is keyword.result");
                        System.out.println("");
                        System.exit(0);
                    } else if(theArg.getName().compareTo("maxPass1Links")==0) {
                        maxPass1Links = Integer.parseInt(theArg.getValue());
                    } else if(theArg.getName().compareTo("maxLinks")==0) {
                        maxLinks = Integer.parseInt(theArg.getValue());
                    } else if(theArg.getName().compareTo("minCoword")==0) {
                        minCoWord = Integer.parseInt(theArg.getValue());
                    } else if(theArg.getName().compareTo("inDatafile")==0) {
                        dataFile = new String(theArg.getValue());
                    } else if(theArg.getName().compareTo("outDatafile")==0) {
                        outDataFile = new String(theArg.getValue());
                    } else {
                        // Man... something went wrooooong!  We should never get here!
                        System.err.println("We should have never gotten here!");
                        System.exit(0);
                    }
                }
            } else {
                System.exit(0);
            }
        }
        
        // Load the data from the file, and calculate the
        // number of times a keyword occurrs, the coword
        // for keyword pairs, and the strength for those
        // cowords that exceed the coword minimum.
        
        CoWord theClass = null;
        try {
            theClass = new CoWord(dataFile);
        } catch (CoWordFileFailure e) {
            System.out.println("Can't load the keyword file: "+e.toString());
            System.exit(0);
        }
        
        File theReadmeFile = new File(outDataFile+".README");
        BufferedWriter theReadmeWriter = null;
        File theGlobalFile = new File(outDataFile+".AutoGraphGlobal.dot");
        BufferedWriter theGlobalWriter = null;
        File theBATFile = new File(outDataFile+".bat");
        BufferedWriter theBATWriter = null;
        
        // Try to open the files.
        try {
            theReadmeWriter = new BufferedWriter(new FileWriter(theReadmeFile));
        } catch (Exception e) {
            // Can't write to the file.
            System.err.println("Can't write to Readme file "+outDataFile+".README: "+e.toString());
            System.exit(0);
        }
        
        try {
            theGlobalWriter = new BufferedWriter(new FileWriter(theGlobalFile));
        } catch (Exception e) {
            // Can't write to the file.
            System.err.println("Can't write to Global file "+outDataFile+".AutoGraphGlobal.dot: "+e.toString());
            System.exit(0);
        }
        
        try {
            theBATWriter = new BufferedWriter(new FileWriter(theBATFile));
            writeToOutFile(theBATWriter,"#!/bin/sh");
        } catch (Exception e) {
            // Can't write to the file.
            System.err.println("Can't write to BAT file "+outDataFile+".bat: "+e.toString());
            System.exit(0);
        }
        
        writeToOutFile(theReadmeWriter, "Results for CoWord analysis run.");
        writeToOutFile(theReadmeWriter, "Input Datafile = "+dataFile);
        writeToOutFile(theReadmeWriter, "Output Datafile = "+outDataFile);
        writeToOutFile(theReadmeWriter, "Date = "+Calendar.getInstance().getTime().toString());
        writeToOutFile(theReadmeWriter,"maxPass1Links="+maxPass1Links);
        writeToOutFile(theReadmeWriter,"maxLinks="+maxLinks);
        writeToOutFile(theReadmeWriter,"minCoWord="+minCoWord);
        writeToOutFile(theReadmeWriter, "");
        
        theClass.generateGraphs(maxPass1Links,maxLinks,minCoWord);
        int i = 0;
        for (Enumeration e = theClass.getGraphs() ; e.hasMoreElements() ; ) {
            Graph theGraph = (Graph)(e.nextElement());
            i += 1;
            
            File theDOTFile = new File(outDataFile+".AutoGraph"+i+".dot");
            BufferedWriter theDOTWriter = null;
            
            // Try to open the files.
            try {
                theDOTWriter = new BufferedWriter(new FileWriter(theDOTFile));
            } catch (Exception e2) {
                // Can't write to the file.
                System.err.println("Can't write to DOT file "+outDataFile+".Autograph"+i+".dot: "+e2.toString());
                System.exit(0);
            }
            
            theGraph.printGraphNeato(theDOTWriter);
            
            try {
                theDOTWriter.close();
            } catch (Exception e3) {
                System.err.println("Can't close the DOT file "+outDataFile+".Autograph"+i+".dot: "+e3.toString());
                System.exit(0);
            }
            
            writeToOutFile(theBATWriter,"dot -Tps -Gsize=8.5,11 -Grotate=90 "+
            "-Glabel=\"Graph Number "+i+"\" "+
            outDataFile+".AutoGraph"+i+".dot -o "+outDataFile+".AutoGraph"+i+".ps");
        }
        
        // Print the combined graph
        writeToOutFile(theGlobalWriter,"graph G{");
        i = 0;
        for	 (Enumeration e = theClass.getGraphs() ; e.hasMoreElements() ; ) {
            i++;
            Graph theGraph = (Graph)(e.nextElement());
            theGraph.printGraphNeato2(theGlobalWriter,i);
        }
        writeToOutFile(theGlobalWriter,"}");
        writeToOutFile(theBATWriter,"dot -Tps "+outDataFile+".AutoGraphGlobal.dot -o "+outDataFile+".AutoGraphGlobal.ps");
        
        // Calculate the density and centrality of the networks.
        i = 0;
        for	 (Enumeration e = theClass.getGraphs() ; e.hasMoreElements() ; ) {
            i += 1;
            Graph theGraph = (Graph)(e.nextElement());
            theGraph.calculateCentralityDensity();
            writeToOutFile(theReadmeWriter,"For graph "+i+": density="+theGraph.getDensity());
            writeToOutFile(theReadmeWriter,", centrality="+theGraph.getCentrality());
        }
        
        try {
            theReadmeWriter.close();
            theGlobalWriter.close();
            theBATWriter.close();
        } catch (Exception e4) {
            System.err.println("Can't close the Readme, Global o BAT files ("+outDataFile+"): "+e4.toString());
            System.exit(0);
        }
        
    }
    
    static void writeToOutFile(BufferedWriter file, String theString) {
        try {
            file.write(theString);
            file.newLine();
        } catch (Exception e) {
            // Can't write to the file.
            System.err.println("Can't write to file file "+outDataFile+": "+e.toString());
            System.exit(0);
        }
    }
    
    static void writeToOutFileNoLF(BufferedWriter file, String theString) {
        try {
            file.write(theString);
        } catch (Exception e) {
            // Can't write to the file.
            System.err.println("Can't write to file file "+outDataFile+": "+e.toString());
            System.exit(0);
        }
    }
}

