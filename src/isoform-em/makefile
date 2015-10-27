all: preclean compile jar clean

preclean: 
	rm -f ../../lib/isoform-em-1.0.jar
	rm -rf bin
	
compile:
	mkdir bin 
	javac -cp "../../lib/junit-4.8.1.jar:../../lib/jopt-simple-3.2.jar:../../lib/log4j-1.2.14.jar:../../lib/isoform-common-1.0.jar:../../lib/isoform-accuracy-1.0.jar" \
	-d bin \
	src/main/java/edu/uconn/engr/dna/*/*.java src/main/java/edu/uconn/engr/dna/*/*/*.java 

jar: 
	jar -cvfe ../../lib/isoform-em-1.0.jar edu.uconn.engr.dna.isoem.Startup -C bin edu 

clean:
	rm -rf bin

