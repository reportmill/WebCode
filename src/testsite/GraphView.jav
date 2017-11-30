package codefun;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A view to graph a function.
 */
public class GraphView extends View {
    
    // The extents
    double     _x0 = -10, _y0 = 10, _x1 = 10, _y1 = -10;
    
    // The function to graph as string in terms of x
    String     _func;
    
    // The amount of function to paint
    double     _amt;
    
    // The transform from graph coords to this view coords
    Transform  _xfm;
    
    // The last mouse point
    Point      _lastPt;
    
    // The duration in milliseconds
    int        DURATION = 2500;
    
    // Some sample functions
    static String     LINEAR_EQ = "-X+2";
    static String     QUAD_EQ = ".2*X*X - 2X";
    static String     CUBIC_EQ = ".5*X*X*X + 3*X*X + 2*X";

/**
 * Creates new GraphView.
 */
public GraphView()
{
    setFill(Color.WHITE); setBorder(Color.GRAY,1);
    setPrefSize(600,600); setGrowWidth(true); setGrowHeight(true);
    setFunction(LINEAR_EQ);
    
    // Register for Mouse, ScrollWheel to pan/zoom
    addEventHandler(e -> doPan(e), MouseEvents);
    addEventHandler(e -> doZoom(e), Scroll);
}

/**
 * Returns the function.
 */
public String getFunction()  { return _func; }

/**
 * Sets the function.
 */
public void setFunction(String aVal)
{
    _func = aVal.replace('x','X'); _amt = 0;
    getAnimCleared(DURATION).startFast().setValue("Amount", 0d, 1d).play();
}

/**
 * Returns the value of the function for given x.
 */
double getY(double aX)
{
    Map map = Collections.singletonMap("X", aX);
    Object val = KeyChain.getValue(map, _func);
    return SnapUtils.doubleValue(val);
}

/**
 * Returns the amount of function to paint.
 */
public double getAmount()  { return _amt; }

/**
 * Sets the amount of function to paint.
 */
public void setAmount(double aVal)  { _amt = aVal; repaint(); }

/**
 * Returns the graph bounds.
 */
public Rect getGraphBounds()  { return new Rect(_x0, _y0, _x1 - _x0, _y1 - _y0); }

/**
 * Sets the graph bounds.
 */
public void setGraphBounds(Rect aRect)
{
    _x0 = aRect.x; _y0 = aRect.y; _x1 = aRect.getMaxX(); _y1 = aRect.getMaxY(); _xfm = null;
    repaint();
}

/**
 * Returns transform from Graph coords to view.
 */
public Transform getGraphToLocal()
{
    return _xfm!=null? _xfm : (_xfm=Transform.get(getGraphBounds(), getBoundsLocal()));
}

/**
 * Returns transform from view coords to Graph.
 */
public Transform getLocalToGraph()  { return getGraphToLocal().getInverse(); }

/**
 * Convert from Graph coords to view coords.
 */
public Point graphToLocal(double aX, double aY)  { return getGraphToLocal().transform(aX, aY); }

/**
 * Converts from view coords to graph.
 */
public Point localToGraph(double aX, double aY)  { return getLocalToGraph().transform(aX, aY); }

/**
 * Paint method.
 */
protected void paintFront(Painter aPntr)
{
    // Draw axis
    aPntr.setColor(Color.GRAY); aPntr.setStroke(Stroke.Stroke1);
    Point px0 = graphToLocal(_x0,0), px1 = graphToLocal(_x1,0);
    Point py0 = graphToLocal(0,_y0), py1 = graphToLocal(0,_y1);
    aPntr.drawLine(px0.x, px0.y, px1.x, px1.y);
    aPntr.drawLine(py0.x, py0.y, py1.x, py1.y);
    
    // Draw axis ticks
    for(int x=(int)_x0;x<=_x1;x++) { Point p = graphToLocal(x,0); aPntr.drawLine(p.x,p.y-2,p.x,p.y+2); }
    for(int y=(int)_y1;y<=_y0;y++) { Point p = graphToLocal(0,y); aPntr.drawLine(p.x-2,p.y,p.x+2,p.y); }
    
    // Get path for function (or fraction of path based on Amount)
    Point p0 = graphToLocal(_x0, getY(_x0));
    Path path = new Path(); path.moveTo(p0.x, p0.y);
    double dx = (_x1 - _x0)/getWidth()*5, x1 = _x0 + (_x1-_x0)*_amt;
    for(double x=_x0;x<x1+dx;x+=dx) {
        Point p1 = graphToLocal(x, getY(x));
        path.lineTo(p1.x, p1.y);
    }
    
    // Draw path
    aPntr.setColor(Color.BLACK); aPntr.clip(getBoundsLocal()); aPntr.draw(path);
}

/**
 * Called when time is updated.
 */
void updateTime(ViewAnim anAnim)
{
    _amt = Math.min(anAnim.getTime()/(double)DURATION,1);
    repaint();
}

/**
 * Handle pan.
 */
void doPan(ViewEvent anEvent)
{
    // Handle MousePress: Record last point in graph coords
    if(anEvent.isMousePress())
        _lastPt = localToGraph(anEvent.getX(), anEvent.getY());
        
    // Handle MouseDrag: Offset GraphBounds by amount of drag in graph coords and record last point
    else if(anEvent.isMouseDrag()) {
        Point pt = localToGraph(anEvent.getX(), anEvent.getY());
        setGraphBounds(getGraphBounds().getOffsetRect(_lastPt.x - pt.x, _lastPt.y - pt.y));
        _lastPt = localToGraph(anEvent.getX(), anEvent.getY());
    }
}

/**
 * Handle zoom.
 */
void doZoom(ViewEvent anEvent)
{
    Rect rect = getGraphBounds();
    rect.inset(anEvent.getScrollY()>0? .1 : -1);
    setGraphBounds(rect);
}

/** Override for JavaScript. */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals("Amount")) setAmount(SnapUtils.doubleValue(aValue));
    else super.setValue(aPropName, aValue);
}

public static void main(String args[])
{
    GraphView gview = new GraphView();
    new ViewOwner(gview).setWindowVisible(true);
}

}