package test;
import cjdom.*;

/**
 * A simple app to bounce balls around page.
 */
public class PlayBall {

    // The list of balls
    Ball         _balls[] = new Ball[50];
    
    // The number of balls
    int          _ballCount;
    
    // The main element to hold balls
    HTMLElement  _mainEmt;
    
    // Whether animation is playing
    int          _playing = -1;
    
    // Whether mouse button is pressed
    boolean      _mouseDown;
    
    // Last add time
    long         _lastAddTime;
    
/**
 * Create new PlayBall.
 */
public PlayBall()
{
    // Get document and body
    HTMLDocument doc = HTMLDocument.current();
    HTMLBodyElement body = doc.getBody();

    // Get main element and register mouse listeners
    _mainEmt = body;
    _mainEmt.addEventListener("mousedown", e -> mouseDown((MouseEvent)e));
    _mainEmt.addEventListener("mousemove", e -> mouseMove((MouseEvent)e));
    _mainEmt.addEventListener("mouseup", e -> _mouseDown = false);
    
    // Add Touch Listeners
    _mainEmt.addEventListener("touchstart", e -> touchStart((TouchEvent)e));
    _mainEmt.addEventListener("touchmove", e -> touchMove((TouchEvent)e));
    _mainEmt.addEventListener("touchend", e -> touchEnd((TouchEvent)e));
    
    // Add bogus element to force body to size of page
    HTMLImageElement bogus = (HTMLImageElement)doc.createElement("img");
    bogus.setSize(getWidth()-4, getHeight()-4); bogus.getStyle().setCSSText("border:0;");
    Window.current().addEventListener("resize", e -> bogus.setSize(getWidth()-4, getHeight()-4));
    body.appendChild(bogus);
    
    // Add button
    HTMLElement btn = (HTMLElement)doc.createElement("button");
    btn.getStyle().setCSSText("position:fixed;bottom:8px;right:8px;");
    btn.setInnerHTML("Clear Balls");
    body.appendChild(btn);
    
    // Seed some starter balls
    for(int i=0;i<8;i++) addBall(30,30);
    body.getStyle().setCSSText("margin:0;");
}

/**
 * Returns page width.
 */
public int getWidth()  { return Window.current().getInnerWidth(); }

/**
 * Returns page height.
 */
public int getHeight()  { return Window.current().getInnerHeight(); }

/**
 * Add ball.
 */
public void addBall(double aX, double aY)
{
    Ball ball = new Ball(aX, aY);
    _mainEmt.appendChild(ball.img);
    _balls[_ballCount++] = ball;
    play();
}

/**
 * Remove ball.
 */
public void removeBall(Ball aBall)  { _mainEmt.removeChild(aBall.img); }

/**
 * Start animation.
 */
public void play()
{
    if(_playing>=0) return;
    _playing = Window.setInterval(() -> moveBalls(), 25);
}

/**
 * Stop animation.
 */
public void stop()
{
    if(_playing<0) return;
    Window.clearInterval(_playing);
    _playing = -1;
}

/**
 * Move balls.
 */
void moveBalls()  { for(int i=0;i<_ballCount;i++) _balls[i].moveBall(); }

/**
 * Handle mouse down event.
 */
public void mouseDown(MouseEvent anEvent)
{
    mouseDown(anEvent.getClientX(),anEvent.getClientY());
    anEvent.preventDefault(); // Stop browser from potentially dragging hit image
}

/**
 * Handle mouse down event.
 */
public void mouseDown(double aX, double aY)
{
    // Set MouseDown
    _mouseDown = true;
    
    // If hit button, clear balls
    if(aX>getWidth()-80 && aY>getHeight()-40) {
        for(int i=0;i<_ballCount;i++) { removeBall(_balls[i]); _balls[i] = null; }
        _ballCount = 0;
        stop();
    }
    
    // Otherwise, add ball
    else {
        addBall(aX, aY);
        _lastAddTime = System.currentTimeMillis();
    }
}

/**
 * Handle mouse move event.
 */
public void mouseMove(MouseEvent anEvent)
{
    mouseMove(anEvent.getClientX(),anEvent.getClientY());
}

/**
 * Handle mouse move event.
 */
public void mouseMove(double aX, double aY)
{
}

/**
 * Called when body gets TouchStart.
 */
public void touchStart(TouchEvent anEvent)
{
    // Get event touches and first touch
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    mouseDown(touch.getClientX(),touch.getClientY());
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets touchMove.
 */
public void touchMove(TouchEvent anEvent)
{
    // Get event touches and first touch
    Touch touches[] = anEvent.getTouches(); if(touches==null || touches.length==0) return;
    Touch touch = touches[0];
    mouseMove(touch.getClientX(),touch.getClientY());
    anEvent.stopPropagation();
    anEvent.preventDefault();
}

/**
 * Called when body gets touchEnd.
 */
public void touchEnd(TouchEvent anEvent)  { _mouseDown = false; }

/**
 * Returns whether bounds contains x/y.
 */
public boolean contains(double aX, double aY)  { return (0<=aX) && (aX<=getWidth()) && (0<=aY) && (aY<=getHeight()); }

/**
 * Returns whether bounds contains rect.
 */
public boolean contains(double aX, double aY, double aW, double aH) { return contains(aX,aY) && contains(aX+aW,aY+aH); }

/**
 * Standard main method.
 */
public static void main(String args[])
{
    BallBounce bb = new BallBounce();
}

}