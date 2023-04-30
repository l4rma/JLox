package io.magelssen.jlox;

import io.magelssen.jlox.models.Token;
import org.junit.jupiter.api.Test;

class ScannerTest {
    @Test
    void shouldCreateTokens() {
        var program =
                "// this is a comment\n" +
                "(( )){} // grouping stuff\n" +
                "!*+-/=<> <= == // operators";
        var scanner = new Scanner(program);
        var tokens = scanner.scanTokens();
        for(Token token : tokens) {
            System.out.println(token);
        }
    }
}