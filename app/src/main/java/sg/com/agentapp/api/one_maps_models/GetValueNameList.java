package sg.com.agentapp.api.one_maps_models;

import java.util.ArrayList;

public class GetValueNameList {

    String found;
    String totalNumPages;
    String pageNum;

    ArrayList<GetValueName> results;

    public String getFound() {
        return found;
    }

    public String getTotalNumPages() {
        return totalNumPages;
    }

    public String getPageNum() {
        return pageNum;
    }

    public ArrayList<GetValueName> getResults() {
        return results;
    }
}
