# Java compiler
JAVAC = javac
# Java runtime
JAVA = java
# Source directory
SRC_DIR = src/main/java
# Output directory for compiled classes
OUT_DIR = target/classes
# Main class
MAIN_CLASS = org.example.lox.Lox
# Find all Java source files
SOURCES := $(shell find $(SRC_DIR) -name '*.java')

# Default target
all: compile

# Compile all Java sources together
compile:
	@mkdir -p $(OUT_DIR)
	$(JAVAC) -d $(OUT_DIR) $(SOURCES)

# Run the application
run: compile
	$(JAVA) -cp $(OUT_DIR) $(MAIN_CLASS)

# Generate AST classes
generate-ast: compile
	$(JAVA) -cp $(OUT_DIR) org.example.tool.GenerateAst $(SRC_DIR)/org/example/lox

# Clean compiled files
clean:
	rm -rf $(OUT_DIR)

.PHONY: all compile run generate-ast clean
