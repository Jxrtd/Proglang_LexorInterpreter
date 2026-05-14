# LEXOR Interpreter Summary & Architecture

This document outlines the step-by-step process and architectural breakdown of the LEXOR interpreter, located in the `src/main/java/com/lexor/` directory. The project is divided into two main increments, establishing the foundational structure and extending it with interactive and logical capabilities.

## Architecture Overview

The LEXOR interpreter follows a classic compiler pipeline design pattern:
1.  **Lexical Analysis (Lexer)**: Scans raw source code characters and groups them into meaningful `Token`s.
2.  **Syntax Analysis (Parser)**: Reads the `Token`s and organizes them into an Abstract Syntax Tree (AST) using `Expr` and `Stmt` classes.
3.  **Semantic Analysis & Execution (Interpreter)**: Walks the AST, managing state via the `Environment`, and executes the program's logic.

---

## File Breakdown

### `com/lexor/lexer/` (Lexical Analysis)
*   **`TokenType.java`**: An enumeration defining every valid symbol, keyword, and literal type in the LEXOR language (e.g., `DECLARE`, `INT`, `PLUS`, `IDENTIFIER`).
*   **`Token.java`**: A simple data class representing a single chunk of code. It stores the `TokenType`, the raw text (`lexeme`), its literal value (if any, like a parsed integer), and the line number for error reporting.
*   **`Lexer.java`**: The core scanner.
    *   `scanTokens()`: Loops through the source string until the end of the file.
    *   `scanToken()`: A large switch statement that evaluates the current character to determine the token type. It handles single-character tokens (like `+`), two-character tokens (like `>=`), strings, numbers, and identifies reserved keywords.
    *   *Increment 1*: Handles basic structure keywords (`SCRIPT`, `START`, `END`), declarations, assignment (`=`), print separators (`&`, `$`), strings, numbers, and `%%` comments.
    *   *Increment 2*: Added support for arithmetic (`+`, `-`, `*`, `/`, `%`), logical operators (`AND`, `OR`, `NOT`), relational operators (`>`, `<`, `==`, `<>`), and the `SCAN` keyword.

### `com/lexor/ast/` (Abstract Syntax Tree)
*   **`Expr.java`**: The base class for all expressions (things that produce a value). It defines the `Visitor` interface for the interpreter to use. Contains inner classes for:
    *   `Literal`: Fixed values (numbers, strings, booleans).
    *   `Variable`: A reference to a variable name.
    *   `Assign`: Assigning a value to a variable (`a = 5`).
    *   *Increment 2 additions*: `Binary` (e.g., `a + b`, `x > y`) and `Unary` (e.g., `-5`, `NOT flag`).
