JFLAGS = -g -cp ..
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Regatta.java \
	RegattaListener.java \
	RegattaEvent.java \
	Race.java \
	Team.java \
	Rotation.java \
	Scorer.java \
	Sail.java \
	Finish.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	rm *.class
