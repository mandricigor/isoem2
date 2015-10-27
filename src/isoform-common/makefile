all: preclean compile jar clean

preclean: 
	rm -f ../../lib/isoform-common-1.0.jar
	rm -rf bin
	
compile:
	mkdir bin 
	javac -cp "../lib/junit-4.8.1.jar" -d bin \
	src/main/java/edu/uconn/engr/dna/*/*.java ../isoform-common/src/main/java/edu/uconn/engr/dna/*/*/*.java  \
	src/main/java/edu/cornell/lassp/houle/RngPack/*.java 

jar:
	jar -cvf ../../lib/isoform-common-1.0.jar  -C bin edu

clean:
	rm -rf bin
	


