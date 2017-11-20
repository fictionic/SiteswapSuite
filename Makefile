all: util siteswap notation transition main package

SOURCE_DIR=src

JAVAC_COMMAND=javac -d build -sourcepath ${SOURCE_DIR} -classpath build

package:
	jar cfe build/SiteswapSuite.jar siteswapsuite.Main -C build .

util:
	${JAVAC_COMMAND} ${SOURCE_DIR}/util/*.java

siteswap:
	${JAVAC_COMMAND} ${SOURCE_DIR}/siteswap/*.java

transition:
	${JAVAC_COMMAND} ${SOURCE_DIR}/transition/*.java

notation:
	${JAVAC_COMMAND} ${SOURCE_DIR}/notation/*.java

main:
	${JAVAC_COMMAND} ${SOURCE_DIR}/main/Argument.java src/main/ParsedArguments.java src/main/Main.java 

clean:
	rm -f build/siteswapsuite/*.class
	rm -f build/SiteswapSuite.jar

test:
	#${JAVAC_COMMAND} src/Test.java
	${JAVAC_COMMAND} src/ArgumentParser.java
