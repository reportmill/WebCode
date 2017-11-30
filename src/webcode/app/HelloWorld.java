package webcode.app;
import snap.view.*;

/**
 * A custom class.
 */
public class HelloWorld extends ViewOwner {

/**
 * Create new UI.
 */
protected View createUI()
{
    Button btn = new Button("Hello World");
    btn.setPrefSize(300,200);
    return btn;
}

public static void main(String args[])
{
    new HelloWorld().setWindowVisible(true);
}


}