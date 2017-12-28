package com.clt.diamant.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.diamant.ExecutionLogger;
import com.clt.diamant.IdMap;
import com.clt.diamant.InputCenter;
import com.clt.diamant.Mapping;
import com.clt.diamant.Resources;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.ReturnNode;
import com.clt.diamant.graph.search.GraphSearchResult;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.script.Environment;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.util.StringTools;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class Procedure
    extends Graph {

  private List<Slot> parameters = new ArrayList<Slot>();
  private List<Slot> returnvars = new ArrayList<Slot>();

  private transient Map<Slot, Value> arguments = null;
  private transient Map<Slot, Value> returns = null;


  public Procedure(GraphOwner owner) {

    super(owner);
  }


  public SubGraph toGraph() {

    SubGraph result = new SubGraph(null);

    Mapping map = new Mapping();

    for (Slot v : this.parameters) {
      Slot newVar = v.clone(map);
      result.getVariables().add(newVar);
    }

    for (Slot v : this.returnvars) {
      Slot newVar = v.clone(map);
      result.getVariables().add(newVar);
    }

    result.copy(this, map);
    result.update(map);

    return result;
  }


  @Override
  public Graph clone(Mapping map) {

    Procedure g = new Procedure(null);

    for (Slot v : this.parameters) {
      Slot newVar = v.clone(map);
      g.parameters.add(newVar);
    }

    for (Slot v : this.returnvars) {
      Slot newVar = v.clone(map);
      g.returnvars.add(newVar);
    }

    g.copy(this, map);
    g.update(map);

    return g;
  }


  @Override
  @SuppressWarnings("unchecked")
  public Class<Node>[] supportedEndNodes() {

    return new Class[] { ReturnNode.class };
  }


  public List<Slot> getParameters() {

    return this.parameters;
  }


  public List<Slot> getReturnVariables() {

    return this.returnvars;
  }


  @Override
  public List<Slot> getAllVariables(boolean scope) {

    List<Slot> vars = super.getAllVariables(scope);

    for (Slot v : this.parameters) {
      if (v._export || (scope == Graph.LOCAL)) {
        vars.add(v);
      }
    }

    for (Slot v : this.returnvars) {
      if (v._export || (scope == Graph.LOCAL)) {
        vars.add(v);
      }
    }

    return vars;
  }


  public Node execute(WozInterface comm, Map<Slot, Value> arguments,
      Map<Slot, Value> returns,
            InputCenter input, ExecutionLogger logger) {

    this.arguments = arguments;
    this.returns = returns;
    return this.execute(comm, input, logger);
  }


  @Override
  protected void initVariables(WozInterface comm) {

    Environment env = this.getOwner().getEnvironment(Graph.GLOBAL);

    for (Slot v : this.parameters) {
      try {
        Value arg =
          this.arguments != null ? (Value)this.arguments.get(v) : null;
        if (arg != null) {
          v.instantiate(arg.copy());
        }
        else {
          v.instantiate(env, comm);
        }
      } catch (Exception exn) {
        if ((this.arguments != null) && (this.arguments.get(v) == null)) {
          throw new RuntimeException(Resources.getString("InitialisationError")
            + ".\n"
                            + Resources.getString("Parameter") + ": "
            + v.getName() + "\n"
                            + Resources.getString("Value") + ": "
            + v.getInitValue());
        }
        else {
          throw new RuntimeException(Resources.format("ParameterError", v
            .getName())
                            + ' ' + exn.getLocalizedMessage());
        }
      }
    }

    for (Slot v : this.returnvars) {
      try {
        v.instantiate(env, comm);
      } catch (Exception exn) {
        throw new RuntimeException(Resources.getString("InitialisationError")
          + ".\n"
                        + Resources.getString("ReturnVariable") + ": "
          + v.getName() + "\n"
                        + Resources.getString("Value") + ": "
          + v.getInitValue());
      }
    }

    super.initVariables(comm);
  }


  @Override
  protected void uninitVariables() {

    for (Slot v : this.parameters) {
      v.uninstantiate();
    }

    for (Slot v : this.returnvars) {
      if (this.returns != null) {
        this.returns.put(v, v.getValue().copy());
      }
      v.uninstantiate();
    }

    super.uninitVariables();
  }


  @Override
  protected void writeVariables(XMLWriter out, IdMap uid_map) {

    for (Slot v : this.parameters) {
      v.write(out, uid_map, "parameter");
    }

    for (Slot v : this.returnvars) {
      v.write(out, uid_map, "returnvar");
    }

    super.writeVariables(out, uid_map);
  }


  protected void validateVariables(Collection<SearchResult> errors,
            Collection<SearchResult> warnings) {

    Environment env = this.getOwner().getEnvironment(Graph.GLOBAL);

    for (Slot v : this.parameters) {
      try {
        if (!StringTools.isEmpty(v.getInitValue())) {
          Type.unify(v.getType(),
                      Expression.parseExpression(v.getInitValue(), env)
                        .getType());
        }
      } catch (Exception exn) {
        String graphName = this.graphPath(false).toString();
        errors.add(new GraphSearchResult(this, graphName, Resources.format(
                    "IllegalParamInitValue", v.getName())
                        + ": " + exn.getLocalizedMessage(),
          SearchResult.Type.WARNING));
      }
    }

    for (Slot v : this.returnvars) {
      try {
        if (!StringTools.isEmpty(v.getInitValue())) {
          Type.unify(v.getType(),
                      Expression.parseExpression(v.getInitValue(), env)
                        .getType());
        }
      } catch (Exception exn) {
        String graphName = this.graphPath(false).toString();
        errors.add(new GraphSearchResult(this, graphName, Resources.format(
                    "IllegalReturnInitValue", v.getName())
                        + ": " + exn.getLocalizedMessage(),
          SearchResult.Type.WARNING));
      }
    }

    super.validateVariables(errors);
  }


  @Override
  protected XMLHandler getGraphHandler(XMLReader r, Runnable completionRoutine,
      IdMap uid_map) {

    return new GraphHandler(r, completionRoutine, uid_map) {

      @Override
      public void start(String name, Attributes atts)
          throws SAXException {

        if (name.equals("parameter") || name.equals("returnvar")) {
          final Slot v = new Slot(null, null, null, false);
          v.setId(atts.getValue("uid"));
          this.uid_map.variables.put(v);

          this.r.setHandler(new AbstractHandler(name) {

            @Override
            protected void end(String name) {

              if (name.equals("name")) {
                v.setName(this.getValue());
              }
              else if (name.equals("type")) {
                Type t = Type.getTypeForName(this.getValue());
                if (t == null) {
                  t = Slot.legacyType(this.getValue());
                }
                v.setType(t);
              }
              else if (name.equals("value")) {
                v.setInitValue(this.getValue());
              }
              else if (name.equals("export")) {
                v._export = true;
              }
            }
          });

          if (name.equals("parameter")) {
            Procedure.this.parameters.add(v);
          }
          else {
            Procedure.this.returnvars.add(v);
          }
        }
        else {
          super.start(name, atts);
        }
      }
    };
  }

}
