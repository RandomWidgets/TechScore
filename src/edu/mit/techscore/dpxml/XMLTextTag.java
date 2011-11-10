package edu.mit.techscore.dpxml;

/**
 * For writing text values in XML file.
 *
 * TODO: Move these classes elsewhere
 *
 * Created: Sun Jun  7 17:07:25 2009
 *
 * @author <a href="mailto:dayan@paez.mit.edu">Dayan Paez</a>
 * @version 1.0
 */
public class XMLTextTag extends XMLTag {
  private String text;

  /**
   * Creates a new <code>XMLTextTag</code> instance.
   *
   */
  public XMLTextTag() {
    this("");
  }

  public XMLTextTag(String t) {
    super("");
    this.text = t;
  }

  public String toXMLString() {
    return this.text;
  }
}
