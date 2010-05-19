# Makefile for DPV's XML Library
JC     = javac
JFLAGS = -g -cp ..
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = XMLTag.java XMLTextTag.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm *.class
