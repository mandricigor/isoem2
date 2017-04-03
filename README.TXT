isoem2/isoDE2 README


1. Installation:
------------

1. Create a isoem2 directory and download the git repository using 

   git clone https://github.com/mandricigor/isoem2.git

3. run the linux shell script 'install' provided in the git repository

4. [Optional] On windows you might want to add the isoem2/isoDE2  
   installation directory to the path, such that you can invoke 
   isoem2 from any location. On linux, you can obtain a similar 
   effect by creating a symbolic link to the isoem2 and isoDE2 executables 
   in /usr/local/bin.



2. Testing your installation:
--------------------------
To test the installation of isoem2 and isoDE2, download and unzip the following 
compressed archive and follow the instructions in the README file included in the archive.

http://dna.engr.uconn.edu/~software/IsoEM/testdata/IsoEM2IsoDE2-MAQC-Sample.zip



3. Running isoem2:
-----------------

isoem2 takes as input a set of known isoforms in GTF format, and a 
file with aligned reads in SAM format. The aligned reads MUST be 
sorted by read name. If not sure, run this command to sort the 
file:

     sort -k 1,1 aligned_reads.sam > aligned_reads_sorted.sam


You can run isoem2 from the command line as follows:

     isoem2 [global options]* [library options]* <aligned_reads.sam>

Or, if you run provide read alignments from the standard input:

     cat <aligned_reads.sam> | isoem2 [global options]* [library options]*


Mandatory global options:
------------------------
-G, --GTF <GTF file>                    Known genes and isoforms in GTF format
Mandatory library options: either -a or both -m and -d must be present:
-------------------------
-m, --fragment-mean <Double>            Fragment length mean
-d, --fragment-std-dev <Double>         Fragment length standard deviation
-a, --auto-fragment-distrib             Automatically detect fragment length
                                          distribution from uniquely mapping
                                          paired reads (DOES NOT WORK FOR
                                          SINGLE READS)
Optional global options:
-----------------------
-c, --gene-clusters <Cluster file>      Override isoform to gene mapping
                                          defined in the GTF file with a
                                          mapping taken from the given file.
                                          The format of each line in the file
                                          is "isoform   gene"
-g <genome fasta file>                  Genome reference sequence (needed by
                                          some library options)
-b                                      Perform hexamer bias correction
-h, --help                              Show help
-r <Repeats GTF>                        Drop alignments falling within
                                          annotated repeats
Optional library options:
------------------------
-s, --directional                       Dataset obtained by directed RNA-Seq
                                          (the strand of each read is
                                          deterministically chosen: for single
                                          reads, the read always comes from
                                          the coding strand; for paired reads,
                                          the first read always comes from the
                                          coding strand, the second from the
                                          opposite strand)
--antisense                             Directional sequencing but the reads
                                          come from the antisense
--mate-pairs                            Paired reads come from the same strand
                                          (as opposed to the default behavior
                                          where the two reads in a pair are
                                          assumed to come from opposite
                                          strands)
--max-mismatches <Integer>              Maximum number of mismatched allowed
                                          for a read. This requires the genome
                                          sequence to be specified (see -g).
-q, --quality-scores                    Weigh the reads based on their quality
                                          scores. This requires the genome
                                          sequence to be specified (see -g).
--repeat-threshold <nbases>             Drop all reads that have more than
                                          this many bases inside annotated
                                          repeats. Default: 20.
--polyA <nbases>                        Reads have been generated from mRNAs
                                          with polyA tails of approximately
                                          the given number of bases
-o <file prefix>                        Output files prefix. It can include
                                          path. Default: same as sam file name
-O <directory prefix>                   Output directory prefix. If read
                                          alignments are read from stdin,
                                          the default value is stdinSample
-C <confidence interval (%)>            Compute expression of genes/isoforms
                                          with specified confidence intervals.
                                          Provide an integer (default: 95,
                                          bootstraps: 200)
--endseq                                Disable length normalization for data
                                          generated using 5' or 3' end-sequen-
                                          cing protocols, which generate a
                                          single fragment per cDNA molecule

Output
------
isoem2 generates the following output files structure under a directory 
with the same name as the sam file, unless the -o is used


