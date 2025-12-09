package clinicmanager.gui;

/**
 * Interface for listening to data changes across panels
 * Allows panels to be notified when data is added/updated/deleted
 */
public interface DataChangeListener {
    /**
     * Called when patients data has changed
     */
    void onPatientsChanged();
    
    /**
     * Called when appointments data has changed
     */
    void onAppointmentsChanged();
    
    /**
     * Called when medical history data has changed
     */
    void onMedicalHistoryChanged();
}
