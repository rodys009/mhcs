package edu.umn.d.grenoble.mhcs.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.umn.d.grenoble.mhcs.bus.AreaClickEvent;
import edu.umn.d.grenoble.mhcs.bus.AreaClickEventHandler;
import edu.umn.d.grenoble.mhcs.bus.AreaUpdateEvent;
import edu.umn.d.grenoble.mhcs.bus.Bus;
import edu.umn.d.grenoble.mhcs.bus.SoundEvent;
import edu.umn.d.grenoble.mhcs.modules.Area;
import edu.umn.d.grenoble.mhcs.modules.ConfigurationBuilder;
import edu.umn.d.grenoble.mhcs.modules.Module;
import edu.umn.d.grenoble.mhcs.modules.Orientation;
import edu.umn.d.grenoble.mhcs.modules.Status;

public class AddModulesPanel extends Tab{
    private static final String moduleSaveName = "grenoble_module_list";
    
    private Area moduleList;
    private FlexTable thisPanel;
    private Button submitButton;
    private Button cancelButton;
    private Button loadButton;
    private Button saveButton;
    private Button testCaseButton;
    private TextBox coorX;
    private TextBox coorY; 
    private TextBox testCaseText;
    private TextBox moduleNumber;
    private Label coorXLabel; 
    private Label coorYLabel;
    private Label moduleNumberLabel;
    private ListBox orientation;
    private ListBox condition;
    private Label orientationLabel;
    private Label conditionLabel;
    private Module moduleToEdit;
    private ConfigurationBuilder configurations;
    
