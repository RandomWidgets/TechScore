# FLAGS
JFLAGS = -g -cp ..
JC = javac

# Objects
OBJECTS = AbstractFrame.class

ASSIS = Factory.class \
	PaneChangeListener.class \
	PaneChangeEvent.class

LIB     = ../regatta/Regatta.class \
	../dpxml/XMLTag.class \
	../dpxml/XMLTextTag.class \
	ICSAScorer.class

# Rules
default: TScoreGUI.class

TScoreGUI.class: TScoreGUI.java \
	$(OBJECTS) $(LIB) $(ASSIS) \
	RegattaIO.class
	$(JC) $(JFLAGS) TScoreGUI.java

DetailsPane.class:    DetailsPane.java AbstractPane.class $(LIB) $(ASSIS)
	$(JC) $(JFLAGS) DetailsPane.java

RacesPane.class:      RacesPane.java AbstractPane.class $(LIB) $(ASSIS)
	$(JC) $(JFLAGS) RacesPane.java

RotationsPane.class:  RotationsPane.java AbstractPane.class $(LIB) $(ASSIS)
	$(JC) $(JFLAGS) RotationsPane.java

FinishesPane.class: FinishesPane.java RaceSpinnerModel.class $(LIB) $(ASSIS)
	$(JC) $(JFLAGS) FinishesPane.java

RotationsDialog.class:RotationsDialog.java AbstractFrame.class $(LIB) $(ASSIS)
	$(JC) $(JFLAGS) RotationsDialog.java

Factory.class: Factory.java $(LIB)
	$(JC) $(JFLAGS) Factory.java

PaneChangeListener.class: PaneChangeListener.java
	$(JC) $(JFLAGS) PaneChangeListener.java

PaneChangeEvent.class: PaneChangeEvent.java
	$(JC) $(JFLAGS) PaneChangeEvent.java

RegattaIO.class: RegattaIO.java ../dpxml/XMLTag.class ../dpxml/XMLTextTag.class
	$(JC) $(JFLAGS) RegattaIO.java

RaceSpinnerModel.class: RaceSpinnerModel.java
	$(JC) $(JFLAGS) RaceSpinnerModel.java

AbstractFrame.class: AbstractFrame.java $(LIB)
	$(JC) $(JFLAGS) AbstractFrame.java

ICSAScorer.class: ICSAScorer.java
	$(JC) $(JFLAGS) ICSAScorer.java

# Other rules
clean:
	rm *.class
