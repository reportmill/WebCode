package webcode.app;
import snap.view.*;
import snap.viewx.CodeView;
import snap.viewx.WebPage;

/**
 * A custom class.
 */
public class JavaPage extends WebPage {
    
    JavaTextPane  _javaPane = new JavaTextPane();

/**
 * Creates a new JavaPage.
 */
public JavaPage()  { }

/**
 * Return the AppPane.
 */
AppPane getAppPane()  { return getBrowser() instanceof AppBrowser? ((AppBrowser)getBrowser()).getAppPane() : null; }

/**
 * Returns the JavaTextView.
 */
//public JavaTextPane getTextPane()  { return _jtextPane; }

/**
 * Returns the JavaTextView.
 */
//public JavaTextView getTextView()  { return getTextPane().getTextView(); }

/**
 * Creates UI panel.
 */
protected View createUI()  { return _javaPane.getUI(); }

/**
 * Init UI.
 */
protected void initUI()
{
    super.initUI();
    CodeView codeView = (CodeView)_javaPane.getTextArea();
    codeView.setSource(getURL());
    //getTextView().setSource(getFile());
    //setFirstFocus(getTextView());
}

}