    public AddModulesPanel() {
        
        this.moduleList = new Area();
        this.thisPanel = new FlexTable();
        
        this.submitButton = new Button("Submit");
        this.cancelButton = new Button("Cancel/Clear");
        this.loadButton = new Button("Load");
        this.saveButton = new Button("Save");
        
        this.coorXLabel = new Label("X Position:");
        this.coorYLabel = new Label("Y Position:");
        this.moduleNumberLabel = new Label("ID Number:");
        this.conditionLabel = new Label("Module Condition:");
        this.orientationLabel = new Label("Orientation:");
        
        this.testCaseButton = new Button("Load a test case");
        this.testCaseText = new TextBox();
        
        this.coorX = new TextBox();
        this.coorY = new TextBox();
        this.moduleNumber = new TextBox();
        
        this.orientation = new ListBox();
        for(Orientation o : Orientation.values()){
            this.orientation.addItem(o.name());
        }

        this.condition = new ListBox();
        for(Status s : Status.values()){
            this.condition.addItem(s.name());
        }
        
        this.configurations = new ConfigurationBuilder();
        
        this.thisPanel.setWidget(0, 0, this.coorXLabel);  
        this.thisPanel.setWidget(0, 1, this.coorX);
        this.thisPanel.setWidget(1, 0, this.coorYLabel);
        this.thisPanel.setWidget(1, 1, this.coorY);
        this.thisPanel.setWidget(0, 3, this.moduleNumberLabel);
        this.thisPanel.setWidget(0, 4, this.moduleNumber);
        this.thisPanel.setWidget(0, 6, this.conditionLabel);
        this.thisPanel.setWidget(0, 7, this.condition);
        this.thisPanel.setWidget(0, 9, this.submitButton);
        this.thisPanel.setWidget(1, 3, this.orientationLabel);
        this.thisPanel.setWidget(1, 4, this.orientation);
        this.thisPanel.setWidget(1, 9, this.cancelButton);
        this.thisPanel.setWidget(0, 11, this.loadButton);
        this.thisPanel.setWidget(0, 12, this.saveButton);
        this.thisPanel.setWidget(1, 6, this.testCaseButton);
        this.thisPanel.setWidget(1, 7, this.testCaseText);
        
        final AddModulesPanel addModulesPanel = this;
        
        this.submitButton.addClickHandler(new ClickHandler() {
            public void onClick(final ClickEvent event) {
                
                final String INVALID = "Invalid entry";
                final String MODULES_LOGGED = " module(s) have been logged";
                
                if ( addModulesPanel.moduleToEdit == null ) {
                    Module currentModule = new Module();
                    currentModule.setId( Integer.parseInt( addModulesPanel.moduleNumber.getText() ) );
                    currentModule.setX( Integer.parseInt( addModulesPanel.coorX.getText() ) );
                    currentModule.setY( Integer.parseInt( addModulesPanel.coorY.getText() ) );
                    currentModule.setOrientation( Orientation.values()[ 
                        addModulesPanel.orientation.getSelectedIndex() ] );
                    currentModule.setStatus( Status.values()[ 
                        addModulesPanel.condition.getSelectedIndex() ] );
                    if ( currentModule.isValid() ) {                    
                        addModulesPanel.moduleList.addModule(currentModule);
                        Bus.bus.fireEvent( new AreaUpdateEvent(addModulesPanel.moduleList) );
                        Bus.bus.fireEvent( new SoundEvent(SoundOutput.Sounds.ModuleAdded) );
                        Window.alert( "Module added \n" + addModulesPanel.moduleList.getModules().size() 
                                + MODULES_LOGGED );
                        addModulesPanel.configurations.addModule(currentModule);
                        addModulesPanel.clearPanel();
                    }
                    else { Window.alert(INVALID); }                    
                } else {
                    Module oldModule = moduleToEdit;
                    addModulesPanel.moduleToEdit.setId( Integer.parseInt( addModulesPanel.moduleNumber.getText() ) );
                    addModulesPanel.moduleToEdit.setX( Integer.parseInt( addModulesPanel.coorX.getText() ) );
                    addModulesPanel.moduleToEdit.setY( Integer.parseInt( addModulesPanel.coorY.getText() ) );
                    addModulesPanel.moduleToEdit.setOrientation( Orientation.values()[ 
                        addModulesPanel.orientation.getSelectedIndex() ] );
                    addModulesPanel.moduleToEdit.setStatus( Status.values()[ 
                        addModulesPanel.condition.getSelectedIndex() ] );
                    if ( addModulesPanel.moduleToEdit.isValid() ) {                        
                        Bus.bus.fireEvent( new AreaUpdateEvent(addModulesPanel.moduleList) );
                        Bus.bus.fireEvent( new SoundEvent(SoundOutput.Sounds.ModuleEdited) );
                        Window.alert( "Module edited \n" + addModulesPanel.moduleList.getModules().size() 
                                + MODULES_LOGGED );
                        addModulesPanel.configurations.editModule(oldModule, moduleToEdit);
                        addModulesPanel.clearPanel();
                    } else { Window.alert(INVALID); }      
                    moduleToEdit = null;
                }
            }
        });
          
        this.cancelButton.addClickHandler( new ClickHandler() {
            public void onClick(final ClickEvent event) {
                addModulesPanel.clearPanel();
            }
        });
        
        
        
        this.loadButton.addClickHandler( new ClickHandler() {
            public void onClick(final ClickEvent event) {

                addModulesPanel.moduleList = AreaHolder.getArea(AreaHolder.asLandedName);

                Bus.bus.fireEvent( new SoundEvent(SoundOutput.Sounds.ModuleLoaded) );
                Bus.bus.fireEvent( new AreaUpdateEvent(addModulesPanel.moduleList) );
                addModulesPanel.configurations = new ConfigurationBuilder( addModulesPanel.moduleList.getModules() );
            }
        });
        
        this.saveButton.addClickHandler( new ClickHandler() {
            public void onClick(final ClickEvent event) {
                AreaHolder.saveArea(AreaHolder.asLandedName, moduleList);
                Bus.bus.fireEvent( new SoundEvent(SoundOutput.Sounds.ModuleSaved) );
                
            }
        });
        
        Bus.bus.addHandler(AreaClickEvent.TYPE, new AreaClickEventHandler() {
            
            @Override
            public void onEvent(final AreaClickEvent event) {
                if(addModulesPanel.isCurrent){
                addModulesPanel.moduleToEdit = addModulesPanel.moduleList.occupied( event.getX(), event.getY() );
                addModulesPanel.coorX.setText( Integer.toString( addModulesPanel.moduleToEdit.getX() ) );
                addModulesPanel.coorY.setText( Integer.toString( addModulesPanel.moduleToEdit.getY() ) );
                addModulesPanel.moduleNumber.setText( Integer.toString( addModulesPanel.moduleToEdit.getId() ) );
                
                for (int i = 0; i < Orientation.values().length; i += 1) {
                    if ( Orientation.values()[i].equals( addModulesPanel.moduleToEdit.getOrientation() ) ) {
                        addModulesPanel.orientation.setSelectedIndex(i);
                        break;
                    }
                }
                for ( int i = 0; i < Status.values().length; i += 1 ) {
                    if ( Status.values()[i].equals( addModulesPanel.moduleToEdit.getStatus() ) ) {
                        addModulesPanel.condition.setSelectedIndex(i);
                        break;
                    }
                }
            }
            }
        });
        
        this.testCaseButton.addClickHandler( new ClickHandler() {
            public void onClick(final ClickEvent event) {
                /*String url= URL.encode("http://d.umn.edu/~redi0068/proxy.php?case=1");// + testCaseText.getText());
                
                JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
                jsonp.setCallbackParam("GPScallback");
                jsonp.requestObject(url,  new AsyncCallback<JavaScriptObject>() {
                    public void onFailure(final Throwable caught){
                        Window.alert("Json onFailure" + caught.getMessage());
                    }
                  
                    @Override          
                    public void onSuccess(final JavaScriptObject s) {
                        JSONObject obj = new JSONObject(s);
                        Window.alert(obj.toString());
                        //update( obj.toString() );              
                    }     
                });*/
                
                String url = "http://www.d.umn.edu/~abrooks/SomeTests.php?q=" + testCaseText.getText();
                url = URL.encode(url);
                
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
                builder.setIncludeCredentials(true);
                builder.setUser("user");
                builder.setPassword("pass");
                
                try{
                    Request request = builder.sendRequest(null, new RequestCallback(){
                        public void onError(Request request, Throwable Exception) {
                            Window.alert("onError");
                        }
                        
                        public void onResponseReceived(Request request, Response response){
                            if(200 == response.getStatusCode()){
                                String rt = response.getText();
                                update(rt);
                            }
                            else{
                                Window.alert("Couldn't retreive JSON: " + response.getStatusText() + response.getStatusCode());
                            }
                        }
                    });

                    
                }
                catch (Exception e){
                    Window.alert("RequestException: Could not retreive JSON");
                }
                
            }
        });
        
    }
    
    public void update(String rt) {
        String stringAll = rt;
        this.moduleList = new Area(stringAll);
        Bus.bus.fireEvent( new AreaUpdateEvent(this.moduleList) );
        
    }
    
    public void clearPanel() {       
        this.coorX.setText(null);
        this.coorY.setText(null);
        this.moduleNumber.setText(null);
        this.orientation.setSelectedIndex(0);
        this.condition.setSelectedIndex(0);  
    }  
    
    public FlexTable getAddModulesPanel() {
        return this.thisPanel;
    }

    @Override
    public Widget getPanel() {
        return this.thisPanel;
        
    }

    @Override
    public void switchedTo() {
        Bus.bus.fireEvent( new AreaUpdateEvent(this.moduleList) );
        
    }

    @Override
    public String getTabName() {
        return "Add Modules";
        
    }
    
    
}
