package com.clt.dialog.client;

/**
 * A package private helper class that can queue input objects. If necessary,
 * the method <code>get()</code> will block until input is available.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class InputQueue<T> {

  private static class QElem<ElemType> {

    public ElemType elem;
    public QElem<ElemType> next;


    public QElem(ElemType o) {

      this.elem = o;
      this.next = null;
    }
  }

  private QElem<T> first, last;
  private boolean disposed = false;
  private boolean interrupted = false;


  public InputQueue() {

    this.clear();
  }


  public synchronized boolean isEmpty() {

    return this.first == null;
  }


  public synchronized void clear() {

    this.first = this.last = null;
  }


  public synchronized void put(T o) {

    QElem<T> e = new QElem<T>(o);
    if (this.last == null) {
      this.first = this.last = e;
    }
    else {
      this.last.next = e;
      this.last = e;
    }
    this.notifyAll();
  }


  public synchronized void interrupt() {

    this.interrupted = true;
    this.notifyAll();
  }


  public synchronized T get()
      throws InterruptedException {

    this.interrupted = false;

    while ((this.first == null) && !this.disposed) {
      this.wait();
    }

    if (this.disposed || this.interrupted) {
      throw new InterruptedException();
    }

    T o = this.first.elem;
    this.first = this.first.next;
    if (this.first == null) {
      this.last = null;
    }
    return o;
  }


  public synchronized T get(long timeout)
      throws InterruptedException {

    this.interrupted = false;

    if ((this.first == null) && !this.disposed) {
      this.wait(timeout);
    }

    if (this.disposed || this.interrupted) {
      throw new InterruptedException();
    }

    if (this.first == null) {
      return null;
    }

    T o = this.first.elem;
    this.first = this.first.next;
    if (this.first == null) {
      this.last = null;
    }
    return o;
  }


  public synchronized void dispose() {

    this.clear();
    this.disposed = true;
    this.notifyAll();
  }


  public synchronized void remove(Object o) {

    QElem<T> previous = null;
    QElem<T> e = this.first;
    while (e != null) {
      // it is important to call o.equals(e.elem) and not e.elem.equals(o)
      // because
      // a) e.elem might be null
      // b) we want to be able to pass in an object that overrides equals()
      // in order to filter an arbitrary number of objects
      if (o == null ? e.elem == null : o.equals(e.elem)) {
        if (previous == null) {
          this.first = e.next;
        }
        else {
          previous.next = e.next;
        }
        if (e == this.last) {
          this.last = previous;
        }
      }
      previous = e;
      e = e.next;
    }
  }
}