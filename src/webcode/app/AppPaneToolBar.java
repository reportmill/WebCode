package webcode.app;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.*;

/**
 * ToolBar.
 */
public class AppPaneToolBar extends ViewOwner {

    // The AppPane
    AppPane                  _appPane;
    
    // The file tabs pane
    RowView                     _fileTabsPane;
    
    // A list of open files
    List <WebFile>           _openFiles = new ArrayList();
    
    // The currently selected file
    WebFile                  _selectedFile;
    
    // The view for the currently selected view
    View                     _selectedView;
    
    // A placeholder for fill from toolbar button under mouse
    Paint                    _tempFill;
    
    // Constant for file tab attributes
    static Font              TAB_FONT = new Font("Arial Bold", 12);
    static Color             TAB_COLOR = new Color(.5,.65,.8,.8);
    static Color             TAB_COLOR_OVER = new Color(.9,.95,1.0,.8);
    static Color             TAB_BORDER_COLOR = new Color(.33,.33,.33,.66);
    static Border            TAB_BORDER = Border.createLineBorder(TAB_BORDER_COLOR,1);
    static Border            TAB_CLOSE_BORDER1 = Border.createLineBorder(Color.BLACK,.5);
    static Border            TAB_CLOSE_BORDER2 = Border.createLineBorder(Color.BLACK,1);
    