<output_directory>
    |
    - output
    |	|
    |	- Isoforms
    |   |   |
    |	|   - iso_fpkm_estimates
    |   |   - iso_tpm_estimates
    |   - Genes 
    |       |
    |       - iso_fpkm_estimates
    |       - iso_tpm_estimates
    - ConfidenceIntervals (Only if -C option is used)
    |   | 
    |   - iso_fpkm_ci
    |   - iso_tpm_ci
    |   - gene_fpkm_ci 
    |   - gene_tpm_ci
    - boostrap.tar.gz

Files under output/Isoforms and output/Genes are tab delimited files with the following two fields
1- Isoform/Gene ID
2- Isoform/Gene FPKM (Fragments Per Kilobase per Million reads) or TPM (Transcripts per Million reads)

Files under output/ConfidenceIntervals are tab delimited files with the following three fields
1- Isoform/Gene ID
2- Lower-bound for the 95% confidence interval of the Isoform/Gene FPKM/TPM estimate determined by bootstrapping
3- Upper-bound for the 95% confidence interval of the Isoform/Gene FPKM/TPM estimate determined by bootstrapping

boostrap.tar.gz is a compressed tar archive containing bootstrap samples used to determine confidence intervals. 
This archive can be used as input to the isoDE2 tool for computing differentially expressed isoforms/genes.

Note: Read Alignment:
---------------------

To align the reads you have one of two options:
1) Use spliced alignment directly on the genome
2) Use unspliced alignment to the transcriptome. 
   If you have a transcriptome reference and no GTF (needed to run isoem2), 
   you can use the fastaToGTF tool, included with the isoem2 suite, to generate a GTF.
   If you want to generate a transcriptome reference using a GTF, you can use the 
   extract-isoform-sequences-from-genome tool, included with the isoem2 suite

4. Running isoDE2
----------------

isoDE2 makes DE calls for gene/isoform FPKM and TPM estimated using the boostrapping output generated by isoem2


isoDE2 -c1 <List of boostraping path for condition 1> -c2 <List of boostraping path for condition 2> -pval <desired p value> -out <output-files-prefix>


Mandatory parameters
--------------------

-c1		List of bootstrapping compressed archives for condition 1 
-c2		List of bootstrapping compressed archives for condition 2
-pval		pval 
-out		prefix for generated output files




Output
------
4 files with the prefix specifies as input and the following suffixes
geneFPKM
geneTPM
isoFPKM
isoTPM

All four output files have the same structure, described below

Description of isoDE2 Output file:
---------------------------------



1- Gene/isoform ID
2- Confident log2(FC): the base 2 logarithm of the largest condition 2 vs condition 1 
                       fold change of gene/isoform FPKM/TPM estimates supported by the 
                       bootstrap samples at a significance level of 'pval'.  Positive values 
                       represent over-expression in condition 2, negative values representing 
                       over-expression in condition 1, and zero values indicate that no 
                       significant change was detected.
3- Single run log2(FC): the base 2 logarithm of the ratio between expression levels estimated 
                        by isoem2 for condition 2 and condition 1 (or the mean estimates in case 
                        replicates are provided for the two conditions).
4- condition 1 FPKM (or TPM) based on isoem2 run without bootstrapping (mean value in case of replicates)
5- condition 2 FPKM (or TPM) based on isoem2 run without bootstrapping (mean value in case of replicates)





Example
-------
isoDE2 -c1 /data1/BRAIN_UHR_Test/BRAIN_Genome_DIR/ /DataSet1/Test1_DIR/ -c2 /data1/BRAIN_UHR_Test/UHR_Genome_DIR/ /DataSet1/Test2_DIR/ -pval 0.05 -out "output1.txt"

isoDE2 -c1 ./BRAIN_Genome_DIR/ ./Test1_DIR/ -c2 ./UHR_Genome_DIR/ ./DataSet1/Test2_DIR/ -pval 0.05 -out "output2.txt"




5. Visualizing read coverage:
-----------------------------

If you want to visualize the isoforms and their coverage by reads, you can use the isoviz 
command. It produces a bedGraph file and a GTF with fpkm values file which can be uploaded 
to the UCSC browser. The name of the output files are automaticaly generated from the input 
file name and ends in _iso_read_coverage.bed and _isoforms_w_fpkm.gtf. They can also be set 
using the -o option. 

