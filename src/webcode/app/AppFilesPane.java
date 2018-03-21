package webcode.app;
import java.io.File;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.*;

/**
 * A class to handle visual management of project files.
 */
public class AppFilesPane extends ViewOwner {

    // The AppPane
    AppPane               _appPane;

    // The file tree
    TreeView <AppFile>    _filesTree;
    
    // The file list
    ListView <AppFile>    _filesList;
    
    // The root AppFiles (for TreeView)
    List <AppFile>        _rootFiles;

    // Images for files tree/list
    static Image FILES_TREE_ICON = Image.get(AppFilesPane.class, "FilesTree.png");
    static Image FILES_LIST_ICON = Image.get(AppFilesPane.class, "FilesList.png");
    
/**
 * Creates a new AppPaneFilesPane.
 */
public AppFilesPane(AppPane anAppPane)  { _appPane = anAppPane; }

/**
 * Returns the AppPane.
 */
public AppPane getAppPane()  { return _appPane; }

/**
 * Returns the selected file.
 */
public WebFile getSelectedFile()  { return _appPane.getSelectedFile(); }

/**
 * Returns the list of selected files.
 */
public List <WebFile> getSelectedFiles()
{
    WebFile sf = _appPane.getSelectedFile();
    return sf==null? Collections.EMPTY_LIST : Collections.singletonList(sf);
    //List files = new ArrayList(); AppFile item = _app.getSelItem();
    //if(item!=null && item.getFile()!=null) files.add(item.getFile()); return files;
}

/**
 * Returns the top level site.
 */
public WebSite getRootSite()  { return _appPane.getRootSite(); }

/**
 * Returns the selected site.
 */
public WebSite getSelectedSite()  { return _appPane.getSelectedSite(); }

/**
 * Returns the AppPane.Browser.
 */
public AppBrowser getBrowser()  { return _appPane.getBrowser(); }

/**
 * Returns the root files.
 */
public List <AppFile> getRootFiles()
{
    if(_rootFiles!=null) return _rootFiles;
    _rootFiles = new ArrayList(_appPane.getSiteCount());
    for(int i=0, iMax=_appPane.getSiteCount(); i<iMax; i++) { WebSite site = _appPane.getSite(i);
        _rootFiles.add(new AppFile(null,site.getRootDir())); }
    return _rootFiles;
}

/**
 * Returns an AppFile for given WebFile.
 */
public AppFile getAppFile(WebFile aFile)
{
    // Handle null
    if(aFile==null) return null;

    // If root, search for file in RootFiles
    if(aFile.isRoot()) {
        for(AppFile af : getRootFiles()) if(aFile==af.getFile()) return af;
        return null;
    }

    // Otherwise, getAppFile for sucessive parents and search them for this file
    for(WebFile par=aFile.getParent(); par!=null; par=par.getParent()) {
        AppFile apar = getAppFile(par);
        if(apar!=null) // && _filesTree.isExpanded(apar))
            for(AppFile af : apar.getChildren()) if(aFile==af.getFile()) return af;
    }
    return null; // Return null since file not found
}

/**
 * Called to update a file in FilesPane.FilesTree.
 */
public void updateFile(WebFile aFile)
{
    List <AppFile> afiles = new ArrayList();
    for(WebFile file=aFile;file!=null;file=file.getParent()) {
        AppFile afile = getAppFile(file);
        if(afile!=null) { afiles.add(afile); if(file==aFile) afile._children = null; }
    }
    _filesTree.updateItems(afiles.toArray(new AppFile[0]));
    _filesList.updateItems(afiles.toArray(new AppFile[0]));
    if(aFile.isDir()) resetLater();
}

/**
 * Shows the given file in tree.
 */
public void showInTree(WebFile aFile)
{
    // Get AppFile and return if already visible
    AppFile afile = getAppFile(aFile);
    if(_filesTree.getItems().contains(afile))
        return;
        
    // Make sure parent is showing and expand item for parent
    showInTree(aFile.getParent());
    _filesTree.expandItem(afile.getParent());
    
    // If file is SelectedFile, make FilesTree select it
    if(aFile==getSelectedFile())
        _filesTree.setSelItem(afile);
}

/**
 * Initializes UI panel.
 */
protected void initUI()
{
    // Get the FilesTree
    _filesTree = getView("FilesTree", TreeView.class);
    _filesTree.setResolver(new AppFile.AppFileTreeResolver());
    _filesTree.setRowHeight(20);
    
    // Get FilesList
    _filesList = getView("FilesList", ListView.class);
    _filesList.setRowHeight(30);
    _filesList.setAltPaint(Color.WHITE);
    _filesList.setCellConfigure(this :: configureFilesListCell);
    enableEvents(_filesList, MousePress, MouseRelease); enableEvents(_filesList, DragEvents);
    
    // Create RootFiles for TreeView (one for each open project)
    _filesTree.setItems(getRootFiles());
    _filesTree.expandItem(getRootFiles().get(0));
    _filesTree.expandItem(_filesTree.getItems().get(1));
    
    // Enable events to get MouseUp on TreeView
    enableEvents(_filesTree, MousePress, MouseRelease); enableEvents(_filesTree, DragEvents);
    
    // Register for copy/paste
    addKeyActionHandler("CopyAction", "Shortcut+C");
    addKeyActionHandler("PasteAction", "Shortcut+V");
}

/**
 * Resets UI panel.
 */
public void resetUI()
{
    // Repaint tree
    WebFile file = getAppPane().getSelectedFile();
    AppFile afile = getAppFile(file);
    _filesTree.setItems(getRootFiles());
    _filesTree.setSelItem(afile);
    
    // Update FilesList
    List <WebFile> wfiles = _appPane._toolBar._openFiles;
    List afiles = new ArrayList(wfiles.size());
    for(WebFile wf : wfiles) afiles.add(getAppFile(wf));
    _filesList.setItems(afiles);
    _filesList.setSelItem(afile);
}

/**
 * Responds to UI panel controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle OpenMenuItem
    if(anEvent.equals("OpenMenuItem"))
        _appPane.getToolBar().selectSearchText();

    // Handle QuitMenuItem
    //if(anEvent.equals("QuitMenuItem")) WelcomePanel.getShared().quitApp();

    // Handle FilesTree
    if(anEvent.equals("FilesTree") || anEvent.equals("FilesList")) {
        
        // Handle PopupTrigger
        if(anEvent.isPopupTrigger()) {
            List <MenuItem> mitems = getView("MenuButton", MenuButton.class).getItems();
            List <MenuItem> mitems2 = ViewUtils.copyMenuItems(mitems);
            Menu menu = new Menu(); for(MenuItem mi : mitems2) menu.addItem(mi);
            menu.setOwner(this);
            PopupWindow popup = menu.getPopup(); popup.show(anEvent.getView(), anEvent.getX(), anEvent.getY());
        }
        
        // Handle MouseClick (double-click): RunSelectedFile
        if(anEvent.isMouseClick() && anEvent.getClickCount()==2) {
            if(getSelectedFile().isFile()) run(getSelectedFile()); }
        
        // Handle DragEvent
        else if(anEvent.isDragEvent()) { //DragEvent de = anEvent.getEvent(DragEvent.class);
            anEvent.acceptDrag(); //de.acceptTransferModes(TransferMode.COPY); de.consume();
            if(anEvent.isDragDropEvent() && anEvent.getClipboard().hasFiles()) {
                List <File> files = anEvent.getClipboard().getJavaFiles(); if(files==null || files.size()==0) return;
                addFiles(files);
                anEvent.dropComplete(); //anEvent.setDropCompleted(true);
            }
        }
        
        // Handle Selection event: Select file for tree selection
        else if(anEvent.isActionEvent()) { //if(anEvent.isSelectionEvent()) {
            AppFile item = (AppFile)anEvent.getSelItem();
            WebFile file = item!=null? item.getFile() : null;
            _appPane.setSelectedFile(file);
        }
    }
    
    // Handle AllFilesButton
    if(anEvent.equals("AllFilesButton"))  {
        boolean flip = !getView("FilesTree").getParent().getParent().isVisible(), flop = !flip;;
        getView("FilesTree").getParent().getParent().setVisible(flip);
        getView("FilesTree").getParent().getParent().setPickable(flip);
        getView("FilesList").getParent().getParent().setVisible(flop);
        getView("FilesList").getParent().getParent().setPickable(flop);
        getView("AllFilesButton", ButtonBase.class).setImage(flip? FILES_TREE_ICON : FILES_LIST_ICON);
    }
    
    // Handle NewFileMenuItem, NewFileButton
    //if(anEvent.equals("NewFileMenuItem") || anEvent.equals("NewFileButton")) _appPane.showNewFilePanel();

    // Handle RemoveFileMenuItem
    //if(anEvent.equals("RemoveFileMenuItem")) showRemoveFilePanel();

    // Handle RenameFileMenuItem
    //if(anEvent.equals("RenameFileMenuItem")) renameFile();

    // Handle DuplicateFileMenuItem
    //if(anEvent.equals("DuplicateFileMenuItem")) { copy(); paste(); }
    
    // Handle RefreshFileMenuItem
    if(anEvent.equals("RefreshFileMenuItem"))
        for(WebFile file : getSelectedFiles())
            file.reload();
    
    // Handle OpenInTextEditorMenuItem
    /*if(anEvent.equals("OpenInTextEditorMenuItem")) {
        WebFile file = getSelectedFile(); WebURL url = file.getURL();
        WebPage page = new TextPage(); page.setFile(file);
        AppBrowser browser = getBrowser(); browser.setPage(url, page);
        browser.setURL(file.getURL());
    }*/
    
