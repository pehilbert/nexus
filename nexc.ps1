javac -d ./bin ./src/*.java ./src/codegen/*.java ./src/parser/*.java ./src/tokenizer/*.java
java -cp ./bin Nexus $args