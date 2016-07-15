#!/bin/bash

rootPath=/home/projects/isoem2/isoem-b
toolpath=${rootPath}/bin
isoDEPath=${rootPath}/IsoDE-1.0.0/bin
supportCalcPath=${rootPath}/calc
fpkmGeneCommand=""
fpkmIsoformCommand=""
tpmGeneCommand=""  
tpmIsoformCommand=""


arg=($*)
i=0
while [ $i -lt $# ]
do
        a=${arg[i]}
	if [ "$a" == "-c1" ]; then
		fpkmGeneCommand="$fpkmGeneCommand -c1"
		fpkmIsoformCommand="$fpkmIsoformCommand -c1"
		tpmGeneCommand="$tpmGeneCommand -c1"
		tpmIsoformCommand="$tpmIsoformCommand -c1"
		
		((i++))
		 a=${arg[i]}
		rep=1
		while [[  `expr index "$a" "/"` -ne 0  && $i -lt $# ]]
		do
			condition1File=$a
			${toolpath}/convertToisoDEFormatWithIsoforms $condition1File c1_rep${rep}

			fpkmGeneCommand="$fpkmGeneCommand c1_rep${rep}_fpkm_G"
			fpkmIsoformCommand="$fpkmIsoformCommand c1_rep${rep}_fpkm_I"
			tpmGeneCommand="$tpmGeneCommand c1_rep${rep}_tpm_G"
			tpmIsoformCommand="$tpmIsoformCommand c1_rep${rep}_tpm_I"
			((rep++))

			((i++))
			 a=${arg[i]}
		done
        elif [ "$a" == "-c2" ]; then 
		fpkmGeneCommand="$fpkmGeneCommand -c2"
		fpkmIsoformCommand="$fpkmIsoformCommand -c2"
		tpmGeneCommand="$tpmGeneCommand -c2"
		tpmIsoformCommand="$tpmIsoformCommand -c2"

		((i++))
		 a=${arg[i]}
		rep=1
		while [[  `expr index "$a" "/"` -ne 0 &&  $i -lt $# ]]
                do
			condition1File=$a
			#echo $condition1File
			${toolpath}/convertToisoDEFormatWithIsoforms $condition1File c2_rep${rep}

			fpkmGeneCommand="$fpkmGeneCommand c2_rep${rep}_fpkm_G"
			fpkmIsoformCommand="$fpkmIsoformCommand c2_rep${rep}_fpkm_I"
			tpmGeneCommand="$tpmGeneCommand c2_rep${rep}_tpm_G"
			tpmIsoformCommand="$tpmIsoformCommand c2_rep${rep}_tpm_I"
			((rep++))

                     ((i++))
                     a=${arg[i]}
                done
	elif [ "$a" == "-pval" ]; then 
		((i++))
		pval=${arg[i]}
		((i++))

	

	elif [ "$a" == "-out" ]; then 
		((i++))
		out_prefix=${arg[i]}
		((i++))


	elif [ "$a" == "-geneTPMout" ]; then 
		((i++))
		geneTPMout_file=${arg[i]}
		((i++))

	elif [ "$a" == "-isoFPKMout" ]; then 
		((i++))
		isoFPKMout_file=${arg[i]}
		((i++))

	elif [ "$a" == "-isoTPMout" ]; then 
		((i++))
		isoTPMout_file=${arg[i]}
		((i++))
	else

		((i++))
	fi

done

support=`java -cp ${supportCalcPath} support 200 200 $pval`
fpkmGeneCommand="$fpkmGeneCommand -b $support"
fpkmIsoformCommand="$fpkmIsoformCommand -b $support"
tpmGeneCommand="$tpmGeneCommand -b $support"
tpmIsoformCommand="$tpmIsoformCommand -b $support"

fpkmGeneCommand="$fpkmGeneCommand -dfc 2"
fpkmIsoformCommand="$fpkmIsoformCommand -dfc 2"
tpmGeneCommand="$tpmGeneCommand -dfc 2"
tpmIsoformCommand="$tpmIsoformCommand -dfc 2"
 

geneFPKMout_file=${out_prefix}_geneFPKM
geneTPMout_file=${out_prefix}_geneTPM
isoFPKMout_file=${out_prefix}_isoFPKM
isoTPMout_file=${out_prefix}_isoTPM


mkdir fpkm_G
cd fpkm_G
mv ../c*_fpkm_G .
${isoDEPath}/isodecalls $fpkmGeneCommand -out "output.txt"
#awk '{for (f=1; f<=NF; f++) {if (f == NF)  printf "%s",$f; else printf "%s\t", $f }; print ""}' Bootstrap_Merge1_DIR/output.txt | sed 1,1d > ${geneFPKMout_file}
awk '{if ($6 == 0 && $7 == 0) {two="NDE";} else {two=$2;} print $1 "\t" one "\t" two "\t" $4 "\t" $5 "\t" $6 "\t" $7}' Bootstrap_Merge1_DIR/output.txt > ${geneFPKMout_file}

cd ..
 
mkdir fpkm_I
cd fpkm_I
mv ../c*_fpkm_I .
${isoDEPath}/isodecalls $fpkmIsoformCommand -out "output.txt"
awk '{if ($6 == 0 && $7 == 0) {two="NDE";} else {two=$2;} print $1 "\t" two "\t"  $5 "\t" $6 "\t" $7}' Bootstrap_Merge1_DIR/output.txt  > ${isoFPKMout_file}
cd ..


mkdir tpm_G
cd tpm_G
mv ../c*_tpm_G .
${isoDEPath}/isodecalls $tpmGeneCommand -out "output.txt"
awk '{if ($6 == 0 && $7 == 0) {two="NDE";} else {two=$2;} print $1 "\t" two "\t"  $5 "\t" $6 "\t" $7}'  Bootstrap_Merge1_DIR/output.txt  > ${geneTPMout_file}
cd ..

mkdir tpm_I
cd tpm_I
mv ../c*_tpm_I .
${isoDEPath}/isodecalls $tpmIsoformCommand -out "output.txt"
awk '{if ($6 == 0 && $7 == 0) {two="NDE";} else {two=$2;} print $1 "\t" two "\t"  $5 "\t" $6 "\t" $7}'  Bootstrap_Merge1_DIR/output.txt  > ${isoTPMout_file}

cd ..

 
#cleanup
#rm -fr fpkm_G fpkm_I tpm_G cd tpm_I

