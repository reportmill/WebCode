package webcode.app;
import snap.view.*;

/**
 * A panel to run a process.
 */
public class RunConsole extends ViewOwner {

    // The AppPane
    AppPane              _appPane;

/**
 * Creates a new DebugPane.
 */
public RunConsole(AppPane anAppPane)  { _appPane = anAppPane; }

/**
 * Returns the AppPane.
 */
public AppPane getAppPane()  { return _appPane; }

}