The options are almost the same as for isoem2 except that isoviz also needs the isoform frequency
file (e.g. obtained by running isoem2). The frequency file is specified using the -f option. The
full command line synopsis is given below:


      isoviz [global options]* [library options]* <aligned_reads.sam>


Mandatory global options:
------------------------
-G, --GTF <GTF file>                    Known genes and isoforms in GTF format
-f <frequency file>                     Isoform FPKMs computed by IsoEM2

Mandatory library options:
-------------------------
-m, --fragment-mean <Double>            Fragment length mean
-d, --fragment-std-dev <Double>         Fragment length standard deviation

Optional global options:
-----------------------
-c, --gene-clusters <Cluster file>      Override isoform to gene mapping
                                          defined in the GTF file with a
                                          mapping taken from the given file.
                                          The format of each line in the file
                                          is "isoform	gene"
-g <genome fasta file>                  Genome reference sequence (needed by
                                          some library options)
-b                                      Perform hexamer bias correction
-h, --help                              Show help

Optional library options:
------------------------
-s, --directed                          Dataset obtained by directed RNA-Seq
                                          (the strand of each read is
                                          deterministically chosen: for single
                                          reads, the read always comes from
                                          the coding strand; for paired reads,
                                          the first read always comes from the
                                          coding strand, the second from the
                                          opposite strand)
--mate-pairs                            Paired reads come from the same strand
                                          (as opposed to the default behavior
                                          where the two reads in a pair are
                                          assumed to come from opposite
                                          strands)
--max-mismatches <Integer>              Maximum number of mismatched allowed
                                          for a read. This requires the genome
                                          sequence to be specified (see -g).
                                          Default: no limit.
-q, --quality-scores                    Weigh the reads based on their quality
                                          scores. This requires the genome
                                          sequence to be specified (see -g).
-o <file prefix>			Output files prefix. It can include path.
					  Default: same as sam file name

Source Code:
------------

The source code can be found in the src directory under the 
installation path.


Revision history
----------------
Version 2.0.0 (1/20/16)  - added TPM estimates for genes and isoforms
			 - added option to compute confidence intervals (bootstrapping)
                         - added option for reading alignments from standard input
			 - integrated IsoDE with IsoEM
			 - Added DE for isoform FPKMs and genes and isoforms TPMs 
Version 1.1.4 (12/18/15) - added --counts option to generate expected read counts and --endseq to 
			   handle data from end-sequencing protocols
Version 1.1.3 (10/11/15) - bug fix in handling CIGAR with indels in convert-iso-to-genome-coords
			 - bug fix related to hisat/hisat2 alignments
Version 1.1.1 (11/5/12)  - bug fix related to clipped read alignments (CIGAR with S field)
Version 1.1.0 (4/24/12)  - added support for alignments with insertions and deletions
Version 1.0.6 (8/12/11)  - extract-isoform-sequences-from-genome (see 
			   http://dna.engr.uconn.edu/software/IsoEM/README-SAMPLE.TXT)
			   generates transcripts in a randomized order
			 - isoviz generates a gtf with fpkm values
			 - added output file name option
Version 1.0.5 (5/08/11)  - bugfix related to paired read data
Version 1.0.4 (2/22/11)  - added polyATail option
                         - further memory and speed improvements
Version 1.0.3 (8/30/10)  - correct for annotated repeats
Version 1.0.2 (8/05/10)  - improved memory requirements for storing genome sequence
                         - added hexamer bias correction option
                         - added isoviz visualization tool
Version 1.0.1 (6/25/10)  - added support for mate pairs
                         - added support for max number of mismatches
                         - performance improvements
Version 1.0.0 (6/16/10)  - first public release


Contact
-------
For questions or suggestions regarding IsoEM2/IsoDE2 you can contact:

     Igor Mandric (imandric1@student.gsu.edu)
     Sahar Al Seesi (sahar@engr.uconn.edu)
     Ion Mandoiu (ion@engr.uconn.edu)
     Alex Zelikovsky (alexz@cs.gsu.edu)
