/*
 * @(#)JarEnvironment.java
 * Created on Fri Jul 30 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;

import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Variable;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class JarEnvironment
    implements Environment {

  ClassEnvironment[] classEnv;


  public JarEnvironment(File f)
      throws IOException {

    this(f.toURL());
  }


  public JarEnvironment(String url)
      throws IOException {

    this(new URL(url));
  }


  public JarEnvironment(URL url)
      throws IOException {

    URLClassLoader classLoader = new URLClassLoader(new URL[] { url });
    try {
      InputStream is = classLoader.getResourceAsStream("classes.txt");
      BufferedReader in = new BufferedReader(new InputStreamReader(is));

      Collection<ClassEnvironment> envs = new HashSet<ClassEnvironment>();

      String className;
      while ((className = in.readLine()) != null) {
        className = className.trim();
        if (className.length() > 0) {
          try {
            Class<?> cls = classLoader.loadClass(className);
            envs.add(new ClassEnvironment(cls));
          } catch (Exception exn) {
            throw new IOException("Could not load class " + className);
          }
        }
      }

      in.close();

      this.classEnv = envs.toArray(new ClassEnvironment[envs.size()]);
    } catch (Exception exn) {
      throw new IOException("JAR file does not contain a file \"classes.txt\"");
    }
  }


  public Type getType(String typeName) {

    throw new TypeException("Unknown type: " + typeName);
  }


  public Variable createVariableReference(String id) {

    throw new TypeException("Unknown variable: " + id);
  }


  public Expression createFunctionCall(String name, Expression[] arguments) {

    for (int i = 0; i < this.classEnv.length; i++) {
      try {
        return this.classEnv[i].createFunctionCall(name, arguments);
      } catch (Environment.NoSuchFunctionException ignore) {
      }
    }

    throw new Environment.NoSuchFunctionException(name, arguments);
  }


  public Reader include(String name)
      throws IOException {

    throw new FileNotFoundException("Could not find " + name);
  }
}
