package webcode.app;
import snap.view.TextArea;
import snap.viewx.CodeView;
import snap.viewx.TextPane;

/**
 * A custom class.
 */
public class JavaTextPane extends TextPane {

/**
 * Creates the JavaTextView.
 */
protected TextArea createTextArea()  { return new CodeView(); }

}