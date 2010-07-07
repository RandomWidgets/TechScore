package edu.mit.techscore.dpxml;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Class for quick XML structures.
 *
 * TODO: move these classes to a different external package
 *
 * Created: Sun Jun  7 16:48:32 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class XMLTag {
  private ArrayList<XMLTag> children;
  private String name;
  private HashMap<String, ArrayList<String>> attr;

  // Constructors
  public XMLTag(String n) {
    this.name = n;
    this.children = new ArrayList<XMLTag>();
    this.attr = new HashMap<String, ArrayList<String>>();
  }
  public XMLTag(String n, XMLTag [] children) {
    this.name = n;
    this.children = new ArrayList<XMLTag>(children.length);
    for (int i = 0; i < children.length; i++) {
      this.add(children[i]);
    }
    this.attr = new HashMap<String, ArrayList<String>>();
  }

  /**
   * Add child <code>XMLTag</code>
   */
  public void add(XMLTag c) {
    this.children.add(c);
  }

  /**
   * Get children
   * @return Array of <code>XMLTag</code>s
   */
  public XMLTag[] getChildren() {
    return this.children.toArray(new XMLTag[]{});
  }

  /**
   * Adds attribute to tag
   */
  public void addAttr(String a, String value) {
    if (!this.attr.containsKey(a)) {
      this.attr.put(a, new ArrayList<String>());
    }
    this.attr.get(a).add(value);
  }

  /**
   * Returns list of attribute names
   * @return list of attribute names
   */
  public String [] getAttrs() {
    String [] list = this.attr.keySet().toArray(new String [] {});
    if (list.length == 0) {
      return new String [] {};
    }
    return (String [])list;
  }

  /**
   * Returns list of attribute values for given attribute name
   * @return list of attribute values
   * @throws  IllegalArgumentException if there is no such attribute
   */
  public String [] getAttr(String name) {
    if (!this.attr.containsKey(name)) {
      throw new IllegalArgumentException("No such attribute for this tag: " +
					 name + " in " + this.name);
    }
    return this.attr.get(name).toArray(new String [] {});
  }

  /**
   * @return XML string, no indentation.
   */
  public String toXMLString() {
    String rep = String.format("<%s", this.name);

    // Write attributes
    String [] attrs = this.getAttrs();
    for (int i = 0; i < attrs.length; i++) {
      String [] values = this.getAttr(attrs[i]);
      String attRep = values[0];
      for (int j = 1; j < values.length; j++) {
	attRep += (" " + values[j]);
      }
      rep += String.format(" %s=\"%s\"", attrs[i], attRep);
    }

    // Any children?
    if (this.children.size() == 0) {
      rep += "/>";
      return rep;
    }
    rep += ">";

    // Write children
    for (int i = 0; i < this.children.size(); i++) {
      rep += this.children.get(i).toXMLString();
    }

    // Close tag
    rep += String.format("</%s>", this.name);
    return rep;
  }
}
