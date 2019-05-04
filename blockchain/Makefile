all:
ifeq ($(OS),Windows_NT)
	#setting classpath
	export CLASSPATH=$CLASSPATH:.:hash/commons-codec-1.11.jar
	# assume windows
	javac -Xlint -g *.java
else
	# assume Linux
	javac -Xlint -g *.java
endif

clean:
	rm lib/*.class
	rm ./*.class

alltests: 
	java BlockchainTest Initial-State 8000
	java BlockchainTest Basic-Mining 8000
	java BlockchainTest Basic-MultiMining 8000
	java BlockchainTest Consensus 8000
	java BlockchainTest Reconnect-Consensus 8000
