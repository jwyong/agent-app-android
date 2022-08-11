package sg.com.agentapp.api.one_maps_models;

public class GetValueName {

    String SEARCHVAL;
    String BLK_NO;
    String ROAD_NAME;
    String BUILDING;
    String ADDRESS;
    String POSTAL;
    String X;
    String Y;
    String LATITUDE;
    String LONGITUDE;
    String LONGTITUDE;


    public String getSEARCHVAL() {
        return SEARCHVAL;
    }

    public String getBLK_NO() {
        return BLK_NO;
    }

    public String getROAD_NAME() {
        return ROAD_NAME;
    }

    public String getBUILDING() {
        return BUILDING;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public String getPOSTAL() {
        return POSTAL;
    }

    public String getX() {
        return X;
    }

    public String getY() {
        return Y;
    }

    public String getLATITUDE() {
        return LATITUDE;
    }

    public String getLONGITUDE() {
        return LONGITUDE;
    }

    public String getLONGTITUDE() {
        return LONGTITUDE;
    }

    @Override
    public String toString() {
        return "GetValueName{" +
                "SEARCHVAL='" + SEARCHVAL + '\'' +
                ", BLK_NO='" + BLK_NO + '\'' +
                ", ROAD_NAME='" + ROAD_NAME + '\'' +
                ", BUILDING='" + BUILDING + '\'' +
                ", ADDRESS='" + ADDRESS + '\'' +
                ", POSTAL='" + POSTAL + '\'' +
                ", X='" + X + '\'' +
                ", Y='" + Y + '\'' +
                ", LATITUDE='" + LATITUDE + '\'' +
                ", LONGITUDE='" + LONGITUDE + '\'' +
                ", LONGTITUDE='" + LONGTITUDE + '\'' +
                '}';
    }
}
