package clinicmanager.gui;

import java.util.ArrayList;
import java.util.List;

// Singleton manager for broadcasting data changes across panels
  //Allows panels to register as listeners and be notified when data changes
 
public class DataChangeManager {
    private static DataChangeManager instance;
    private List<DataChangeListener> listeners = new ArrayList<>();
    
    private DataChangeManager() {
    }
    
    // Get the singleton instance
     
    public static synchronized DataChangeManager getInstance() {
        if (instance == null) {
            instance = new DataChangeManager();
        }
        return instance;
    }
    
    // Register a listener to be notified of data changes
   
    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    // Unregister a listener
   
    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    
    // Notify all listeners that patients have changed
   
    public void notifyPatientsChanged() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onPatientsChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Notify all listeners that appointments have changed
     
    public void notifyAppointmentsChanged() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onAppointmentsChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Notify all listeners that medical history has changed
    
    public void notifyMedicalHistoryChanged() {
        for (DataChangeListener listener : listeners) {
            try {
                listener.onMedicalHistoryChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
//