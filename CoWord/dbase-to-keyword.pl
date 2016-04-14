#!/usr/bin/perl -w

# This is part of the CoWord software. 
#
#Copyright Notice: Portions of the code in this distribution are 
#copyrighted by various parties and is marked accordingly.  
#Where code is not marked, copyright is asserted as one or more of:
#  Copyright 1997 by Purdue Research Foundation
#  Copyright 1998-2001 by Ivan Krsul.
#  Copyright 2002 by the University of Florida
#
#You may download and use this code for non-commercial purposes.   
#You may reuse this code in other non-commercial programs and 
#projects provided that this copyright notice is included, intact, 
#with all such code and code fragments.  Any sale, operation or 
#use of any portion of this code for profit or to support commercial 
#activity requires a license.  Contact Ivan Krsul <ivan@acis.ufl.edu> 
#for details.
#
#No warranty is express or implied for any of this code.  It is 
#considered experimental and is provided "as is."  Any use of this code 
#is at your own risk.
#
#
# This program takes a dump from the database and
# generates a list of keywords suitable for input to the co_word
# analysis tool (co_word.java).  This program reads a file called 
# data.fields with a list of the columns that should be considered.
#
# This program assumes that the datafile is called data.txt, that the
# first row in the file is the list of column titles, that the first
# column is the record ID (VID), that all columns whose name end in
# "Def?" can be ignored, that all fields whose name has a hash (#) in
# the middle (and all the boolean classifiers defined in this program)
# are YES/NO fields, and that all other fields are list classifiers. 
# We also assume that data elements for columns that are classifiers
# and that have the values *, ? or INVALID can be ignored (i.e. we will 
# not generate a keyword).
#
# The program generates keywords as follows: 
#  * All classifier key are generated as classifierName_value, except when
#        the value is *, ? or INVALID
#  * All YES/NO fields are generated as fieldName (with # replaced with _) 
#    if values is YES
# 
# The output is written to the file data.keywords

$datafile = "data.txt";
$outfile = "data.keywords";
$fieldfile = "data.fields";

# In addition to the fields whose name includes a #, these fields are 
# considered boolean
%bClass = ( 
	  );

# Read the fields.  
%allowedFields = ();
open(FIELDS,$fieldfile) || die "Can't open fieldfile: $!\n";
while(<FIELDS>) {
  chop;
  $temp = $_;
  next if( $temp =~ /^\#/ ); 	# Ignore comments
  next if( $temp =~ /^\s*$/ );	# Ignore empty lines
  $temp =~ s/^(\s)*(\S*.*\S)(\s)*$/$2/; # Remove leading and trailing spaces
  $allowedFields{$temp} = 1;
}

open(DATA,$datafile) || die "Can't open datafile: $!\n";

open(DATAOUT,">$outfile") || die "Can't open output datafile: $!\n";

$temp = <DATA>; chop($temp);
@titles = split(/\s*,\s*/,$temp);
while(<DATA>) {
  chop;
  ($vid,@fields) = split(/\s*,\s*/); # Split data into fields (CSV)
  $vid = $vid;			# Get rid of annoying error message
  $fieldNum = 0;
  $printed = 0;
  foreach $temp (@fields) {
    $fieldNum++;		# Remember which column we are on
    
    # Skip this field... we don't know it
    next if (!defined($allowedFields{$titles[$fieldNum]}));
    
    # If the field is a boolean type.... we look for YES
    if ( ($titles[$fieldNum]=~/.+\#.+/) 
	 || (defined $bClass{$titles[$fieldNum]})) {
      if("\U$temp" eq "YES") {
	print DATAOUT ", " if($printed != 0);
	$printname = $titles[$fieldNum];
	$printname =~ s/\#/_/g;
	print DATAOUT $printname;
	$printed = 1;
      }
    } else {
      # Remove leading and trailing spaces
      $temp =~ s/^(\s)*(\S*.*\S)(\s)*$/$2/; 
      
      # Ignore fields with data *, ? or INVALID
      next if(($temp eq "*") || ($temp eq "?") ||
	      ($temp eq "INVALID"));
      
      # Print data as a keyword
      print DATAOUT ", " if($printed ne 0);
      print DATAOUT $titles[$fieldNum]."_".$temp;
      $printed = 1;
    }
  }
  print DATAOUT "\n" if($printed != 0);
}
