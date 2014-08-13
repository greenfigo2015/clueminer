/* Generated By:JavaCC: Do not edit this line. NewickParser.java */
package org.clueminer.importer.newick;

import java.util.*;

@SuppressWarnings("unchecked")
public class NewickParser implements NewickParserConstants {

    /**
     * a very simple tree node class
     */
    public static class TreeNode {

        private List<TreeNode> children;
        private double weight;
        private String name;

        public TreeNode() {
            this.children = new ArrayList<TreeNode>();
        }

        public void addChild(TreeNode child) {
            this.children.add(child);
        }

        public List<TreeNode> getChildren() {
            return this.children;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public List<TreeNode> getLeaves() {
            List<TreeNode> leaves = new ArrayList<TreeNode>();
            if (children.isEmpty()) {
                leaves.add(this);
            } else {
                for (TreeNode child : children) {
                    leaves.addAll(child.getLeaves());
                }
            }
            return leaves;
        }
    }

    /**
     * just a test
     */
    public static void main(String args[]) throws Exception {
        TreeNode root = new NewickParser(System.in).tree();
        System.out.println(root.getName() + ":" + root.getChildren().size());
        for (TreeNode node : root.getLeaves()) {
            System.out.println("leaf: " + node.getName());
        }
    }

    final public TreeNode tree() throws ParseException {
        TreeNode root = null;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case LPAR:
            case IDENTIFIER:
            case SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING:
                root = branch();
                break;
            default:
                jj_la1[0] = jj_gen;
                ;
        }
        jj_consume_token(SEMICOLON);
        jj_consume_token(0);
        if (root == null) {
            root = new TreeNode();
        }
        {
            if (true) {
                return root;
            }
        }
        throw new Error("Missing return statement in function");
    }

    final private TreeNode branch() throws ParseException {
        Token t;
        TreeNode node;
        node = subtree();
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case COLON:
                jj_consume_token(COLON);
                t = jj_consume_token(REALNUMBER);
                node.setWeight(Double.parseDouble(t.image));
                break;
            default:
                jj_la1[1] = jj_gen;
                ;
        }
        {
            if (true) {
                return node;
            }
        }
        throw new Error("Missing return statement in function");
    }

    final private TreeNode subtree() throws ParseException {
        TreeNode node = null;
        Token t;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case IDENTIFIER:
                t = jj_consume_token(IDENTIFIER);
                node = new TreeNode();
                node.setName(t.image);
                break;
            case SINGLE_QUOTED_STRING:
                t = jj_consume_token(SINGLE_QUOTED_STRING);
                node = new TreeNode();
                node.setName(t.image.substring(1, t.image.length() - 1));
                break;
            case DOUBLE_QUOTED_STRING:
                t = jj_consume_token(DOUBLE_QUOTED_STRING);
                node = new TreeNode();
                node.setName(t.image.substring(1, t.image.length() - 1));
                break;
            case LPAR:
                node = internal();
                break;
            default:
                jj_la1[2] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
        }
        {
            if (true) {
                return node;
            }
        }
        throw new Error("Missing return statement in function");
    }

    final private TreeNode internal() throws ParseException {
        Token t;
        TreeNode node = new TreeNode();
        TreeNode child;
        jj_consume_token(LPAR);
        child = branch();
        node.addChild(child);
        label_1:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case COMMA:
        ;
                    break;
                default:
                    jj_la1[3] = jj_gen;
                    break label_1;
            }
            jj_consume_token(COMMA);
            child = branch();
            node.addChild(child);
        }
        jj_consume_token(RPAR);
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case IDENTIFIER:
            case SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING:
                switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case IDENTIFIER:
                        t = jj_consume_token(IDENTIFIER);
                        node.setName(t.image);
                        break;
                    case SINGLE_QUOTED_STRING:
                        t = jj_consume_token(SINGLE_QUOTED_STRING);
                        node.setName(t.image.substring(1, t.image.length() - 1));
                        break;
                    case DOUBLE_QUOTED_STRING:
                        t = jj_consume_token(DOUBLE_QUOTED_STRING);
                        node.setName(t.image.substring(1, t.image.length() - 1));
                        break;
                    default:
                        jj_la1[4] = jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
                break;
            default:
                jj_la1[5] = jj_gen;
                ;
        }
        {
            if (true) {
                return node;
            }
        }
        throw new Error("Missing return statement in function");
    }

    /**
     * Generated Token Manager.
     */
    public NewickParserTokenManager token_source;
    SimpleCharStream jj_input_stream;
    /**
     * Current token.
     */
    public Token token;
    /**
     * Next token.
     */
    public Token jj_nt;
    private int jj_ntk;
    private int jj_gen;
    final private int[] jj_la1 = new int[6];
    static private int[] jj_la1_0;

    static {
        jj_la1_init_0();
    }

    private static void jj_la1_init_0() {
        jj_la1_0 = new int[]{0x3880, 0x8000, 0x3880, 0x200, 0x3800, 0x3800,};
    }

    /**
     * Constructor with InputStream.
     */
    public NewickParser(java.io.InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    public NewickParser(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source = new NewickParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream) {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.InputStream stream, String encoding) {
        try {
            jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    public NewickParser() {

    }

    /**
     * Constructor.
     */
    public NewickParser(java.io.Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new NewickParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    /**
     * Constructor with generated Token Manager.
     */
    public NewickParser(NewickParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    /**
     * Reinitialise.
     */
    public void ReInit(NewickParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 6; i++) {
            jj_la1[i] = -1;
        }
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) {
            token = token.next;
        } else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    /**
     * Get the next Token.
     */
    final public Token getNextToken() {
        if (token.next != null) {
            token = token.next;
        } else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    /**
     * Get the specific Token.
     */
    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) {
                t = t.next;
            } else {
                t = t.next = token_source.getNextToken();
            }
        }
        return t;
    }

    private int jj_ntk() {
        if ((jj_nt = token.next) == null) {
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        } else {
            return (jj_ntk = jj_nt.kind);
        }
    }

    private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
    private int[] jj_expentry;
    private int jj_kind = -1;

    /**
     * Generate ParseException.
     */
    public ParseException generateParseException() {
        jj_expentries.clear();
        boolean[] la1tokens = new boolean[16];
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 6; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.add(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.get(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    /**
     * Enable tracing.
     */
    final public void enable_tracing() {
    }

    /**
     * Disable tracing.
     */
    final public void disable_tracing() {
    }

}