    // Shared images
    static Image             SIDEBAR_EXPAND = Image.get(AppPane.class, "SideBar_Expand.png");
    static Image             SIDEBAR_COLLAPSE = Image.get(AppPane.class, "SideBar_Collapse.png");
    
/**
 * Creates a new AppPaneToolBar.
 */
public AppPaneToolBar(AppPane anAppPane)  { _appPane = anAppPane; }

/**
 * Returns the AppPane.
 */
public AppPane getAppPane()  { return _appPane; }

/**
 * Returns the RootSite.
 */
public WebSite getRootSite()  { return getAppPane().getRootSite(); }

/**
 * Notification that a file was opened/selected by AppPane.
 */
public void setSelectedFile(WebFile aFile)
{
    // Set selected file
    if(aFile==_selectedFile) return;
    _selectedFile = aFile;
    
    // Add to OpenFiles
    addOpenFile(_selectedFile);
    buildFileTabs();
}

/**
 * Returns whether a file is an "OpenFile" (whether it needs a File Bookmark).
 */
protected boolean isOpenFile(WebFile aFile)
{
    if(aFile.isDir()) return false; // No directories
    return getAppPane().getSites().contains(aFile.getSite()) || aFile.getType().equals("java");
}

/**
 * Adds a file to OpenFiles list.
 */
public void addOpenFile(WebFile aFile)
{
    if(aFile==null || !isOpenFile(aFile)) return;
    if(ListUtils.containsId(_openFiles, aFile)) return;
    _openFiles.add(aFile);
}

/**
 * Removes a file from OpenFiles list.
 */
public int removeOpenFile(WebFile aFile)
{
    // Remove file from list (just return if not available)
    int index = ListUtils.indexOfId(_openFiles, aFile); if(index<0) return index;
    _openFiles.remove(index);
    
    // If removed file is selected file, set browser file to last file (that is still in OpenFiles list)
    if(aFile==_selectedFile) {
        WebURL url = getFallbackURL();
        if(!url.equals(getAppPane().getHomePageURL()))
            getAppPane().getBrowser().setTransition(WebBrowser.Instant);
        getAppPane().getBrowser().setURL(url);
    }
    
    // Rebuild file tabs and return
    buildFileTabs();
    return index;
}

/**
 * Returns the URL to fallback on when open file is closed.
 */
private WebURL getFallbackURL()
{
    // Return the most recently opened of the remaining OpenFiles, or the Project.HomePageURL
    AppPane appPane = getAppPane(); AppBrowser browser = appPane.getBrowser();
    WebURL urls[] = browser.getHistory().getNextURLs();
    for(WebURL url : urls) { WebFile file = url.getFile();
        if(_openFiles.contains(file))
            return url.getFileURL(); }
    urls = browser.getHistory().getLastURLs();
    for(WebURL url : urls) { WebFile file = url.getFile();
        if(_openFiles.contains(file))
            return url.getFileURL(); }
    return getAppPane().getHomePageURL();
}

/**
 * Selects the search text.
 */
public void selectSearchText()  { runLater(() -> requestFocus("SearchComboBox")); }

/**
 * Override to add menu button.
 */
protected View createUI()
{
    // Do normal version
    SpringView uin = (SpringView)super.createUI();
    
    // Add MenuButton
    MenuButton mbtn = new MenuButton(); mbtn.setName("RunMenuButton"); mbtn.setBounds(207,29,15,14);
    mbtn.setItems(getRunMenuButtonItems()); mbtn.getGraphicAfter().setPadding(0,0,0,0);
    uin.addChild(mbtn);
    
    // Add FileTabsPane pane
    _fileTabsPane = new RowView(); _fileTabsPane.setPadding(4,0,0,4);
    _fileTabsPane.setBounds(0,45,uin.getWidth()-10,24); _fileTabsPane.setAutosizing("-~-,~--");
    uin.addChild(_fileTabsPane);
    buildFileTabs();
    
    // Add Expand button
    Button ebtn = new Button(); ebtn.setName("ExpandButton"); ebtn.setImage(SIDEBAR_EXPAND); ebtn.setShowBorder(false);
    ebtn.setBounds(uin.getWidth()-20,uin.getHeight()-20,16,16); ebtn.setAutosizing("~--,~--");
    uin.addChild(ebtn);
    
    // Set min height and return
    uin.setMinHeight(uin.getHeight());
    return uin;
}

/**
 * Override to set PickOnBounds.
 */
protected void initUI()
{
    // Get/configure SearchComboBox
    ComboBox <WebFile> searchComboBox = getView("SearchComboBox", ComboBox.class);
    //searchComboBox.setItemTextFunction(itm -> itm.getName());
    //searchComboBox.setCellConfigure(this :: configureSearchListCell);
    //searchComboBox.setPrefixFunction(s -> getFilesForPrefix(s));
    
    // Get/configure SearchComboBox.PopupList
    searchComboBox.getPopupList().setRowHeight(22); searchComboBox.getPopupList().setPrefWidth(300);
    searchComboBox.getPopupList().setAltPaint(Color.get("#F8F8F8"));
    
    // Get/configure SearchText: radius, prompt, image, animation
    TextField searchText = searchComboBox.getTextField(); searchText.setRadius(8);
    searchText.setPromptText("Search"); searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
    TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
    
    // Enable events on buttons
    String bnames[] = { "HomeButton", "BackButton", "NextButton", "RefreshButton", "RunButton" };
    for(String name : bnames) enableEvents(name, MouseRelease, MouseEnter, MouseExit);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    Image img = getAppPane().isShowSideBar()? SIDEBAR_EXPAND : SIDEBAR_COLLAPSE;
    getView("ExpandButton", Button.class).setImage(img);
}

/**
 * Respond to UI changes.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get AppPane
    AppPane appPane = getAppPane();
    
    // Make buttons glow
    if(anEvent.isMouseEnter() && anEvent.getView()!=_selectedView) { View view = anEvent.getView();
        _tempFill = view.getFill(); view.setFill(Color.WHITE); return; }
    if(anEvent.isMouseExit() && anEvent.getView()!=_selectedView) { View view = anEvent.getView();
        view.setFill(_tempFill); return; }
    
    // Handle HomeButton
    if(anEvent.equals("HomeButton") && anEvent.isMouseRelease()) appPane.showHomePage();
    
    // Handle LastButton, NextButton
    if(anEvent.equals("BackButton") && anEvent.isMouseRelease())
        appPane.getBrowser().trackBack();
    if(anEvent.equals("NextButton") && anEvent.isMouseRelease())
        appPane.getBrowser().trackForward();
    
    // Handle RefreshButton
    if(anEvent.equals("RefreshButton") && anEvent.isMouseRelease())
        appPane.getBrowser().reloadPage();
    
    // Handle RunButton
    if(anEvent.equals("RunButton") && anEvent.isMouseRelease())
        appPane._filesPane.run();
    
    // Show history
    if(anEvent.equals("ShowHistoryMenuItem"))
        showHistory();
    
    // Handle FileTab
    if(anEvent.equals("FileTab") && anEvent.isMouseRelease())
        handleFileTabClicked(anEvent);

    // Handle SearchComboBox
    if(anEvent.equals("SearchComboBox"))
        handleSearchComboBox(anEvent);
        
    // Handle ExpandButton
    if(anEvent.equals("ExpandButton")) {
        boolean showSideBar = !appPane.isShowSideBar();
        appPane.setShowSideBar(showSideBar);
        WebPage page = appPane.getBrowser().getPage();
        if(page!=null)
            page.getUI().setProp("HideSideBar", !showSideBar);
    }
}

/**
 * Handle FileTab clicked.
 */
protected void handleFileTabClicked(ViewEvent anEvent)
{
    FileTab fileTab = anEvent.getView(FileTab.class);
    WebFile file = fileTab.getFile();
    
    // Handle single click
    if(anEvent.getClickCount()==1) {
        getAppPane().getBrowser().setTransition(WebBrowser.Instant);
        getAppPane().setSelectedFile(file);
    }
    
    // Handle double click
    else if(anEvent.getClickCount()==2) {
        WebBrowserPane bpane = new WebBrowserPane();
        bpane.getBrowser().setURL(file.getURL());
        bpane.getWindow().setVisible(true);
    }
}

/**
 * Handle SearchComboBox changes.
 */
public void handleSearchComboBox(ViewEvent anEvent)
{
    // Get selected file and/or text
    WebFile file = (WebFile)anEvent.getSelItem();
    String text = anEvent.getStringValue();
    
    // If file available, open file
    if(file!=null)
        getAppPane().getBrowser().setFile(file);

    // If text available, either open URL or search for string
    else if(text!=null && text.length()>0) {
        int colon = text.indexOf(':');
        if(colon>0 && colon<6) {
            WebURL url = WebURL.getURL(text);
            getAppPane().getBrowser().setURL(url);
        }
        else {
            //getAppPane().getSearchPane().search(text);
            getAppPane().setSupportTrayIndex(SupportTray.SEARCH_PANE);
        }
    }
    
    // Clear SearchComboBox
    setViewText("SearchComboBox", null);
}

/**
 * Creates a pop-up menu for preview edit button (currently with look and feel options).
 */
private List <MenuItem> getRunMenuButtonItems()
{
    // Create MenuItems list
    List <MenuItem> items = new ArrayList(); MenuItem mi;
    
    // Add RunConfigs MenuItems
    //List <RunConfig> rconfs = RunConfigs.get(getRootSite()).getRunConfigs();
    //for(RunConfig rconf : rconfs) { String name = rconf.getName();
    //    mi = new MenuItem(); mi.setName(name + "RunConfigMenuItem"); mi.setText(name); items.add(mi); }
    //if(rconfs.size()>0) items.add(new MenuItem()); //new SeparatorMenuItem()
    
    // Add RunConfigsMenuItem
    mi = new MenuItem(); mi.setText("Run Configurations..."); mi.setName("RunConfigsMenuItem"); items.add(mi);
    mi = new MenuItem(); mi.setText("Show History..."); mi.setName("ShowHistoryMenuItem"); items.add(mi);
    
    // Return MenuItems
    return items;
}

/**
 * Sets the RunMenuButton items.
 */
public void setRunMenuButtonItems()
{
    MenuButton rmb = getView("RunMenuButton", MenuButton.class);
    rmb.setItems(getRunMenuButtonItems());
    for(MenuItem mi : rmb.getItems()) mi.setOwner(this);
}

/**
 * Builds the file tabs.
 */
public void buildFileTabs()
{
    // If not on event thread, come back on that
    if(!isEventThread()) { runLater(() -> buildFileTabs()); return; }
    
    // Clear selected view
    _selectedView = null;
    
    // Create HBox for tabs
    RowView hbox = new RowView(); hbox.setSpacing(2);
    
    // Iterate over OpenFiles, create FileTabs, init and add
    for(WebFile file : _openFiles) {
        Label bm = new FileTab(file); bm.setOwner(this);
        enableEvents(bm, MouseEvents);
        hbox.addChild(bm);
    }
    
    // Add box and call scaleFileTabs to scale them if needed
    _fileTabsPane.setChildren(hbox);
    scaleFileTabs();
}

/**
 * Scales file tabs HBox so all tabs show.
 */
private void scaleFileTabs()
{
    RowView hbox = (RowView)_fileTabsPane.getChild(0);
    double width1 = hbox.getPrefWidth(), width2 = _fileTabsPane.getWidth();
    if(width1>width2-4) {
        hbox.setScaleX((width2-4)/width1);
        hbox.setTransX(-(width1 - width2 + 4)/2);
    }
}

/**
 * Configures a search list cell.
 */
public void configureSearchListCell(ListCell<WebFile> aCell)
{
    WebFile file = aCell.getItem(); if(file==null) return;
    Label after = new Label("- " + file.getParent().getPath()); if(aCell.isSelected()) after.setTextFill(Color.WHITE);
    aCell.setGraphicAfter(after);
}

/**
 * Returns a list of files for given prefix.
 */
/*private List <WebFile> getFilesForPrefix(String aPrefix)
{
    if(aPrefix.length()==0) return Collections.EMPTY_LIST;
    List <WebFile> files = new ArrayList(); if(aPrefix==null || aPrefix.length()==0) return files;
    for(WebSite site : getAppPane().getSites())
        getFilesForPrefix(aPrefix, site.getRootDir(), files);
    Collections.sort(files, _fileComparator);
    return files;
}*/

/**
 * Gets files for given name prefix.
 */
/*private void getFilesForPrefix(String aPrefix, WebFile aFile, List <WebFile> theFiles)
{
    // If hidden file, just return
    SitePane spane = SitePane.get(aFile.getSite()); if(spane.isHiddenFile(aFile)) return;

    // If directory, recurse
    if(aFile.isDir()) for(WebFile file : aFile.getFiles())
        getFilesForPrefix(aPrefix, file, theFiles);
        
    // If file that starts with prefix, add to files
    else if(StringUtils.startsWithIC(aFile.getName(), aPrefix))
        theFiles.add(aFile);
}*/

/**
 * Shows history.
 */
private void showHistory()
{
    WebBrowser browser = getAppPane().getBrowser();
    WebBrowserHistory history = browser.getHistory();
    StringBuffer sb = new StringBuffer();
    for(WebURL url : history.getLastURLs())
        sb.append(url.getString()).append('\n');
    WebFile file = WebURL.getURL("/tmp/History.txt").createFile(false);
    file.setText(sb.toString());
    browser.setFile(file);
}

/**
 * Comparator for files.
 */
Comparator<WebFile> _fileComparator = new Comparator<WebFile>() {
    public int compare(WebFile o1, WebFile o2) {
        int c = o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
        return c!=0? c : o1.getName().compareToIgnoreCase(o2.getName());
    }
};

/**
 * A class to represent a rounded label.
 */
protected class FileTab extends Label {
    
