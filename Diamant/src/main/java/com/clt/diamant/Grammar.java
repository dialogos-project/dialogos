package com.clt.diamant;

import java.util.Collection;

import com.clt.diamant.graph.search.GraphSearchResult;
import com.clt.diamant.graph.search.SearchResult;

public class Grammar implements IdentityObject {

    private String id;
    private String name;
    private String grammar = "";

    public Grammar(String name) {

        this(name, "root $input;\n $input = ;");
    }

    public Grammar(String name, String s) {

        this.name = name;
        this.grammar = s;
    }

    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        return this.getName();
    }

    public String getGrammar() {

        return this.grammar;
    }

    public void setGrammar(String grammar) {

        this.grammar = grammar;
    }

    public void validate(Collection<SearchResult> errors) {

        try {
            com.clt.srgf.Grammar.create(this.grammar);
        } catch (Exception exn) {
            errors.add(new GraphSearchResult(null, Resources.getString("Grammar")
                    + " \"" + this.name
                    + "\"", exn.getLocalizedMessage(),
                    SearchResult.Type.WARNING));
        }
    }

    public String getId() {

        return this.id;
    }

    public void setId(String id) {

        this.id = id;
    }
}
