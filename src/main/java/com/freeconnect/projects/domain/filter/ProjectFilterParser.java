package com.freeconnect.projects.domain.filter;

import com.freeconnect.projects.domain.filter.ProjectFilterExpression.Always;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression.And;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression.Comparison;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression.Operator;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression.Or;
import com.freeconnect.shared.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Parser del lenguaje de filtros de proyectos.
 *
 * <p><b>Patron: Interpreter (parser).</b> Gramatica soportada:</p>
 * <pre>
 * expr    := term (OR term)*
 * term    := factor (AND factor)*
 * factor  := '(' expr ')' | comparison
 * comparison := field op value
 * field   := category | modality | status | budget | skill | title | q | description
 * op      := '=' | '!=' | '&gt;' | '&gt;=' | '&lt;' | '&lt;=' | contains
 * </pre>
 * <p>Ejemplo: {@code budget >= 1000 AND modality = REMOTE}</p>
 */
public final class ProjectFilterParser {

    private static final Set<String> FIELDS = Set.of("category", "modality", "status", "budget", "skill",
            "title", "q", "description");
    private static final Set<String> TEXT_FIELDS = Set.of("title", "q", "description");
    private static final Set<String> ENUM_FIELDS = Set.of("category", "modality", "status");
    private static final Set<String> NUMERIC_FIELDS = Set.of("budget");

    private final List<Token> tokens;
    private int position;

    private ProjectFilterParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static ProjectFilterExpression parse(String expression) {
        if (expression == null || expression.isBlank()) {
            return new Always(true);
        }
        ProjectFilterParser parser = new ProjectFilterParser(tokenize(expression));
        ProjectFilterExpression result = parser.parseOr();
        if (!parser.isAtEnd()) {
            throw new ValidationException("Expresion de filtro invalida: tokens sobrantes");
        }
        return result;
    }

    private ProjectFilterExpression parseOr() {
        ProjectFilterExpression left = parseAnd();
        while (match(TokenType.OR)) {
            left = new Or(left, parseAnd());
        }
        return left;
    }

    private ProjectFilterExpression parseAnd() {
        ProjectFilterExpression left = parsePrimary();
        while (match(TokenType.AND)) {
            left = new And(left, parsePrimary());
        }
        return left;
    }

    private ProjectFilterExpression parsePrimary() {
        if (match(TokenType.LPAREN)) {
            ProjectFilterExpression inner = parseOr();
            if (!match(TokenType.RPAREN)) {
                throw new ValidationException("Falta parentesis de cierre en el filtro");
            }
            return inner;
        }
        return parseComparison();
    }

    private ProjectFilterExpression parseComparison() {
        String field = consume(TokenType.WORD, "Se esperaba un campo de filtro").value()
                .toLowerCase(Locale.ROOT);
        if (!FIELDS.contains(field)) {
            throw new ValidationException("Campo de filtro desconocido: " + field);
        }
        Token operatorToken = advance();
        Operator operator = toOperator(operatorToken);
        validateOperator(field, operator);
        Token value = advance();
        if (value.type() != TokenType.WORD && value.type() != TokenType.TEXT) {
            throw new ValidationException("Se esperaba un valor para el filtro " + field);
        }
        return new Comparison(field, operator, value.value());
    }

    private void validateOperator(String field, Operator operator) {
        if (ENUM_FIELDS.contains(field) || TEXT_FIELDS.contains(field)) {
            if (operator != Operator.EQ && operator != Operator.NE && operator != Operator.CONTAINS) {
                throw new ValidationException("Operador no permitido para el campo " + field);
            }
        }
        if (NUMERIC_FIELDS.contains(field) && operator == Operator.CONTAINS) {
            throw new ValidationException("El campo budget no admite el operador contains");
        }
    }

    private Operator toOperator(Token token) {
        if (token.type() == TokenType.OPERATOR) {
            return switch (token.value()) {
                case "=" -> Operator.EQ;
                case "!=" -> Operator.NE;
                case ">" -> Operator.GT;
                case ">=" -> Operator.GTE;
                case "<" -> Operator.LT;
                case "<=" -> Operator.LTE;
                default -> throw new ValidationException("Operador desconocido: " + token.value());
            };
        }
        if (token.type() == TokenType.WORD && "contains".equalsIgnoreCase(token.value())) {
            return Operator.CONTAINS;
        }
        throw new ValidationException("Se esperaba un operador de comparacion");
    }

    private boolean match(TokenType type) {
        if (peek().type() == type) {
            position++;
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (peek().type() != type) {
            throw new ValidationException(message);
        }
        return advance();
    }

    private Token advance() {
        if (isAtEnd()) {
            throw new ValidationException("Expresion de filtro incompleta");
        }
        return tokens.get(position++);
    }

    private Token peek() {
        return tokens.get(position);
    }

    private boolean isAtEnd() {
        return position >= tokens.size() || peek().type() == TokenType.EOF;
    }

    private static List<Token> tokenize(String input) {
        List<Token> result = new ArrayList<>();
        int index = 0;
        while (index < input.length()) {
            char current = input.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }
            if (current == '(') {
                result.add(new Token(TokenType.LPAREN, "("));
                index++;
                continue;
            }
            if (current == ')') {
                result.add(new Token(TokenType.RPAREN, ")"));
                index++;
                continue;
            }
            if (current == '"') {
                int closing = input.indexOf('"', index + 1);
                if (closing < 0) {
                    throw new ValidationException("Cadena sin comillas de cierre en el filtro");
                }
                result.add(new Token(TokenType.TEXT, input.substring(index + 1, closing)));
                index = closing + 1;
                continue;
            }
            if (current == '!' || current == '>' || current == '<' || current == '=') {
                if (index + 1 < input.length() && input.charAt(index + 1) == '=') {
                    result.add(new Token(TokenType.OPERATOR, input.substring(index, index + 2)));
                    index += 2;
                } else if (current == '!') {
                    throw new ValidationException("Operador '!' invalido, use '!='");
                } else {
                    result.add(new Token(TokenType.OPERATOR, String.valueOf(current)));
                    index++;
                }
                continue;
            }
            int start = index;
            while (index < input.length()) {
                char c = input.charAt(index);
                if (Character.isWhitespace(c) || c == '(' || c == ')' || c == '"' || c == '='
                        || c == '!' || c == '>' || c == '<') {
                    break;
                }
                index++;
            }
            String word = input.substring(start, index);
            if ("AND".equalsIgnoreCase(word)) {
                result.add(new Token(TokenType.AND, word));
            } else if ("OR".equalsIgnoreCase(word)) {
                result.add(new Token(TokenType.OR, word));
            } else {
                result.add(new Token(TokenType.WORD, word));
            }
        }
        result.add(new Token(TokenType.EOF, ""));
        return result;
    }

    private enum TokenType {
        WORD,
        TEXT,
        OPERATOR,
        AND,
        OR,
        LPAREN,
        RPAREN,
        EOF
    }

    private record Token(TokenType type, String value) {
    }
}
