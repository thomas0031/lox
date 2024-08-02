package org.example.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.lox.TokenType.AND;
import static org.example.lox.TokenType.BANG;
import static org.example.lox.TokenType.BANG_EQUAL;
import static org.example.lox.TokenType.CLASS;
import static org.example.lox.TokenType.COMMA;
import static org.example.lox.TokenType.DOT;
import static org.example.lox.TokenType.ELSE;
import static org.example.lox.TokenType.EOF;
import static org.example.lox.TokenType.EQUAL;
import static org.example.lox.TokenType.EQUAL_EQUAL;
import static org.example.lox.TokenType.FALSE;
import static org.example.lox.TokenType.FOR;
import static org.example.lox.TokenType.FUN;
import static org.example.lox.TokenType.GREATER;
import static org.example.lox.TokenType.GREATER_EQUAL;
import static org.example.lox.TokenType.IDENTIFIER;
import static org.example.lox.TokenType.IF;
import static org.example.lox.TokenType.LEFT_BRACE;
import static org.example.lox.TokenType.LEFT_PAREN;
import static org.example.lox.TokenType.LESS;
import static org.example.lox.TokenType.LESS_EQUAL;
import static org.example.lox.TokenType.MINUS;
import static org.example.lox.TokenType.NIL;
import static org.example.lox.TokenType.NUMBER;
import static org.example.lox.TokenType.OR;
import static org.example.lox.TokenType.PLUS;
import static org.example.lox.TokenType.PRINT;
import static org.example.lox.TokenType.RETURN;
import static org.example.lox.TokenType.RIGHT_BRACE;
import static org.example.lox.TokenType.RIGHT_PAREN;
import static org.example.lox.TokenType.SEMICOLON;
import static org.example.lox.TokenType.SLASH;
import static org.example.lox.TokenType.STAR;
import static org.example.lox.TokenType.STRING;
import static org.example.lox.TokenType.SUPER;
import static org.example.lox.TokenType.THIS;
import static org.example.lox.TokenType.TRUE;
import static org.example.lox.TokenType.VAR;
import static org.example.lox.TokenType.WHILE;

public class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }


    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);

            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);

            case '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            }

            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;

            case '"' -> string();

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
            }
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }
}
