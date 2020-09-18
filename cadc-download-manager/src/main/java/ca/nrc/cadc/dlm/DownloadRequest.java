package ca.nrc.cadc.dlm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadRequest {
    public final List<DownloadTuple> requestList;
    public Map<String, List<String>> params;

    // TODO: where is this set and why? Is it part of the data model?
    public Boolean removeDuplicates;

    public DownloadRequest(List<DownloadTuple> requests) {
        List<DownloadTuple> tmpList = new ArrayList<DownloadTuple>();
        if (requests != null) {
            tmpList = requests;
        }

        this.requestList = tmpList;
        // TODO: probably a lighter weight way than HashMap,
        //  yet this is always the first that comes to mind..
        this.params = new HashMap<String, List<String>>();
    }

    public DownloadRequest() {
        this(null);
    }

    public void addRequest(DownloadTuple dt) {
        this.requestList.add(dt);
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

}
