package modelCheckCTL.model;

/** A node in the CTL abstract syntax tree (AST). */
public final class Formula {
    public enum Op { ATOM, NOT, AND, OR, IMPLIES, EX, AX, EF, AF, EG, AG, EU, AU }
    public final Op op;
    public final String atom;
    public final Formula left, right;
    private Formula(Op op, String atom, Formula left, Formula right) {
        this.op=op; this.atom=atom; this.left=left; this.right=right;
    }
    public static Formula atom(String name) { return new Formula(Op.ATOM, name, null, null); }
    public static Formula unary(Op op, Formula child) { return new Formula(op, null, child, null); }
    public static Formula binary(Op op, Formula left, Formula right) { return new Formula(op, null, left, right); }
}
