all: preclean compile jar clean

preclean: 
	rm -f ../../lib/SimTools.jar
	rm -rf bin
	
compile:
	mkdir bin 
	javac -d bin src/main/java/edu/uconn/engr/dna/*/*.java 

jar: 
	jar -cvfe ../../lib/SimTools.jar edu.uconn.engr.dna.SimTools.main.FastatoGTF -C bin edu 

clean:
	rm -rf bin

