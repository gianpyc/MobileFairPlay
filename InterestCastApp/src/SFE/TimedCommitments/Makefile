# Makefile
#
# Makefile for SFE gradual-release timed commitments test code
#
# $Id: Makefile,v 1.1 2003/11/27 12:01:05 orip Exp $

JAVA_SRCS    = *.java
CLASS_FILES  = *.class

all:
	$(JAVAC) $(JAVAC_FLAGS) -classpath $(CLASSPATH) $(JAVA_SRCS)

clean:
	$(RM) $(CLASS_FILES) *~

# use the Java executables in $(JAVA_HOME)/bin, if $(JAVA_HOME) exists, e.g.
# from the environment, or from make's -D flag
ifdef JAVA_HOME
JAVA_BIN_PATH = ${JAVA_HOME}/bin/
else
JAVA_BIN_PATH =
endif

JAVAC = ${JAVA_BIN_PATH}javac
RMIC  = ${JAVA_BIN_PATH}rmic
JAR   = ${JAVA_BIN_PATH}jar
JAVAC_FLAGS = -source 1.4
CLASSPATH = .

# end of Makefile

