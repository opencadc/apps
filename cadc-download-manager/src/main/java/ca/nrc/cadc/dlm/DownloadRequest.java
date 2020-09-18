package ca.nrc.cadc.dlm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DownloadRequest {
    // Duplicate requests not allowed
    public Set<DownloadTuple> requestList = new TreeSet<DownloadTuple>();
    public List<Exception> validationErrors = new ArrayList<Exception>();

    public Map<String, List<String>> params;

    public DownloadRequest(Set<DownloadTuple> requests) {
        this.requestList = requests;
        // TODO: probably a lighter weight way than HashMap,
        //  yet this is always the first that comes to mind..
        this.params = new HashMap<String, List<String>>();
    }

    public DownloadRequest() { }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

}
