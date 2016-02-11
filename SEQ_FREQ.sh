#!/bin/sh

isoEMDir=/home/software/public_html/IsoEM/isoem-1.1.5
if [ $1 == "auto" ]
   ${isoEMDir}/SEQ_FREQ-auto.sh
   exit 1
fi

if [ $# -ne 5 -a $# -ne 6 ]
then
	echo "Usage: [sam] referenceFasta [fastqFile|samFileInReferenceCoords] fragMean fragSD outputFile"
	echo "or"
	echo "Usage: auto [sam] referenceFasta [fastqFile|samFileInReferenceCoords] outputFile"
	exit 1
fi

if [ $# -eq 6 ]
then
   if [ "$1" != "sam" ]
      then
	  echo "Usage: [sam] referenceFasta [fastqFile|samFileInReferenceCoords] fragMean fragSD outputFile"
	  echo "or"
	  echo "Usage: auto [sam] referenceFasta [fastqFile|samFileInReferenceCoords] outputFile"
	  exit 1
      else
	Reference=$2
        SORTEDSAM=$3
        fragMean=$4
        fragSD=$5
	output=$6
      fi
else
  Reference=$1
  fastq=$2
  fragMean=$3
  fragSD=$4
  output=$5
fi

#1. generate GTF
echo "Generating GTF"
echo  $Reference 
echo  $Reference.gtf
${isoEMDir}/bin/fastaToGTF $Reference  

if [ $# -eq 5 ]
then
    SAM=tmp.SAM
    SORTEDSAM=tmp.SORTEDSAM
#2. build index    
    echo "Building BWA index"
    bwa index -a is $Reference 
#    bwa index -a bwtsw $Reference  # for long "genomes"
#3. map
    echo "Aligning reads"
    bwa bwasw -t 16 -f $SAM $Reference $fastq
#3.1 sort alignments
    echo "Sorting alignments"
    sort -k 1,1 $SAM > $SORTEDSAM
#    \rm -f $SAM
fi


#4. run isoem
echo "Estimating frequencies"
${isoEMDir}/bin/isoem  -G $Reference.GTF  -m $fragMean -d $fragSD $SORTEDSAM -o $output

#5. cleanup
mv ${output}.iso_estimates $output.seq_fpkm
rm -f ${output}.gene_estimates

#6. convert fpkm to frquencies 
cat $output.seq_fpkm | \
  awk '{n++; sum+= $2; id[n] = $1; fpkm[n] = $2} END{for(i=1; i<=n; i++) printf "%s\t%f\n", id[i], fpkm[i]/sum}' \
 > $output.seq_freq
