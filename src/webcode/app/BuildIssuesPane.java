package webcode.app;
import snap.view.*;

/**
 * A pane/panel to show current build issues.
 */
public class BuildIssuesPane extends ViewOwner {

    // The AppPane
    AppPane                 _appPane;
    
/**
 * Creates a new ProblemsPane.
 */
public BuildIssuesPane(AppPane anAP)  { _appPane = anAP; }

}