package io.magelssen.jlox;

import static io.magelssen.jlox.models.TokenType.AND;
import static io.magelssen.jlox.models.TokenType.BANG;
import static io.magelssen.jlox.models.TokenType.BANG_EQUAL;
import static io.magelssen.jlox.models.TokenType.CLASS;
import static io.magelssen.jlox.models.TokenType.COMMA;
import static io.magelssen.jlox.models.TokenType.DOT;
import static io.magelssen.jlox.models.TokenType.ELSE;
import static io.magelssen.jlox.models.TokenType.EOF;
import static io.magelssen.jlox.models.TokenType.EQUAL;
import static io.magelssen.jlox.models.TokenType.EQUAL_EQUAL;
import static io.magelssen.jlox.models.TokenType.FALSE;
import static io.magelssen.jlox.models.TokenType.FOR;
import static io.magelssen.jlox.models.TokenType.FUN;
import static io.magelssen.jlox.models.TokenType.GREATER;
import static io.magelssen.jlox.models.TokenType.GREATER_EQUAL;
import static io.magelssen.jlox.models.TokenType.IDENTIFIER;
import static io.magelssen.jlox.models.TokenType.IF;
import static io.magelssen.jlox.models.TokenType.LEFT_BRACE;
import static io.magelssen.jlox.models.TokenType.LEFT_PAREN;
import static io.magelssen.jlox.models.TokenType.LESS;
import static io.magelssen.jlox.models.TokenType.LESS_EQUAL;
import static io.magelssen.jlox.models.TokenType.MINUS;
import static io.magelssen.jlox.models.TokenType.NIL;
import static io.magelssen.jlox.models.TokenType.NUMBER;
import static io.magelssen.jlox.models.TokenType.OR;
import static io.magelssen.jlox.models.TokenType.PLUS;
import static io.magelssen.jlox.models.TokenType.PRINT;
import static io.magelssen.jlox.models.TokenType.RETURN;
import static io.magelssen.jlox.models.TokenType.RIGHT_BRACE;
import static io.magelssen.jlox.models.TokenType.RIGHT_PAREN;
import static io.magelssen.jlox.models.TokenType.SEMICOLON;
import static io.magelssen.jlox.models.TokenType.SLASH;
import static io.magelssen.jlox.models.TokenType.STAR;
import static io.magelssen.jlox.models.TokenType.STRING;
import static io.magelssen.jlox.models.TokenType.SUPER;
import static io.magelssen.jlox.models.TokenType.THIS;
import static io.magelssen.jlox.models.TokenType.TRUE;
import static io.magelssen.jlox.models.TokenType.VAR;
import static io.magelssen.jlox.models.TokenType.WHILE;

import io.magelssen.jlox.models.Token;
import io.magelssen.jlox.models.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            this.start = this.current;
            scanToken();
        }
        this.tokens.add(new Token(EOF, "", null, line));
        return this.tokens;
    }

    private void scanToken() {
        char currentCharacter = advance();
        switch (currentCharacter) {
            // Single-character tokens.
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // One or two character tokens.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // Strings
            case '"': string(); break;

            // Comments
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            // White space
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                this.line++;
                break;

            default:
                if (isDigit(currentCharacter)) {
                    number();
                } else if (isAlpha(currentCharacter)) {
                    identifier();
                } else {
                    Lox.error(this.line, "Unexpected character.");
                    break;
                }
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = this.source.substring(this.start, this.current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(this.source.substring(start, current)));
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) return '\0';
        return this.source.charAt(this.current + 1);
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') this.line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(this.line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = this.source.substring(this.start + 1, this.current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (this.source.charAt(this.current) != expected) return false;

        this.current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }

    private char advance() {
        return this.source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return this.source.charAt(current);
    }



    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }
}