    // Handle OpenInBrowserMenuItem
    /*if(anEvent.equals("OpenInBrowserMenuItem")) {
        WebFile file = getSelectedFile();
        WebBrowserPane bpane = new WebBrowserPane();
        bpane.getBrowser().setURL(file.getURL());
        bpane.getWindow().setVisible(true);
    }*/
    
    // Handle ShowFileMenuItem
    /*if(anEvent.equals("ShowFileMenuItem")) {
        WebFile file = getSelectedFile(); if(!file.isDir()) file = file.getParent();
        File file2 = file.getStandardFile();
        FileUtils.openFile(file2);
    }*/
    
    // Handle RunFileMenuItem, DebugFileMenuItem
    //if(anEvent.equals("RunFileMenuItem")) run(null, getSelectedFile(), false);
    //if(anEvent.equals("DebugFileMenuItem")) run(null, getSelectedFile(), true);
    
    // Handle CopyAction, PasteAction
    if(anEvent.equals("CopyAction")) copy();
    if(anEvent.equals("PasteAction")) paste();
}

/**
 * Adds a list of files.
 */
boolean addFiles(List <File> theFiles)  { return true; }

/**
 * Adds a file.
 */
boolean addFile(WebFile aDirectory, File aFile)
{
    // Get site
    WebSite site = aDirectory.getSite();
    
    // Handle directory
    if(aFile.isDirectory()) {
        
        // Create new directory
        WebFile directory = site.createFile(aDirectory.getDirPath() + aFile.getName(), true);
        for(File file : aFile.listFiles())
            addFile(directory, file);
    }
    
    // Handle plain file
    else {
        
        // Get name and file
        String name = aFile.getName();
        WebFile siteFile = site.getFile(aDirectory.getDirPath() + name);
        
        // See if IsDuplicating (there is a local file and it is the same as given file)
        File siteLocalFile = siteFile!=null? siteFile.getJavaFile() : null;
        boolean isDuplicating = SnapUtils.equals(aFile, siteLocalFile);
        
        // If file exists, run option panel for replace
        if(siteFile!=null) {
            
            // If not duplicating, ask user if they want to Replace, Rename, Cancel
            String options[] = new String[] { "Replace", "Rename", "Cancel" }, defaultOption = "Replace";
            int option = 1;
            if(!isDuplicating) {
                String msg = "A file named " + name + " already exists in this location.\n Do you want to proceed?";
                DialogBox dbox = new DialogBox("Add File"); //dbox._comp = _appPane.getUI();
                dbox.setWarningMessage(msg); dbox.setOptions(options);
                option = dbox.showOptionDialog(_appPane.getUI(), defaultOption); // getAppPaneUI()
                if(option<0 || options[option].equals("Cancel")) return false;
            }
            
            // If user wants to Rename, ask for new name
            if(options[option].equals("Rename")) {
                if(isDuplicating) name = "Duplicate " + name;
                DialogBox dbox = new DialogBox("Rename File"); //dbox._comp = _appPane.getUI();
                dbox.setQuestionMessage("Enter new file name:");
                name = dbox.showInputDialog(_appPane.getUI(), name); //getAppPaneUI()
                if(name==null) return false;
                name = name.replace(" ", "");
                if(!StringUtils.endsWithIC(name, '.' + siteFile.getType())) name = name + '.' + siteFile.getType();
                if(name.equals(aFile.getName()))
                    return addFile(aDirectory, aFile);
            }
        }
        
        // Get file (force this time), set bytes, save and select file
        siteFile = site.createFile(aDirectory.getDirPath() + name, false);
        siteFile.setBytes(FileUtils.getBytes(aFile));
        try { siteFile.save(); }
        catch(Exception e) { throw new RuntimeException(e); }
        _appPane.setSelectedFile(siteFile);
    }
    
    // Return true
    return true;
}

