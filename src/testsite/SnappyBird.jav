package codefun;
import java.util.Random;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.CodeView;
import snap.web.WebURL;

/**
 * A Flappy-Bird app.
 */
public class SnappyBird extends ViewOwner {

    // The main view
    ChildView    _view;
    
    // The flappy view
    ImageView    _flappy;
    
    // The velocity of flappy
    double       _veloc;
    
    // The elapsed time
    int          _time;
    
    // The acceleration of gravity (points/sec^2)
    double       GRAVITY = 75;
    
    // The image for the pipe
    Image        FLAPPY_IMAGE = Image.get(getClass(), "Snappy.png");
    Image        PIPE_IMAGE = Image.get(getClass(), "SnappyPipe.png");
    
/**
 * Create UI.
 */
protected View createUI()
{
    // Create main view
    _view = new ChildView(); _view.setPrefSize(800,600); _view.setFill(new Color("#EEFFFF"));
    _view.addEventHandler(e -> flap(), MouseRelease);
    
    // Create Flappy Image and ImageView and add
    _flappy = new ImageView(FLAPPY_IMAGE); _flappy.setBounds(180, 180, 60, 45);
    _view.addChild(_flappy);

    // Return main view
    //return _view;
    
    // Create code view
    CodeView cv = new CodeView(); cv.setSource(WebURL.getURL(getClass(), "SnappyBird.jav"));
    TabView tview = new TabView(); _view.setClipToBounds(true); _view.setBorder(Color.GRAY,1);
    BoxView box = new ScaleBox(_view,true,true); box.setPadding(10,10,10,10);
    tview.addTab("SnappyBird", box);
    tview.addTab("Source", new ScrollView(cv)); tview.setGrowWidth(true); tview.setBorder(Color.GRAY,1);
    return tview;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    addPipe();
    new ViewTimer(40, t -> updateTime(t)).start(500);
    getWindow().setGrowWidth(true);
}

void addPipe()
{
    // Create 4 pipe parts (top base+cap and bottom base+cap)
    View p1 = createPipePart(); p1.setPrefWidth(64);
    View p2 = createPipePart(); p2.setPrefSize(90,40);
    View p3 = createPipePart(); p3.setPrefSize(90,40); p3.setLeanY(VPos.BOTTOM);
    View p4 = createPipePart(); p4.setPrefWidth(64);
    
    // Create Pipe VBox view to hold pipe parts
    ColView vbox = new ColView(); vbox.setAlign(Pos.TOP_CENTER); vbox.setPrefHeight(_view.getPrefHeight());
    vbox.setChildren(p1, p2, p3, p4); vbox.setSize(vbox.getBestSize());
    _view.addChild(vbox);
    resetPipe(vbox);
}

View createPipePart()
{
    ImageView pipe = new ImageView(PIPE_IMAGE, true, true);
    pipe.setBorder(new Color("#008000"), 2);
    return pipe;
}

void resetPipe(ColView aPipe)
{
    // Reset pipe levels
    double dh = new Random().nextInt(3)*100 - 100;
    aPipe.getChild(0).setPrefHeight(200 + dh);
    aPipe.getChild(3).setPrefHeight(200 - dh);
    
    // Reset pipe location
    aPipe.setX(_view.getPrefWidth());
    aPipe.getAnim(4000).setX(-aPipe.getWidth()).setOnFinish(a -> getEnv().runLater(() -> resetPipe(aPipe))).play();
}

/**
 * Called on MouseRelease on main view.
 */
void flap()  { _veloc -= 60; if(_veloc<-80) _veloc = -80; }

/**
 * Called when timer updates.
 */
void updateTime(ViewTimer aTimer)
{
    // Get time change since last update
    int time = aTimer.getTime();
    double dt = (time - _time)/1000d; _time = time;
    
    // If flappy exploding, just return
    if(_flappy.getOpacity()!=1)
         return;
        
    // Calculate change in y and update: dist = accel*time^2 + veloc*time
    double dy = _veloc*dt;
    double ny = _flappy.getY() + dy; if(ny>_view.getHeight()-100) ny = _view.getHeight()-100;
    _flappy.setY(ny);
    
    // Update velocity
    _veloc += GRAVITY*dt; if(_veloc>80) _veloc = 80;
    
    // Update rotation
    _flappy.setRotate(_veloc/80*30);
    
    // If flappy hit pipe, explode and reset attributes
    ViewList vlist = _flappy.getParent().getViewList();
    View hitView = vlist.getHitView(_flappy, null, 8);
    if(hitView!=null) {
        Explode.explode(_flappy);
        _veloc = 0; _flappy.setRotate(0); _flappy.setY(250);
    }
}

/** Standard main method. */
public static void main(String args[])
{
    SnappyBird sbird = new SnappyBird();
    sbird.getUI().setGrowWidth(true);
    sbird.setWindowVisible(true);
}

}