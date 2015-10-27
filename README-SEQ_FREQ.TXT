	SEQ_FREQ.sh README

The script SEQ_FREQ.sh estimates the frequencies of a set of references sequences given a set of reads.
The script accepts as input a fasta file for the sequences and either a fastq file for the reads or a 
SAM file for alignments of the reads against the reference sequences. It also takes the mean and standard 
deviation of the fragments length distribution for the input reads. It runs IsoEM and generates the 
frequencies for the reference sequences.


To run this script on a fastq file, BWA must be installed on the machine. Alternatively, the script can be 
edited to change the mapping command to the mapper of choice.

To use the script:

- download and install the latest version of IsoEM from 
  http://dna.engr.uconn.edu/?page_id=105

- edit isoEMDir location in the script according to your local installation

- run the script using 

  ./SEQ_FREQ.sh [sam] referenceFasta [fastqFile|samFileInReferenceCoords] fragMean 
                   fragSD outputFile

Contact
-------
For questions or suggestions regarding this simulator you can contact:

     Sahar Al Seesi (sahar@engr.uconn.edu)
     Ion Mandoiu (ion@engr.uconn.edu)
     Marius Nicolae (man09004@engr.uconn.edu)
     Serghei Mangul (serghei@cs.gsu.edu)
     Alex Zelikovsky (alexz@cs.gsu.edu)

