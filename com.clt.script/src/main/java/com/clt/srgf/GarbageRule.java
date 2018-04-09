package com.clt.srgf;

import java.io.PrintWriter;

class GarbageRule extends Rule {

    public GarbageRule() {
        super(false, false, Grammar.GARBAGE, new Garbage());
    }

    @Override
    public String getGlobalName() {

        return this.getName();
    }

    @Override
    public void export(PrintWriter w, Grammar.Format format) {

        switch (format) {
            case SRGF:
            case GRXML:
                // do nothing. $GARBAGE is predefined
                break;
            case TEMIC:
                // delegate to the TEMIC builtin garbage model
                w.println("<GARBAGE> = %unknowns ;");
                break;
            case JSGF:
            case JSGFwithGarbage:
                // delegate to the JSGF builtin garbage model
                // TIMO: this rule is not implemented in Sphinx-4's JSGF
                //w.println("<GARBAGE> = <garbage> ;");
                break;
            case LH:
                // delegate to the builtin garbage model
                w.println("<GARBAGE> : <...> ;");
                break;
            case VOCON:
            case VOCON_G:
                // delegate to the builtin garbage model
                // w.println("<GARBAGE> : <...> ;");

                // The builtin garbage model will match any number of words.
                // We therefore handle $GARBAGE in the Rulename class
                break;
            case NGSL:
                // w.println("GARBAGE:filler ()");
                break;
            case EXTLAT:
                w.println("# name");
                w.println("SUBLAT=GARBAGE");
                w.println("SUBLATTYPE=VP-WordClass");
                w.println("# number of nodes and links");
                w.println("N=3 L=2");
                w.println("# nodes");
                w.println("I=0 W=!NULL");
                w.println("I=1 W=<oov>");
                w.println("I=2 W=!NULL");
                w.println("# links");
                w.println("J=0 S=0 E=1 l=0.00 C=0.00");
                w.println("J=0 S=1 E=2 l=0.00 C=0.00");
                break;
            default:
                throw new IllegalArgumentException("Unknown export format");
        }
    }
}
