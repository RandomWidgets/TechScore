# Makefile for TechScore
# DPV
# 2009-09-23

# Flags, edit these accordingly
JFLAGS  = -g -cp .
JC      = javac
JAR     = jar
VERSION = 1.3.5

#----------------------------------------------------------------------
# You should not need to edit anything below this line
#----------------------------------------------------------------------

# include dpxml/Makefile regatta/Makefile tscore/Makefile
.PHONY:	doc

default:
	$(MAKE) $(MAKEFLAGS) -C dpxml  && $(MAKE) $(MAKEFLAGS) -C regatta && \
	$(MAKE) $(MAKEFLAGS) -C nscore && $(MAKE) $(MAKEFLAGS) -C tscore
jar:
	jar cfev tscore-$(VERSION).jar tscore.TScoreGUI \
	tscore/*.class dpxml/*.class regatta/*.class tscore/img tscore/inc \
	org/sourceforge/jcalendarbutton/*.class

clean:
	rm dpxml/*.class regatta/*.class tscore/*.class nscore/*.class

doc:
	rm -r doc; \
	javadoc -classpath . -doctitle "TechScore $(VERSION) Documentation" \
		-footer "Written by Dayan Paez" -package -d doc tscore regatta dpxml nscore
