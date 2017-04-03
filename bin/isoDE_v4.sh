#!/bin/bash

isoEMDir=/galaxy-prod/galaxy/tools-dependencies/bin/isoem2

START=$(date +%s)

echo

java -cp ${isoEMDir}/lib/argparse4j-0.4.1.jar:${isoEMDir}/lib/. IsoDE_Parse_Arg "$@"
status=$?

if [ $status -eq 12 ]; then
        echo "Either m or a must be present, not both! "
        exit 12
fi


old_IFS="$IFS"
IFS=$'\n'
lines=($(<param.txt)) # array
IFS="$old_IFS"


sam1_DIR=${lines[0]}     #boostraping path 1  bootstrap support B (default B = 95)
sam2_DIR=${lines[1]}     #booststrapping path 2
B=${lines[2]}            #bootstrap support (default B = 95)
DFC=${lines[3]}          #desired Fold Change (default FC=2)
out=${lines[4]}          #output file
pair=${lines[5]}         #pairing of ratio: matching or all pair


rm param.txt
#echo "end of parsing arguments"


#Access sub-directories

access_directories(){
# Passed args are sam1_DIR and sam2_DIR

        d1=$1    #$sam1_DIR
        d2=$2    #$sam2_DIR

        ext1=/
        g_dir=Genes
        r1=One_run
        g_one=Gene
	
        g1=$d1$g_dir    #genes estimations for the bootstrap of file1
	g2=$d2$g_dir	#genes estimations for the bootstrap of file2
	one_run_g1=$d1$r1$ext1$g_one	#genes estimations for single run of file1
	one_run_g2=$d2$r1$ext1$g_one	#genes estimations for single run of file2


	#echo "Get name of the file for one_run genes/isoforms estimates"
	
	ls $one_run_g1 > file_temp
	one_run_g1_est=`cat file_temp`
	rm file_temp 
	one_run_g1_est=$one_run_g1$ext1$one_run_g1_est

        ls $one_run_g2 > file_temp
        one_run_g2_est=`cat file_temp`
        rm file_temp 
        one_run_g2_est=$one_run_g2$ext1$one_run_g2_est


}


echo;

access_directories $sam1_DIR $sam2_DIR
#echo " END: Create paths to directories"

echo;


mem=`free | grep -o -e 'Mem:\s*[0-9]*' | grep -o -E '[0-9]+'`
mem=$((mem/2048))
if [ -n $mem ]
then
        maxMem=-Xmx${mem}M
        startMem=-Xms${mem}M
fi

echo " Computing P_min, P_max, FC support, 1/FC support and save it in a file"
java $startMem $maxMem -cp ${isoEMDir}/lib/. Log_Lower_Upper_Matching_Ratio $g1 $g2 $B $DFC $one_run_g1_est $one_run_g2_est $pair 


echo;

echo " Computing Exp FC"
java -cp ${isoEMDir}/lib/.  Exp_Log_FC $one_run_g1_est $one_run_g2_est
echo "END: Exp FC"

echo;

echo " Merge Pmin, Pmax, FC support with Exp FC"
java -cp ${isoEMDir}/lib/. Output_Matrix  $B $DFC $out 
echo "END: Merge Pmin, Pmax, FC support with Exp FC"

# delete the two temporary files used for merging and move output file into each input directories
rm Exp_log2_FC.txt
rm log2_FC_Pmin_Pmax.txt
cp $out $sam1_DIR
cp $out $sam2_DIR
rm $out

echo "End of script!!!"


END=$(date +%s)
elapsed=$(( $END - $START ))


echo Elapsed in secs: $elapsed

remainder="$(expr $elapsed % 3600)"
hours="$(expr $(expr $elapsed - $remainder) / 3600)"

seconds="$(expr $remainder % 60)"
minutes="$(expr $(expr $remainder - $seconds) / 60)"

echo "Elapsed time: $hours:$minutes:$seconds (h:mn:s)"





