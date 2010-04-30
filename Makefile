# Makefile for TechScore
# DPV
# 2009-09-23

# Flags, edit these accordingly
JFLAGS  = -g -cp .
JC      = javac
JAR     = jar

#----------------------------------------------------------------------
# You should not need to edit anything below this line
#----------------------------------------------------------------------
VERSION = 1.3.5

# include dpxml/Makefile regatta/Makefile tscore/Makefile
default:
	$(MAKE) $(MAKEFLAGS) -C dpxml  && $(MAKE) $(MAKEFLAGS) -C regatta && \
	$(MAKE) $(MAKEFLAGS) -C nscore && $(MAKE) $(MAKEFLAGS) -C tscore
jar:
	jar cfev tscore-$(VERSION).jar tscore.TScoreGUI \
	tscore/*.class dpxml/*.class regatta/*.class tscore/img tscore/inc \
	org/sourceforge/jcalendarbutton/*.class

clean:
	rm dpxml/*.class regatta/*.class tscore/*.class nscore/*.class