    // The File
    WebFile  _file;
    
    /** Creates a new FileTab for given file. */
    public FileTab(WebFile aFile)
    {
        // Create label for file and configure
        _file = aFile;
        setText(aFile.getName()); setFont(TAB_FONT); setName("FileTab");
        setPrefHeight(19); setMaxHeight(19); setBorder(TAB_BORDER); setPadding(1,2,1,4);
        setFill(aFile==_selectedFile? Color.WHITE : TAB_COLOR);
        if(aFile==_selectedFile) _selectedView = this;
        
        // Add a close box graphic
        Polygon poly = new Polygon(0,2,2,0,5,3,8,0,10,2,7,5,10,8,8,10,5,7,2,10,0,8,3,5);
        ShapeView sview = new ShapeView(poly); sview.setBorder(TAB_CLOSE_BORDER1); sview.setPrefSize(11,11);
        sview.addEventFilter(e->handleTabCloseBoxEvent(e), MouseEnter, MouseExit, MouseRelease);
        setGraphicAfter(sview);
    }
    
    /** Returns the file. */
    public WebFile getFile()  { return _file; }
    
    /** Called for events on tab close button. */
    private void handleTabCloseBoxEvent(ViewEvent anEvent)
    {
        View cbox = anEvent.getView();
        if(anEvent.isMouseEnter()) { cbox.setFill(Color.CRIMSON); cbox.setBorder(TAB_CLOSE_BORDER2); }
        else if(anEvent.isMouseExit()) { cbox.setFill(null); cbox.setBorder(TAB_CLOSE_BORDER1); }
        else if(anEvent.isMouseRelease()) removeOpenFile(_file);
        anEvent.consume();
    }

    /** Returns bounds shape as rounded rect. */
    public Shape getBoundsShape()  { return new RoundRect(0,0,getWidth(),getHeight(),6); }
}

}