*   **`Stmt.java`**: The base class for all statements (things that perform an action but don't return a value). Also defines a `Visitor` interface. Contains inner classes for:
    *   `Expression`: Wraps an `Expr` to be executed as a standalone statement.
    *   `Print`: Represents a `PRINT:` command, holding a list of expressions to concatenate.
    *   `Declare`: Represents variable declarations (`DECLARE INT x`), holding types, names, and optional initializers.
    *   *Increment 2 addition*: `Scan`, representing the `SCAN:` command to read user input into variables.

### `com/lexor/parser/` (Syntax Analysis)
*   **`Parser.java`**: Converts the flat list of `Token`s from the `Lexer` into a structured AST. It uses a technique called *Recursive Descent Parsing*.
    *   `parse()`: The main entry point. It enforces the overall structure (`SCRIPT AREA`, `START SCRIPT`, statements, `END SCRIPT`).
    *   `declaration() / varDeclaration()`: Handles variable declarations and ensures they only appear at the top of the script.
    *   `statement() / printStatement() / scanStatement()`: Routes to specific statement parsing logic.
    *   `expression() -> assignment() -> or() -> and() -> comparison() -> term() -> factor() -> unary() -> primary()`: This chain of methods parses expressions while enforcing **operator precedence**. For example, `factor` handles `*` and `/` before `term` handles `+` and `-`.

### `com/lexor/interpreter/` (Execution)
*   **`Environment.java`**: Acts as the memory of the interpreter. It maps variable names (Strings) to their current values and declared types.
    *   `define()`: Creates a new variable. It automatically assigns default "zero-values" (e.g., `0` for `INT`, `FALSE` for `BOOL`) if no initializer is provided.
    *   `assign()`: Updates a variable. It calls `validateType()` to strictly enforce LEXOR's static typing rules, throwing a runtime error if a mismatch occurs.
    *   `get()`: Retrieves a variable's value.
*   **`Interpreter.java`**: The core execution engine. It implements the `Visitor` interfaces defined in `Expr` and `Stmt`, defining what happens when the interpreter "visits" each type of node.
    *   `visitPrintStmt()`: Evaluates a list of expressions and prints them, handling the `$` newline character and formatting booleans to uppercase (`TRUE`/`FALSE`).
    *   `visitDeclareStmt()` & `visitAssignExpr()`: Updates the `Environment`.
    *   *Increment 2 additions*:
        *   `visitScanStmt()`: Uses `java.util.Scanner` to read input from the console, parses it into the correct data type based on the variable's declaration, and stores it in the `Environment`. Supports multiple comma-separated variables.
        *   `visitBinaryExpr()`: Contains the core logic for math (`+`, `-`, `*`, `/`, `%`), comparisons (`>`, `<`, `==`, `<>`), and logical operations (`AND`, `OR`). It handles type promotion (e.g., an `INT` and `FLOAT` operation results in a `Double`).
        *   `visitUnaryExpr()`: Handles numeric negation (`-`) and boolean negation (`NOT`).

### `com/lexor/core/` (Orchestration)
*   **`Main.java`**: The entry point of the application. It reads `program.lexor`, initializes the `Lexer`, passes the tokens to the `Parser`, and passes the resulting AST to the `Interpreter`.
*   **`LexorException.java`**: A custom runtime exception used throughout the pipeline to provide formatted error messages that include the line number where the failure occurred.

---

## Increment Feature Mapping

### Increment 1 Features
*   **Basic Structure**: Implemented in `Parser.parse()`, enforcing the `SCRIPT AREA`, `START SCRIPT`, and `END SCRIPT` tags.
*   **Comments**: Implemented in `Lexer.scanToken()`. When it sees `%%`, it advances the scanner to the end of the line, ignoring the text.
*   **Reserved Words**: Managed by the `keywords` map in `Lexer.java`.
*   **Variable Declaration & Assignment**: Handled by `Parser.varDeclaration()`, `Parser.assignment()`, and executed via `Environment.define()` and `Environment.assign()`. Includes default value assignment.
*   **Display (PRINT) & Concatenation**: Parsed in `Parser.printStatement()` (recognizing `&` and `$`) and executed in `Interpreter.visitPrintStmt()`.

### Increment 2 Features
*   **Read Input Data (SCAN)**: Added `SCAN` to `TokenType` and Lexer. Parsed via `Parser.scanStatement()` (handling comma-separated lists) and executed interactively via `Interpreter.visitScanStmt()`.
*   **Arithmetic Operations**: Added tokens (`PLUS`, `MINUS`, `STAR`, `SLASH`, `PERCENT`). Parsed with correct precedence in `Parser.term()` and `Parser.factor()`. Executed with type promotion in `Interpreter.visitBinaryExpr()`.
*   **Logical & Relational Operations**: Added operators (`AND`, `OR`, `>`, `<`, `==`, `<>`). Parsed in `Parser.and()`, `Parser.or()`, and `Parser.comparison()`. Evaluated to boolean results in `Interpreter.visitBinaryExpr()`.
*   **Unary Operators**: Added support for numeric negation (`-`) and logical negation (`NOT`) in `Parser.unary()` and `Interpreter.visitUnaryExpr()`.
