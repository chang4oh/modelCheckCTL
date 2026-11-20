package modelCheckCTL.util;

import modelCheckCTL.model.Formula;
import modelCheckCTL.model.Formula.Op;
import java.util.*;

/** Recursive-descent CTL parser with explicit precedence and detailed positions. */
public final class FormulaParser {
    public static final class SyntaxException extends Exception {
        public SyntaxException(String message) { super(message); }
    }
    private record Token(String value, int column) { }
    private final List<Token> tokens;
    private int index = 0;
    private FormulaParser(String expression) throws SyntaxException { tokens = lex(expression); }
    public static Formula parse(String expression) throws SyntaxException {
        FormulaParser p = new FormulaParser(expression);
        Formula f = p.implication();
        if (p.hasNext()) throw p.error("Unexpected token '" + p.peek() + "'");
        return f;
    }
    private Formula implication() throws SyntaxException {
        Formula a = disjunction();
        if (take("->")) return Formula.binary(Op.IMPLIES, a, implication());
        return a;
    }
    private Formula disjunction() throws SyntaxException {
        Formula a = conjunction();
        while (take("or")) a = Formula.binary(Op.OR, a, conjunction());
        return a;
    }
    private Formula conjunction() throws SyntaxException {
        Formula a = unary();
        while (take("and")) a = Formula.binary(Op.AND, a, unary());
        return a;
    }
    private Formula unary() throws SyntaxException {
        if (take("not")) return Formula.unary(Op.NOT, unary());
        for (String kw : List.of("EX", "AX", "EF", "AF", "EG", "AG")) {
            if (take(kw)) return Formula.unary(Op.valueOf(kw), unary());
        }
        if (hasNext() && (peek().equals("E") || peek().equals("A"))) {
            String kind = advance();
            String opening = peek();
            if (!take("(") && !take("[")) throw error("Expected '(' or '[' after " + kind);
            Formula first = implication();
            expect("U");
            Formula second = implication();
            expect(opening.equals("[") ? "]" : ")");
            return Formula.binary(kind.equals("E") ? Op.EU : Op.AU, first, second);
        }
        if (take("(")) {
            Formula inside = implication();
            expect(")");
            return inside;
        }
        if (hasNext() && peek().matches("[A-Za-z0-9_]+") && !peek().equals("U")) {
            return Formula.atom(advance());
        }
        throw error("Expected atomic proposition, unary operator, or '(' ");
    }
    private boolean hasNext() { return index < tokens.size(); }
    private String peek() { return hasNext() ? tokens.get(index).value() : "<end>"; }
    private String advance() { return tokens.get(index++).value(); }
    private boolean take(String val) { if (peek().equals(val)) { index++; return true; } return false; }
    private void expect(String val) throws SyntaxException { if (!take(val)) throw error("Expected '" + val + "' but got '" + peek() + "'"); }
    private SyntaxException error(String msg) {
        int column = hasNext() ? tokens.get(index).column() : (tokens.isEmpty() ? 1 : tokens.get(tokens.size()-1).column()+1);
        return new SyntaxException("Column " + column + ": " + msg);
    }
    private static List<Token> lex(String s) throws SyntaxException {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < s.length();) {
            if (Character.isWhitespace(s.charAt(i))) { i++; continue; }
            char c = s.charAt(i);
            if (s.startsWith("->", i)) { result.add(new Token("->", i+1)); i+=2; continue; }
            if ("()[]".indexOf(c)>=0) { result.add(new Token(""+c,i+1)); i++; continue; }
            boolean wordStart = i == 0 || !Character.isLetterOrDigit(s.charAt(i-1));
            boolean matched = false;
            for (String op : List.of("not", "and", "or")) {
                if (s.startsWith(op,i) && (wordStart || s.charAt(i-1)==')') &&
                    (i+op.length()==s.length() || !Character.isLetterOrDigit(s.charAt(i+op.length())))) {
                    result.add(new Token(op,i+1)); i+=op.length(); matched = true; break;
                }
            }
            if (matched) continue;
            for (String op : List.of("AX", "EX", "AF", "EF", "AG", "EG")) {
                if (s.startsWith(op,i)) { result.add(new Token(op,i+1)); i+=2; matched=true; break; }
            }
            if (matched) continue;
            if (c=='U' || c=='A' || c=='G' || c=='F' || c=='X' ||
                (c=='E' && (i+1==s.length() || s.charAt(i+1)=='(' || s.charAt(i+1)=='['))) {
                // Reserved uppercase CTL syntax letters; U also separates pUq without spaces.
                result.add(new Token(""+c,i+1)); i++; continue;
            }
            if (Character.isLetterOrDigit(c) || c=='_') {
                int start = i++;
                while (i<s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i)=='_') &&
                    "UAEGFX".indexOf(s.charAt(i))<0) i++;
                result.add(new Token(s.substring(start,i),start+1)); continue;
            }
            throw new SyntaxException("Column " + (i+1) + ": Unexpected character '" + c + "'");
        }
        return result;
    }
}
