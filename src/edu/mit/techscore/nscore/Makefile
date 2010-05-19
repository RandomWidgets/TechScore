# Makefile for Navy Scoring compatibility tools (DPV)
JC     = javac
JFLAGS = -g -cp ..
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = NavyScoreLoader.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm *.class
