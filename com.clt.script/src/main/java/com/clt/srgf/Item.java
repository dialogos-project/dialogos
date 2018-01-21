package com.clt.srgf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.clt.util.MetaCollection;

/**
 * An <code>Item</code> is an instance of an {@link Expansion} during a parse.
 *
 * Items hold information about the minimum and maximum number of repetitions as
 * well as the number of repetitions already used. Subclasses need to call
 * {@link #setDone} to signal that an item has successfully matched the input.
 * The actual match happens in {@link #shift_} which all subclasses need to
 * override.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
abstract class Item implements Cloneable {

    public static final int REPEAT_NEVER = 0,
            REPEAT_ALWAYS = 1,
            REPEAT_AS_NEEDED = 2;

    private int repeatMin;
    private int repeatMax;
    private int repeatCount;

    private boolean done = false;

    public Item(int repeatMin, int repeatMax) {

        this.repeatMin = repeatMin;
        this.repeatMax = repeatMax;

        if ((repeatMax >= 0) && (repeatMax < repeatMin)) {
            throw new IllegalArgumentException(
                    "Repeat maximum may not be smaller than repeat minimum");
        }

        // signal that we have not started processing this item yet
        this.repeatCount = 0;
    }

    protected Item(Item item) {

        this.repeatMin = item.repeatMin;
        this.repeatMax = item.repeatMax;
        this.repeatCount = item.repeatCount;
        this.done = item.done;
    }

    public abstract Item copy();

    public boolean isDone() {

        return this.done;
    }

    public void setDone(boolean done) {

        this.done = done;
    }

    public void preventRepetition() {

        this.repeatMax = 0;
    }

    public int getRepeatCount() {

        return this.repeatCount;
    }

    public int getRepeatState() {

        if (this.repeatCount < this.repeatMin) {
            return Item.REPEAT_ALWAYS;
        } else if ((this.repeatMax >= 0) && (this.repeatCount >= this.repeatMax)) {
            return Item.REPEAT_NEVER;
        } else {
            return Item.REPEAT_AS_NEEDED;
        }
    }

    @SuppressWarnings("unchecked")
    final Collection<Parse> shift(Parse p) {

        if (this.isDone()) {
            Collection<Parse> continuations = new ArrayList<Parse>(2);
            Collection<Parse> sparseBranches = null;

            // System.out.println("Successfully finished " + this);
            Item item = p.pop();
            if (item != this) {
                throw new IllegalStateException("Popped item is not the current item!");
            }

            // If the item was a Terminal or an automatic rule, create sparse
            // alternatives,
            // but only if the item wasn't already garbage.
            if ((item instanceof TerminalItem) || (item instanceof Rule.DynVocItem)) {
                sparseBranches
                        = p.createSparseBranches(item instanceof Garbage.GarbageItem, null);
            } else if (item instanceof RuleItem) {
                RuleItem ri = (RuleItem) item;
                if (ri.isAutomatic()) {
                    sparseBranches = p.createSparseBranches(false, ri.getRule());
                }
            }

            // repush repeated items
            int state = this.getRepeatState();
            // System.out.println("Got repeat state " + state + " after " +
            // item.repeatCount + " repetitions of item " + item);

            switch (state) {
                case Item.REPEAT_NEVER:
                    // there is nothing else to do
                    continuations.add(p);
                    break;
                case Item.REPEAT_ALWAYS:
                    // must repeat, so repush the last item
                    p.push(this.createRepetition());
                    continuations.add(p);
                    break;
                case Item.REPEAT_AS_NEEDED:
                    // can repeat
                    // branch with repetition
                    Parse branch = p.branch();
                    branch.push(this.createRepetition());
                    continuations.add(branch);

                    // original parse continues without repetition
                    continuations.add(p);
                    break;
                default:
                    throw new IllegalStateException("Unknown repeat state: " + state);
            }

            if ((sparseBranches == null) || sparseBranches.isEmpty()) {
                return continuations;
            } else {
                return new MetaCollection<Parse>(continuations, sparseBranches);
            }
        } else {
            // System.out.println("working on " + repeatCount + ". repetition (" +
            // repeatMin + "-" + repeatMax + ") of item " + this);
            // System.out.println("Shifting " + getClass().getName() + ": " + source);

            if (this.repeatCount == 0) { // first call
                Collection<Parse> optionalBranch = null;
                Collection<Parse> continuations = null;
                if (this.repeatMin == 0) {
                    // if item is optional (min=0), then add a possible parse where the
                    // item
                    // is immediately popped.
                    Parse branch = p.branch();
                    branch.pop();
                    optionalBranch = Arrays.asList(new Parse[]{branch});
                }

                if (this.repeatMax != 0) { // max>0 and max=-1 both mean "at least once"
                    // so start the item
                    this.repeatCount = 1;
                    continuations = this.shift_(p);
                }

                if ((continuations == null) || continuations.isEmpty()) {
                    if (optionalBranch == null) {
                        return Collections.emptyList();
                    } else {
                        return optionalBranch;
                    }
                } else {
                    if (optionalBranch == null) {
                        return continuations;
                    } else {
                        return new MetaCollection<Parse>(optionalBranch, continuations);
                    }
                }
            } else {
                return this.shift_(p);
            }
        }
    }

    /**
     * Advance one step in parsing the input and return a list of parse objects
     * that can be used to continue parsing. Parsing alternatives can be
     * constructed by calling {@link Parse#branch}. If a parse fails (i.e. there
     * is no continuation), shift_() should return an empty list.
     */
    protected abstract List<Parse> shift_(Parse p);

    /**
     * Return the penalty associated with expanding this item. The default is no
     * penalty (0). Subclasses can override this method to produce items that
     * have a penalty (e.g. garbage models).
     */
    public int getPenalty() {

        return 0;
    }

    private final Item createRepetition() {

        Item item = this.copy();
        item.reset();
        item.repeatCount++;
        return item;
    }

    /**
     * Reset the inner state of an item. This method is called to reset an item
     * for repetition. The default method only resets the <code>done</code>
     * flag.
     */
    protected void reset() {

        this.setDone(false);
    }

    ;


  /**
   * Construct a list that holds only the given Parse object. This function is a
   * useful helper for {@link #shift_} where you often advance the current parse
   * and then return a list containing exactly this one parse.
   */
  protected static List<Parse> itemList(Parse p) {

        return Arrays.asList(new Parse[]{p});
    }
}
