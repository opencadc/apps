package ca.nrc.cadc.dlm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadRequest {
    public List<DownloadTuple> requestList = new ArrayList<DownloadTuple>();
    public List<Exception> validationErrors = new ArrayList<Exception>();

    // TODO: where is this set and why? Is it part of the data model?
    public Boolean removeDuplicates;

    public Map<String, List<String>> params;

    public DownloadRequest(List<DownloadTuple> requests) {
        this.requestList = requests;
        // TODO: probably a lighter weight way than HashMap,
        //  yet this is always the first that comes to mind..
        this.params = new HashMap<String, List<String>>();
    }

    public DownloadRequest() { }

    public void addRequest(DownloadTuple dt) {
        this.requestList.add(dt);
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public List<Exception> getValidationErrors() {
        return this.validationErrors;
    }

    public void addValidationError(Exception e) {
        this.validationErrors.add(e);
    }

}
