all: preclean compile jar clean

preclean: 
	rm -f ../../lib/isoform-accuracy-1.0.jar
	rm -rf bin
	
compile:
	mkdir bin 
	javac -cp "../../lib/junit-4.8.1.jar:../../lib/jfreechart-1.0.13.jar:../../lib/jcommon-1.0.15.jar:../../lib/isoform-common-1.0.jar:../../lib/commons-math-2.0.jar" \
	-d bin \
	src/main/java/edu/uconn/engr/dna/*/*/*.java  \

jar:
	jar -cvf ../../lib/isoform-accuracy-1.0.jar -C bin edu

clean:
	rm -rf bin

