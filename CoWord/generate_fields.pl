#!/bin/perl -w

open FILE, "data.txt" || die "Can't open datafile data.txt: $!\n";
$firstline = <FILE>;
$firstline =~ s/^VID\,\s?//; 
$firstline =~ s/\,\s?/\n/g; 
print $firstline."\n";
close FILE;