/**
 * Removes a list of files.
 */
public void removeFiles(List <WebFile> theFiles)
{
}

/**
 * Deletes a file.
 */
public void removeFile(WebFile aFile)
{
    try { aFile.delete(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Saves all unsaved files.
 */
public void saveAllFiles()
{
    saveFiles(getRootSite().getRootDir(), true);
}

/**
 * Saves any unsaved files in given directory.
 */
public int saveFiles(WebFile aFile, boolean doSaveAll)
{
    // Handle directory
    if(aFile.isDir()) {
        //if(aFile==_appPane.getBuildDir()) return doSaveAll? 1 : 0;
        for(WebFile file : aFile.getFiles()) {
            int choice = saveFiles(file, doSaveAll);
            if(choice<0 || choice==2)
                return -1;
            if(choice==1) doSaveAll = true;
        }
    }
    
    // Handle file
    else if(aFile.isUpdateSet()) {
        DialogBox dbox = new DialogBox("Save Modified File"); //dbox._comp = _appPane.getUI();
        dbox.setMessage("File has been modified:\n" + aFile.getPath());
        dbox.setOptions("Save File", "Save All Files", "Cancel");
        int choice = doSaveAll? 1 : dbox.showOptionDialog(_appPane.getUI(), "Save File"); //getAppPaneUI()
        if(choice==0 || choice==1)
            try { aFile.save(); }
            catch(Exception e) { throw new RuntimeException(e); }
        return choice;
    }
    
    return doSaveAll? 1 : 0;
}

/**
 * Renames currently selected file.
 */
public void renameFile()
{
    WebFile file = _appPane.getSelectedFile(); if(file==null || !_appPane.getSites().contains(file.getSite())) return;
    DialogBox dbox = new DialogBox("Rename File"); dbox.setMessage("Enter new name for " + file.getName());
    String newName = dbox.showInputDialog(_appPane.getUI(), file.getName());
    if(newName!=null)
        renameFile(file, newName);
}

/**
 * Renames a file.
 */
public boolean renameFile(WebFile aFile, String aName)
{
    // TODO - this is totally bogus
    if(aFile.isDir() && aFile.getFileCount()>0) {
        //File file = getLocalFile(aFile), file2 = new File(file.getParentFile(), aName); file.renameTo(file2);
        DialogBox dbox = new DialogBox("Can't rename non-empty directory"); //dbox._comp = _appPane.getUI();
        dbox.setErrorMessage("I know this is bogus, but app can't yet rename non-empty directory");
        dbox.showMessageDialog(_appPane.getUI()); //getAppPaneUI()
        return false;
    }
    
    // Get file name (if no extension provided, default to file extension) and path
    String name = aName; if(name.indexOf('.')<0 && aFile.getType().length()>0) name += "." + aFile.getType();
    String path = aFile.getParent().getDirPath() + name;
    
    // If file for NewPath already exists, complain
    if(aFile.getSite().getFile(path)!=null) {
        beep(); return false; }
    
    // Set bytes and save
    WebFile newFile = aFile.getSite().createFile(path, aFile.isDir());
    newFile.setBytes(aFile.getBytes());
    try { newFile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
    
    // Remove old file
    removeFile(aFile);
    
    // Select new file pane
    _appPane.setSelectedFile(newFile);
    
    // Return true
    return true;
}

/**
 * Runs a panel for a new file (Java, JFX, Swing, table, etc.).
 */
public void showNewFilePanel()
{
    // Get new FormBuilder and configure
    FormBuilder form = new FormBuilder(); form.setPadding(20, 5, 15, 5);
    form.addLabel("Select file type:           ").setFont(new snap.gfx.Font("Arial", 24));
    form.setSpacing(15);
    
    // Define options
    String options[][] = {
        { "Java Programming File", ".java" },
        { "Graphics + UI File", ".snp" },
        { "Sound File", ".wav" },
        { "Directory", ".dir" },
        { "ReportMill\u2122 Report Template", ".rpt" } };
    
    // Add and configure radio buttons
    for(int i=0; i<options.length; i++) { String option = options[i][0];
        form.addRadioButton("EntryType", option, i==0); }

    // Run dialog panel (just return if null), select type and extension
    if(!form.showPanel(_appPane.getUI(), "New Project File", DialogBox.infoImage)) return;
    String desc = form.getStringValue("EntryType");
    int index = 0; for(int i=0; i<options.length; i++) if(desc.equals(options[i][0])) index = i;
    String extension = options[index][1];
    boolean isDir = extension.equals(".dir"); if(isDir) extension = "";
    
    // Get source dir
    WebSite site = getSelectedSite();
    WebFile sfile = _appPane.getSelectedFile(); if(sfile.getSite()!=site) sfile = site.getRootDir();
    WebFile sdir = sfile.isDir()? sfile : sfile.getParent();
    //if(extension.equals(".java") && sdir==site.getRootDir())
    //    sdir = Project.get(site).getSourceDir();
    
    // Get suggested "Untitled.xxx" path for AppPane.SelectedFile and extension
    String path = sdir.getDirPath() + "Untitled" + extension;
    
    // Create suggested file and page
    WebFile file = site.createFile(path, isDir);
    WebPage page = _appPane.getBrowser().createPage(file);
    
    // ShowNewFilePanel and save returned file
    file = page.showNewFilePanel(_appPane.getUI(), file); if(file==null) return;
    try { file.save(); }
    catch(Exception e) { _appPane.getBrowser().showException(file.getURL(), e); return; }

    // Select file and show in tree
    _appPane.setSelectedFile(file);
    showInTree(file);
}

/**
 * Runs the remove file panel.
 */
public void showRemoveFilePanel()
{
    // Get selected files - if any are root, beep and return
    List <WebFile> files = getSelectedFiles();
    for(WebFile file : files)
        if(file.isRoot()) { beep(); return; }
    
    // Give the user one last chance to bail
    DialogBox dbox = new DialogBox("Remove File(s)"); //dbox._comp = _appPane.getUI();
    dbox.setQuestionMessage("Are you sure you want to remove the currently selected File(s)?");
    if(!dbox.showConfirmDialog(_appPane.getUI())) return; //_appPane.getUI()
    
    // Get top parent
    WebFile parent = files.size()>0? files.get(0).getParent() : null;
    for(WebFile file : files) parent = WebUtils.getCommonAncestor(parent, file);
    
    // Remove files (check File.Exists in case previous file was a parent directory)
    removeFiles(files);
    
    // Update tree again
    _appPane.setSelectedFile(parent);
}

/**
 * Run application.
 */
public void run()  { run(getSelectedFile()); }

/**
 * Runs a given RunConfig or file as a separate process.
 */
protected void run(WebFile aFile)
{
    // Get site and RunConfig (if available)
    WebSite site = getRootSite();

    // Get file
    WebFile file = aFile;
    if(file==null) file = _lastRunFile;
    if(file==null) file = getSelectedFile();
    
    if(file!=null) {
        String name = file.getName();
        String urls = "http://reportmill.com/cj/PlayBall/PlayBall.html";
        if(name.contains("BusyBox"))
            urls = "http://reportmill.com/cj/BusyBox/BusyBox.html";
        if(name.contains("Snappy"))
            urls = "http://reportmill.com/cj/SnappyBird/SnappyBird.html";
        if(name.contains("GraphView"))
            urls = "http://reportmill.com/cj/GraphView/GraphView.html";
        GFXEnv.getEnv().openURL(urls);
        _lastRunFile = file;
    }
}

WebFile _lastRunFile;

/**
 * Returns the HomePageURL.
 */
public HomePage getHomePage()
{
    if(_homePage!=null) return _homePage;
    _homePage = new HomePage(); _homePage.setURL(WebURL.getURL(HomePage.class));
    getBrowser().setPage(_homePage.getURL(),_homePage);
    return _homePage;
}
HomePage _homePage;

/**
 * Returns the HomePageURL.
 */
public WebURL getHomePageURL()  { return getHomePage().getURL(); }

/**
 * Handle Copy.
 */
public void copy()
{
    List <WebFile> dfiles = getSelectedFiles();
    List <File> files = new ArrayList();
    for(WebFile df : dfiles) if(df.getJavaFile()!=null) files.add(df.getJavaFile());
    Clipboard cb = Clipboard.get();
    cb.addData(files);
}

/**
 * Handle Paste.
 */
public void paste()
{
    Clipboard cb = Clipboard.get();
    if(cb.hasFiles()) addFiles(cb.getJavaFiles());
}

/**
 * Called to configure FilesList cell.
 */
private void configureFilesListCell(ListCell <AppFile> aCell)
{
    AppFile item = aCell.getItem(); if(item==null) return;
    aCell.setPadding(2,6,2,4);
    aCell.setGraphic(item.getGraphic()); aCell.setGrowWidth(true); aCell.getStringView().setGrowWidth(true);
    Polygon poly = new Polygon(0,2,2,0,5,3,8,0,10,2,7,5,10,8,8,10,5,7,2,10,0,8,3,5);
    
    ShapeView sview = new ShapeView(poly); sview.setPrefSize(11,11); sview.setFillSize(true);
    sview.setFill(Color.WHITE); sview.setBorder(CLOSE_BOX_BORDER1); sview.setProp("File", item.getFile());
    sview.addEventFilter(e->handleBookmarkEvent(e), MouseEnter, MouseExit, MouseRelease);
    aCell.setGraphicAfter(sview);
}

/**
 * Called for events on bookmark close button.
 */
private void handleBookmarkEvent(ViewEvent anEvent)
{
    View cbox = anEvent.getView();
    if(anEvent.isMouseEnter()) { cbox.setFill(Color.CRIMSON); cbox.setBorder(CLOSE_BOX_BORDER2); }
    else if(anEvent.isMouseExit()) { cbox.setFill(Color.WHITE); cbox.setBorder(CLOSE_BOX_BORDER1); }
    else if(anEvent.isMouseRelease()) _appPane._toolBar.removeOpenFile((WebFile)cbox.getProp("File"));
    anEvent.consume();
}

    static Border            CLOSE_BOX_BORDER1 = Border.createLineBorder(Color.LIGHTGRAY,.5);
    static Border            CLOSE_BOX_BORDER2 = Border.createLineBorder(Color.BLACK,1);
}